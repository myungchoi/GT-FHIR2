package edu.gatech.chai.gtfhir2.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Address.AddressUse;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import edu.gatech.chai.gtfhir2.utilities.AddressUtil;
import edu.gatech.chai.omopv5.jpa.entity.CareSite;
import edu.gatech.chai.omopv5.jpa.entity.Concept;
import edu.gatech.chai.omopv5.jpa.entity.FPerson;
import edu.gatech.chai.omopv5.jpa.entity.Location;
import edu.gatech.chai.omopv5.jpa.entity.Provider;
import edu.gatech.chai.omopv5.jpa.service.CareSiteService;
import edu.gatech.chai.omopv5.jpa.service.FPersonService;
import edu.gatech.chai.omopv5.jpa.service.LocationService;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;
import edu.gatech.chai.omopv5.jpa.service.ProviderService;

public class OmopPractitioner implements IResourceMapping<Practitioner, Provider>{

	private CareSiteService careSiteService;
	private LocationService locationService;
	private ProviderService providerService;
	
	
	public OmopPractitioner(WebApplicationContext context) {
		careSiteService = context.getBean(CareSiteService.class);
		locationService = context.getBean(LocationService.class);
		providerService = context.getBean(ProviderService.class);
	}
	
	/**
	 * Omop on FHIR mapping - from OMOP to FHIR.
	 * 
	 * @param practitioner ID
	 * 	The Practitioner Resource ID in IdType variable type.
	 * 
	 * @return Practitioner
	 * 	Returns Practitioner Resource mapped from OMOP Provider table.
	 */
	@Override
	public Practitioner toFHIR(IdType id) {
		String practitioncerResourceName = ResourceType.Practitioner.getPath();
		Long id_long_part = id.getIdPartAsLong();
		Long omopId = IdMapping.getOMOPfromFHIR(id_long_part, practitioncerResourceName);
		
		Provider omopProvider = providerService.findById(omopId);
		if(omopProvider == null) return null;
		
		Long fhirId = IdMapping.getFHIRfromOMOP(id_long_part, practitioncerResourceName);
		
		return constructResource(fhirId, omopProvider, null);
	}
	
	@Override
	public Practitioner constructResource(Long fhirId, Provider entity,List<String> includes) {
		Practitioner practitioner = constructFHIR(fhirId,entity); //Assuming default active state
		return practitioner;
	}
	
	public Practitioner constructFHIR(Long fhirId, Provider omopProvider) {
		Practitioner practitioner = new Practitioner(); //Assuming default active state
		practitioner.setId(new IdType(fhirId));
		
		CareSite omopCareSite = omopProvider.getCareSite();
		
		if(omopProvider.getProviderName() != null && !omopProvider.getProviderName().isEmpty()) {
			HumanName fhirName = new HumanName();
			fhirName.setText(omopProvider.getProviderName());
			List<HumanName> fhirNameList = new ArrayList<HumanName>();
			fhirNameList.add(fhirName);
			practitioner.setName(fhirNameList);
		}
		
		//TODO: Need practictioner telecom information
		//Set address
		if(omopCareSite.getLocation() != null && omopCareSite.getLocation().getId() != 0L) {
			practitioner.addAddress()
			.setUse(AddressUse.WORK)
			.addLine(omopCareSite.getLocation().getAddress1())
			.addLine(omopCareSite.getLocation().getAddress2())//WARNING check if mapping for lines are correct
			.setCity(omopCareSite.getLocation().getCity())
			.setPostalCode(omopCareSite.getLocation().getZipCode())
			.setState(omopCareSite.getLocation().getState());
		}
		//Set gender
		if (omopProvider.getGenderConcept() != null) {
			String gName = omopProvider.getGenderConcept().getName().toLowerCase(); 
			AdministrativeGender gender;
			try {
				gender = AdministrativeGender.fromCode(gName);
				practitioner.setGender(gender);
			} catch (FHIRException e) {
				e.printStackTrace();
			}
		}
		return practitioner;
	}

	@Override
	public Long toDbase(Practitioner Fhir, IdType fhirId) {
		Provider omopProvider = new Provider();
		String providerSourceValue = null;
		CareSite omopCareSite = new CareSite();
		//Set name
		Iterator<HumanName> practitionerIterator = Fhir.getName().iterator();
		if(practitionerIterator.hasNext()) {
			HumanName next = practitionerIterator.next();
			omopProvider.setProviderName(next.getText());
		}
		//Set address
		List<Address> addresses = Fhir.getAddress();
		Location retLocation = null;
		if (addresses != null && addresses.size() > 0) {
			Address address = addresses.get(0);
			retLocation = AddressUtil.searchAndUpdate(locationService, address, null);
			if (retLocation != null) {
				omopCareSite.setLocation(retLocation);
			}
		}
		//Set gender concept
		omopProvider.setGenderConcept(new Concept());
		String genderCode = Fhir.getGender().toCode();
		try {
			omopProvider.getGenderConcept().setId(OmopConceptMapping.omopForAdministrativeGenderCode(genderCode));
		} catch (FHIRException e) {
			e.printStackTrace();
		}
		//Create a new caresite if does not exist
		if(!Fhir.getAddress().isEmpty()) {
			CareSite careSite = searchAndUpdateCareSite(Fhir.getAddress().get(0));
			careSiteService.createOrUpdate(careSite);
		}
		
		List<Identifier> identifiers = Fhir.getIdentifier();
		Provider allreadyIdentifiedProvider = null;
		for (Identifier identifier : identifiers) {
			if (identifier.getValue().isEmpty() == false) {
				providerSourceValue = identifier.getValue();

				// See if we have existing patient
				// with this identifier.
				allreadyIdentifiedProvider = providerService.searchByColumnString("providerSourceValue", providerSourceValue).get(0);
				if (allreadyIdentifiedProvider != null) {
					omopProvider.setId(allreadyIdentifiedProvider.getId());
					break;
				}
			}
		}
		// If we have match in identifier, then we can update or create since
		// we have the patient. If we have no match, but fhirId is not null,
		// then this is update with fhirId. We need to do another search.
		if (allreadyIdentifiedProvider == null && fhirId != null) {
			// Search for this ID.
			Long omopId = IdMapping.getOMOPfromFHIR(fhirId.getIdPartAsLong(), ResourceType.Practitioner.getPath());
			if (omopId == null) {
				// This is update. We don't have this provider. Return null.
				return null;
			}
			
			// See if we have this in our database.
			allreadyIdentifiedProvider = providerService.findById(omopId);
			if (allreadyIdentifiedProvider == null) {
				// We don't have this patient
				return null;
			} else {
				omopProvider.setId(allreadyIdentifiedProvider.getId());
			}
		}
		
		Long omopRecordId = providerService.createOrUpdate(omopProvider).getId();
		Long fhirRecordId = IdMapping.getFHIRfromOMOP(omopRecordId, ResourceType.Practitioner.getPath());
		return fhirRecordId;
	}
	
	@Override
	public Long getSize() {
		return providerService.getSize();
	}

	public Long getSize(Map<String, List<ParameterWrapper>> map) {
		return providerService.getSize(map);
	}
	
	public Location searchAndUpdateLocation (Address address, Location location) {
		if (address == null) return null;
		
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
					return new Location (line1, line2, city, state, zipCode);
				}
			}			
		}
		
		return null;
	}
	
	public CareSite searchAndUpdateCareSite(Address address) {
		Location location = AddressUtil.searchAndUpdate(locationService, address, null);
		if(location == null) return null;
		CareSite careSite = careSiteService.searchByLocation(location);
		if(careSite != null) {
			return careSite;
		}
		else {
			careSite = new CareSite();
			careSite.setLocation(location);
			return careSite;
		}
	}

	@Override
	public void searchWithoutParams(int fromIndex, int toIndex, List<IBaseResource> listResources,
			List<String> includes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void searchWithParams(int fromIndex, int toIndex, Map<String, List<ParameterWrapper>> map,
			List<IBaseResource> listResources, List<String> includes) {
		// TODO Auto-generated method stub
		
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
		case Practitioner.SP_ACTIVE:
			// True of False in FHIR. In OMOP, this is 1 or 0.
			String activeValue = ((TokenParam) value).getValue();
			String activeString;
			if (activeValue.equalsIgnoreCase("true"))
				activeString = "1";
			else
				activeString = "0";
			paramWrapper.setParameterType("Short");
			paramWrapper.setParameters(Arrays.asList("active"));
			paramWrapper.setOperators(Arrays.asList("="));
			paramWrapper.setValues(Arrays.asList(activeString));
			paramWrapper.setRelationship("or");
			mapList.add(paramWrapper);
			break;
		case Practitioner.SP_FAMILY:
			// This is family name, which is string. use like.
			String familyString;
			if (((StringParam) value).isExact())
				familyString = ((StringParam) value).getValue();
			else
				familyString = "%"+((StringParam) value).getValue()+"%";
			paramWrapper.setParameterType("String");
			paramWrapper.setParameters(Arrays.asList("provider_name"));
			paramWrapper.setOperators(Arrays.asList("like"));
			paramWrapper.setValues(Arrays.asList(familyString));
			paramWrapper.setRelationship("or");
			mapList.add(paramWrapper);
			break;
		case Practitioner.SP_GIVEN:
			String givenString;
			if (((StringParam) value).isExact())
				givenString = ((StringParam) value).getValue();
			else
				givenString = "%"+((StringParam) value).getValue()+"%";
			paramWrapper.setParameterType("String");
			paramWrapper.setParameters(Arrays.asList("provider_name"));
			paramWrapper.setOperators(Arrays.asList("like"));
			paramWrapper.setValues(Arrays.asList(givenString));
			paramWrapper.setRelationship("or");
			mapList.add(paramWrapper);
			break;
		case Patient.SP_GENDER:
			//Not sure whether we should just search the encoded concept, or the source concept as well. Doing both for now.
			String genderValue = ((TokenParam) value).getValue();
			Long genderLongCode = null;
			//Setting the value to omop concept NULL if we cannot find an omopId
			try {
				genderLongCode = OmopConceptMapping.omopForAdministrativeGenderCode(genderValue);
			} catch (FHIRException e) {
				genderLongCode = OmopConceptMapping.NULL.omopConceptId;
			}
			paramWrapper.setParameterType("Long");
			paramWrapper.setParameters(Arrays.asList("gender_source_concept_id", "gender_source_value"));
			paramWrapper.setOperators(Arrays.asList("="));
			paramWrapper.setValues(Arrays.asList(genderLongCode.toString(),genderLongCode.toString()));
			paramWrapper.setRelationship("or");
			mapList.add(paramWrapper);
			break;
		default:
			mapList = null;
		}
		return mapList;
	}
}
