package edu.gatech.chai.gtfhir2.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.Bundle.HTTPVerb;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.exceptions.FHIRException;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.rest.annotation.Transaction;
import ca.uhn.fhir.rest.annotation.TransactionParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import edu.gatech.chai.gtfhir2.mapping.BaseOmopResource;
import edu.gatech.chai.gtfhir2.mapping.OmopTransaction;
import edu.gatech.chai.gtfhir2.model.MyBundle;
import edu.gatech.chai.gtfhir2.utilities.ThrowFHIRExceptions;

public class SystemTransactionProvider {

	private WebApplicationContext myAppCtx;
	private String myDbType;
	private OmopTransaction myMapper;
	private int preferredPageSize = 30;
	private Map<String, Object> supportedProvider = new HashMap<String, Object>();

	public static String getType() {
		return "Bundle";
	}

	public void addSupportedProvider(String resourceName, Object resourceMapper) {
		supportedProvider.put(resourceName, resourceMapper);
	}

	public SystemTransactionProvider() {
		myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
		myDbType = myAppCtx.getServletContext().getInitParameter("backendDbType");
		if (myDbType.equalsIgnoreCase("omopv5") == true) {
			myMapper = new OmopTransaction(myAppCtx);
		} else {
			myMapper = new OmopTransaction(myAppCtx);
		}

		String pageSizeStr = myAppCtx.getServletContext().getInitParameter("preferredPageSize");
		if (pageSizeStr != null && pageSizeStr.isEmpty() == false) {
			int pageSize = Integer.parseInt(pageSizeStr);
			if (pageSize > 0) {
				preferredPageSize = pageSize;
			}
		}

//		String url = myAppCtx.getServletContext().getInitParameter("transactionServer");
//		if (url != null && url.isEmpty() == false) {
//			setMyTransactionServerUrl(url);
//			if (url.equals("${requestUrl}")) {
//				getTransactionServerUrlFromRequest = true;
//			} else {
//				getTransactionServerUrlFromRequest = false;
//			}
//		} else {
//			getTransactionServerUrlFromRequest = true;
//		}

	}

//	public void setMyTransactionServerUrl(String myTransactionServerUrl) {
//		this.myTransactionServerUrl = myTransactionServerUrl;
//	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <v extends BaseOmopResource> void undoCreate(List<Object> resourcesAdded) {
		v mapper;

		for (Object entry : resourcesAdded) {
			Pair<v, Long> entryPair = (Pair<v, Long>) entry;
			mapper = entryPair.getKey();
			Long id = entryPair.getValue();
			mapper.removeDbase(id);
		}
	}

//	private String getServerBaseUrl(HttpServletRequest theRequest) {
//		if (getTransactionServerUrlFromRequest) {
//			StringBuffer requestUrl = theRequest.getRequestURL();
//			myTransactionServerUrl = requestUrl.toString();
//		}
//
//		if (myTransactionServerUrl.endsWith("/")) {
//			myTransactionServerUrl = myTransactionServerUrl.substring(0, myTransactionServerUrl.length() - 1);
//		}
//
//		return myTransactionServerUrl;
//	}

	/**
	 */
	@Transaction
	public Bundle transaction(@TransactionParam MyBundle theBundle, HttpServletRequest theRequest) {
		validateResource(theBundle);

		Bundle retVal = new Bundle();

		List<Map<String, Object>> transactionEntries = new ArrayList<Map<String, Object>>();
		// Check the type of bundle transaction and act on it.
		try {

			switch (theBundle.getType()) {
			case DOCUMENT:
			case TRANSACTION:
				// We send both Document and Transaction to OmopBundle mapping.
				for (BundleEntryComponent nextEntry : theBundle.getEntry()) {
					Resource resource = nextEntry.getResource();
					BundleEntryRequestComponent request = nextEntry.getRequest();

					// We require a transaction to have a request so that we can
					// handle the transaction. Without it, we have nothing to
					// do.
					if (request == null)
						continue;

					if (!request.isEmpty()) {
						// First check the Resource to see if we can support
						// this.
						String resourceName = null;
						if (resource != null && !resource.isEmpty()) {
							resourceName = resource.getResourceType().toString();
						} else {
							String[] urls = request.getUrl().split("/");
							if (urls.length == 2) {
								resourceName = urls[0];
							}
						}

						if (resourceName == null) {
							// We must have the name resource.
							ThrowFHIRExceptions.unprocessableEntityException(
									"We must have resource in either resource or url in request element");
						}

						if (!supportedProvider.containsKey(resourceName)) {
							ThrowFHIRExceptions.unprocessableEntityException("We do not support the Resource, "
									+ resourceName + ", in Bundle " + theBundle.getType().toCode() + " type.");
						}

						// Now we have a request that we support. Add this into
						// the
						// entry to process.
						HTTPVerb method = request.getMethod();
						Map<String, Object> transactionEntry = new HashMap<String, Object>();
						transactionEntry.put("method", method.toString());
						transactionEntry.put("url", request.getUrl());
						transactionEntry.put("resource", resource);
						transactionEntry.put("resourceName", resourceName);
						transactionEntry.put("mapper", supportedProvider.get(resourceName));

						transactionEntries.add(transactionEntry);
					}
				}
				
				if (!transactionEntries.isEmpty()) {
					List<BundleEntryComponent> responseTransaction = myMapper.executeTransaction(transactionEntries);
					// If any one of entries caused an error, entire transaction will be cancelled (atomic commit). In
					// this case, the responseTransaction will have a entry that caused the error and inserted to 
					// transaction response. So, what we need to do here is just add all entries into bundle.
					if (responseTransaction.size() > 0) {
						retVal.setEntry(responseTransaction);
					}
				} else {
					// We have nothing to do. Return success with empty response.
					retVal.setType(BundleType.TRANSACTIONRESPONSE);			
				}
				
				break;
			case MESSAGE:
				break;
			default:
				ThrowFHIRExceptions.unprocessableEntityException("Unsupported Bundle Type, "
						+ theBundle.getType().toString() + ". We support DOCUMENT, TRANSACTION, and MESSAGE");
			}

		} catch (FHIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retVal;
	}

	// TODO: Add more validation code here.
	private void validateResource(MyBundle theBundle) {
	}

}
