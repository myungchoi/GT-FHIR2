package edu.gatech.chai.omoponfhir.omopv5.stu3.utilities;

import org.hl7.fhir.dstu3.model.Resource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import edu.gatech.chai.omoponfhir.omopv5.stu3.model.USCorePatient;

public class ExtensionUtil {

	public static USCorePatient usCorePatientFromResource(Resource resource) {
		IParser p = FhirContext.forDstu3().newJsonParser();
		String patientJSON = p.encodeResourceToString(resource);

		return p.parseResource(USCorePatient.class, patientJSON);
	}
}
