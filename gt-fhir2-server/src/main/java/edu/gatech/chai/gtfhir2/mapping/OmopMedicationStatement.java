package edu.gatech.chai.gtfhir2.mapping;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import edu.gatech.chai.omopv5.jpa.entity.DrugExposure;
import edu.gatech.chai.omopv5.jpa.service.DrugExposureService;

public class OmopMedicationStatement extends BaseOmopResource<MedicationStatement, DrugExposure, DrugExposureService> implements IResourceMapping<MedicationStatement, DrugExposure> {

	private static OmopMedicationStatement omopMedicationStatement = new OmopMedicationStatement();
	
	public OmopMedicationStatement(WebApplicationContext context) {
		super(context, DrugExposure.class, DrugExposureService.class, ResourceType.MedicationStatement.getPath());
	}

	public OmopMedicationStatement() {
		super(ContextLoaderListener.getCurrentWebApplicationContext(), DrugExposure.class, DrugExposureService.class, ResourceType.MedicationStatement.getPath());
	}
	
	public static OmopMedicationStatement getInstance() {
		return omopMedicationStatement;
	}
	
	@Override
	public Long toDbase(MedicationStatement fhirResource, IdType fhirId) throws FHIRException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MedicationStatement constructResource(Long fhirId, DrugExposure entity, List<String> includes) {
		// TODO Auto-generated method stub
		return null;
	}

}
