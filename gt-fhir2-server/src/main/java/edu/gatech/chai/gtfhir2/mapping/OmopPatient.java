package edu.gatech.chai.gtfhir2.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Address.AddressUse;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.codesystems.V3MaritalStatus;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import edu.gatech.chai.omopv5.jpa.entity.Concept;
import edu.gatech.chai.omopv5.jpa.entity.FPerson;
import edu.gatech.chai.omopv5.jpa.entity.Location;
import edu.gatech.chai.omopv5.jpa.entity.Provider;
import edu.gatech.chai.omopv5.jpa.service.FPersonService;
import edu.gatech.chai.omopv5.jpa.service.LocationService;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;
import edu.gatech.chai.omopv5.jpa.service.ProviderService;

public class OmopPatient implements ResourceMapping<Patient> {

	private FPersonService myOmopService;
	private LocationService locationService;
	private ProviderService providerService;

	public OmopPatient(WebApplicationContext context) {
		myOmopService = context.getBean(FPersonService.class);
		locationService = context.getBean(LocationService.class);
		providerService = context.getBean(ProviderService.class);
	}

	/**
	 * Omop on FHIR mapping - from OMOP to FHIR.
	 * 
	 * @param patient
	 *            ID The Patient Resource ID in IdType variable type.
	 * 
	 * @return Patient Returns Patient Resource mapped from OMOP Person table.
	 */
	@Override
	public Patient toFHIR(IdType id) {
		String patientResourceName = ResourceType.Patient.getPath();
		Long id_long_part = id.getIdPartAsLong();
		Long myId = IdMapping.getOMOPfromFHIR(id_long_part, patientResourceName);

		FPerson fPerson = (FPerson) myOmopService.findById(myId);
		if (fPerson == null)
			return null;

		Long fhirId = IdMapping.getFHIRfromOMOP(myId, patientResourceName);

		return constructPatient(fhirId, fPerson);
	}

	private Patient constructPatient(Long fhirId, FPerson fPerson) {
		Patient patient = new Patient();
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

		calendar.set(yob, mob - 1, dob);
		patient.setBirthDate(calendar.getTime());

		if (fPerson.getLocation() != null && fPerson.getLocation().getId() != 0L) {
			patient.addAddress().setUse(AddressUse.HOME).addLine(fPerson.getLocation().getAddress1())
					.addLine(fPerson.getLocation().getAddress2())// WARNING
																	// check if
																	// mapping
																	// for lines
																	// are
																	// correct
					.setCity(fPerson.getLocation().getCity()).setPostalCode(fPerson.getLocation().getZipCode())
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
		patient.addName(humanName);
		if (fPerson.getGivenName2() != null)
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
				Coding coding = new Coding(maritalStatus.getSystem(), maritalStatus.toCode(),
						maritalStatus.getDisplay());
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

	/**
	 * OMOP on FHIR mapping - from FHIR to OMOP
	 * 
	 * @param Patient
	 *            resource.
	 * 
	 * @return Resource ID. Returns ID in Long. This is what needs to be used to
	 *         refer this resource.
	 */
	@Override
	public Long toDbase(Patient patient) {
		FPerson fperson = new FPerson();
		String personSourceValue = null;

		// Set name
		Iterator<HumanName> patientIterator = patient.getName().iterator();
		if (patientIterator.hasNext()) {
			HumanName next = patientIterator.next();
			fperson.setGivenName1(next.getGiven().get(0).getValue());// the next
																		// method
																		// was
																		// not
																		// advancing
																		// to
																		// the
																		// next
																		// element,
																		// then
																		// the
																		// need
																		// to
																		// use
																		// the
																		// get(index)
																		// method
			if (next.getGiven().size() > 1) // TODO add unit tests, to assure
											// this won't be changed to hasNext
				fperson.setGivenName2(next.getGiven().get(1).getValue());
			String family = next.getFamily();
			fperson.setFamilyName(family);
			if (next.getSuffix().iterator().hasNext())
				fperson.setSuffixName(next.getSuffix().iterator().next().getValue());
			if (next.getPrefix().iterator().hasNext())
				fperson.setPrefixName(next.getPrefix().iterator().next().getValue());
		}

		// Search Location entity to see if we have this address available.
		// If not, create this one.
		List<Address> addresses = patient.getAddress();
		Location retLocation = null;
		if (addresses != null && addresses.size() > 0) {
			Address address = addresses.get(0);
			retLocation = searchAndUpdate(address, null);
			if (retLocation != null) {
				fperson.setLocation(retLocation);
			}
		}

		// In OMOP, we have person source column.
		// We will use identifier field as our source column if exists. The
		// identifier better identifies
		// the identity of this resource across all servers that may have this
		// copy.
		//
		// Identifier has many fields. We can't have them all in OMOP. We only
		// have string field and
		// size is very limited. So, for now, we only get value part.
		List<Identifier> identifiers = patient.getIdentifier();

		// TODO: For now, we choose the first identifier if exists.
		if (identifiers.isEmpty() == false) {
			Identifier identifier = identifiers.get(0);
			if (identifier.getValue().isEmpty() == false) {
				personSourceValue = identifier.getValue();

				// If ID is not set, then we see if we have existing patient
				// with this identifier.
				FPerson person = myOmopService.searchByColumnString("personSourceValue", personSourceValue);
				fperson.setId(person.getId());
			}
		} else if (retLocation != null) {
			// FHIR Patient identifier is empty. Use name and address
			// to see if we have a patient exits.
			if (retLocation.getId() != null) {
				FPerson existingPerson = myOmopService.searchByNameAndLocation(fperson.getFamilyName(),
						fperson.getGivenName1(), fperson.getGivenName2(), retLocation);
				if (existingPerson != null) {
					System.out.println("Patient Exists with PID=" + existingPerson.getId());
					fperson.setId(existingPerson.getId());
				}
			}
		}

		Concept race = new Concept();
		race.setId(8552L);
		fperson.setRaceConcept(race);

		// Ethnicity is not available in FHIR resource. Set to 0L as there is no
		// unknown ethnicity.
		Concept ethnicity = new Concept();
		ethnicity.setId(0L);
		fperson.setEthnicityConcept(ethnicity);

		Calendar c = Calendar.getInstance();
		c.setTime(patient.getBirthDate());
		fperson.setYearOfBirth(c.get(Calendar.YEAR));
		fperson.setMonthOfBirth(c.get(Calendar.MONTH) + 1);
		fperson.setDayOfBirth(c.get(Calendar.DAY_OF_MONTH));

		// TODO set deceased value in Person; Set gender concept (source value
		// is set); list of addresses (?)
		// this.death = patient.getDeceased();

		fperson.setGenderConcept(new Concept());
		String genderCode = patient.getGender().toCode();
		try {
			fperson.getGenderConcept().setId(OmopConceptMapping.omopForAdministrativeGenderCode(genderCode));
		} catch (FHIRException e) {
			e.printStackTrace();
		}

		List<Reference> generalPractitioners = patient.getGeneralPractitioner();
		if (generalPractitioners.size() > 0) {
			// We can handle only one provider.
			Provider retProvider = searchAndUpdate(generalPractitioners.get(0));
			if (retProvider != null) {
				fperson.setProvider(retProvider);
			}
		}

		if (personSourceValue != null)
			fperson.setPersonSourceValue(personSourceValue);

		if (patient.getActive())
			fperson.setActive((short) 1);
		else
			fperson.setActive((short) 0);

		CodeableConcept maritalStat = patient.getMaritalStatus();
		if (maritalStat != null) {
			Coding coding = maritalStat.getCodingFirstRep();
			if (coding != null) {
				System.out.println("MARITAL STATUS:" + coding.getCode());
				fperson.setMaritalStatus(coding.getCode());
			}
		}

		// Get contact information.
		List<ContactPoint> contactPoints = patient.getTelecom();
		Iterator<ContactPoint> contactIterator = contactPoints.iterator();
		int index = 0;
		while (contactIterator.hasNext()) {
			ContactPoint contactPoint = contactIterator.next();
			String system = contactPoint.getSystem().getSystem();
			String use = contactPoint.getUse().toCode();
			String value = contactPoint.getValue();
			if (index == 0) {
				fperson.setContactPoint1(system + ":" + use + ":" + value);
			} else if (index == 1) {
				fperson.setContactPoint2(system + ":" + use + ":" + value);
			} else {
				fperson.setContactPoint3(system + ":" + use + ":" + value);
				break;
			}
			index++;
		}

		Long omopRecordId = myOmopService.createOrUpdate(fperson).getId();
		Long fhirId = IdMapping.getFHIRfromOMOP(omopRecordId, ResourceType.Patient.getPath());
		return fhirId;
	}

	/**
	 * 
	 * @param fromIndex
	 * @param toIndex
	 * @param listResources
	 */
	public void searchWithoutParams(int fromIndex, int toIndex, List<IBaseResource> listResources) {
		List<FPerson> fPersons = myOmopService.searchWithoutParams(fromIndex, toIndex);

		// We got the results back from OMOP database. Now, we need to construct
		// the list of
		// FHIR Patient resources to be included in the bundle.
		for (FPerson fPerson : fPersons) {
			Long omopId = fPerson.getId();
			Long fhirId = IdMapping.getFHIRfromOMOP(omopId, ResourceType.Patient.getPath());
			listResources.add(constructPatient(fhirId, fPerson));
		}
	}

	public void searchWithParams(int fromIndex, int toIndex, Map<String, List<ParameterWrapper>> map,
			List<IBaseResource> listResources) {
		List<FPerson> fPersons = myOmopService.searchWithParams(fromIndex, toIndex, map);

		for (FPerson fPerson : fPersons) {
			Long omopId = fPerson.getId();
			Long fhirId = IdMapping.getFHIRfromOMOP(omopId, ResourceType.Patient.getPath());
			listResources.add(constructPatient(fhirId, fPerson));
		}
	}

	/**
	 * searchAndUpdate: search the database for general Practitioner. This is
	 * provider table in OMOP. If exist, return it. We may have this received
	 * before, in this case, search it from source column and return it.
	 * Otherwise, create a new one.
	 * 
	 * Returns provider entity in OMOP.
	 * 
	 * @param generalPractitioner
	 * @return
	 */
	public Provider searchAndUpdate(Reference generalPractitioner) {
		if (generalPractitioner == null)
			return null;

		// See if this exists.
		Long fhirId = generalPractitioner.getReferenceElement().getIdPartAsLong();
		Long omopId = IdMapping.getOMOPfromFHIR(fhirId, ResourceType.Practitioner.getPath());
		Provider provider = (Provider) providerService.findById(omopId);
		if (provider != null) {
			return provider;
		} else {
			// Check source column to see if we have received this before.
			provider = (Provider) providerService.searchByColumnString("providerSourceValue",
					generalPractitioner.getReferenceElement().getIdPart());
			if (provider != null) {
				return provider;
			} else {
				provider = new Provider();
				provider.setProviderSourceValue(generalPractitioner.getReferenceElement().getIdPart());
				if (generalPractitioner.getDisplay() != null)
					provider.setProviderName(generalPractitioner.getDisplay().toString());
				return provider;
			}
		}
	}

	@Override
	public Long getSize() {
		return myOmopService.getSize();
	}

	public Long getSize(Map<String, List<ParameterWrapper>> map) {
		return myOmopService.getSize(map);
	}

	/**
	 * mapParameter: This maps the FHIR parameter to OMOP column name.
	 * 
	 * @param parameter
	 *            FHIR parameter name.
	 * @param value
	 *            FHIR value for the parameter
	 * @return returns ParameterWrapper class, which contains OMOP column name
	 *         and value with operator.
	 */
	public List<ParameterWrapper> mapParameter(String parameter, Object value) {
		List<ParameterWrapper> mapList = new ArrayList<ParameterWrapper>();
		ParameterWrapper paramWrapper = new ParameterWrapper();
		switch (parameter) {
		case Patient.SP_ACTIVE:
			// True of False in FHIR. In OMOP, this is 1 or 0.
			String activeValue = ((TokenParam) value).getValue();
			String activeString;
			if (activeValue.equalsIgnoreCase("true"))
				activeString = "1";
			else
				activeString = "0";
			paramWrapper.setParameterType("Short");
			paramWrapper.setParameters(Arrays.asList("active"));
			paramWrapper.setOperator("=");
			paramWrapper.setValue(activeString);
			mapList.add(paramWrapper);
			break;
		case Patient.SP_FAMILY:
			// This is family name, which is string. use like.
			String familyString = ((StringParam) value).getValue();
			paramWrapper.setParameterType("String");
			paramWrapper.setParameters(Arrays.asList("familyName"));
			paramWrapper.setOperator("like");
			paramWrapper.setValue(familyString);
			mapList.add(paramWrapper);
			break;
		case Patient.SP_GIVEN:
			// This is given name, which is string. use like.
			String givenName = ((StringParam) value).getValue();
			paramWrapper.setParameterType("String");
			paramWrapper.setParameters(Arrays.asList("givenName1", "givenName2"));
			paramWrapper.setOperator("like");
			paramWrapper.setValue(givenName);
			mapList.add(paramWrapper);
			break;
		default:
			mapList = null;
		}

		return mapList;
	}

	public Location searchAndUpdate(Address address, Location location) {
		if (address == null)
			return null;

		List<StringType> addressLines = address.getLine();
		if (addressLines.size() > 0) {
			String line1 = addressLines.get(0).getValue();
			String line2 = null;
			if (address.getLine().size() > 1)
				line2 = address.getLine().get(1).getValue();
			String zipCode = address.getPostalCode();
			String city = address.getCity();
			String state = address.getState();

			Location existingLocation = locationService.searchByAddress(line1, line2, city, state, zipCode);
			if (existingLocation != null) {
				return existingLocation;
			} else {
				// We will return new Location. But, if Location is provided,
				// then we update the parameters here.
				if (location != null) {
					location.setAddress1(line1);
					if (line2 != null)
						location.setAddress2(line2);
					location.setZipCode(zipCode);
					location.setCity(city);
					location.setState(state);
				} else {
					return new Location(line1, line2, city, state, zipCode);
				}
			}
		}

		return null;
	}

}
