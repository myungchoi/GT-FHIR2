package edu.gatech.chai.gtfhir2.provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.TokenParamModifier;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import edu.gatech.chai.gtfhir2.mapping.OmopMedicationStatement;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;

public class MedicationStatementResourceProvider implements IResourceProvider {

	private WebApplicationContext myAppCtx;
	private String myDbType;
	private OmopMedicationStatement myMapper;
	private int preferredPageSize = 30;

	public MedicationStatementResourceProvider() {
		myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
		myDbType = myAppCtx.getServletContext().getInitParameter("backendDbType");
		if (myDbType.equalsIgnoreCase("omopv5") == true) {
			myMapper = new OmopMedicationStatement(myAppCtx);
		} else {
			myMapper = new OmopMedicationStatement(myAppCtx);
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
		return "MedicationStatement";
	}

	public OmopMedicationStatement getMyMapper() {
		return myMapper;
		
	}
	
	/**
	 * The "@Create" annotation indicates that this method implements "create=type", which adds a 
	 * new instance of a resource to the server.
	 */
	@Create()
	public MethodOutcome createMedicationStatement(@ResourceParam MedicationStatement theMedicationStatement) {
		validateResource(theMedicationStatement);
		
		Long id=null;
		try {
			id = myMapper.toDbase(theMedicationStatement, null);
		} catch (FHIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (id == null) {
			OperationOutcome outcome = new OperationOutcome();
			CodeableConcept detailCode = new CodeableConcept();
			detailCode.setText("Failed to create entity.");
			outcome.addIssue().setSeverity(IssueSeverity.FATAL).setDetails(detailCode);
			throw new UnprocessableEntityException(FhirContext.forDstu3(), outcome);
		}

		return new MethodOutcome(new IdDt(id));
	}

	@Delete()
	public void deleteMedicationStatement(@IdParam IdType theId) {
		if (myMapper.removeByFhirId(theId) <= 0) {
			throw new ResourceNotFoundException(theId);
		}
	}

	@Update()
	public MethodOutcome updateMedicationStatement(@IdParam IdType theId, @ResourceParam MedicationStatement theMedicationStatement) {
		validateResource(theMedicationStatement);
		
		Long fhirId=null;
		try {
			fhirId = myMapper.toDbase(theMedicationStatement, theId);
		} catch (FHIRException e) {
			e.printStackTrace();
		}

		if (fhirId == null) {
			throw new ResourceNotFoundException(theId);
		}

		return new MethodOutcome();
	}

	@Read()
	public MedicationStatement readMedicationStatement(@IdParam IdType theId) {
		MedicationStatement retval = (MedicationStatement) myMapper.toFHIR(theId);
		if (retval == null) {
			throw new ResourceNotFoundException(theId);
		}
			
		return retval;
	}

	@Search()
	public IBundleProvider findMedicationStatementsByParams(
			@OptionalParam(name = MedicationStatement.SP_RES_ID) TokenParam theMedicationStatementId,
			@OptionalParam(name = MedicationStatement.SP_CODE) TokenParam theCode,
			@OptionalParam(name = MedicationStatement.SP_CONTEXT) ReferenceParam theContext,
			@OptionalParam(name = MedicationStatement.SP_EFFECTIVE) DateParam theDate,
			@OptionalParam(name = MedicationStatement.SP_PATIENT) ReferenceParam thePatient,
			@OptionalParam(name = MedicationStatement.SP_SUBJECT) ReferenceParam theSubject,
			@OptionalParam(name = MedicationStatement.SP_SOURCE) ReferenceParam theSource
			) {
		final InstantType searchTime = InstantType.withCurrentTime();

		Map<String, List<ParameterWrapper>> paramMap = new HashMap<String, List<ParameterWrapper>> ();
		
		if (theMedicationStatementId != null) {
			mapParameter (paramMap, MedicationStatement.SP_RES_ID, theMedicationStatementId);
		}
		if (theCode != null) {
			if (theCode.getModifier().compareTo(TokenParamModifier.IN) == 0) {
				// We have modifier to search data in certain code value set. 
			} else {
				mapParameter (paramMap, MedicationStatement.SP_CODE, theCode);
			}
		}
		if (theContext != null) {
			mapParameter (paramMap, MedicationStatement.SP_CONTEXT, theContext);
		}
		if (theDate != null) {
			mapParameter (paramMap, MedicationStatement.SP_EFFECTIVE, theDate);
		}
		if (theSubject != null) {
			if (theSubject.getResourceType().equals(PatientResourceProvider.getType())) {
				thePatient = theSubject;
			} else {
				errorProcessing("subject search allows Only Patient Resource.");
			}
		}
		if (thePatient != null) {
			mapParameter (paramMap, MedicationStatement.SP_PATIENT, thePatient);
		}
		if (theSource != null) {
			mapParameter (paramMap, MedicationStatement.SP_SOURCE, theSource);
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

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return MedicationStatement.class;
	}

	private void errorProcessing(String msg) {
		OperationOutcome outcome = new OperationOutcome();
		CodeableConcept detailCode = new CodeableConcept();
		detailCode.setText(msg);
		outcome.addIssue().setSeverity(IssueSeverity.FATAL).setDetails(detailCode);
		throw new UnprocessableEntityException(FhirContext.forDstu3(), outcome);		
	}
	
	/**
	 * This method just provides simple business validation for resources we are storing.
	 * 
	 * @param theMedication
	 *            The medication statement to validate
	 */
	private void validateResource(MedicationStatement theMedication) {
		/*
		 * Our server will have a rule that patients must have a family name or we will reject them
		 */
//		if (thePatient.getNameFirstRep().getFamily().isEmpty()) {
//			OperationOutcome outcome = new OperationOutcome();
//			CodeableConcept detailCode = new CodeableConcept();
//			detailCode.setText("No family name provided, Patient resources must have at least one family name.");
//			outcome.addIssue().setSeverity(IssueSeverity.FATAL).setDetails(detailCode);
//			throw new UnprocessableEntityException(FhirContext.forDstu3(), outcome);
//		}
	}

}
