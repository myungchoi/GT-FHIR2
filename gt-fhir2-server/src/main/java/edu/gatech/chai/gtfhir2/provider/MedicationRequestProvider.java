package edu.gatech.chai.gtfhir2.provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import edu.gatech.chai.gtfhir2.mapping.OmopMedicationRequest;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;

public class MedicationRequestProvider implements IResourceProvider {

	private WebApplicationContext myAppCtx;
	private String myDbType;
	private OmopMedicationRequest myMapper;
	private int preferredPageSize = 30;

	public MedicationRequestProvider() {
		myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
		myDbType = myAppCtx.getServletContext().getInitParameter("backendDbType");
		if (myDbType.equalsIgnoreCase("omopv5") == true) {
			myMapper = new OmopMedicationRequest(myAppCtx);
		} else {
			myMapper = new OmopMedicationRequest(myAppCtx);
		}

		String pageSizeStr = myAppCtx.getServletContext().getInitParameter("preferredPageSize");
		if (pageSizeStr != null && pageSizeStr.isEmpty() == false) {
			int pageSize = Integer.parseInt(pageSizeStr);
			if (pageSize > 0) {
				preferredPageSize = pageSize;
			}
		}
	}
	
	public static String getType() {
		return "MedicationRequest";
	}

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return MedicationRequest.class;
	}

	@Read()
	public MedicationRequest readMedicationStatement(@IdParam IdType theId) {
		MedicationRequest retval = (MedicationRequest) myMapper.toFHIR(theId);
		if (retval == null) {
			throw new ResourceNotFoundException(theId);
		}
			
		return retval;
	}

	@Search()
	public IBundleProvider findMedicationRequestsByParams(
			@OptionalParam(name = MedicationRequest.SP_RES_ID) TokenParam theMedicationRequestId,
			@OptionalParam(name = MedicationRequest.SP_CODE) TokenParam theCode,
			@OptionalParam(name = MedicationRequest.SP_CONTEXT) ReferenceParam theContext,
			@OptionalParam(name = MedicationRequest.SP_AUTHOREDON) DateParam theDate,
			@OptionalParam(name = MedicationRequest.SP_PATIENT) ReferenceParam thePatient,
			@OptionalParam(name = MedicationRequest.SP_SUBJECT) ReferenceParam theSubject
			) {
		final InstantType searchTime = InstantType.withCurrentTime();

		Map<String, List<ParameterWrapper>> paramMap = new HashMap<String, List<ParameterWrapper>> ();
		
		if (theMedicationRequestId != null) {
			mapParameter (paramMap, MedicationRequest.SP_RES_ID, theMedicationRequestId);
		}
		
		if (theCode != null) {
			mapParameter (paramMap, MedicationRequest.SP_CODE, theCode);
		}

		if (theContext != null) {
			mapParameter (paramMap, MedicationRequest.SP_CONTEXT, theContext);
		}

		if (theDate != null) {
			mapParameter (paramMap, MedicationRequest.SP_AUTHOREDON, theDate);
		}

		if (thePatient != null || theSubject != null) {
			// We only support Patient for subject so we handle it here.
			if (thePatient != null)
				mapParameter (paramMap, MedicationRequest.SP_PATIENT, thePatient);
			else
				mapParameter (paramMap, MedicationRequest.SP_SUBJECT, theSubject);
		}

		// Now finalize the parameter map.
		final Map<String, List<ParameterWrapper>> finalParamMap = paramMap;
		final Long totalSize;
		if (paramMap.size() == 0) {
			totalSize = myMapper.getSize();
		} else {
			totalSize = myMapper.getSize(finalParamMap);
		}

		return new IBundleProvider() {

			@Override
			public IPrimitiveType<Date> getPublished() {
				return searchTime;
			}

			@Override
			public List<IBaseResource> getResources(int fromIndex, int toIndex) {
				List<IBaseResource> retv = new ArrayList<IBaseResource>();

				// _Include
				List<String> includes = new ArrayList<String>();

				if (finalParamMap.size() == 0) {
					myMapper.searchWithoutParams(fromIndex, toIndex, retv, includes);
				} else {
					myMapper.searchWithParams(fromIndex, toIndex, finalParamMap, retv, includes);
				}

				return retv;
			}

			@Override
			public String getUuid() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Integer preferredPageSize() {
				return preferredPageSize;
			}

			@Override
			public Integer size() {
				return totalSize.intValue();
			}
			
		};
	}
	
	private void mapParameter(Map<String, List<ParameterWrapper>> paramMap, String FHIRparam, Object value) {
		List<ParameterWrapper> paramList = myMapper.mapParameter(FHIRparam, value);
		if (paramList != null) {
			paramMap.put(FHIRparam, paramList);
		}
	}

	private void validateResource(MedicationRequest theMedication) {
		// TODO: implement validation method
	}
}
