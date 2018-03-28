package edu.gatech.chai.gtfhir2.provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import edu.gatech.chai.gtfhir2.mapping.OmopProcedure;
import edu.gatech.chai.gtfhir2.utilities.ThrowFHIRExceptions;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;

public class ProcedureResourceProvider implements IResourceProvider {

	private WebApplicationContext myAppCtx;
	private String myDbType;
	private OmopProcedure myMapper;
	private int preferredPageSize = 30;

	public ProcedureResourceProvider() {
		myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
		myDbType = myAppCtx.getServletContext().getInitParameter("backendDbType");
		if (myDbType.equalsIgnoreCase("omopv5") == true) {
			myMapper = new OmopProcedure(myAppCtx);
		} else {
			myMapper = new OmopProcedure(myAppCtx);
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
	public Class<Procedure> getResourceType() {
		return Procedure.class;
	}

	public static String getType() {
		return "Procedure";
	}

	/**
	 * The "@Create" annotation indicates that this method implements
	 * "create=type", which adds a new instance of a resource to the server.
	 */
	@Create()
	public MethodOutcome createProcedure(@ResourceParam Procedure theProcedure) {
		validateResource(theProcedure);

		Long id = null;
		try {
			id = myMapper.toDbase(theProcedure, null);
		} catch (FHIRException e) {
			e.printStackTrace();
			ThrowFHIRExceptions.unprocessableEntityException(e.getMessage());
		}
		return new MethodOutcome(new IdDt(id));
	}

	/**
	 * The "@Update" annotation indicates that this method supports replacing an
	 * existing resource (by ID) with a new instance of that resource.
	 * 
	 * @param theId
	 *            This is the ID of the patient to update
	 * @param theProcedure
	 *            This is the actual resource to save
	 * @return This method returns a "MethodOutcome"
	 */
	@Update()
	public MethodOutcome updatePractitioner(@IdParam IdType theId, @ResourceParam Procedure theProcedure) {
		validateResource(theProcedure);

		Long fhirId=null;
		try {
			fhirId = myMapper.toDbase(theProcedure, theId);
		} catch (FHIRException e) {
			e.printStackTrace();
			ThrowFHIRExceptions.unprocessableEntityException(e.getMessage());
		}
		if (fhirId == null) {
			throw new ResourceNotFoundException(theId);
		}

		return new MethodOutcome();
	}

	/**
	 * This is the "read" operation. The "@Read" annotation indicates that this method supports the read and/or vread operation.
	 * <p>
	 * Read operations take a single parameter annotated with the {@link IdParam} paramater, and should return a single resource instance.
	 * </p>
	 * 
	 * @param theId
	 *            The read operation takes one parameter, which must be of type IdDt and must be annotated with the "@Read.IdParam" annotation.
	 * @return Returns a resource matching this identifier, or null if none exists.
	 */
	@Read()
	public Procedure readProcedure(@IdParam IdType theId) {
		Procedure retval = myMapper.toFHIR(theId);
		if (retval == null) {
			throw new ResourceNotFoundException(theId);
		}
			
		return retval;
	}
	
	/**
	 * The "@Search" annotation indicates that this method supports the search operation. You may have many different method annotated with this annotation, to support many different search criteria.
	 * This example searches by family name.
	 * 
	 * @param theFamilyName
	 *            This operation takes one parameter which is the search criteria. It is annotated with the "@Required" annotation. This annotation takes one argument, a string containing the name of
	 *            the search criteria. The datatype here is StringParam, but there are other possible parameter types depending on the specific search criteria.
	 * @return This method returns a list of Patients in bundle. This list may contain multiple matching resources, or it may also be empty.
	 */
	@Search()
	public IBundleProvider findProceduresByParams(
			@OptionalParam(name = Procedure.SP_RES_ID) TokenParam theProcedureId,
			@OptionalParam(name = Procedure.SP_CODE) TokenParam theCode
			) {
		final InstantType searchTime = InstantType.withCurrentTime();

		/*
		 * Create parameter map, which will be used later to construct
		 * predicate. The predicate construction should depend on the DB schema.
		 * Therefore, we should let our mapper to do any necessary mapping on the
		 * parameter(s). If the FHIR parameter is not mappable, the mapper should
		 * return null, which will be skipped when predicate is constructed.
		 */
		Map<String, List<ParameterWrapper>> paramMap = new HashMap<String, List<ParameterWrapper>> ();

		if (theProcedureId != null) {
			mapParameter (paramMap, Procedure.SP_RES_ID, theProcedureId);
		}
		if (theCode != null) {
			mapParameter (paramMap, Procedure.SP_CODE, theCode);
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

	/**
	 * This method just provides simple business validation for resources we are
	 * storing.
	 * 
	 * @param thePractitioner
	 *            The thePractitioner to validate
	 */
	private void validateResource(Procedure theProcedure) {
	}

}
