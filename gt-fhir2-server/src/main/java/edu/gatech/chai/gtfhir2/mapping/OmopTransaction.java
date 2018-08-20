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
import edu.gatech.chai.gtfhir2.provider.PatientResourceProvider;
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
		ContextLoaderListener.getCurrentWebApplicationContext().getBean(TransactionService.class);
	}

	public static OmopTransaction getInstance() {
		return omopTransaction;
	}

	private void addBaseEntity(Map<String, List<BaseEntity>> entityToCreate, String key, BaseEntity entity) {
		if (key == null)
			key = "";

		List<BaseEntity> list = entityToCreate.get(key);
		if (list == null) {
			list = new ArrayList<BaseEntity>();
			entityToCreate.put(key, list);
		}
		list.add(entity);
	}

	public List<BundleEntryComponent> executeTransaction(Map<HTTPVerb, Object> entries) throws FHIRException {
		List<BundleEntryComponent> responseEntries = new ArrayList<BundleEntryComponent>();
		Map<String, List<BaseEntity>> entityToCreate = new HashMap<String, List<BaseEntity>>();

		@SuppressWarnings("unchecked")
		List<Resource> postList = (List<Resource>) entries.get(HTTPVerb.POST);
		String keyString;
		for (Resource resource : postList) {
			switch (resource.getResourceType()) {
			case Patient:
				FPerson fPerson = OmopPatient.getInstance().constructOmop(null, (Patient) resource);
				keyString = "Patient/" + resource.getId() + "^FPerson";
				addBaseEntity(entityToCreate, keyString, fPerson);
				System.out.println("key:" + keyString + ", fPerson");
				break;
			case Observation:
				Observation observation = (Observation) resource;
				Map<String, Object> obsEntityMap = OmopObservation.getInstance()
						.constructOmopMeasurementObservation(null, observation);
				if (((String) obsEntityMap.get("type")).equalsIgnoreCase("Measurement")) {
					keyString = observation.getSubject().getReference() + "^Measurement";
					List<Measurement> measurements = (List<Measurement>) obsEntityMap.get("entity");
					for (Measurement measurement : measurements) {
						addBaseEntity(entityToCreate, keyString, measurement);
					}
				} else {
					keyString = observation.getSubject().getReference() + "^Observation";
					edu.gatech.chai.omopv5.jpa.entity.Observation omopObservation = (edu.gatech.chai.omopv5.jpa.entity.Observation) obsEntityMap
							.get("entity");
					addBaseEntity(entityToCreate, keyString, omopObservation);
				}

				break;
			default:
				break;
			}
		}

		List<BundleEntryComponent> retVal = new ArrayList<BundleEntryComponent>();
		if (entityToCreate.size() > 0) {
			int performStatus = myService.writeTransaction(entityToCreate);
			if (performStatus < 0) {
				// This is an error.
				// TODO: respond accordingly
				return retVal;
			}
			for (String myKeyString : entityToCreate.keySet()) {
				List<BaseEntity> entities = entityToCreate.get(myKeyString);
				String[] myKeyInfo = myKeyString.split("^");
				if (myKeyInfo.length != 2) {
					return null;
				}
				String entityName = myKeyInfo[1];
				for (BaseEntity entity : entities) {
					BundleEntryComponent bundleEntryComponent = new BundleEntryComponent();
					Resource fhirResource;
					if (entityName.equals("FPerson")) {
						// This is Person table.
						fhirResource = OmopPatient.getInstance().constructFHIR(
								IdMapping.getFHIRfromOMOP(entity.getIdAsLong(), PatientResourceProvider.getType()),
								(FPerson) entity);
						bundleEntryComponent.setResource(fhirResource);
						retVal.add(bundleEntryComponent);
					} else if (entityName.equals("Measurement")) {
						
					}
				}
			}
		}
		return retVal;
	}

	public List<ParameterWrapper> mapParameter(String parameter, Object value) {
		// TODO Auto-generated method stub
		return null;
	}
}
