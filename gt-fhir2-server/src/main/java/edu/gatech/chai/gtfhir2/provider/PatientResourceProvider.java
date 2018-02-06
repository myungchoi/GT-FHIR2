package edu.gatech.chai.gtfhir2.provider;

import java.util.LinkedList;
import java.util.List;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.hl7.fhir.dstu3.model.Patient;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import edu.gatech.chai.gtfhir2.mapping.OmopPatient;

/**
 * This is a resource provider which stores Patient resources in memory using a HashMap. This is obviously not a production-ready solution for many reasons, 
 * but it is useful to help illustrate how to build a fully-functional server.
 */
public class PatientResourceProvider implements IResourceProvider {

private WebApplicationContext myAppCtx;
private String myDbType;
private OmopPatient myMapper;

	public PatientResourceProvider() {
		myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
		myDbType = myAppCtx.getServletContext().getInitParameter("backendDbType");
		if (myDbType.equalsIgnoreCase("omopv5") == true) {
			myMapper = new OmopPatient(myAppCtx);
		} else {
			myMapper = new OmopPatient(myAppCtx);
		}
	}


	/**
	 * The "@Create" annotation indicates that this method implements "create=type", which adds a 
	 * new instance of a resource to the server.
	 */
	@Create()
	public MethodOutcome createPatient(@ResourceParam Patient thePatient) {
		validateResource(thePatient);
		
		myMapper.toDbase(thePatient);
		
		long id = 1L;
		return new MethodOutcome(new IdDt(id));
	}

	/**
	 * The "@Search" annotation indicates that this method supports the search operation. You may have many different method annotated with this annotation, to support many different search criteria.
	 * This example searches by family name.
	 * 
	 * @param theFamilyName
	 *            This operation takes one parameter which is the search criteria. It is annotated with the "@Required" annotation. This annotation takes one argument, a string containing the name of
	 *            the search criteria. The datatype here is StringDt, but there are other possible parameter types depending on the specific search criteria.
	 * @return This method returns a list of Patients. This list may contain multiple matching resources, or it may also be empty.
	 */
	@Search()
	public List<Patient> findPatientsByName(@RequiredParam(name = Patient.SP_FAMILY) StringDt theFamilyName) {
		LinkedList<Patient> retVal = new LinkedList<Patient>();

//		/*
//		 * Look for all patients matching the name
//		 */
//		for (Deque<Patient> nextPatientList : myIdToPatientVersions.values()) {
//			Patient nextPatient = nextPatientList.getLast();
//			for (HumanName nextName : nextPatient.getName()) {
//				String nextFamily = nextName.getFamily();
//				if (theFamilyName.equals(nextFamily)) {
//					retVal.add(nextPatient);
//					break;
//				}
//			}
//		}

		return retVal;
	}

	@Search
	public List<Patient> findPatientsUsingArbitraryCtriteria() {
		LinkedList<Patient> retVal = new LinkedList<Patient>();

//		for (Deque<Patient> nextPatientList : myIdToPatientVersions.values()) {
//			Patient nextPatient = nextPatientList.getLast();
//			retVal.add(nextPatient);
//		}
	
		return retVal;
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

//		Long id;
//		try {
//			id = theId.getIdPartAsLong();
//		} catch (DataFormatException e) {
//			throw new InvalidRequestException("Invalid ID " + theId.getValue() + " - Must be numeric");
//		}
//
//		/*
//		 * Throw an exception (HTTP 404) if the ID is not known
//		 */
//		if (!myIdToPatientVersions.containsKey(id)) {
//			throw new ResourceNotFoundException(theId);
//		}
//
//		addNewVersion(thePatient, id);

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
//		if (thePatient.getNameFirstRep().getFamilyFirstRep().isEmpty()) {
		if (thePatient.getNameFirstRep().getFamily().isEmpty()) {
			OperationOutcome outcome = new OperationOutcome();
			CodeableConcept detailCode = new CodeableConcept();
			detailCode.setText("No family name provided, Patient resources must have at least one family name.");
			outcome.addIssue().setSeverity(IssueSeverity.FATAL).setDetails(detailCode);
			throw new UnprocessableEntityException(outcome);
		}
	}

}
