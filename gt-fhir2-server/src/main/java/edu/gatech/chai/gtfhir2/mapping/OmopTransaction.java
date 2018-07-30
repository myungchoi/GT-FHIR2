package edu.gatech.chai.gtfhir2.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.exceptions.FHIRException;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import edu.gatech.chai.omopv5.jpa.service.TransactionService;
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

	public List<BundleEntryComponent> executeTransaction(List<Map<String, Object>> entries) throws FHIRException {
		List<BundleEntryComponent> responseEntries = new ArrayList<BundleEntryComponent>();
		
		for (Map<String, Object> entry : entries) {
			if ((String)entry.get("method") == "POST") {
				// This is to write to database. This means that we should have
				// resource that we need to write.
				Resource resource = (Resource) entry.get("resource");
				IResourceMapping resourceMapper = (IResourceMapping) entry.get("mapper");
				resourceMapper.toDbase(resource, null);
			}
		}
		// This is create
		// In bundle write, we need to check the bundle type.
//		switch (fhirResource.getType()) {
//		case DOCUMENT:
//			break;
//		case TRANSACTION:
//			// We need to walk through the entries and perform transaction as indicated.
//			// This should be atomic commit. Thus, we need to keep the records that succeeded, 
//			// which can be removed if any failed.
//			for (BundleEntryComponent nextEntry : fhirResource.getEntry()) {
//				// For now, we support Resource CRUD.
//				Resource resource = nextEntry.getResource();
//				if (resource == null) continue;
//
//				BundleEntryRequestComponent request = nextEntry.getRequest();
//				if (request != null && !request.isEmpty()) {
//					HTTPVerb method = request.getMethod();
//					if (method.equals(HTTPVerb.POST)) {
//						// Create the resource in server.
//					}
//				} else {
//					continue;
//				}
//				
//			}
//
//			break;
//		default:
//			// We only handle document and transaction.
//			return null;
//		}
		return null;
	}

	public List<ParameterWrapper> mapParameter(String parameter, Object value) {
		// TODO Auto-generated method stub
		return null;
	}
}
