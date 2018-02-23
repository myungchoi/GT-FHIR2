package edu.gatech.chai.gtfhir2.mapping;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.dstu3.model.Observation.ObservationStatus;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.rest.param.TokenParam;

import edu.gatech.chai.omopv5.jpa.entity.FObservationView;
import edu.gatech.chai.omopv5.jpa.service.FObservationViewService;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;

public class OmopObservation implements IResourceMapping<Observation, FObservationView> {

	public static final Long SYSTOLIC_CONCEPT_ID = 3004249L;
	public static final Long DIASTOLIC_CONCEPT_ID = 3012888L;	

	private FObservationViewService myOmopService;

	public OmopObservation(WebApplicationContext context) {
		myOmopService = context.getBean(FObservationViewService.class);
	}
	
	@Override
	public Observation toFHIR(IdType id) {
		String observationResourceName = ResourceType.Observation.getPath();
		Long id_long_part = id.getIdPartAsLong();
		Long myId = IdMapping.getOMOPfromFHIR(id_long_part, observationResourceName);

		FObservationView fObservationView = (FObservationView) myOmopService.findById(myId);
		if (fObservationView == null)
			return null;

		Long fhirId = IdMapping.getFHIRfromOMOP(myId, observationResourceName);

		return constructFHIR(fhirId, fObservationView);
	}

	public static Observation constructFHIR(Long fhirId, FObservationView fObservationView) {
		Observation observation = new Observation();
		observation.setId(new IdType(fhirId));

		String systemUriString = fObservationView.getObservationConcept().getVocabulary().getSystemUri();
		String codeString = fObservationView.getObservationConcept().getConceptCode();
		String displayString;
		if (fObservationView.getObservationConcept().getId() == 0L) {
			displayString = fObservationView.getSourceValue();
		} else {
			displayString = fObservationView.getObservationConcept().getName();
		}
		
		// OMOP database maintains Systolic and Diastolic Blood Pressures separately.
		// FHIR however keeps them together. Observation DAO filters out Diastolic values.
		// Here, when we are reading systolic, we search for matching diastolic and put them
		// together. The Observation ID will be systolic's OMOP ID. 
		// public static final Long SYSTOLIC_CONCEPT_ID = new Long(3004249);
		// public static final Long DIASTOLIC_CONCEPT_ID = new Long(3012888);		
		if (SYSTOLIC_CONCEPT_ID.equals(fObservationView.getObservationConcept().getId())) {
			// Set coding for systolic and diastolic observation
			systemUriString = "http://loinc.org";
			codeString = "55284-4";
			displayString = "Blood pressure systolic & diastolic";
			
			List<ObservationComponentComponent> components = new ArrayList<ObservationComponentComponent>();
			// First we add systolic component.
			ObservationComponentComponent comp = new ObservationComponentComponent();
			Coding coding = new Coding(fObservationView.getObservationConcept().getVocabulary().getSystemUri(),
					fObservationView.getObservationConcept().getConceptCode(), fObservationView.getObservationConcept().getName());
			CodeableConcept componentCode = new CodeableConcept();
			componentCode.addCoding(coding);
			comp.setCode(componentCode);
			
			if (fObservationView.getValueAsNumber() != null) {
				Quantity quantity = new Quantity(fObservationView.getValueAsNumber().doubleValue());
				
				// Unit is defined as a concept code in omop v4, then unit and code are the same in this case
				if (fObservationView.getUnitConcept() != null) {
					quantity.setUnit(fObservationView.getUnitConcept().getConceptCode());
					quantity.setCode(fObservationView.getUnitConcept().getConceptCode());
					quantity.setSystem(fObservationView.getUnitConcept().getVocabulary().getSystemUri());
					comp.setValue(quantity);
				}
			}
			components.add(comp);
			
			// Now search for diastolic component.
			WebApplicationContext myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
			FObservationViewService myService = myAppCtx.getBean(FObservationViewService.class);
			FObservationView diastolicDb = myService.findDiastolic(DIASTOLIC_CONCEPT_ID, fObservationView.getPerson().getId(), fObservationView.getDate(), fObservationView.getTime());
			if (diastolicDb != null) {
				comp = new ObservationComponentComponent();
				coding = new Coding(diastolicDb.getObservationConcept().getVocabulary().getSystemUri(),
						diastolicDb.getObservationConcept().getConceptCode(), diastolicDb.getObservationConcept().getName());
				componentCode = new CodeableConcept();
				componentCode.addCoding(coding);
				comp.setCode(componentCode);
				
				if (diastolicDb.getValueAsNumber() != null) {
					Quantity quantity = new Quantity(diastolicDb.getValueAsNumber().doubleValue());
					// Unit is defined as a concept code in omop v4, then unit and code are the same in this case
					if (diastolicDb.getUnitConcept() != null) {
						quantity.setUnit(diastolicDb.getUnitConcept().getName());
						quantity.setCode(diastolicDb.getUnitConcept().getConceptCode());
						quantity.setSystem(diastolicDb.getUnitConcept().getVocabulary().getSystemUri());
						comp.setValue(quantity);
					}
				}
				components.add(comp);
			}
			
			if (components.size() > 0) {
				observation.setComponent(components);
			}
		} else {
			if (fObservationView.getValueAsNumber() != null) {
				Quantity quantity = new Quantity(fObservationView.getValueAsNumber().doubleValue());
				if (fObservationView.getUnitConcept() != null) {
					// Unit is defined as a concept code in omop v4, then unit and code are the same in this case				
					quantity.setUnit(fObservationView.getUnitConcept().getName());
					quantity.setCode(fObservationView.getUnitConcept().getConceptCode());
					quantity.setSystem(fObservationView.getUnitConcept().getVocabulary().getSystemUri());
				}
				observation.setValue(quantity);
			} else if (fObservationView.getValueAsString() != null) {
				observation.setValue(new StringType(fObservationView.getValueAsString()));
			} else if (fObservationView.getValueAsConcept() != null && fObservationView.getValueAsConcept().getId() != 0L) {
				// vocabulary is a required attribute for concept, then it's expected to not be null
				Coding coding = new Coding(fObservationView.getValueAsConcept().getVocabulary().getSystemUri(), 
						fObservationView.getValueAsConcept().getConceptCode(), fObservationView.getValueAsConcept().getName());
				CodeableConcept valueAsConcept = new CodeableConcept();
				valueAsConcept.addCoding(coding);
				observation.setValue(valueAsConcept);
			} else {
				observation.setValue(new StringType(fObservationView.getValueSourceValue()));
			}
		}

		if (fObservationView.getRangeLow() != null) {
			SimpleQuantity low = new SimpleQuantity();
			low.setValue(fObservationView.getRangeLow().doubleValue());
			observation.getReferenceRangeFirstRep().setLow(low);
		}
		if (fObservationView.getRangeHigh() != null) {
			SimpleQuantity high = new SimpleQuantity();
			high.setValue(fObservationView.getRangeHigh().doubleValue());
			observation.getReferenceRangeFirstRep().setHigh(high);
		}
		
		Coding resourceCoding = new Coding(systemUriString, codeString, displayString);
		CodeableConcept code = new CodeableConcept();
		code.addCoding(resourceCoding);
		observation.setCode(code);
		
		observation.setStatus(ObservationStatus.FINAL);

		if (fObservationView.getDate() != null) {
			Date myDate = createDateTime(fObservationView);			
			if (myDate != null) {
				DateTimeType appliesDate = new DateTimeType(myDate);
				observation.setEffective(appliesDate);
			}
		}
		if (fObservationView.getPerson() != null) {
			Reference personRef = new Reference(new IdType(fObservationView.getPerson().getId()));
			personRef.setDisplay(fObservationView.getPerson().getNameAsSingleString());
			observation.setSubject(personRef);
		}
		if (fObservationView.getVisitOccurrence() != null)
			observation.getContext().setReferenceElement(new IdType (ResourceType.Encounter.getPath(), fObservationView.getVisitOccurrence().getId()));
		
		if (fObservationView.getTypeConcept() != null) {
			if (fObservationView.getTypeConcept().getId() == 44818701L) {
				// This is From physical examination.
				
				CodeableConcept typeConcept = new CodeableConcept();
				Coding typeCoding = new Coding("http://hl7.org/fhir/observation-category", "exam", "");
				typeConcept.addCoding(typeCoding);
				observation.addCategory(typeConcept);
			} else if (fObservationView.getTypeConcept().getId() == 44818702L) {
				CodeableConcept typeConcept = new CodeableConcept();
				// This is Lab result
				Coding typeCoding = new Coding("http://hl7.org/fhir/observation-category", "laboratory", "");
				typeConcept.addCoding(typeCoding);				
				observation.addCategory(typeConcept);
			} else if (fObservationView.getTypeConcept().getId() == 45905771L) {
				CodeableConcept typeConcept = new CodeableConcept();
				// This is Lab result
				Coding typeCoding = new Coding("http://hl7.org/fhir/observation-category", "survey", "");
				typeConcept.addCoding(typeCoding);				
				observation.addCategory(typeConcept);
			} else if (fObservationView.getTypeConcept().getId() == 38000277L || fObservationView.getTypeConcept().getId() == 38000278L) {
				CodeableConcept typeConcept = new CodeableConcept();
				// This is Lab result
				Coding typeCoding = new Coding("http://hl7.org/fhir/observation-category", "laboratory", "");
				typeConcept.addCoding(typeCoding);				
				observation.addCategory(typeConcept);
			} else if (fObservationView.getTypeConcept().getId() == 38000280L || fObservationView.getTypeConcept().getId() == 38000281L) {
				CodeableConcept typeConcept = new CodeableConcept();
				// This is Lab result
				Coding typeCoding = new Coding("http://hl7.org/fhir/observation-category", "exam", "");
				typeConcept.addCoding(typeCoding);				
				observation.addCategory(typeConcept);
			}
		}
		
		return observation;
	}
	
	@Override
	public Observation constructResource(Long fhirId, FObservationView entity, List<String> includes) {
		Observation observation = constructFHIR(fhirId, entity);

		return observation;
	}

	@Override
	public Long toDbase(Observation Fhir) {
		// TODO Auto-generated method stub
		return null;
	}

	// Blood Pressure is stored in the component. So, we store two values in
	// the component section. We do this by selecting diastolic when systolic
	// is selected. Since we are selecting this already, we need to skip diastolic.
	final ParameterWrapper exceptionParam = new ParameterWrapper(
			"Long",
			Arrays.asList("observationConcept.id"),
			Arrays.asList("!="),
			Arrays.asList(String.valueOf(DIASTOLIC_CONCEPT_ID))
			);
	
	@Override
	public Long getSize() {
		Map<String, List<ParameterWrapper>> map = new HashMap<String, List<ParameterWrapper>> ();

		List<ParameterWrapper> exceptions = new ArrayList<ParameterWrapper>();
		exceptions.add(exceptionParam);
		map.put(Observation.SP_COMPONENT_CODE, exceptions);

		return myOmopService.getSize(map);
	}

	@Override
	public Long getSize(Map<String, List<ParameterWrapper>> map) {
		List<ParameterWrapper> exceptions = new ArrayList<ParameterWrapper>();
		exceptions.add(exceptionParam);
		map.put(Observation.SP_COMPONENT_CODE, exceptions);
		
		return myOmopService.getSize(map);
	}

	@Override
	public void searchWithoutParams(int fromIndex, int toIndex, List<IBaseResource> listResources,
			List<String> includes) {
		Map<String, List<ParameterWrapper>> map = new HashMap<String, List<ParameterWrapper>> ();

		List<ParameterWrapper> exceptions = new ArrayList<ParameterWrapper>();
		exceptions.add(exceptionParam);
		map.put(Observation.SP_COMPONENT_CODE, exceptions);

		List<FObservationView> fObservationViews = myOmopService.searchWithParams(fromIndex, toIndex, map);

		// We got the results back from OMOP database. Now, we need to construct
		// the list of
		// FHIR Patient resources to be included in the bundle.
		for (FObservationView fObservationView : fObservationViews) {
			Long omopId = fObservationView.getId();
			Long fhirId = IdMapping.getFHIRfromOMOP(omopId, ResourceType.Observation.getPath());
			listResources.add(constructResource(fhirId, fObservationView, includes));
		}		
	}

	@Override
	public void searchWithParams(int fromIndex, int toIndex, Map<String, List<ParameterWrapper>> map,
			List<IBaseResource> listResources, List<String> includes) {
		List<ParameterWrapper> exceptions = new ArrayList<ParameterWrapper>();
		exceptions.add(exceptionParam);
		map.put(Observation.SP_COMPONENT_CODE, exceptions);

		List<FObservationView> fObservationViews = myOmopService.searchWithParams(fromIndex, toIndex, map);

		for (FObservationView fObservationView : fObservationViews) {
			Long omopId = fObservationView.getId();
			Long fhirId = IdMapping.getFHIRfromOMOP(omopId, ResourceType.Observation.getPath());
			listResources.add(constructResource(fhirId, fObservationView, includes));
		}
	}
	
	private static Date createDateTime(FObservationView fObservationView) {
		Date myDate = null;
		if (fObservationView.getDate() != null) {
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
			String dateString = fmt.format(fObservationView.getDate());
			fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				if (fObservationView.getTime() != null && fObservationView.getTime().isEmpty() == false) {
					myDate = fmt.parse(dateString+" "+fObservationView.getTime());
				} else {
					myDate = fObservationView.getDate();
				}				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return myDate;
	}

	public List<ParameterWrapper> mapParameter(String parameter, Object value) {
		List<ParameterWrapper> mapList = new ArrayList<ParameterWrapper>();
		ParameterWrapper paramWrapper = new ParameterWrapper();
		switch (parameter) {
		case Patient.SP_RES_ID:
			String organizationId = ((TokenParam) value).getValue();
			paramWrapper.setParameterType("Long");
			paramWrapper.setParameters(Arrays.asList("id"));
			paramWrapper.setOperators(Arrays.asList("="));
			paramWrapper.setValues(Arrays.asList(organizationId));
			paramWrapper.setRelationship("or");
			mapList.add(paramWrapper);
			break;
		default:
			mapList = null;
		}
		
		return mapList;
	}
	

}
