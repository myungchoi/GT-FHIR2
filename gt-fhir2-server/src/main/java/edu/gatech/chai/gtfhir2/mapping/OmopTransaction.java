package edu.gatech.chai.gtfhir2.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.HTTPVerb;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import edu.gatech.chai.omopv5.jpa.service.TransactionService;
import edu.gatech.chai.omopv5.jpa.entity.BaseEntity;
import edu.gatech.chai.omopv5.jpa.entity.FPerson;
import edu.gatech.chai.omopv5.jpa.entity.Measurement;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;

public class OmopTransaction {

	private static OmopTransaction omopTransaction = new OmopTransaction();
	private TransactionService myService;

	public OmopTransaction(WebApplicationContext context) {
		context.getBean(TransactionService.class);
	}

	public OmopTransaction() {
		ContextLoaderListener.getCurrentWebApplicationContext()
			.getBean(TransactionService.class);
	}

	public static OmopTransaction getInstance() {
		return omopTransaction;
	}

	private void addBaseEntity (Map<String, List<BaseEntity>> entityToCreate, String key, BaseEntity entity) {
		List<BaseEntity> list = entityToCreate.get(key);
		if (list == null) {
			list = new ArrayList<BaseEntity> ();
			entityToCreate.put(key, list);
		}
		list.add(entity);		
	}
	
	public List<BundleEntryComponent> executeTransaction(Map<HTTPVerb, Object> entries) throws FHIRException {
		List<BundleEntryComponent> responseEntries = new ArrayList<BundleEntryComponent>();
		Map<String, List<BaseEntity>> entityToCreate = new HashMap<String, List<BaseEntity>>();
		
		@SuppressWarnings("unchecked")
		List<Resource> postList = (List<Resource>) entries.get(HTTPVerb.POST);
		String key;
		for (Resource resource: postList) {
			switch (resource.getResourceType()) {
			case Patient:
				FPerson fPerson = OmopPatient.getInstance().constructOmop(null, (Patient) resource);
				key = "Patient/"+resource.getId()+"^FPerson";
				addBaseEntity(entityToCreate, key, fPerson);
				System.out.println("key:"+key+", fPerson");
				break;
			case Observation:
				Observation observation = (Observation) resource;
				Map<String, Object> obsEntityMap = OmopObservation.getInstance().constructOmopMeasurementObservation(null, observation);
				if (((String)obsEntityMap.get("type")).equalsIgnoreCase("Measurement")) {
					key = observation.getSubject().getReference()+"^Measurement";
					List<Measurement> measurements = (List<Measurement>)obsEntityMap.get("entity");
					for (Measurement measurement: measurements) {
						addBaseEntity(entityToCreate, key, measurement);
					}
					System.out.println("key:"+key+", "+measurements.size()+" measurement(s)");
				} else {
					key = observation.getSubject().getReference()+"^Observation";
					edu.gatech.chai.omopv5.jpa.entity.Observation omopObservation = (edu.gatech.chai.omopv5.jpa.entity.Observation) obsEntityMap.get("entity");
					addBaseEntity(entityToCreate, key, omopObservation);
					System.out.println("key:"+key+", observation");
				}
				
				break;
			default:
				break;
			}
		}
		
		System.out.println("entityToCreate: "+entityToCreate.size());
		
		return null;
	}

	public List<ParameterWrapper> mapParameter(String parameter, Object value) {
		// TODO Auto-generated method stub
		return null;
	}
}
