package edu.gatech.chai.gtfhir2.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Dosage;
import org.hl7.fhir.dstu3.model.Duration;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestDispenseRequestComponent;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.fhir.dstu3.model.Medication.MedicationIngredientComponent;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import edu.gatech.chai.gtfhir2.provider.EncounterResourceProvider;
import edu.gatech.chai.gtfhir2.provider.MedicationRequestProvider;
import edu.gatech.chai.gtfhir2.provider.MedicationStatementResourceProvider;
import edu.gatech.chai.gtfhir2.provider.PatientResourceProvider;
import edu.gatech.chai.gtfhir2.provider.PractitionerResourceProvider;
import edu.gatech.chai.gtfhir2.utilities.CodeableConceptUtil;
import edu.gatech.chai.omopv5.jpa.entity.Concept;
import edu.gatech.chai.omopv5.jpa.entity.DrugExposure;
import edu.gatech.chai.omopv5.jpa.entity.Provider;
import edu.gatech.chai.omopv5.jpa.entity.VisitOccurrence;
import edu.gatech.chai.omopv5.jpa.service.ConceptService;
import edu.gatech.chai.omopv5.jpa.service.DrugExposureService;
import edu.gatech.chai.omopv5.jpa.service.FPersonService;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;
import edu.gatech.chai.omopv5.jpa.service.ProviderService;
import edu.gatech.chai.omopv5.jpa.service.VisitOccurrenceService;

/**
 * 
 * @author mc142
 *
 * concept id	OHDSI drug type	FHIR
 * 38000179		Physician administered drug (identified as procedure), MedicationAdministration
 * 38000180		Inpatient administration, MedicationAdministration
 * 43542356	Physician administered drug (identified from EHR problem list), MedicationAdministration
 * 43542357	Physician administered drug (identified from referral record), MedicationAdministration
 * 43542358	Physician administered drug (identified from EHR observation), MedicationAdministration
 * 581373	Physician administered drug (identified from EHR order), MedicationAdministration
 * 38000175	Prescription dispensed in pharmacy, MedicationDispense
 * 38000176	Prescription dispensed through mail order, MedicationDispense
 * 581452	Dispensed in Outpatient office, MedicationDispense
 * ******
 * 38000177	Prescription written, MedicationRequest
 * ******
 * 44787730	Patient Self-Reported Medication, MedicationStatement
 * 38000178	Medication list entry	 
 * 38000181	Drug era - 0 days persistence window	 
 * 38000182	Drug era - 30 days persistence window	 
 * 44777970	Randomized Drug	 
 */
public class OmopMedicationRequest extends BaseOmopResource<MedicationRequest, DrugExposure, DrugExposureService>
		implements IResourceMapping<MedicationRequest, DrugExposure> {

	private static Long MEDICATIONREQUEST_CONCEPT_TYPE_ID = 38000177L;
	private static OmopMedicationRequest omopMedicationRequest = new OmopMedicationRequest();
	private VisitOccurrenceService visitOccurrenceService;
	private ConceptService conceptService;
	private ProviderService providerService;
	private FPersonService fPersonService;

	public OmopMedicationRequest(WebApplicationContext context) {
		super(context, DrugExposure.class, DrugExposureService.class, MedicationRequestProvider.getType());
		initialize(context);
	}
	
	public OmopMedicationRequest() {
		super(ContextLoaderListener.getCurrentWebApplicationContext(), DrugExposure.class, DrugExposureService.class, MedicationStatementResourceProvider.getType());
		initialize(ContextLoaderListener.getCurrentWebApplicationContext());
	}

	private void initialize(WebApplicationContext context) {
		visitOccurrenceService = context.getBean(VisitOccurrenceService.class);
		conceptService = context.getBean(ConceptService.class);
		providerService = context.getBean(ProviderService.class);
		fPersonService = context.getBean(FPersonService.class);
	}

	public static OmopMedicationRequest getInstance() {
		return omopMedicationRequest;
	}
	
	@Override
	public Long toDbase(MedicationRequest fhirResource, IdType fhirId) throws FHIRException {
		DrugExposure drugExposure = null;
		Long omopId = null;
		if (fhirId != null) {
			// Update
			Long fhirIdLong = fhirId.getIdPartAsLong();
			omopId = IdMapping.getOMOPfromFHIR(fhirIdLong, MedicationRequestProvider.getType());
			drugExposure = getMyOmopService().findById(omopId);
			if (drugExposure == null) { 
				// PUT (update) to non-existing data
				return null;
			}
		} else {
			// Create
			List<Identifier> identifiers = fhirResource.getIdentifier();
			for (Identifier identifier: identifiers) {
				if (identifier.isEmpty()) continue;
				String identifierValue = identifier.getValue();
				List<DrugExposure> results = getMyOmopService().searchByColumnString("drugSourceValue", identifierValue);
				if (results.size() > 0) {
					drugExposure = results.get(0);
					break;
				}
			}
			
			if (drugExposure == null) {
				drugExposure = new DrugExposure();
				// Add the source column.
				Identifier identifier = fhirResource.getIdentifierFirstRep();
				if (!identifier.isEmpty()) {
					drugExposure.setDrugSourceValue(identifier.getValue());
				}
			}
		}
		
		
		return null;
	}

	@Override
	public MedicationRequest constructFHIR(Long fhirId, DrugExposure entity) {
		MedicationRequest medicationRequest = new MedicationRequest();
		
		medicationRequest.setId(new IdType(fhirId));
		
		// Subject from FPerson
		Reference patientRef = new Reference(new IdType(PatientResourceProvider.getType(), entity.getFPerson().getId()));
		patientRef.setDisplay(entity.getFPerson().getNameAsSingleString());
		medicationRequest.setSubject(patientRef);		
		
		// AuthoredOn. TODO: endDate is lost if we map to AuthoredOn.
		Date startDate = entity.getDrugExposureStartDate();
		Date endDate = entity.getDrugExposureEndDate();
		if (startDate != null)
			medicationRequest.setAuthoredOn(startDate);
		
		// Get codeableconcept for medication
		Concept omopDrugConcept = entity.getDrugConcept();
		CodeableConcept medicationCodeableConcept;
		try {
			medicationCodeableConcept = CodeableConceptUtil.getCodeableConceptFromOmopConcept(entity.getDrugConcept());
		} catch (FHIRException e1) {
			e1.printStackTrace();
			return null;
		}
		
		// Get ingredient codeable concept for medication
		Concept ingredient = conceptService.getIngredient(omopDrugConcept);

		// If ingredient is available, then use contained. Otherwise
		// use medicationCodeableConcept.
		if (ingredient != null) {
			CodeableConcept ingredientCodeableConcept;
			try {
				ingredientCodeableConcept = CodeableConceptUtil.getCodeableConceptFromOmopConcept(ingredient);
				if (!ingredientCodeableConcept.isEmpty()) {
					// We have ingredient information. Add this to MedicationStatement.
					// To do this, we need to add Medication resource to contained section.
					Medication medicationResource = new Medication();
					medicationResource.setCode(medicationCodeableConcept);
					MedicationIngredientComponent medIngredientComponent = new MedicationIngredientComponent();
					medIngredientComponent.setItem(ingredientCodeableConcept);
					medicationResource.addIngredient(medIngredientComponent);
					medicationResource.setId("med1");
					medicationRequest.addContained(medicationResource);
					medicationRequest.setMedication(new Reference("#med1"));
				}
			} catch (FHIRException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			medicationRequest.setMedication(medicationCodeableConcept);
		}

		// Dosage mapping
		Double dose = entity.getEffectiveDrugDose();
		SimpleQuantity doseQuantity = new SimpleQuantity();
		if (dose != null) {
			doseQuantity.setValue(dose);
		}
		
		Concept unitConcept = entity.getDoseUnitConcept();
		String unitUnit = null;
		String unitCode = null;
		String unitSystem = null;
		if (unitConcept != null) {
			String omopUnitVocab = unitConcept.getVocabulary().getId();
			String omopUnitCode = unitConcept.getConceptCode();
			String omopUnitName = unitConcept.getName();
			
			String fhirUnitUri;
			try {
				fhirUnitUri = OmopCodeableConceptMapping.fhirUriforOmopVocabulary(omopUnitVocab);
				if ("None".equals(fhirUnitUri)) {
					fhirUnitUri = unitConcept.getVocabulary().getVocabularyReference();
				}

				unitUnit = omopUnitName;
				unitCode = omopUnitCode;
				unitSystem = fhirUnitUri; 
			} catch (FHIRException e) {
				e.printStackTrace();
			}
		}
		
		if (!doseQuantity.isEmpty()) {
			doseQuantity.setUnit(unitUnit);
			doseQuantity.setCode(unitCode);
			doseQuantity.setSystem(unitSystem);
			
			Dosage dosage = new Dosage();
			dosage.setDose(doseQuantity);
			medicationRequest.addDosageInstruction(dosage);
		}

		// dispense request mapping.
		Integer refills = entity.getRefills();
		MedicationRequestDispenseRequestComponent dispenseRequest = new MedicationRequestDispenseRequestComponent();
		if (refills != null) {
			dispenseRequest.setNumberOfRepeatsAllowed(refills);
		}

		Double quantity = entity.getQuantity();
		if (quantity != null) {
			SimpleQuantity simpleQty = new SimpleQuantity();
			simpleQty.setValue(quantity);
			simpleQty.setUnit(unitUnit);
			simpleQty.setCode(unitCode);
			simpleQty.setSystem(unitSystem);
			dispenseRequest.setQuantity(simpleQty);
		}
		
		Integer daysSupply = entity.getDaysSupply();
		if (daysSupply != null) {
			Duration qty = new Duration();
			qty.setValue(daysSupply);
			// Set the UCUM unit to day.
			String fhirUri = OmopCodeableConceptMapping.UCUM.getFhirUri();
			qty.setSystem(fhirUri);
			qty.setCode("d");
			qty.setUnit("day");
			dispenseRequest.setExpectedSupplyDuration(qty);
		}
		
		if (!dispenseRequest.isEmpty()) {
			medicationRequest.setDispenseRequest(dispenseRequest);
		}
		
		// Recorder mapping
		Provider provider = entity.getProvider();
		if (provider != null) {
			Reference recorderReference = 
					new Reference(new IdType(PractitionerResourceProvider.getType(), provider.getId()));
			recorderReference.setDisplay(provider.getProviderName());
			medicationRequest.setRecorder(recorderReference);
		}
		
		// Context mapping
		VisitOccurrence visitOccurrence = entity.getVisitOccurrence();
		if (visitOccurrence != null) {
			Reference contextReference = 
					new Reference(new IdType(EncounterResourceProvider.getType(), visitOccurrence.getId()));
			medicationRequest.setContext(contextReference);
		}
		
		return medicationRequest;
	}
	
	@Override
	public List<ParameterWrapper> mapParameter(String parameter, Object value) {
		List<ParameterWrapper> mapList = new ArrayList<ParameterWrapper>();
		ParameterWrapper paramWrapper = new ParameterWrapper();
		switch (parameter) {
		case MedicationRequest.SP_RES_ID:
			String medicationRequestId = ((TokenParam) value).getValue();
			paramWrapper.setParameterType("Long");
			paramWrapper.setParameters(Arrays.asList("id"));
			paramWrapper.setOperators(Arrays.asList("="));
			paramWrapper.setValues(Arrays.asList(medicationRequestId));
			paramWrapper.setRelationship("or");
			mapList.add(paramWrapper);
			break;
		case MedicationRequest.SP_CODE:
			String system = ((TokenParam) value).getSystem();
			String code = ((TokenParam) value).getValue();
			
			if ((system == null || system.isEmpty()) && (code == null || code.isEmpty()))
				break;
			
			String omopVocabulary = "None";
			if (system != null && !system.isEmpty()) {
				try {
					omopVocabulary = OmopCodeableConceptMapping.omopVocabularyforFhirUri(system);
				} catch (FHIRException e) {
					e.printStackTrace();
				}
			} 

			paramWrapper.setParameterType("String");
			if ("None".equals(omopVocabulary) && code != null && !code.isEmpty()) {
				paramWrapper.setParameters(Arrays.asList("drugConcept.conceptCode"));
				paramWrapper.setOperators(Arrays.asList("like"));
				paramWrapper.setValues(Arrays.asList(code));
			} else if (!"None".equals(omopVocabulary) && (code == null || code.isEmpty())) {
				paramWrapper.setParameters(Arrays.asList("drugConcept.vocabulary.id"));
				paramWrapper.setOperators(Arrays.asList("like"));
				paramWrapper.setValues(Arrays.asList(omopVocabulary));				
			} else {
				paramWrapper.setParameters(Arrays.asList("drugConcept.vocabulary.id", "drugConcept.conceptCode"));
				paramWrapper.setOperators(Arrays.asList("like","like"));
				paramWrapper.setValues(Arrays.asList(omopVocabulary, code));
			}
			paramWrapper.setRelationship("and");
			mapList.add(paramWrapper);
			break;
		case MedicationRequest.SP_CONTEXT:
			Long fhirEncounterId = ((ReferenceParam) value).getIdPartAsLong();
			Long omopVisitOccurrenceId = IdMapping.getOMOPfromFHIR(fhirEncounterId, EncounterResourceProvider.getType());
			String resourceName = ((ReferenceParam) value).getResourceType();
			
			// We support Encounter so the resource type should be Encounter.
			if (EncounterResourceProvider.getType().equals(resourceName)
					&& omopVisitOccurrenceId != null) {
				paramWrapper.setParameterType("Long");
				paramWrapper.setParameters(Arrays.asList("visitOccurrence.id"));
				paramWrapper.setOperators(Arrays.asList("="));
				paramWrapper.setValues(Arrays.asList(String.valueOf(omopVisitOccurrenceId)));
				paramWrapper.setRelationship("or");
				mapList.add(paramWrapper);
			}
			break;
		case MedicationRequest.SP_AUTHOREDON:
			DateParam authoredOnDataParam = ((DateParam) value);
			ParamPrefixEnum apiOperator = authoredOnDataParam.getPrefix();
			String sqlOperator = null;
			if (apiOperator.equals(ParamPrefixEnum.GREATERTHAN)) {
				sqlOperator = ">";
			} else if (apiOperator.equals(ParamPrefixEnum.GREATERTHAN_OR_EQUALS)) {
				sqlOperator = ">=";
			} else if (apiOperator.equals(ParamPrefixEnum.LESSTHAN)) {
				sqlOperator = "<";
			} else if (apiOperator.equals(ParamPrefixEnum.LESSTHAN_OR_EQUALS)) {
				sqlOperator = "<=";
			} else if (apiOperator.equals(ParamPrefixEnum.NOT_EQUAL)) {
				sqlOperator = "!=";
			} else {
				sqlOperator = "=";
			}
			Date authoredOnDate = authoredOnDataParam.getValue();
			
			paramWrapper.setParameterType("Date");
			paramWrapper.setParameters(Arrays.asList("drugExposureStartDate"));
			paramWrapper.setOperators(Arrays.asList(sqlOperator));
			paramWrapper.setValues(Arrays.asList(String.valueOf(authoredOnDate.getTime())));
			paramWrapper.setRelationship("or");
			mapList.add(paramWrapper);
			break;
		case MedicationRequest.SP_PATIENT:
		case MedicationRequest.SP_SUBJECT:
			ReferenceParam patientReference = ((ReferenceParam) value);
			Long fhirPatientId = patientReference.getIdPartAsLong();
			Long omopPersonId = IdMapping.getOMOPfromFHIR(fhirPatientId, PatientResourceProvider.getType());

			String omopPersonIdString = String.valueOf(omopPersonId);
			
			paramWrapper.setParameterType("Long");
			paramWrapper.setParameters(Arrays.asList("fPerson.id"));
			paramWrapper.setOperators(Arrays.asList("="));
			paramWrapper.setValues(Arrays.asList(omopPersonIdString));
			paramWrapper.setRelationship("or");
			mapList.add(paramWrapper);
			break;
		default:
			mapList = null;
		}
		
		return mapList;
	}
	
	final ParameterWrapper filterParam = new ParameterWrapper(
			"Long",
			Arrays.asList("drugTypeConcept.id"),
			Arrays.asList("="),
			Arrays.asList(String.valueOf(MEDICATIONREQUEST_CONCEPT_TYPE_ID)),
			"or"
			);

	@Override
	public Long getSize() {
		Map<String, List<ParameterWrapper>> map = new HashMap<String, List<ParameterWrapper>> ();
		return getSize(map);
	}

	@Override
	public Long getSize(Map<String, List<ParameterWrapper>> map) {
		List<ParameterWrapper> exceptions = new ArrayList<ParameterWrapper>();
		exceptions.add(filterParam);
		map.put(MAP_EXCEPTION_FILTER, exceptions);

		return getMyOmopService().getSize(map);
	}

	@Override
	public void searchWithoutParams(int fromIndex, int toIndex, List<IBaseResource> listResources,
			List<String> includes) {

		// This is read all. But, since we will add an exception conditions to add filter.
		// we will call the search with params method.
		Map<String, List<ParameterWrapper>> map = new HashMap<String, List<ParameterWrapper>> ();
		searchWithParams (fromIndex, toIndex, map, listResources, includes);
	}

	@Override
	public void searchWithParams(int fromIndex, int toIndex, Map<String, List<ParameterWrapper>> map,
			List<IBaseResource> listResources, List<String> includes) {
		List<ParameterWrapper> exceptions = new ArrayList<ParameterWrapper>();
		exceptions.add(filterParam);
		map.put(MAP_EXCEPTION_FILTER, exceptions);

		List<DrugExposure> entities = getMyOmopService().searchWithParams(fromIndex, toIndex, map);

		for (DrugExposure entity : entities) {
			Long omopId = entity.getIdAsLong();
			Long fhirId = IdMapping.getFHIRfromOMOP(omopId, getMyFhirResourceType());
			MedicationRequest fhirResource = constructResource(fhirId, entity, includes);
			if (fhirResource != null) {
				listResources.add(fhirResource);			
				// Do the rev_include and add the resource to the list.
				addRevIncludes(omopId, includes, listResources);
			}

		}
	}
}
