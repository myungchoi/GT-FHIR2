package edu.gatech.chai.gtfhir2.provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.dstu3.model.Organization;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;

import edu.gatech.chai.gtfhir2.mapping.OmopPatient;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;


/**
 * This is a resource provider which stores Patient resources in memory using a HashMap. This is obviously not a production-ready solution for many reasons, 
 * but it is useful to help illustrate how to build a fully-functional server.
 */
public class PatientResourceProvider implements IResourceProvider {

private WebApplicationContext myAppCtx;
private String myDbType;
private OmopPatient myMapper;
private int preferredPageSize = 30;

	public PatientResourceProvider() {
		myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
		myDbType = myAppCtx.getServletContext().getInitParameter("backendDbType");
		if (myDbType.equalsIgnoreCase("omopv5") == true) {
			myMapper = new OmopPatient(myAppCtx);
		} else {
			myMapper = new OmopPatient(myAppCtx);
		}
		
		String pageSizeStr = myAppCtx.getServletContext().getInitParameter("preferredPageSize");
		if (pageSizeStr != null && pageSizeStr.isEmpty() == false) {
			int pageSize = Integer.parseInt(pageSizeStr);
			if (pageSize > 0) {
				preferredPageSize = pageSize;
			} 
		}
	}


	/**
	 * The "@Create" annotation indicates that this method implements "create=type", which adds a 
	 * new instance of a resource to the server.
	 */
	@Create()
	public MethodOutcome createPatient(@ResourceParam Patient thePatient) {
		validateResource(thePatient);
		
		Long id=null;
		try {
			id = myMapper.toDbase(thePatient, null);
		} catch (FHIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return new MethodOutcome(new IdDt(id));
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
	public IBundleProvider findPatientsByParams(
			@OptionalParam(name = Patient.SP_RES_ID) TokenParam thePatientId,
			@OptionalParam(name = Patient.SP_ACTIVE) TokenParam theActive,
			@OptionalParam(name = Patient.SP_FAMILY) StringParam theFamilyName,
			@OptionalParam(name = Patient.SP_GIVEN) StringParam theGivenName,
			@OptionalParam(name = Patient.SP_NAME) StringParam theName,
			@OptionalParam(name = Patient.SP_BIRTHDATE) DateParam theBirthDate,
			@OptionalParam(name = Patient.SP_ADDRESS) StringParam theAddress,
			@OptionalParam(name = Patient.SP_ADDRESS_CITY) StringParam theAddressCity,
			@OptionalParam(name = Patient.SP_ADDRESS_STATE) StringParam theAddressState,
			@OptionalParam(name = Patient.SP_ADDRESS_POSTALCODE) StringParam theAddressZip,
			@OptionalParam(name = Patient.SP_EMAIL) TokenParam theEmail,
			@OptionalParam(name = Patient.SP_PHONE) TokenParam thePhone,
			@OptionalParam(name = Patient.SP_TELECOM) TokenParam theTelecom,

			@OptionalParam(name = Patient.SP_ORGANIZATION, chainWhitelist={"", Organization.SP_NAME}) ReferenceParam theOrganization,
			
			@IncludeParam(allow={"Patient:general-practitioner", "Patient:organization", "Patient:link"})
			final Set<Include> theIncludes,
			
			@IncludeParam(allow={"Encounter:subject", "Observation:subject"}, reverse=true)
            final Set<Include> theReverseIncludes
			) {
		final InstantType searchTime = InstantType.withCurrentTime();
		
		/*
		 * Create parameter map, which will be used later to construct
		 * predicate. The predicate construction should depend on the DB schema.
		 * Therefore, we should let our mpper to do any necessary mapping on the
		 * parameter(s). If the FHIR parameter is not mappable, the mapper should
		 * return null, which will be skipped when predicate is constructed.
		 */
		Map<String, List<ParameterWrapper>> paramMap = new HashMap<String, List<ParameterWrapper>> ();
		
		if (thePatientId != null) {
			mapParameter (paramMap, Patient.SP_RES_ID, thePatientId);
		}
		if (theActive != null) {
			mapParameter (paramMap, Patient.SP_ACTIVE, theActive);
		}
		if (theEmail != null) {
			mapParameter (paramMap, Patient.SP_EMAIL, theEmail);
		}
		if (thePhone != null) {
			mapParameter (paramMap, Patient.SP_PHONE, thePhone);
		}
		if (theTelecom != null) {
			mapParameter (paramMap, Patient.SP_TELECOM, theTelecom);
		}
		if (theFamilyName != null) {
			mapParameter (paramMap, Patient.SP_FAMILY, theFamilyName);
		}
		if (theName != null) {
			mapParameter (paramMap, Patient.SP_NAME, theName);
		}
		if (theGivenName != null) {
			mapParameter (paramMap, Patient.SP_GIVEN, theGivenName);
		}
		if (theBirthDate != null) {
			mapParameter (paramMap, Patient.SP_BIRTHDATE, theBirthDate);
		}
		if (theAddress != null) {
			mapParameter (paramMap, Patient.SP_ADDRESS, theAddress);
		}
		if (theAddressCity != null) {
			mapParameter (paramMap, Patient.SP_ADDRESS_CITY, theAddressCity);
		}
		if (theAddressState != null) {
			mapParameter (paramMap, Patient.SP_ADDRESS_STATE, theAddressState);
		}
		if (theAddressZip != null) {
			mapParameter (paramMap, Patient.SP_ADDRESS_POSTALCODE, theAddressZip);
		}
		
		// Chain Search.
		// Chain search is a searching by reference with specific field name (including reference ID).
		// As SP names are not unique across the FHIR resources, we need to tag the name
		// of the resource in front to indicate our OMOP* can handle these parameters.
		if (theOrganization != null) {
			String orgChain = theOrganization.getChain();
			if (orgChain != null) {
				if (Organization.SP_NAME.equals(orgChain)) {
					String theOrgName = theOrganization.getValue();
					mapParameter (paramMap, "Organization:"+Organization.SP_NAME, theOrgName);
				} else if ("".equals(orgChain)) {
					mapParameter (paramMap, "Organization:"+Organization.SP_RES_ID, theOrganization.getValue());
				}
			} else {
				mapParameter (paramMap, "Organization:"+Organization.SP_RES_ID, theOrganization.getIdPart());
			}
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
				if (theIncludes.contains(new Include("Patient:general-practitioner"))) {
					includes.add("Patient:general-practitioner");
				}
				
				if (theIncludes.contains(new Include("Patient:organization"))) {
					includes.add("Patient:organization");
				}
				
				if (theIncludes.contains(new Include("Patient:link"))) {
					includes.add("Patient:link");
				}

				if (theReverseIncludes.contains(new Include("*"))) {
					// This is to include all the reverse includes...
					includes.add("Encounter:subject");
					includes.add("Observation:subject");
					includes.add("Device:patient");
					includes.add("Condition:subject");
					includes.add("Procedure:subject");
					includes.add("MedicationRequest:subject");
					includes.add("MedicationAdministration:subject");
					includes.add("MedicationDispense:subject");
					includes.add("MedicationStatement:subject");
				} else {
					if (theReverseIncludes.contains(new Include("Encounter:subject"))) {
						includes.add("Encounter:subject");						
					}
					if (theReverseIncludes.contains(new Include("Observation:subject"))) {
						includes.add("Observation:subject");
					}
					if (theReverseIncludes.contains(new Include("Device:patient"))) {
						includes.add("Device:patient");
					}
					if (theReverseIncludes.contains(new Include("Condition:subject"))) {
						includes.add("Condition:subject");
					}
					if (theReverseIncludes.contains(new Include("Procedure:subject"))) {
						includes.add("Procedure:subject");
					}
					if (theReverseIncludes.contains(new Include("MedicationRequest:subject"))) {
						includes.add("MedicationRequest:subject");
					}
					if (theReverseIncludes.contains(new Include("MedicationAdministration:subject"))) {
						includes.add("MedicationAdministration:subject");
					}
					if (theReverseIncludes.contains(new Include("MedicationDispense:subject"))) {
						includes.add("MedicationDispense:subject");
					}
					if (theReverseIncludes.contains(new Include("MedicationStatement:subject"))) {
						includes.add("MedicationStatement:subject");
					}
				}
				
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
	 * The getResourceType method comes from IResourceProvider, and must be overridden to indicate what type of resource this provider supplies.
	 */
	@Override
	public Class<Patient> getResourceType() {
		return Patient.class;
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
	public Patient readPatient(@IdParam IdType theId) {
		Patient retval = (Patient) myMapper.toFHIR(theId);
		if (retval == null) {
			throw new ResourceNotFoundException(theId);
		}
			
		return retval;
	}

	/**
	 * The "@Update" annotation indicates that this method supports replacing an existing 
	 * resource (by ID) with a new instance of that resource.
	 * 
	 * @param theId
	 *            This is the ID of the patient to update
	 * @param thePatient
	 *            This is the actual resource to save
	 * @return This method returns a "MethodOutcome"
	 */
	@Update()
	public MethodOutcome updatePatient(@IdParam IdType theId, @ResourceParam Patient thePatient) {
		validateResource(thePatient);

		Long fhirId=null;
		try {
			fhirId = myMapper.toDbase(thePatient, theId);
		} catch (FHIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (fhirId == null) {
			throw new ResourceNotFoundException(theId);
		}

		return new MethodOutcome();
	}

	/**
	 * This method just provides simple business validation for resources we are storing.
	 * 
	 * @param thePatient
	 *            The patient to validate
	 */
	private void validateResource(Patient thePatient) {
		/*
		 * Our server will have a rule that patients must have a family name or we will reject them
		 */
		if (thePatient.getNameFirstRep().getFamily().isEmpty()) {
			OperationOutcome outcome = new OperationOutcome();
			CodeableConcept detailCode = new CodeableConcept();
			detailCode.setText("No family name provided, Patient resources must have at least one family name.");
			outcome.addIssue().setSeverity(IssueSeverity.FATAL).setDetails(detailCode);
			throw new UnprocessableEntityException(FhirContext.forDstu3(), outcome);
		}
	}

}