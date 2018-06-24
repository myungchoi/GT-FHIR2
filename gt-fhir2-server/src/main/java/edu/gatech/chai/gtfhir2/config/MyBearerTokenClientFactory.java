package edu.gatech.chai.gtfhir2.config;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import javax.servlet.http.HttpServletRequest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.server.util.ITestingUiClientFactory;

public class MyBearerTokenClientFactory implements ITestingUiClientFactory {

	@Override
	public IGenericClient newClient(FhirContext theFhirContext, HttpServletRequest theRequest,
			String theServerBaseUrl) {
		// Create a client
		IGenericClient client = theFhirContext.newRestfulGenericClient(theServerBaseUrl);

		String apiKey = theRequest.getParameter("apiKey");
		if (isNotBlank(apiKey)) {
			client.registerInterceptor(new BearerTokenAuthInterceptor(apiKey));
		}

//		theFhirContext.getRestfulClientFactory().setConnectionRequestTimeout(600000);
		theFhirContext.getRestfulClientFactory().setSocketTimeout(600000);
		return client;
	}

}
