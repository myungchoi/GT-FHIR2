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
import org.hl7.fhir.dstu3.model.DateType;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.dstu3.model.Observation.ObservationReferenceRangeComponent;
import org.hl7.fhir.dstu3.model.Observation.ObservationStatus;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Type;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.param.TokenParam;
import edu.gatech.chai.gtfhir2.provider.EncounterResourceProvider;
import edu.gatech.chai.gtfhir2.provider.ObservationResourceProvider;
import edu.gatech.chai.gtfhir2.provider.OrganizationResourceProvider;
import edu.gatech.chai.gtfhir2.provider.PatientResourceProvider;
import edu.gatech.chai.gtfhir2.provider.PractitionerResourceProvider;
import edu.gatech.chai.gtfhir2.utilities.CodeableConceptUtil;
import edu.gatech.chai.omopv5.jpa.entity.Concept;
import edu.gatech.chai.omopv5.jpa.entity.FObservationView;
import edu.gatech.chai.omopv5.jpa.entity.FPerson;
import edu.gatech.chai.omopv5.jpa.entity.Measurement;
import edu.gatech.chai.omopv5.jpa.entity.VisitOccurrence;
import edu.gatech.chai.omopv5.jpa.service.ConceptService;
import edu.gatech.chai.omopv5.jpa.service.FObservationViewService;
import edu.gatech.chai.omopv5.jpa.service.MeasurementService;
import edu.gatech.chai.omopv5.jpa.service.ObservationService;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;
import edu.gatech.chai.omopv5.jpa.service.VisitOccurrenceService;

public class OmopObservation extends BaseOmopResource<Observation, FObservationView, FObservationViewService> implements IResourceMapping<Observation, FObservationView> {

	private static OmopObservation omopObservation = new OmopObservation();
	
	public static final Long SYSTOLIC_CONCEPT_ID = 3004249L;
	public static final Long DIASTOLIC_CONCEPT_ID = 3012888L;
	public static final String SYSTOLIC_LOINC_CODE = "8480-6";
	public static final String DIASTOLIC_LOINC_CODE = "8462-4";
	public static final String BP_SYSTOLIC_DIASTOLIC_CODE = "85354-9";
	public static final String BP_SYSTOLIC_DIASTOLIC_DISPLAY = "Blood pressure systolic & diastolic";

	private ConceptService conceptService;
	private MeasurementService measurementService;
	private ObservationService observationService;
	private VisitOccurrenceService visitOccurrenceService;

	public OmopObservation(WebApplicationContext context) {
		super(context, FObservationView.class, FObservationViewService.class, ObservationResourceProvider.getType());
		initialize(context);
	}
	
	public OmopObservation() {
		super(ContextLoaderListener.getCurrentWebApplicationContext(), FObservationView.class, FObservationViewService.class, ObservationResourceProvider.getType());
		initialize(ContextLoaderListener.getCurrentWebApplicationContext());
	}
	
	private void initialize(WebApplicationContext context) {		
		// Get bean for other services that we need for mapping.
		conceptService = context.getBean(ConceptService.class);
		measurementService = context.getBean(MeasurementService.class);
		observationService = context.getBean(ObservationService.class);
		visitOccurrenceService = context.getBean(VisitOccurrenceService.class);
		
	}
	
	public static OmopObservation getInstance() {
		return omopObservation;
	}
	
	@Override
	public Observation constructFHIR(Long fhirId, FObservationView fObservationView) {
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
			systemUriString = OmopCodeableConceptMapping.LOINC.getFhirUri();
			codeString = BP_SYSTOLIC_DIASTOLIC_CODE;
			displayString = BP_SYSTOLIC_DIASTOLIC_DISPLAY;
			
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
					quantity.setUnit(fObservationView.getUnitConcept().getName());
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
			Reference personRef = new Reference(new IdType(PatientResourceProvider.getType(), fObservationView.getPerson().getId()));
			personRef.setDisplay(fObservationView.getPerson().getNameAsSingleString());
			observation.setSubject(personRef);
		}
		if (fObservationView.getVisitOccurrence() != null)
			observation.getContext().setReferenceElement(new IdType (EncounterResourceProvider.getType(), fObservationView.getVisitOccurrence().getId()));
		
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
		
		if (fObservationView.getProvider() != null) {
			Reference performerRef = new Reference (new IdType(PractitionerResourceProvider.getType(), fObservationView.getProvider().getId()));
			String providerName = fObservationView.getProvider().getProviderName();
			if (providerName != null && !providerName.isEmpty())
				performerRef.setDisplay(providerName);
			observation.addPerformer(performerRef);
		}
		
		return observation;
	}
	
//	@Override
//	public Observation constructResource(Long fhirId, FObservationView entity, List<String> includes) {
//		Observation observation = constructFHIR(fhirId, entity);
//
//		return observation;
//	}

	private Long HandleBloodPressure(Observation fhirResource, IdType fhirId, Long fhirSubjectId, Long omopPersonId) throws FHIRException {
		// This is measurement. And, fhirId is for systolic. 
		// And, for update, we need to find diastolic and update that as well.
		Measurement systolicMeasurement = null;
		Measurement diastolicMeasurement = null;
		Long fhirIdLong = null;
		Long omopId = null;
		
		if (fhirId != null) {
			fhirIdLong = fhirId.getIdPartAsLong();
			omopId = IdMapping.getOMOPfromFHIR(fhirIdLong, OrganizationResourceProvider.getType());
			if (omopId == null)
				return null;
		}
		
		String identifier_value = null;
		List<Identifier> identifiers = fhirResource.getIdentifier();
		for (Identifier identifier : identifiers) {
			identifier_value = identifier.getValue();
			List<Measurement> measurements = measurementService.searchByColumnString("sourceValue", identifier_value);
			
			for (Measurement measurement : measurements) {
				if (systolicMeasurement == null && measurement.getMeasurementConcept().getId() == SYSTOLIC_CONCEPT_ID) {
					systolicMeasurement = measurement;
				} 
				if (diastolicMeasurement == null && measurement.getMeasurementConcept().getId() == DIASTOLIC_CONCEPT_ID) {
					diastolicMeasurement = measurement;
				}
			}
			if (systolicMeasurement != null && diastolicMeasurement != null)
				break;
		}
		
		Type systolicValue = null;
		Type diastolicValue = null;
		List<ObservationComponentComponent> components = fhirResource.getComponent();
		for (ObservationComponentComponent component : components) {
			List<Coding> codings = component.getCode().getCoding();
			for (Coding coding : codings) {
				String fhirSystem = coding.getSystem();
				String fhirCode = coding.getCode();
				
				if (OmopCodeableConceptMapping.LOINC.getFhirUri().equals(fhirSystem) &&
						SYSTOLIC_LOINC_CODE.equals(fhirCode)) {
					Type value = component.getValue();
					if (value != null && !value.isEmpty()) {
						systolicValue = value;
					}
				} else if (OmopCodeableConceptMapping.LOINC.getFhirUri().equals(fhirSystem) &&
						DIASTOLIC_LOINC_CODE.equals(fhirCode)) {
					Type value = component.getValue();
					if (value != null && !value.isEmpty()) {
						diastolicValue = value;
					}
				}
			}
		}
		
		if (systolicValue == null && diastolicValue == null) {
			throw new FHIRException("Either systolic or diastolic needs to be available in component");
		}
		
		if (fhirIdLong == null) {
			// Create. 
			// Note: we will return systolic measurement's id unless
			//   systolic value is missing in FHIR.
			
			
			FPerson tPerson = new FPerson();
			tPerson.setId(omopPersonId);

			if (systolicMeasurement == null && systolicValue != null) {
				systolicMeasurement = new Measurement();
				
				if (identifier_value != null) {
					systolicMeasurement.setSourceValue(identifier_value);
				}
				systolicMeasurement.setFperson(tPerson);
				
			}
			if (diastolicMeasurement == null && diastolicValue != null) {
				diastolicMeasurement = new Measurement();

				if (identifier_value != null) {
					diastolicMeasurement.setSourceValue(identifier_value);
				}
				diastolicMeasurement.setFperson(tPerson);
			}
			
			
		} else {
			// Update
			// Sanity check. The entry found from identifier should have matching id.
			if (systolicMeasurement != null) {
				if (systolicMeasurement.getId() != omopId) {
					throw new FHIRException("The systolic measurement has incorrect id or identifier.");
				}
			} else {
				// Now check if we have disastoic measurement.
				if (diastolicMeasurement != null) {
					// OK, originally, we had no systolic. Do the sanity check 
					// with diastolic measurement.
					if (diastolicMeasurement.getId() != omopId) {
						throw new FHIRException("The diastolic measurement has incorrect id or identifier.");
					}
				}
			}
			
			// Update. We use systolic measurement id as our prime id. However,
			// sometimes, there is a chance that only one is available.
			// If systolic is not available, diastolic will use the id.
			// Thus, we first need to check if
			if (systolicMeasurement == null) {
				if (systolicValue != null)
					systolicMeasurement = measurementService.findById(omopId);
			}
			if (diastolicMeasurement == null) {
				if (diastolicValue != null) {
					// We have diastolic value. But, we cannot use omopId here.
					// 
					diastolicMeasurement = measurementService.findById(omopId);
				}
			}
			
			if (systolicMeasurement == null && diastolicMeasurement == null) {
				throw new FHIRException("Failed to get either systolic or diastolic measurement for update.");
			}
		}
	
		// We look at component coding.
		if (systolicMeasurement != null) {
			Concept codeConcept = new Concept();
			codeConcept.setId(SYSTOLIC_CONCEPT_ID);
			systolicMeasurement.setMeasurementConcept(codeConcept);
			
			if (systolicValue instanceof Quantity) {
				systolicMeasurement.setValueAsNumber(((Quantity)systolicValue).getValue().doubleValue());

				// Save the unit in the unit source column to save the source value.
				String unitString = ((Quantity) systolicValue).getUnit();
				systolicMeasurement.setUnitSourceValue(unitString);
				
				String unitSystem = ((Quantity) systolicValue).getSystem();
				String unitCode = ((Quantity) systolicValue).getCode();
				String omopVocabularyId = OmopCodeableConceptMapping.omopVocabularyforFhirUri(unitSystem);
				if (omopVocabularyId != null) {
					Concept unitConcept = CodeableConceptUtil.getOmopConceptWith(conceptService, omopVocabularyId, unitCode);
					systolicMeasurement.setUnitConcept(unitConcept);
				}
				systolicMeasurement.setValueSourceValue(((Quantity) systolicValue).getValue().toString());
			} else if (systolicValue instanceof CodeableConcept) {
				Concept systolicValueConcept = CodeableConceptUtil.searchConcept(conceptService, (CodeableConcept)systolicValue);
				systolicMeasurement.setValueAsConcept(systolicValueConcept);
				systolicMeasurement.setValueSourceValue(((CodeableConcept) systolicValue).toString());
			} else
				throw new FHIRException("Systolic measurement should be either Quantity or CodeableConcept");
		}
		
		if (diastolicMeasurement != null) {
			Concept codeConcept = new Concept();
			codeConcept.setId(DIASTOLIC_CONCEPT_ID);
			diastolicMeasurement.setMeasurementConcept(codeConcept);

			if (diastolicValue instanceof Quantity) {
				diastolicMeasurement.setValueAsNumber(((Quantity)diastolicValue).getValue().doubleValue());
				
				// Save the unit in the unit source column to save the source value.
				String unitString = ((Quantity) diastolicValue).getUnit();
				diastolicMeasurement.setUnitSourceValue(unitString);
				
				String unitSystem = ((Quantity) diastolicValue).getSystem();
				String unitCode = ((Quantity) diastolicValue).getCode();
				String omopVocabularyId = OmopCodeableConceptMapping.omopVocabularyforFhirUri(unitSystem);
				if (omopVocabularyId != null) {
					Concept unitConcept = CodeableConceptUtil.getOmopConceptWith(conceptService, omopVocabularyId, unitCode);
					diastolicMeasurement.setUnitConcept(unitConcept);
				}
				diastolicMeasurement.setValueSourceValue(((Quantity) diastolicValue).getValue().toString());
			} else if (diastolicValue instanceof CodeableConcept) {
				Concept diastolicValueConcept = CodeableConceptUtil.searchConcept(conceptService, (CodeableConcept)diastolicValue);
				diastolicMeasurement.setValueAsConcept(diastolicValueConcept);
				diastolicMeasurement.setValueSourceValue(((CodeableConcept)diastolicValue).toString());
			} else
				throw new FHIRException("Diastolic measurement should be either Quantity or CodeableConcept");			
		}
				
		// Get low and high range if available. 
		// Components have two value. From the range list, we should
		// find the matching range. If exists, we can update measurement 
		// entity class.
		List<ObservationReferenceRangeComponent> ranges = fhirResource.getReferenceRange();
		List<Coding> codings;
		
		// For BP, we should walk through these range references and
		// find a right matching one to put our measurement entries.
		for (ObservationReferenceRangeComponent range : ranges) {
			if (range.isEmpty()) continue;
			
			// Get high and low values.
			SimpleQuantity highQtyValue = range.getHigh();
			SimpleQuantity lowQtyValue = range.getLow();
			if (highQtyValue.isEmpty() && lowQtyValue.isEmpty()) {
				// We need these values. If these are empty.
				// We have no reason to look at the appliesTo data.
				// Skip to next reference.
				continue;
			}
			
			// Check the all the included FHIR concept codes. 
			List<CodeableConcept> rangeConceptCodes = range.getAppliesTo();
			for (CodeableConcept rangeConceptCode : rangeConceptCodes) {
				codings = rangeConceptCode.getCoding();
				for (Coding coding : codings) {
					if (OmopCodeableConceptMapping.LOINC.fhirUri.equals(coding.getSystem())) {
						if (SYSTOLIC_LOINC_CODE.equals(coding.getCode())) {
							// This applies to Systolic blood pressure.
							if (systolicMeasurement != null) {
								if (!highQtyValue.isEmpty()) {
									systolicMeasurement.setRangeHigh(highQtyValue.getValue().doubleValue());
								}
								if (!lowQtyValue.isEmpty()) {
									systolicMeasurement.setRangeLow(lowQtyValue.getValue().doubleValue());
								}
								break;
							} else {
								throw new FHIRException("Systolic value is not available. But, range for systolic is provided. BP data inconsistent");
							}
						} else if (DIASTOLIC_LOINC_CODE.equals(coding.getCode())) {
							// This applies to Diastolic blood pressure.
							if (diastolicMeasurement != null) {
								if (!highQtyValue.isEmpty()) {
									diastolicMeasurement.setRangeHigh(highQtyValue.getValue().doubleValue());
								}
								if (!lowQtyValue.isEmpty()) {
									diastolicMeasurement.setRangeLow(lowQtyValue.getValue().doubleValue());
								}
								break;								
							} else {
								throw new FHIRException("Diastolic value is not available. But, range for diastolic is provided. BP data inconsistent");								
							}
						}
					}
				}
			}			
		}
				
		SimpleDateFormat timeFormat = new SimpleDateFormat ("HH:mm:ss");
		if (fhirResource.getEffective() instanceof DateTimeType) {
			Date date = ((DateTimeType) fhirResource.getEffective()).getValue();
			if (systolicMeasurement != null) {
				systolicMeasurement.setDate(date);
				systolicMeasurement.setTime(timeFormat.format(date));
			}
			if (diastolicMeasurement != null) {
				diastolicMeasurement.setDate(date);
				diastolicMeasurement.setTime(timeFormat.format(date));
			}
		} else if (fhirResource.getEffective() instanceof Period) {
			Date startDate = ((Period) fhirResource.getEffective()).getStart();
			if (startDate != null) {
				if (systolicMeasurement != null) {
					systolicMeasurement.setDate(startDate);
					systolicMeasurement.setTime(timeFormat.format(startDate));
				}
			}
			if (startDate != null) {
				if (diastolicMeasurement != null) {
					diastolicMeasurement.setDate(startDate);
					diastolicMeasurement.setTime(timeFormat.format(startDate));
				}
			}
		}
		
		/* Set visit occurrence */
		Reference contextReference = fhirResource.getContext();
		VisitOccurrence visitOccurrence = null;
		if (contextReference != null && !contextReference.isEmpty()) {
			if (contextReference.getReferenceElement().getResourceType().equals(EncounterResourceProvider.getType())) {
				// Encounter context.
				Long fhirEncounterId = contextReference.getReferenceElement().getIdPartAsLong();
				Long omopVisitOccurrenceId = IdMapping.getOMOPfromFHIR(fhirEncounterId, EncounterResourceProvider.getType());
				if (omopVisitOccurrenceId != null) {
					visitOccurrence  = visitOccurrenceService.findById(omopVisitOccurrenceId);
				}
				if (visitOccurrence == null) {
					throw new FHIRException("The Encounter ("+contextReference.getReference()+") context couldn't be found.");
				} else {
					if (systolicMeasurement != null) {
						systolicMeasurement.setVisitOccurrence(visitOccurrence);
					}
					if (diastolicMeasurement != null) {
						diastolicMeasurement.setVisitOccurrence(visitOccurrence);
					}
				}
			} else {
				// Episode of Care context.
				// TODO: Do we have a mapping for the Episode of Care??
			}
		}

		List<CodeableConcept> categories = fhirResource.getCategory();
		Long typeConceptId = 0L;
		for (CodeableConcept category : categories) {
			codings = category.getCoding();
			for (Coding coding : codings) {
				String fhirSystem = coding.getSystem();
				String fhirCode = coding.getCode();
				if (fhirSystem == null || fhirSystem.isEmpty() || fhirCode == null || fhirCode.isEmpty()) {
					continue;
				}
				typeConceptId = OmopConceptMapping.omopForObservationCategoryCode(fhirCode);
				if (typeConceptId > 0L) break;
			}
			if (typeConceptId > 0L) break;
		}
		
		Concept typeConcept = new Concept();
		typeConcept.setId(typeConceptId);

		Long retvalSystolic=null, retvalDiastolic=null;
		if (systolicMeasurement != null) {
			systolicMeasurement.setType(typeConcept);
			if (systolicMeasurement.getId() != null) {
				retvalSystolic = measurementService.update(systolicMeasurement).getId();
			} else {
				retvalSystolic = measurementService.create(systolicMeasurement).getId();
			}
		} 
		if (diastolicMeasurement != null) {
			diastolicMeasurement.setType(typeConcept);
			if (diastolicMeasurement.getId() != null) {
				retvalDiastolic = measurementService.update(diastolicMeasurement).getId();				
			} else {
				retvalDiastolic = measurementService.create(diastolicMeasurement).getId();
			}
		}
		
		if (retvalSystolic != null)
			return retvalSystolic;
		else if (retvalDiastolic != null) 
			return retvalDiastolic;
		else
			return null;
	}
	
	@Override
	public Long toDbase(Observation fhirResource, IdType fhirId) throws FHIRException {
		Long fhirIdLong = null;
		if (fhirId != null) {
			fhirIdLong = fhirId.getIdPartAsLong();
		}
		
		// Observation data needs to be split into 
		// observation and measurement in OMOP. We decide them by domain of
		// the value.
		String omopTableName = null;
		
		Reference subjectReference = fhirResource.getSubject();
		if (subjectReference == null) {
			throw new FHIRException("We requres subject to contain a Patient");
		}
		if (!subjectReference.getReferenceElement().getResourceType().equalsIgnoreCase(PatientResourceProvider.getType())) {
			throw new FHIRException("We only support "+PatientResourceProvider.getType()+" for subject. But provided ["+subjectReference.getReferenceElement().getResourceType()+"]");
		}
		
		Long fhirSubjectId;
		try {
			fhirSubjectId = subjectReference.getReferenceElement().getIdPartAsLong();
		} catch (NumberFormatException e) {
			throw new FHIRException(e.getMessage());
		}
		
		Long omopPersonId = IdMapping.getOMOPfromFHIR(fhirSubjectId, PatientResourceProvider.getType());
		if (omopPersonId == null) {
			throw new FHIRException("We couldn't find the patient in the Subject");
		} else {
			
		}
		
		for (Coding coding : fhirResource.getCode().getCoding()) {
			String code = coding.getCode();
			String system = coding.getSystem();
			
			// If we have BP information, we handle this separately.
			// OMOP cannot handle multiple entries. So, we do not have 
			// this code in our concept table.
			if (system.equals(OmopCodeableConceptMapping.LOINC.getFhirUri()) &&
					code.equals(BP_SYSTOLIC_DIASTOLIC_CODE)) {
				// OK, we have BP systolic & diastolic. Handle this separately.
				// We will not come back.
				return HandleBloodPressure(fhirResource, fhirId, fhirSubjectId, omopPersonId);
			}
			List<Concept> conceptForCodes = conceptService.searchByColumnString("conceptCode", code);
			for (Concept conceptForCode : conceptForCodes) {
				String domain = conceptForCode.getDomain();
				String systemName = conceptForCode.getVocabulary().getId();
				try {
					if (domain.equalsIgnoreCase("measurement") && 
							systemName.equalsIgnoreCase(OmopCodeableConceptMapping.omopVocabularyforFhirUri(system))) {
						omopTableName = "Measurement";
						break;
					} else if (domain.equalsIgnoreCase("observation") &&
							systemName.equalsIgnoreCase(OmopCodeableConceptMapping.omopVocabularyforFhirUri(system))) {
						omopTableName = "Observation";
						break;
					}
				} catch (FHIRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (omopTableName != null) break;
		}
		
		if (omopTableName == null) {
			return null;
		}
		
		Measurement measurement = null;
		edu.gatech.chai.omopv5.jpa.entity.Observation observation = null;
		if (fhirIdLong == null) {
			// This is CREATE.
			// Check if we already have this measurement.
			// Check identifier to check if we have this.
			List<Identifier> identifiers = fhirResource.getIdentifier();
			String identifier_value = null;
			for (Identifier identifier : identifiers) {
				identifier_value = identifier.getValue();
				if (identifier_value != null) {
					if ("Measurement".equals(omopTableName)) {
						List<Measurement> results = measurementService.searchByColumnString("sourceValue", identifier_value);
						if (results.size()>0) {
							// We do not CREATE. Instead, we update this.
							// set the measurement.
							measurement = results.get(0);
							break;
						}
					} else {
						List<edu.gatech.chai.omopv5.jpa.entity.Observation> results = observationService.searchByColumnString("sourceValue", identifier_value);
						if (results.size()>0) {
							// We do not CREATE. Instead, we update this.
							// set the measurement.
							observation = results.get(0);
							break;
						}
					}
				}
			}
			
			FPerson tPerson = new FPerson();
			tPerson.setId(omopPersonId);

			if ("Measurement".equals(omopTableName) && measurement == null) {
				// This is NEW.
				measurement = new Measurement();
				if (identifier_value != null) {
					measurement.setSourceValue(identifier_value);
				}
				measurement.setFperson(tPerson);
			}
			if ("Observation".equals(omopTableName) && observation == null) {
				// This is NEW.
				observation = new edu.gatech.chai.omopv5.jpa.entity.Observation();
				if (identifier_value != null) {
					observation.setSourceValue(identifier_value);
				}
				observation.setFperson(tPerson);
			}
		} else {
			// This is UPDATE
			Long omopId = IdMapping.getOMOPfromFHIR(fhirIdLong, ObservationResourceProvider.getType());
			if ("Measurement".equals(omopTableName)) {
				measurement = measurementService.findById(omopId);
				if (measurement == null) {
					// We have no measurement to update.
					throw new FHIRException("We have no matching FHIR Observation (Measurement) to update.");
				}
				// Update subject.
				measurement.getFperson().setId(omopPersonId);
				// We may have the identifier changed, which is source column in OMOP.
				// We always use the first identifier.
				Identifier identifier = fhirResource.getIdentifierFirstRep();
				if (identifier != null) {
					String value = identifier.getValue();
					if (value != null && !value.isEmpty())
						measurement.setSourceValue(value);
				}
			} else {
				observation = observationService.findById(omopId);
				if (observation == null) {
					// We have no observation to update.
					throw new FHIRException("We have no matching FHIR Observation (Observation) to update.");
				}
				// Update subject.
				observation.getFperson().setId(omopPersonId);
				// We may have the identifier changed, which is source column in OMOP.
				// We always use the first identifier.
				Identifier identifier = fhirResource.getIdentifierFirstRep();
				if (identifier != null) {
					String value = identifier.getValue();
					if (value != null && !value.isEmpty())
						observation.setSourceValue(value);
				}
			}			
		}

		// We prefer LOINC code. So, even though we looped coding.
		// Let's do it again.
		// If we do not have LOINC there, we try first one.
		CodeableConcept code = fhirResource.getCode();
		
		// code should NOT be null as this is required field.
		// And, validation should check this.
		List<Coding> codings = code.getCoding();
		Coding codingFound = null;
		Coding codingSecondChoice = null;
		String OmopSystem = null;
		for (Coding coding : codings) {
			String fhirSystemUri = coding.getSystem();
			if (fhirSystemUri.equals(OmopCodeableConceptMapping.LOINC.getFhirUri())) {
				// Found the code we want.
				codingFound = coding;
				break;
			} else {
				// See if we can handle this coding.
				try {
					OmopSystem = OmopCodeableConceptMapping.omopVocabularyforFhirUri(fhirSystemUri);
				} catch (FHIRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if ("None".equals(OmopSystem) == false) {
					// We can at least handle this. Save it
					// We may find another one we can handle. Let it replace.
					// 2nd choice is just 2nd choice.
					codingSecondChoice = coding;
				}
			}
		}
		
		if (codingFound==null && codingSecondChoice == null) {
			// We can't save this resource to OMOP.. sorry...
			throw new FHIRException("We couldn't support the code");
		}
		
		Concept concept = null;
		if (codingFound != null) {
			// Find the concept id for this coding.
			concept = CodeableConceptUtil.getOmopConceptWith(
					conceptService, 
					OmopCodeableConceptMapping.LOINC.getOmopVocabulary(), 
					codingFound.getCode());
			if (concept == null) {
				throw new FHIRException("We couldn't map the code - "+OmopCodeableConceptMapping.LOINC.getFhirUri()+":"+codingFound.getCode());
			}
		} else {
			// This is not our first choice. But, found one that we can
			// map.
			concept = CodeableConceptUtil.getOmopConceptWith(
					conceptService, 
					OmopSystem, 
					codingSecondChoice.getCode());
			if (concept == null) {
				throw new FHIRException("We couldn't map the code - "+OmopCodeableConceptMapping.fhirUriforOmopVocabularyi(OmopSystem)+":"+codingSecondChoice.getCode());
			}
		}
		
		if ("Measurement".equals(omopTableName))
			measurement.setMeasurementConcept(concept);
		else
			observation.setObservationConcept(concept);
		
		/* Set the value of the observation */
		Type valueType = fhirResource.getValue();
		if (valueType instanceof Quantity) {
			if ("Measurement".equals(omopTableName)) {
				measurement.setValueAsNumber(((Quantity) valueType).getValue().doubleValue());
				measurement.setValueSourceValue(String.valueOf(((Quantity) valueType).getValue()));
			} else {
				observation.setValueAsNumber(((Quantity) valueType).getValue().doubleValue());
			}
			
			// For unit, OMOP need unit concept
			String unitCode = ((Quantity) valueType).getCode();
			String unitSystem = ((Quantity) valueType).getSystem();
			
			String omopVocabulary = null;
			concept = null;
			if (unitCode != null && !unitCode.isEmpty()) {
				if (unitSystem == null || unitSystem.isEmpty()) {
					// If system is empty, then we check UCUM for the unit.
					omopVocabulary = OmopCodeableConceptMapping.UCUM.getOmopVocabulary();
				} else {
					omopVocabulary = OmopCodeableConceptMapping.omopVocabularyforFhirUri(unitSystem);
				}
				concept = CodeableConceptUtil.getOmopConceptWith(
						conceptService, 
						omopVocabulary, 
						unitCode);
			}
			
			// Save the unit in the unit source column to save the source value.
			String unitString = ((Quantity) valueType).getUnit();
			
			if ("Measurement".equals(omopTableName)) {
				measurement.setUnitSourceValue(unitString);
			} else {
				observation.setUnitSourceValue(unitString);
			}
			
			if (concept != null) {
				// If we found the concept for unit, use it. Otherwise, leave it empty.
				// We still have this in the unit source column.
				if ("Measurement".equals(omopTableName)) {
					measurement.setUnitConcept(concept);
				} else {
					observation.setUnitConcept(concept);
				}
			}
			
		} else if (valueType instanceof CodeableConcept) {
			// We have coeable concept value. Get System and Value.
			// FHIR allows one value[x]. 
			codings = ((CodeableConcept) valueType).getCoding();
			concept = null;
			for (Coding coding : codings) {
				String fhirSystem = coding.getSystem();
				String fhirCode = coding.getCode();
				
				if (fhirSystem == null || fhirSystem.isEmpty() || fhirCode == null || fhirCode.isEmpty()) {
					continue;
				}
				
				String omopVocabulary = OmopCodeableConceptMapping.omopVocabularyforFhirUri(fhirSystem);
				concept = CodeableConceptUtil.getOmopConceptWith(
						conceptService, 
						omopVocabulary, 
						fhirCode);
				
				if (concept == null) {
					throw new FHIRException("We couldn't map the codeable concept value - "+fhirSystem+":"+fhirCode);
				}
				break;
			}
			if (concept == null) {
				throw new FHIRException("We couldn't find a concept to map the codeable concept value.");
			}
			
			if ("Measurement".equals(omopTableName))
				measurement.setValueAsConcept(concept);
			else
				observation.setValueAsConcept(concept);
		} 

		// Get low and high range if available. This is only applicable to 
		// measurement.
		if ("Measurement".equals(omopTableName)) {
			if (!fhirResource.getReferenceRangeFirstRep().isEmpty()) {
				SimpleQuantity high = fhirResource.getReferenceRangeFirstRep().getHigh();
				if (!high.isEmpty()) {
					measurement.setRangeHigh(high.getValue().doubleValue());
				}
			}
			if (!fhirResource.getReferenceRangeFirstRep().isEmpty()) {
				SimpleQuantity low = fhirResource.getReferenceRangeFirstRep().getLow();
				if (!low.isEmpty()) {
					measurement.setRangeLow(low.getValue().doubleValue());
				}
			}
		}
		
		SimpleDateFormat timeFormat = new SimpleDateFormat ("HH:mm:ss");
		if (fhirResource.getEffective() instanceof DateType) {
			Date date = ((DateType) fhirResource.getEffective()).getValue();
			if ("Measurement".equals(omopTableName)) {
				measurement.setDate(date);
				measurement.setTime(timeFormat.format(date));
			} else {
				observation.setDate(date);
				observation.setTime(timeFormat.format(date));
			}
		} else if (fhirResource.getEffective() instanceof Period) {
			Date startDate = ((Period) fhirResource.getEffective()).getStart();
			if (startDate != null) {
				if ("Measurement".equals(omopTableName)) {
					measurement.setDate(startDate);
					measurement.setTime(timeFormat.format(startDate));
				} else {
					observation.setDate(startDate);
					observation.setTime(timeFormat.format(startDate));
				}
			}
		}
		/* Set visit occurrence */
		Reference contextReference = fhirResource.getContext();
		VisitOccurrence visitOccurrence = null;
		if (contextReference != null) {
			if (contextReference.getReferenceElement().getResourceType().equals(EncounterResourceProvider.getType())) {
				// Encounter context.
				Long fhirEncounterId = contextReference.getReferenceElement().getIdPartAsLong();
				Long omopVisitOccurrenceId = IdMapping.getOMOPfromFHIR(fhirEncounterId, EncounterResourceProvider.getType());
				if (omopVisitOccurrenceId != null) {
					visitOccurrence  = visitOccurrenceService.findById(omopVisitOccurrenceId);
				}
				if (visitOccurrence == null) {
					throw new FHIRException("The Encounter ("+contextReference.getReference()+") context couldn't be found.");
				} else {
					if ("Measurement".equals(omopTableName))
						measurement.setVisitOccurrence(visitOccurrence);
					else
						observation.setVisitOccurrence(visitOccurrence);
				}
			} else {
				// Episode of Care context.
				// TODO: Do we have a mapping for the Episode of Care??
			}
		}

		List<CodeableConcept> categories = fhirResource.getCategory();
		Long typeConceptId = 0L;
		for (CodeableConcept category : categories) {
			codings = category.getCoding();
			for (Coding coding : codings) {
				String fhirSystem = coding.getSystem();
				String fhirCode = coding.getCode();
				if (fhirSystem == null || fhirSystem.isEmpty() || fhirCode == null || fhirCode.isEmpty()) {
					continue;
				}
				typeConceptId = OmopConceptMapping.omopForObservationCategoryCode(fhirCode);
				if (typeConceptId > 0L) break;
			}
			if (typeConceptId > 0L) break;
		}
		
		concept = new Concept();
		concept.setId(typeConceptId);

		Long retval;
		if ("Measurement".equals(omopTableName)) {
			measurement.setType(concept);
			if (measurement.getId() != null) {
				retval = measurementService.update(measurement).getId();
			} else {
				retval = measurementService.create(measurement).getId();
			}
		} else {
			observation.setTypeConcept(concept);
			if (observation.getId() != null) {
				retval = observationService.update(observation).getId();
			} else {
				retval = observationService.create(observation).getId();
			}
		}
		
		Long retFhirId = IdMapping.getFHIRfromOMOP(retval, ObservationResourceProvider.getType()); 
		return retFhirId;
	}

	
	// Blood Pressure is stored in the component. So, we store two values in
	// the component section. We do this by selecting diastolic when systolic
	// is selected. Since we are selecting this already, we need to skip diastolic.
	final ParameterWrapper exceptionParam = new ParameterWrapper(
			"Long",
			Arrays.asList("observationConcept.id"),
			Arrays.asList("!="),
			Arrays.asList(String.valueOf(DIASTOLIC_CONCEPT_ID)),
			"or"
			);
	
	@Override
	public Long getSize() {
		Map<String, List<ParameterWrapper>> map = new HashMap<String, List<ParameterWrapper>> ();

		List<ParameterWrapper> exceptions = new ArrayList<ParameterWrapper>();
		exceptions.add(exceptionParam);
		map.put(MAP_EXCEPTION_EXCLUDE, exceptions);

		return getMyOmopService().getSize(map);
	}

	@Override
	public Long getSize(Map<String, List<ParameterWrapper>> map) {
		List<ParameterWrapper> exceptions = new ArrayList<ParameterWrapper>();
		exceptions.add(exceptionParam);
		map.put(MAP_EXCEPTION_EXCLUDE, exceptions);
		
		return getMyOmopService().getSize(map);
	}

	@Override
	public void searchWithoutParams(int fromIndex, int toIndex, List<IBaseResource> listResources,
			List<String> includes) {
		
		Map<String, List<ParameterWrapper>> map = new HashMap<String, List<ParameterWrapper>> ();
		searchWithParams(fromIndex, toIndex, map, listResources, includes);

//		List<ParameterWrapper> exceptions = new ArrayList<ParameterWrapper>();
//		exceptions.add(exceptionParam);
//		map.put(MAP_EXCEPTION_EXCLUDE, exceptions);
//
//		List<FObservationView> fObservationViews = getMyOmopService().searchWithParams(fromIndex, toIndex, map);
//
//		// We got the results back from OMOP database. Now, we need to construct
//		// the list of
//		// FHIR Patient resources to be included in the bundle.
//		for (FObservationView fObservationView : fObservationViews) {
//			Long omopId = fObservationView.getId();
//			Long fhirId = IdMapping.getFHIRfromOMOP(omopId, ObservationResourceProvider.getType());
//			Observation fhirResource = constructResource(fhirId, fObservationView, includes);
//			if (fhirResource != null) {
//				listResources.add(fhirResource);
//				// Do the rev_include and add the resource to the list.
//				addRevIncludes(omopId, includes, listResources);
//			}
//		}
//		
	}

	@Override
	public void searchWithParams(int fromIndex, int toIndex, Map<String, List<ParameterWrapper>> map,
			List<IBaseResource> listResources, List<String> includes) {
		List<ParameterWrapper> exceptions = new ArrayList<ParameterWrapper>();
		exceptions.add(exceptionParam);
		map.put(MAP_EXCEPTION_EXCLUDE, exceptions);

		List<FObservationView> fObservationViews = getMyOmopService().searchWithParams(fromIndex, toIndex, map);

		for (FObservationView fObservationView : fObservationViews) {
			Long omopId = fObservationView.getId();
			Long fhirId = IdMapping.getFHIRfromOMOP(omopId, ObservationResourceProvider.getType());
			Observation fhirResource = constructResource(fhirId, fObservationView, includes);
			if (fhirResource != null) {
				listResources.add(fhirResource);
				// Do the rev_include and add the resource to the list.
				addRevIncludes(omopId, includes, listResources);
			}
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
		case Observation.SP_RES_ID:
			String organizationId = ((TokenParam) value).getValue();
			paramWrapper.setParameterType("Long");
			paramWrapper.setParameters(Arrays.asList("id"));
			paramWrapper.setOperators(Arrays.asList("="));
			paramWrapper.setValues(Arrays.asList(organizationId));
			paramWrapper.setRelationship("or");
			mapList.add(paramWrapper);
			break;
		case Observation.SP_CODE:
			String system = ((TokenParam) value).getSystem();
			String code = ((TokenParam) value).getValue();
			String omopVocabulary = null;
			if (system != null && !system.isEmpty()) {
				try {
					omopVocabulary = OmopCodeableConceptMapping.omopVocabularyforFhirUri(system);
				} catch (FHIRException e) {
					e.printStackTrace();
					break;
				}
			} else {
				omopVocabulary = "None";
			}

			if (omopVocabulary.equals(OmopCodeableConceptMapping.LOINC.getOmopVocabulary())) {
				// This is LOINC code.
				// Check if this is for BP.
				if (code != null && !code.isEmpty()) {
					if (BP_SYSTOLIC_DIASTOLIC_CODE.equals(code)) {
						// In OMOP, we have systolic and diastolic as separate entries.
						// We search for systolic. When constructing FHIR<, constructFHIR
						// will search matching diastolic value.
						paramWrapper.setParameterType("String");
						paramWrapper.setParameters(Arrays.asList("observationConcept.vocabulary.id", "observationConcept.conceptCode"));
						paramWrapper.setOperators(Arrays.asList("like", "like"));
						paramWrapper.setValues(Arrays.asList(omopVocabulary, SYSTOLIC_LOINC_CODE));
						paramWrapper.setRelationship("and");
						mapList.add(paramWrapper);
					} else {
						paramWrapper.setParameterType("String");
						paramWrapper.setParameters(Arrays.asList("observationConcept.vocabulary.id", "observationConcept.conceptCode"));
						paramWrapper.setOperators(Arrays.asList("like", "like"));
						paramWrapper.setValues(Arrays.asList(omopVocabulary, code));
						paramWrapper.setRelationship("and");
						mapList.add(paramWrapper);
					}
				} else {
					// We have no code specified. Search by system.
					paramWrapper.setParameterType("String");
					paramWrapper.setParameters(Arrays.asList("observationConcept.vocabulary.id"));
					paramWrapper.setOperators(Arrays.asList("like"));
					paramWrapper.setValues(Arrays.asList(omopVocabulary));
					paramWrapper.setRelationship("or");
					mapList.add(paramWrapper);
				}
			} else {
				if (system == null || system.isEmpty()) {
					if (code == null || code.isEmpty()) {
						// nothing to do
						break;
					} else {
						// no system but code.
						paramWrapper.setParameterType("String");
						paramWrapper.setParameters(Arrays.asList("observationConcept.conceptCode"));
						paramWrapper.setOperators(Arrays.asList("like"));
						if (BP_SYSTOLIC_DIASTOLIC_CODE.equals(code)) 
							paramWrapper.setValues(Arrays.asList(SYSTOLIC_LOINC_CODE));
						else
							paramWrapper.setValues(Arrays.asList(code));
						paramWrapper.setRelationship("or");
						mapList.add(paramWrapper);
					}
				} else {
					if (code == null || code.isEmpty()) {
						// yes system but no code.
						paramWrapper.setParameterType("String");
						paramWrapper.setParameters(Arrays.asList("observationConcept.vocabulary.id"));
						paramWrapper.setOperators(Arrays.asList("like"));
						paramWrapper.setValues(Arrays.asList(omopVocabulary));
						paramWrapper.setRelationship("or");
						mapList.add(paramWrapper);
					} else {
						// We have both system and code.
						paramWrapper.setParameterType("String");
						paramWrapper.setParameters(Arrays.asList("observationConcept.vocabulary.id", "observationConcept.conceptCode"));
						paramWrapper.setOperators(Arrays.asList("like", "like"));
						paramWrapper.setValues(Arrays.asList(omopVocabulary, code));
						paramWrapper.setRelationship("and");
						mapList.add(paramWrapper);
					}
				}
			}
			break;
		case "Patient:"+Patient.SP_RES_ID:
			String pId = (String) value;
			paramWrapper.setParameterType("Long");
			paramWrapper.setParameters(Arrays.asList("person.id"));
			paramWrapper.setOperators(Arrays.asList("="));
			paramWrapper.setValues(Arrays.asList(pId));
			paramWrapper.setRelationship("or");
			mapList.add(paramWrapper);
			break;
		case "Patient:"+Patient.SP_NAME:
			String patientName = (String) value;
			paramWrapper.setParameterType("String");
			paramWrapper.setParameters(Arrays.asList("person.familyName", "person.givenName1", "person.givenName2", "person.prefixName", "person.suffixName"));
			paramWrapper.setOperators(Arrays.asList("like", "like", "like", "like", "like"));
			paramWrapper.setValues(Arrays.asList("%"+patientName+"%"));
			paramWrapper.setRelationship("or");
			mapList.add(paramWrapper);
			break;
		default:
			mapList = null;
		}
		
		return mapList;
	}
	

}
