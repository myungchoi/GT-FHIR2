package edu.gatech.chai.omoponfhir.omopv5.stu3.provider;

import java.util.List;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ConceptMap;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Parameters;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.UriType;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import edu.gatech.chai.omoponfhir.omopv5.stu3.mapping.OmopConceptMap;

public class ConceptMapResourceProvider implements IResourceProvider {
	private WebApplicationContext myAppCtx;
	private String myDbType;
	private OmopConceptMap myMapper;
	private int preferredPageSize = 30;

	public ConceptMapResourceProvider() {
		myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
		myDbType = myAppCtx.getServletContext().getInitParameter("backendDbType");
		if (myDbType.equalsIgnoreCase("omopv5") == true) {
			myMapper = new OmopConceptMap(myAppCtx);
		} else {
			myMapper = new OmopConceptMap(myAppCtx);
		}

		String pageSizeStr = myAppCtx.getServletContext().getInitParameter("preferredPageSize");
		if (pageSizeStr != null && pageSizeStr.isEmpty() == false) {
			int pageSize = Integer.parseInt(pageSizeStr);
			if (pageSize > 0) {
				preferredPageSize = pageSize;
			}
		}
	}

	@Override
	public Class<ConceptMap> getResourceType() {
		return ConceptMap.class;
	}

	public static String getType() {
		return "ConceptMap";
	}

	/**
	 * $translate operation for concept translation.
	 * 
	 */
	@Operation(name = "$translate", idempotent = true)
	public Parameters translateOperation(RequestDetails theRequestDetails, 
//			@IdParam IdType thePatientId,
			@OperationParam(name = "code") CodeType theCode, 
			@OperationParam(name = "system") UriType theSystem,
			@OperationParam(name = "version") StringType theVersion, 
			@OperationParam(name = "source") UriType theSource,
			@OperationParam(name = "coding") Coding theCoding,
			@OperationParam(name = "codeableConcept") CodeableConcept theCodeableConcept,
			@OperationParam(name = "target") UriType theTarget,
			@OperationParam(name = "targetsystem") UriType theTargetSystem,
			@OperationParam(name = "reverse") BooleanType theReverse) {

		Parameters retVal = new Parameters();

		String targetUri;
		String targetSystem;
		if (theTarget == null || theTarget.isEmpty()) {
			targetUri = "";
		} else {
			targetUri = theTarget.getValueAsString();
		}
		if (theTargetSystem == null || theTargetSystem.isEmpty()) {
			targetSystem = "";
		} else {
			targetSystem = theTargetSystem.getValueAsString();
		}

		if (theCodeableConcept != null && !theCodeableConcept.isEmpty()) {
			// If codeableconcept exists, use this.
			// If we have multiple codings, run them until we have a matching
			// translation.
			List<Coding> codings = theCodeableConcept.getCoding();
			for (Coding coding : codings) {
				String code = coding.getCode();
				String system = coding.getSystem();

				retVal = myMapper.translateConcept(code, system, targetUri, targetSystem);
				if (retVal != null && !retVal.isEmpty()) {
					return retVal;
				}
			}
		}

		if (theCoding != null && !theCoding.isEmpty()) {
			// if coding is provided, use this.
			String code = theCoding.getCode();
			String system = theCoding.getSystem();

			retVal = myMapper.translateConcept(code, system, targetUri, targetSystem);
			if (retVal != null && !retVal.isEmpty()) {
				return retVal;
			}
		}

		if (theCode != null && !theCode.isEmpty() && theSystem != null && !theSystem.isEmpty()) {
			String code = theCode.getValueAsString();
			String system = theSystem.getValueAsString();
			retVal = myMapper.translateConcept(code, system, targetUri, targetSystem);
			return retVal;
		}

		return retVal;
	}
}
