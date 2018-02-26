package edu.gatech.chai.gtfhir2.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Address.AddressUse;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import edu.gatech.chai.gtfhir2.model.MyOrganization;
import edu.gatech.chai.gtfhir2.utilities.AddressUtil;
import edu.gatech.chai.omopv5.jpa.entity.CareSite;
import edu.gatech.chai.omopv5.jpa.entity.Concept;
import edu.gatech.chai.omopv5.jpa.entity.Location;
import edu.gatech.chai.omopv5.jpa.service.CareSiteService;
import edu.gatech.chai.omopv5.jpa.service.LocationService;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;

public class OmopOrganization implements IResourceMapping<Organization, CareSite> {
	private CareSiteService myOmopService;
	private LocationService locationService;
	private WebApplicationContext myAppCtx;

	public OmopOrganization(WebApplicationContext context) {
		myAppCtx = context;
		myOmopService = context.getBean(CareSiteService.class);
		locationService = context.getBean(LocationService.class);
	}

	@Override
	public MyOrganization toFHIR(IdType id) {
		String organizationResourceName = ResourceType.Organization.getPath();
		Long id_long_part = id.getIdPartAsLong();
		Long myId = IdMapping.getOMOPfromFHIR(id_long_part, organizationResourceName);

		CareSite careSite = (CareSite) myOmopService.findById(myId);
		if (careSite == null)
			return null;

		// Actual mapping here.
		// fhir.id = (idMap) = omop.care_site_id
		// fhir.name = omop.care_site_name
		// fhir.type = omop.place_of_service_concept_id
		// address = location_id
		Long fhirId = IdMapping.getFHIRfromOMOP(myId, organizationResourceName);

		return constructFHIR(fhirId, careSite);
	}

	public static MyOrganization constructFHIR(Long fhirId, CareSite careSite) {
		MyOrganization organization = new MyOrganization();

		organization.setId(new IdType(fhirId));

		if (careSite.getCareSiteName() != null && careSite.getCareSiteName() != "") {
			organization.setName(careSite.getCareSiteName());
		}

		if (careSite.getPlaceOfServiceConcept() != null) {
			String codeString = careSite.getPlaceOfServiceConcept().getConceptCode();
			String systemUriString = careSite.getPlaceOfServiceConcept().getVocabulary().getVocabularyReference();
			String displayString = careSite.getPlaceOfServiceConcept().getName();

			CodeableConcept typeCodeableConcept = new CodeableConcept()
					.addCoding(new Coding(systemUriString, codeString, displayString));
			organization.addType(typeCodeableConcept);
		}

		if (careSite.getLocation() != null) {
			// WARNING check if mapping for lines are correct
			organization.addAddress().setUse(AddressUse.HOME).addLine(careSite.getLocation().getAddress1())
					.addLine(careSite.getLocation().getAddress2())
					.setCity(careSite.getLocation().getCity()).setPostalCode(careSite.getLocation().getZipCode())
					.setState(careSite.getLocation().getState());
			// .setPeriod(period);
		}

		// TODO: Static Extensions for sample. Remove this later.
		// Populate the first, primitive extension
		organization.setBillingCode(new CodeType("00102-1"));

		// The second extension is repeatable and takes a block type
		MyOrganization.EmergencyContact contact = new MyOrganization.EmergencyContact();
		contact.setActive(new BooleanType(true));
		contact.setContact(new ContactPoint());
		organization.getEmergencyContact().add(contact);

		return organization;
	}

	@Override
	public Long toDbase(Organization organization, IdType fhirId) {
		// If fhirId is null, then it's CREATE.
		// If fhirId is not null, then it's UPDATE.

		CareSite careSite;
		Long omopId;
		String careSiteSourceValue = null;
		MyOrganization myOrganization = (MyOrganization) organization;
		Location location = null;
		
		if (fhirId != null) {
			omopId = IdMapping.getOMOPfromFHIR(fhirId.getIdPartAsLong(), ResourceType.Organization.getPath());
			if (omopId == null) {
				// This is a problem. We should have the valid omopID that matches to
				// FHIR ID. return null.
				return null;
			} else {
				careSite = myOmopService.findById(omopId);
			}
			
			location = careSite.getLocation();
		} else {
			// See if we have this already. If so, we throw error.
			// Get the identifier to store the source information.
			// If we found a matching one, replace this with the careSite.
			List<Identifier> identifiers = myOrganization.getIdentifier();
			CareSite existingCareSite = null;
			for (Identifier identifier: identifiers) {
				if (identifier.getValue().isEmpty() == false) {
					careSiteSourceValue = identifier.getValue();
					
					existingCareSite = myOmopService.searchByColumnString("careSiteSourceValue", careSiteSourceValue).get(0);
					if (existingCareSite != null) {
						break;
					}
				}
			}
			if (existingCareSite != null) {
				careSite = existingCareSite;
			} else {
				careSite = new CareSite();
			}
		}
		
		Location existingLocation = AddressUtil.searchAndUpdate(locationService, organization.getAddressFirstRep(), location);
		if (existingLocation != null) {
			careSite.setLocation(existingLocation);
		}

		// Organization.name to CareSiteName
		careSite.setCareSiteName(myOrganization.getName());

		// Organzation.type to Place of Service Concept
		List<CodeableConcept> orgTypes = myOrganization.getType();
		for (CodeableConcept orgType: orgTypes) {
			List<Coding> typeCodings = orgType.getCoding();
			if (typeCodings.size() > 0) {
				String typeCode = typeCodings.get(0).getCode();
				Long placeOfServiceId;
				try {
					placeOfServiceId = OmopConceptMapping.omopForOrganizationTypeCode(typeCode);
					Concept placeOfServiceConcept = new Concept();
					placeOfServiceConcept.setId(placeOfServiceId);
					careSite.setPlaceOfServiceConcept(placeOfServiceConcept);
				} catch (FHIRException e) {
					e.printStackTrace();
				}
			}
		}

		// Address to Location ID
		List<Address> addresses = myOrganization.getAddress();
		for (Address address: addresses) {
			// We can only store one address.
			Location retLocation = AddressUtil.searchAndUpdate(locationService, address, careSite.getLocation());
			if (retLocation != null) {
				careSite.setLocation(retLocation);
				break;
			}
		}

		Long omopRecordId = myOmopService.createOrUpdate(careSite).getId();
		Long fhirRecordId = IdMapping.getFHIRfromOMOP(omopRecordId, ResourceType.Organization.getPath());
		return fhirRecordId;
	}

	@Override
	public Long getSize() {
		return myOmopService.getSize();
	}
	
	public Long getSize(Map<String, List<ParameterWrapper>> map) {
		return myOmopService.getSize(map);
	}
	

	@Override
	public Organization constructResource(Long fhirId, CareSite entity, List<String> includes) {
		MyOrganization myOrganization = constructFHIR(fhirId, entity);
		
		if (!includes.isEmpty()) {
			if (includes.contains("Organization:partof")) {
				Reference partOfOrganization = myOrganization.getPartOf();
				if (partOfOrganization != null && partOfOrganization.isEmpty() == false) {
					IIdType partOfOrgId = partOfOrganization.getReferenceElement();
					Long partOfOrgFhirId = partOfOrgId.getIdPartAsLong();
					Long omopId = IdMapping.getOMOPfromFHIR(partOfOrgFhirId, ResourceType.Organization.getPath());
					CareSite partOfCareSite = myOmopService.findById(omopId);
					MyOrganization partOfOrgResource = constructFHIR(partOfOrgFhirId, partOfCareSite);
					
					partOfOrganization.setResource(partOfOrgResource);
				}
			}
		}

		return myOrganization;
	}

	/**
	 * 
	 * @param fromIndex
	 * @param toIndex
	 * @param listResources
	 */
	public void searchWithoutParams(int fromIndex, int toIndex, List<IBaseResource> listResources, List<String> includes) {
		List<CareSite> careSites = myOmopService.searchWithoutParams(fromIndex, toIndex);

		// We got the results back from OMOP database. Now, we need to construct
		// the list of
		// FHIR Patient resources to be included in the bundle.
		for (CareSite careSite : careSites) {
			Long omopId = careSite.getId();
			Long fhirId = IdMapping.getFHIRfromOMOP(omopId, ResourceType.Patient.getPath());
			listResources.add(constructResource(fhirId, careSite, includes));
		}
	}

	public void searchWithParams(int fromIndex, int toIndex, Map<String, List<ParameterWrapper>> map,
			List<IBaseResource> listResources, List<String> includes) {
		List<CareSite> careSites = myOmopService.searchWithParams(fromIndex, toIndex, map);

		for (CareSite careSite : careSites) {
			Long omopId = careSite.getId();
			Long fhirId = IdMapping.getFHIRfromOMOP(omopId, ResourceType.Patient.getPath());
			listResources.add(constructResource(fhirId, careSite, includes));
		}
	}

	public List<ParameterWrapper> mapParameter(String parameter, Object value) {
		List<ParameterWrapper> mapList = new ArrayList<ParameterWrapper>();
		ParameterWrapper paramWrapper = new ParameterWrapper();
		switch (parameter) {
		case MyOrganization.SP_RES_ID:
			String orgnizationId = ((TokenParam) value).getValue();
			paramWrapper.setParameterType("Long");
			paramWrapper.setParameters(Arrays.asList("id"));
			paramWrapper.setOperators(Arrays.asList("="));
			paramWrapper.setValues(Arrays.asList(orgnizationId));
			paramWrapper.setRelationship("or");
			mapList.add(paramWrapper);
			break;
		case MyOrganization.SP_NAME:
			// This is family name, which is string. use like.
			String familyString = ((StringParam) value).getValue();
			paramWrapper.setParameterType("String");
			paramWrapper.setParameters(Arrays.asList("careSiteName"));
			paramWrapper.setOperators(Arrays.asList("like"));
			paramWrapper.setValues(Arrays.asList(familyString));
			paramWrapper.setRelationship("or");
			mapList.add(paramWrapper);
			break;
		default:
			mapList = null;
		}

		return mapList;
	}
}
