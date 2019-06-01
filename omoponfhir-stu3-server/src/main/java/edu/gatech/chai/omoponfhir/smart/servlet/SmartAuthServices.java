/*******************************************************************************
 * Copyright (c) 2019 Georgia Tech Research Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package edu.gatech.chai.omoponfhir.smart.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import edu.gatech.chai.omoponfhir.security.Authorization;
import edu.gatech.chai.smart.jpa.entity.SmartLaunchContext;
import edu.gatech.chai.smart.jpa.entity.SmartLaunchContextParam;
import edu.gatech.chai.smart.jpa.service.SmartOnFhirLaunchContextService;

/**
 * Servlet implementation class SmartServices
 * 
 * @WebServlet(description = "SMART on FHIR internal services", urlPatterns = {
 *                         "/_services/smart/Launch" })
 */

public class SmartAuthServices extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SmartAuthServices.class);

	private WebApplicationContext myAppCtx;

	private String url;
	private String client_id;
	private String client_secret;
	private SmartOnFhirLaunchContextService mySmartOnFhirContextService;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SmartAuthServices() {
		super();

		myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
		mySmartOnFhirContextService = myAppCtx.getBean(SmartOnFhirLaunchContextService.class);
	}

	public void init() throws ServletException {
		// According to SMART on FHIR folks in Harvard. They want to support both
		// basic AUTH and bearer AUTH for the internal communication for this.
		url = System.getenv("SMART_INTROSPECTURL");
		client_id = System.getenv("SMART_CLIENTID");
		client_secret = System.getenv("SMART_CLIENTSECRET");

		if (url == null)
			url = getServletConfig().getInitParameter("introspectUrl");
		if (client_id == null)
			client_id = getServletConfig().getInitParameter("client_id");
		if (client_secret == null)
			client_secret = getServletConfig().getInitParameter("client_secret");

//        bookDB = (BookDBAO)getServletContext().
//            getAttribute("bookDB");
//        if (bookDB == null) throw new
//            UnavailableException("Couldn’t get database.");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String launchId = new String(request.getPathInfo().substring(1));

		if (launchId == null || launchId.isEmpty()) {
			System.out.println("LaunchID is invalid");
			return;
		}

		System.out.println("Get LaunchID = " + launchId);

		Authorization smartAuth = new Authorization(url, client_id, client_secret);
		if (smartAuth.asBasicAuth(request) == true || smartAuth.asBearerAuth(request) == true) {
			SmartLaunchContext smartLaunchContext = (SmartLaunchContext) mySmartOnFhirContextService
					.getContext(Long.valueOf(launchId));
			if (smartLaunchContext != null) {
				JSONObject jsonResp = smartLaunchContext.getJSONObject();

				if (jsonResp == null) {
					System.out.println("Launch ID " + launchId + " does not exist.");
					return;
				}

				String respString = jsonResp.toString();
				response.setContentType("application/json; charset=UTF-8;");
				response.getWriter().append(respString);
				System.out.println(respString);
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// We have received a request to create Launch context
		StringBuilder buffer = new StringBuilder();
		BufferedReader reader = request.getReader();

		// This is Authorization Request.
		// Get parameters.

		String launchContext = request.getParameter("launch");
		String responseType = request.getParameter("response_type");
		String clientId = request.getParameter("client_id");
		String redirectUrl = request.getParameter("redirect_uri");
		String scope = request.getParameter("scope");
		String aud = request.getParameter("aud");
		String state = request.getParameter("state");

		// We should use the client Id and check the scope to decide if
		// we want to authenticate this app request. 
		// But, for now, we authenticate this.
		// TODO: connect to Authenticate server to authenticate the app.
		//       One idea is to use Google or Facebook.
		//
		// For now, we expect the public app. client_id is required.
		// http://www.hl7.org/fhir/smart-app-launch/#step-3-app-exchanges-authorization-code-for-access-token
		
		// scope check. We need to make sure if this app has these scopes registered.
		// But, for this simulation, we allow everything.
		// If scope has launch, it should have launchContext as it's launched from EHR
		// If scope has launch/patient, then we need to run patient browser
		// If scope has launch/encounter, then we need to run encounter browser
		if (scope == null || scope.isEmpty()) {
			// TODO: redirect to /error
			return;
		}
			
		String[] scopes = scope.split(" ");
		boolean launcedInEhr = false;
		boolean needToChoosePatient = false;
		boolean needToChooseEncounter = false;
		for (String s : scopes) {
			if ("launch".equals(s)) {
				launcedInEhr = true;
				if (launchContext == null || launchContext.isEmpty()) {
					// Redirect to /error for missing launchContext
					return;
				}
			}
			if ("launch/patient".equals(s)) {
				needToChoosePatient = true;
			}
			if ("launch/encounter".equals(s)) {
				needToChooseEncounter = true;
			}
		}
		
		if (!"code".equals(responseType)) {
			// TODO: redirect to /error
			return;
		}
		
		// The launchContext, if exists, contains a context to resolve this to
		// patient, encounter, provider, etc. We however do not have EHR that can
		// store the information as this is initiated from smart-launcher.
		// The context itself has those information encoded. Decode it now.
		String code = null;
		String patientId = null;
		if (launchContext != null && !launchContext.isEmpty()) {
			code = new String(Base64.decodeBase64(launchContext));
			// decode the code. 
			JSONObject codeJson = new JSONObject(code);
			JSONObject decodedCode = SmartLauncherCodec.decode(codeJson);
			
			if ("1".equals(decodedCode.getString("launch_ehr"))) {
				// We are launching in EHR mode.
			}
			
			if ("1".equals(decodedCode.getString("sim_ehr"))) {
				// We are simulating EHR.
			}
			
			patientId = decodedCode.getString("patient");
			
			
		} else {
			if (needToChoosePatient) {
				// Redirect to /patient
				return;
			}
			if (needToChooseEncounter) {
				// Redirect to /encounter
				return;
			}
		}

		java.util.Date date = new java.util.Date();
		Timestamp ts = new Timestamp(date.getTime());

		// We should have redirected....
		// redirect to /error
	}
}
