package edu.gatech.chai.gtfhir2.mapping;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterStatus;
import org.hl7.fhir.dstu3.model.codesystems.V3ActCode;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.rest.param.TokenParam;
import edu.gatech.chai.omopv5.jpa.entity.VisitOccurrence;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;
import edu.gatech.chai.omopv5.jpa.service.VisitOccurrenceService;

public class OmopEncounter implements IResourceMapping<Encounter, VisitOccurrence> {

	private VisitOccurrenceService myOmopService;
	
	public OmopEncounter(WebApplicationContext context) {
		myOmopService = context.getBean(VisitOccurrenceService.class);
	}


	@Override
	public Encounter toFHIR(IdType id) {
		String encounterResourceName = ResourceType.Encounter.getPath();
		Long id_long_part = id.getIdPartAsLong();
		Long myId = IdMapping.getOMOPfromFHIR(id_long_part, encounterResourceName);

		VisitOccurrence visitOccurence = (VisitOccurrence) myOmopService.findById(myId);
		if (visitOccurence == null)
			return null;

		Long fhirId = IdMapping.getFHIRfromOMOP(myId, encounterResourceName);

		return constructFHIR(fhirId, visitOccurence);
	}

	public static Encounter constructFHIR(Long fhirId, VisitOccurrence visitOccurrence) {
		Encounter encounter = new Encounter();
		encounter.setId(new IdType(fhirId));
		
		if (visitOccurrence.getVisitConcept() != null) {
			String visitString = visitOccurrence.getVisitConcept().getName().toLowerCase();
			Coding coding = new Coding();
			if (visitString.contains("inpatient")) {
				coding.setSystem(V3ActCode.IMP.getSystem());
				coding.setCode(V3ActCode.IMP.toCode());
				coding.setDisplay(V3ActCode.IMP.getDisplay());
			} else if (visitString.toLowerCase().contains("outpatient")) {
				coding.setSystem(V3ActCode.AMB.getSystem());
				coding.setCode(V3ActCode.AMB.toCode());
				coding.setDisplay(V3ActCode.AMB.getDisplay());
			} else if (visitString.toLowerCase().contains("ambulatory")
					|| visitString.toLowerCase().contains("office")) {
				coding.setSystem(V3ActCode.AMB.getSystem());
				coding.setCode(V3ActCode.AMB.toCode());
				coding.setDisplay(V3ActCode.AMB.getDisplay());
			} else if (visitString.toLowerCase().contains("home")) {
				coding.setSystem(V3ActCode.HH.getSystem());
				coding.setCode(V3ActCode.HH.toCode());
				coding.setDisplay(V3ActCode.HH.getDisplay());
			} else if (visitString.toLowerCase().contains("emergency")) {
				coding.setSystem(V3ActCode.EMER.getSystem());
				coding.setCode(V3ActCode.EMER.toCode());
				coding.setDisplay(V3ActCode.EMER.getDisplay());
			} else if (visitString.toLowerCase().contains("field")) {
				coding.setSystem(V3ActCode.FLD.getSystem());
				coding.setCode(V3ActCode.FLD.toCode());
				coding.setDisplay(V3ActCode.FLD.getDisplay());
			} else if (visitString.toLowerCase().contains("daytime")) {
				coding.setSystem(V3ActCode.SS.getSystem());
				coding.setCode(V3ActCode.SS.toCode());
				coding.setDisplay(V3ActCode.SS.getDisplay());
			} else if (visitString.toLowerCase().contains("virtual")) {
				coding.setSystem(V3ActCode.VR.getSystem());
				coding.setCode(V3ActCode.VR.toCode());
				coding.setDisplay(V3ActCode.VR.getDisplay());
			} else {
				coding = null;
			}
			
			if (coding != null)
				encounter.setClass_(coding);			

		}
		
		encounter.setStatus(EncounterStatus.FINISHED);
		
		// set Patient Reference
		Reference patientReference = new Reference(new IdType(ResourceType.Patient.getPath(), visitOccurrence.getPerson().getId()));
		patientReference.setDisplay(visitOccurrence.getPerson().getNameAsSingleString());
		encounter.setSubject(patientReference);
		
		// set Period
		Period visitPeriod = new Period();
		DateFormat dateOnlyFormat = new SimpleDateFormat("yyyy/MM/dd");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		try {
			// For start Date
			String timeString = "00:00:00";
			if (visitOccurrence.getStartTime() != null && !visitOccurrence.getStartTime().isEmpty()) {
				timeString = visitOccurrence.getStartTime();
			}
			String dateTimeString = dateOnlyFormat.format(visitOccurrence.getStartDate())+" "+timeString;
			Date DateTime = dateFormat.parse(dateTimeString);
			visitPeriod.setStart(DateTime);

			// For end Date
			timeString = "00:00:00";
			if (visitOccurrence.getEndTime() != null && !visitOccurrence.getEndTime().isEmpty()) {
				timeString = visitOccurrence.getEndTime();
			}
			dateTimeString = dateOnlyFormat.format(visitOccurrence.getEndDate())+" "+timeString;
			DateTime = dateFormat.parse(dateTimeString);
			visitPeriod.setEnd(DateTime);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		encounter.setPeriod(visitPeriod);
		
		if (visitOccurrence.getProvider() != null) {
			Reference serviceProviderReference = new Reference(new IdType(ResourceType.Practitioner.getPath(), visitOccurrence.getProvider().getId()));
			serviceProviderReference.setDisplay(visitOccurrence.getProvider().getProviderName());
			encounter.setServiceProvider(serviceProviderReference);
		}
		
		if (visitOccurrence.getCareSite() != null) {
			Reference individualReference = new Reference(new IdType(ResourceType.Organization.getPath(), visitOccurrence.getCareSite().getId()));
			individualReference.setDisplay(visitOccurrence.getCareSite().getCareSiteName());
			EncounterParticipantComponent participate = new EncounterParticipantComponent();
			participate.setIndividual(individualReference);
			
			encounter.addParticipant(participate);
			
		}
		
		return encounter;
	}
	
	@Override
	public Long toDbase(Encounter Fhir) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getSize() {
		return myOmopService.getSize();
	}

	@Override
	public Long getSize(Map<String, List<ParameterWrapper>> map) {
		return myOmopService.getSize(map);
	}

	@Override
	public Encounter constructResource(Long fhirId, VisitOccurrence entity, List<String> includes) {
		Encounter encounter = constructFHIR(fhirId, entity);
		
		return encounter;
	}

	@Override
	public void searchWithoutParams(int fromIndex, int toIndex, List<IBaseResource> listResources,
			List<String> includes) {
		List<VisitOccurrence> visitOccurrences = myOmopService.searchWithoutParams(fromIndex, toIndex);

		// We got the results back from OMOP database. Now, we need to construct
		// the list of
		// FHIR Patient resources to be included in the bundle.
		for (VisitOccurrence visitOccurrence : visitOccurrences) {
			Long omopId = visitOccurrence.getId();
			Long fhirId = IdMapping.getFHIRfromOMOP(omopId, ResourceType.Encounter.getPath());
			listResources.add(constructResource(fhirId, visitOccurrence, includes));
		}		
	}

	@Override
	public void searchWithParams(int fromIndex, int toIndex, Map<String, List<ParameterWrapper>> map,
			List<IBaseResource> listResources, List<String> includes) {
		List<VisitOccurrence> visitOccurrences = myOmopService.searchWithParams(fromIndex, toIndex, map);

		for (VisitOccurrence visitOccurrence : visitOccurrences) {
			Long omopId = visitOccurrence.getId();
			Long fhirId = IdMapping.getFHIRfromOMOP(omopId, ResourceType.Encounter.getPath());
			listResources.add(constructResource(fhirId, visitOccurrence, includes));
		}
	}

	public List<ParameterWrapper> mapParameter(String parameter, Object value) {
		List<ParameterWrapper> mapList = new ArrayList<ParameterWrapper>();
		ParameterWrapper paramWrapper = new ParameterWrapper();
		switch (parameter) {
		case Encounter.SP_RES_ID:
			String encounterId = ((TokenParam) value).getValue();
			paramWrapper.setParameterType("Long");
			paramWrapper.setParameters(Arrays.asList("id"));
			paramWrapper.setOperators(Arrays.asList("="));
			paramWrapper.setValues(Arrays.asList(encounterId));
			paramWrapper.setRelationship("or");
			mapList.add(paramWrapper);
			break;
		default:
			mapList = null;
		}
		
		return mapList;

	}
}
