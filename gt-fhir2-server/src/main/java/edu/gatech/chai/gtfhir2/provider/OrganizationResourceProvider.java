package edu.gatech.chai.gtfhir2.provider;

import javax.persistence.Entity;
import javax.persistence.Table;

//import ca.uhn.fhir.model.primitive.BooleanDt;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.DomainResource;
//import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.hl7.fhir.dstu3.model.Address.AddressUse;

//import ca.uhn.fhir.model.dstu2.composite.ContactPointDt;
//import ca.uhn.fhir.model.dstu2.valueset.ContactPointUseEnum;
//import ca.uhn.fhir.model.primitive.CodeDt;
//import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import edu.gatech.chai.gtfhir2.config.FhirServerConfig;
import edu.gatech.chai.gtfhir2.mapping.IdMapping;
import edu.gatech.chai.gtfhir2.mapping.OmopOrganization;
import edu.gatech.chai.gtfhir2.mapping.ResourceMapping;
//import edu.gatech.chai.gtfhir2.mapping.OmopForOrganization;
import edu.gatech.chai.gtfhir2.model.MyOrganization;
import edu.gatech.chai.omopv5.jpa.entity.CareSite;
import edu.gatech.chai.omopv5.jpa.service.CareSiteService;

/**
 * This is a simple resource provider which only implements "read/GET" methods, but
 * which uses a custom subclassed resource definition to add statically bound
 * extensions.
 * 
 * See the MyOrganization definition to see how the custom resource 
 * definition works.
 */
public class OrganizationResourceProvider implements IResourceProvider {
//	private CareSiteService careSiteService;
	private WebApplicationContext myAppCtx;
	private String myDbType;
	private OmopOrganization myMapper;
	
	public OrganizationResourceProvider() {
		myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
		myDbType = myAppCtx.getServletContext().getInitParameter("backendDbType");
		if (myDbType.equalsIgnoreCase("omopv5") == true) {
			myMapper = new OmopOrganization(myAppCtx);
		} else {
			myMapper = new OmopOrganization(myAppCtx);
		}
	}
	
	/**
	 * The getResourceType method comes from IResourceProvider, and must be overridden to indicate what type of resource this provider supplies.
	 */
	@Override
	public Class<MyOrganization> getResourceType() {
		return MyOrganization.class;
	}

	/**
	 * The "@Read" annotation indicates that this method supports the read operation. It takes one argument, the Resource type being returned.
	 * 
	 * @param theId
	 *            The read operation takes one parameter, which must be of type IdDt and must be annotated with the "@Read.IdParam" annotation.
	 * @return Returns a resource matching this identifier, or null if none exists.
	 */
	@Read()
	public MyOrganization getResourceById(@IdParam IdType theId) {		
		MyOrganization retVal = (MyOrganization) myMapper.toFHIR(theId);
		if (retVal == null) {
			throw new ResourceNotFoundException(theId);
		}
		
		// Populate the first, primitive extension
		retVal.setBillingCode(new CodeType("00102-1"));
		
		// The second extension is repeatable and takes a block type
		MyOrganization.EmergencyContact contact = new MyOrganization.EmergencyContact();
		contact.setActive(new BooleanType(true));
		contact.setContact(new ContactPoint());
		retVal.getEmergencyContact().add(contact);

		return retVal;		
	}

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
//	private MyOrganization omop2FHIR(Long id) {
//		MyOrganization organization = new MyOrganization();
//		
//		if (careSiteService == null) {
//			System.out.println("myOMOPService is null");
//			return null;
//		}
//		CareSite careSite = (CareSite) careSiteService.findById(id);
//		if (careSite == null) return null;
//		
//		// Actual mapping here.
//		// fhir.id = (idMap) = omop.care_site_id
//		// fhir.name = omop.care_site_name
//		// fhir.type = omop.place_of_service_concept_id
//		// address = location_id
//
//		Table table = CareSite.class.getAnnotation(Table.class);
//		String tableName = table.name();
//		Long fhirId = IdMapping.getFHIRfromOMOP(id, tableName);
//		organization.setId(new IdType (fhirId));
//		
//		if (careSite.getCareSiteName() != null && careSite.getCareSiteName() != "") {
//			organization.setName(careSite.getCareSiteName());
//		}
//		
//		if (careSite.getPlaceOfServiceConcept() != null) {
//			String codeString = careSite.getPlaceOfServiceConcept().getConceptCode();
//			String systemUriString = careSite.getPlaceOfServiceConcept().getVocabulary().getVocabularyReference();
//			String displayString = careSite.getPlaceOfServiceConcept().getName();
//			
//			CodeableConcept typeCodeableConcept = new CodeableConcept().addCoding(new Coding(systemUriString, codeString, displayString));
//			organization.addType(typeCodeableConcept);
//		}
//		
//		if (careSite.getLocation() != null) {
//			organization.addAddress()
//				.setUse(AddressUse.HOME)
//				.addLine(careSite.getLocation().getAddress1())
//				.addLine(careSite.getLocation().getAddress2())//WARNING check if mapping for lines are correct
//				.setCity(careSite.getLocation().getCity())
//				.setPostalCode(careSite.getLocation().getZipCode())
//				.setState(careSite.getLocation().getState());
////				.setPeriod(period);
//		}
//
//		
//		return organization; 
//	}

}
