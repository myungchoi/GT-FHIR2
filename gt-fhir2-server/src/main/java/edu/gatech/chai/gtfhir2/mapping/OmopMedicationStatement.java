package edu.gatech.chai.gtfhir2.mapping;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.web.context.WebApplicationContext;

import edu.gatech.chai.omopv5.jpa.entity.DrugExposure;
import edu.gatech.chai.omopv5.jpa.service.DrugExposureService;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;

public class OmopMedicationStatement extends BaseOmopResource<MedicationStatement, DrugExposure, DrugExposureService> implements IResourceMapping<MedicationStatement, DrugExposure> {

//	private DrugExposureService myOmopService;
	
	public OmopMedicationStatement(WebApplicationContext context) {
		super(context, DrugExposure.class, DrugExposureService.class);
//		myOmopService = context.getBean(DrugExposureService.class);
	}
	
	@Override
	public MedicationStatement toFHIR(IdType id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long toDbase(MedicationStatement fhirResource, IdType fhirId) throws FHIRException {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public Long getSize() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Long getSize(Map<String, List<ParameterWrapper>> map) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public MedicationStatement constructResource(Long fhirId, DrugExposure entity, List<String> includes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void searchWithoutParams(int fromIndex, int toIndex, List<IBaseResource> listResources,
			List<String> includes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void searchWithParams(int fromIndex, int toIndex, Map<String, List<ParameterWrapper>> map,
			List<IBaseResource> listResources, List<String> includes) {
		// TODO Auto-generated method stub
		
	}

}
