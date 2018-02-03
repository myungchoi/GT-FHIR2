package edu.gatech.chai.gtfhir2.mapping;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.ResourceType;

import org.hl7.fhir.dstu3.model.Address.AddressUse;
import org.springframework.web.context.WebApplicationContext;

import edu.gatech.chai.gtfhir2.model.MyOrganization;
import edu.gatech.chai.omopv5.jpa.entity.CareSite;
import edu.gatech.chai.omopv5.jpa.service.CareSiteService;

public class OmopOrganization implements ResourceMapping<Organization> {
	private CareSiteService myOmopService;
	private WebApplicationContext myAppCtx;

	public OmopOrganization(WebApplicationContext context) {
		myAppCtx = context;
		myOmopService = context.getBean(CareSiteService.class);
	}
	
	// get the ID that we will use to get OMOP data.
	// There are two identifications we can get from 
	// FHIR.
	// 1. ID: Long data type ID, which is local ID in the server.
	// 2. Identifier: ID like MRN exists here.
	
	/**
	 * The Omop v5 to FHIR mapping. The input parameter id is 
	 * Omop table index ID, which in this case care_site_id in
	 * care_site table.
	 * 
	 * returns FHIR Organization Resource.
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public Organization toFHIR(IdType id) {
		MyOrganization organization = new MyOrganization();
		String organizationResourceName = ResourceType.Organization.getPath();
		Long id_long_part = id.getIdPartAsLong();
		Long myId = IdMapping.getOMOPfromFHIR(id_long_part, organizationResourceName);
		
		CareSite careSite = (CareSite) myOmopService.findById(myId);
		if (careSite == null) return null;
		
		// Actual mapping here.
		// fhir.id = (idMap) = omop.care_site_id
		// fhir.name = omop.care_site_name
		// fhir.type = omop.place_of_service_concept_id
		// address = location_id

		Long fhirId = IdMapping.getFHIRfromOMOP(myId, organizationResourceName);
		organization.setId(new IdType (fhirId));
		
		if (careSite.getCareSiteName() != null && careSite.getCareSiteName() != "") {
			organization.setName(careSite.getCareSiteName());
		}
		
		if (careSite.getPlaceOfServiceConcept() != null) {
			String codeString = careSite.getPlaceOfServiceConcept().getConceptCode();
			String systemUriString = careSite.getPlaceOfServiceConcept().getVocabulary().getVocabularyReference();
			String displayString = careSite.getPlaceOfServiceConcept().getName();
			
			CodeableConcept typeCodeableConcept = new CodeableConcept().addCoding(new Coding(systemUriString, codeString, displayString));
			organization.addType(typeCodeableConcept);
		}
		
		if (careSite.getLocation() != null) {
			organization.addAddress()
				.setUse(AddressUse.HOME)
				.addLine(careSite.getLocation().getAddress1())
				.addLine(careSite.getLocation().getAddress2())//WARNING check if mapping for lines are correct
				.setCity(careSite.getLocation().getCity())
				.setPostalCode(careSite.getLocation().getZipCode())
				.setState(careSite.getLocation().getState());
//				.setPeriod(period);
		}
		
		return organization; 
	}

	@Override
	public void toDbase(Organization Fhir) {
		// TODO Auto-generated method stub
		
	}
}
