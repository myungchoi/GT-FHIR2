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
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Enumerations.MessageEvent;
import org.hl7.fhir.dstu3.model.MessageHeader;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
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
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;

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

		// String url =
		// myAppCtx.getServletContext().getInitParameter("transactionServer");
		// if (url != null && url.isEmpty() == false) {
		// setMyTransactionServerUrl(url);
		// if (url.equals("${requestUrl}")) {
		// getTransactionServerUrlFromRequest = true;
		// } else {
		// getTransactionServerUrlFromRequest = false;
		// }
		// } else {
		// getTransactionServerUrlFromRequest = true;
		// }

	}

	// public void setMyTransactionServerUrl(String myTransactionServerUrl) {
	// this.myTransactionServerUrl = myTransactionServerUrl;
	// }

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

	// private String getServerBaseUrl(HttpServletRequest theRequest) {
	// if (getTransactionServerUrlFromRequest) {
	// StringBuffer requestUrl = theRequest.getRequestURL();
	// myTransactionServerUrl = requestUrl.toString();
	// }
	//
	// if (myTransactionServerUrl.endsWith("/")) {
	// myTransactionServerUrl = myTransactionServerUrl.substring(0,
	// myTransactionServerUrl.length() - 1);
	// }
	//
	// return myTransactionServerUrl;
	// }

	/**
	 */
	@Transaction
	public Bundle transaction(@TransactionParam MyBundle theBundle, HttpServletRequest theRequest) {
		validateResource(theBundle);

		Bundle retVal = new Bundle();
		List<Resource> postList = new ArrayList<Resource>();
		List<Resource> putList = new ArrayList<Resource>();
		List<String> deleteList = new ArrayList<String>();
		List<ParameterWrapper> getList = new ArrayList<ParameterWrapper>();

		Map<HTTPVerb, Object> transactionEntries = new HashMap<HTTPVerb, Object>();
		transactionEntries.put(HTTPVerb.POST, postList);
		transactionEntries.put(HTTPVerb.PUT, putList);
		transactionEntries.put(HTTPVerb.DELETE, deleteList);
		transactionEntries.put(HTTPVerb.GET, getList);

		try {
			switch (theBundle.getType()) {
			case DOCUMENT:
			case TRANSACTION:
				System.out.println("We are at the transaction");
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
						// this. resourceName =
						// resource.getResourceType().toString();

						// Now we have a request that we support. Add this into
						// the entry to process.
						HTTPVerb method = request.getMethod();
						if (method == HTTPVerb.POST) {
							postList.add(resource);
						} else if (method == HTTPVerb.PUT) {
							putList.add(resource);
						} else if (method == HTTPVerb.DELETE) {
							deleteList.add(request.getUrl());
						} else if (method == HTTPVerb.GET) {
							// TODO: getList.add(new ParameterWrapper());
							// create parameter here.
						} else {
							continue;
						}
					}
				}

				break;
			case MESSAGE:
				BundleEntryComponent entry = theBundle.getEntryFirstRep();
				Resource resource = entry.getResource();
				if (resource.getResourceType() == ResourceType.MessageHeader) {
					MessageHeader messageHeader = (MessageHeader) resource;
					// We handle observation-type.
					// TODO: Add other types later.
					Coding event = messageHeader.getEvent();
					if ("R01".equals(event.getCode())) {
						List<BundleEntryComponent> entries = theBundle.getEntry();
						int sizeOfEntries = entries.size();
						for (int i = 1; i < sizeOfEntries; i++) {
							entry = entries.get(i);
							if (entry.getRequest() != null && !entry.getRequest().isEmpty()) {
								if (entry.getRequest().getMethod() == HTTPVerb.POST) {
									postList.add(entry.getResource());
								} else if (entry.getRequest().getMethod() == HTTPVerb.PUT) {
									// This is to update. Get URL
									String putId = entry.getRequest().getUrlElement().getId();
									entry.getResource().setId(putId);
									putList.add(entry.getResource());
								} else {
									ThrowFHIRExceptions.unprocessableEntityException(
											"We support POST and PUT for Messages");
								}
							}
						}
					} else {
						ThrowFHIRExceptions.unprocessableEntityException(
								"We currently support only HL7 v2 R01 Message");
					}
				} else {
					// First entry must be message header.
					ThrowFHIRExceptions
							.unprocessableEntityException("First entry in Bundle message type should be MessageHeader");
				}
				break;
			default:
				ThrowFHIRExceptions.unprocessableEntityException("Unsupported Bundle Type, "
						+ theBundle.getType().toString() + ". We support DOCUMENT, TRANSACTION, and MESSAGE");
			}

			List<BundleEntryComponent> responseTransaction = myMapper.executeMessage(transactionEntries);
			if (responseTransaction != null && responseTransaction.size() > 0) {
				retVal.setEntry(responseTransaction);
				retVal.setType(BundleType.TRANSACTIONRESPONSE);
			} else {
				ThrowFHIRExceptions
						.unprocessableEntityException("Faied process the bundle, " + theBundle.getType().toString());
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
