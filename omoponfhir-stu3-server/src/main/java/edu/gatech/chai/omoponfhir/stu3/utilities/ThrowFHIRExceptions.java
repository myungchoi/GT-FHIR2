package edu.gatech.chai.omoponfhir.stu3.utilities;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;

public class ThrowFHIRExceptions {

	public static UnprocessableEntityException unprocessableEntityException(String message) {
		OperationOutcome outcome = new OperationOutcome();
		CodeableConcept detailCode = new CodeableConcept();
		detailCode.setText(message);
		outcome.addIssue().setSeverity(IssueSeverity.FATAL).setDetails(detailCode);
		throw new UnprocessableEntityException(FhirContext.forDstu3(), outcome);
	}
	
	public static InternalErrorException internalErrorException(String message) {
		throw new InternalErrorException(message);
	}
}
