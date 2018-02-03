package edu.gatech.chai.gtfhir2.mapping;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.Address.AddressUse;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.codesystems.V3MaritalStatus;
import org.hl7.fhir.exceptions.FHIRException;
import org.springframework.web.context.WebApplicationContext;

import edu.gatech.chai.omopv5.jpa.entity.FPerson;
import edu.gatech.chai.omopv5.jpa.service.FPersonService;

public class OmopPatient implements ResourceMapping<Patient> {

	private FPersonService myOmopService;

	public OmopPatient(WebApplicationContext context) {
		myOmopService = context.getBean(FPersonService.class);
	}
	
	@Override
	public Patient toFHIR(IdType id) {
		Patient patient = new Patient();
		String patientResourceName = ResourceType.Patient.getPath();
		Long id_long_part = id.getIdPartAsLong();
		Long myId = IdMapping.getOMOPfromFHIR(id_long_part, patientResourceName);
		
		FPerson fPerson = (FPerson) myOmopService.findById(myId);
		if (fPerson == null) return null;
		
		Long fhirId = IdMapping.getFHIRfromOMOP(myId, patientResourceName);
		patient.setId(new IdType(fhirId));

		// Start mapping Person/FPerson table to Patient Resource.
		Calendar calendar = Calendar.getInstance();
		int yob, mob, dob;
		if (fPerson.getYearOfBirth() != null) 
			yob = fPerson.getYearOfBirth(); 
		else 
			yob = 1;
		if (fPerson.getMonthOfBirth() != null) 
			mob = fPerson.getMonthOfBirth(); 
		else 
			mob = 1;
		if (fPerson.getDayOfBirth() != null) 
			dob = fPerson.getDayOfBirth(); 
		else 
			dob = 1;
		
		calendar.set(yob, mob-1, dob);
		patient.setBirthDate(calendar.getTime());
		
		if(fPerson.getLocation() != null && fPerson.getLocation().getId() != 0L){
			patient.addAddress()
				.setUse(AddressUse.HOME)
				.addLine(fPerson.getLocation().getAddress1())
				.addLine(fPerson.getLocation().getAddress2())//WARNING check if mapping for lines are correct
				.setCity(fPerson.getLocation().getCity())
				.setPostalCode(fPerson.getLocation().getZipCode())
				.setState(fPerson.getLocation().getState());
		}
		
		if (fPerson.getGenderConcept() != null) {
			String gName = fPerson.getGenderConcept().getName().toLowerCase(); 
			AdministrativeGender gender;
			try {
				gender = AdministrativeGender.fromCode(gName);
				patient.setGender(gender);
			} catch (FHIRException e) {
				e.printStackTrace();
			}
		}
		
		if (fPerson.getProvider() != null && fPerson.getProvider().getId() != 0L) {
			Reference generalPractitioner = new Reference(new IdType(fPerson.getProvider().getId()));
			generalPractitioner.setDisplay(fPerson.getProvider().getProviderName());
			List<Reference> generalPractitioners = new ArrayList<Reference>();
			generalPractitioners.add(generalPractitioner);
			patient.setGeneralPractitioner(generalPractitioners);
		}

		HumanName humanName = new HumanName();
		humanName.setFamily(fPerson.getFamilyName()).addGiven(fPerson.getGivenName1());
		if(fPerson.getGivenName2() != null)
			patient.getName().get(0).addGiven(fPerson.getGivenName2());

		if (fPerson.getActive() == null || fPerson.getActive() == 0)
			patient.setActive(false);
		else
			patient.setActive(true);

		if (fPerson.getMaritalStatus() != null && !fPerson.getMaritalStatus().isEmpty()) {
			CodeableConcept maritalStatusCode = new CodeableConcept();
			V3MaritalStatus maritalStatus;
			try {
				maritalStatus = V3MaritalStatus.fromCode(fPerson.getMaritalStatus().toUpperCase());
				Coding coding = new Coding(maritalStatus.getSystem(), maritalStatus.toCode(), maritalStatus.getDisplay());
				maritalStatusCode.addCoding(coding);
				patient.setMaritalStatus(maritalStatusCode);
			} catch (FHIRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		List<ContactPoint> contactPoints = new ArrayList<ContactPoint>();
		if (fPerson.getContactPoint1() != null && !fPerson.getContactPoint1().isEmpty()) {
			String[] contactInfo = fPerson.getContactPoint1().split(":");
			if (contactInfo.length == 3) {
				ContactPoint contactPoint = new ContactPoint();
				contactPoint.setSystem(ContactPoint.ContactPointSystem.valueOf(contactInfo[0].toUpperCase()));
				contactPoint.setUse(ContactPoint.ContactPointUse.valueOf(contactInfo[1].toUpperCase()));
				contactPoint.setValue(contactInfo[2]);
				contactPoints.add(contactPoint);
			}
		}
		if (fPerson.getContactPoint2() != null && !fPerson.getContactPoint2().isEmpty()) {
			String[] contactInfo = fPerson.getContactPoint2().split(":");
			if (contactInfo.length == 3) {
				ContactPoint contactPoint = new ContactPoint();
				contactPoint.setSystem(ContactPoint.ContactPointSystem.valueOf(contactInfo[0].toUpperCase()));
				contactPoint.setUse(ContactPoint.ContactPointUse.valueOf(contactInfo[1].toUpperCase()));
				contactPoint.setValue(contactInfo[2]);
				contactPoints.add(contactPoint);
			}
		}
		if (fPerson.getContactPoint3() != null && !fPerson.getContactPoint3().isEmpty()) {
			String[] contactInfo = fPerson.getContactPoint3().split(":");
			if (contactInfo.length == 3) {
				ContactPoint contactPoint = new ContactPoint();
				contactPoint.setSystem(ContactPoint.ContactPointSystem.valueOf(contactInfo[0].toUpperCase()));
				contactPoint.setUse(ContactPoint.ContactPointUse.valueOf(contactInfo[1].toUpperCase()));
				contactPoint.setValue(contactInfo[2]);
				contactPoints.add(contactPoint);
			}
		}
		
		patient.setTelecom(contactPoints);
		
		return patient;
	}

	@Override
	public void toDbase(Patient Fhir) {
		// TODO Auto-generated method stub
		
	}

}
