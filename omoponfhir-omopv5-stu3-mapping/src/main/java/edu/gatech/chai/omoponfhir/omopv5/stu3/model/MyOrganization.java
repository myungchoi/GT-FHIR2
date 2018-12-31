/*******************************************************************************
 * Copyright (c) 2019 Georgia Tech Research Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package edu.gatech.chai.omoponfhir.omopv5.stu3.model;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Organization;

import ca.uhn.fhir.model.api.BaseIdentifiableElement;
import ca.uhn.fhir.model.api.IElement;
import ca.uhn.fhir.model.api.IExtension;
import ca.uhn.fhir.model.api.annotation.Block;
import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
//import ca.uhn.fhir.model.dstu2.composite.ContactPointDt;
//import ca.uhn.fhir.model.dstu2.resource.Organization;
//import ca.uhn.fhir.model.primitive.BooleanDt;
//import ca.uhn.fhir.model.primitive.CodeDt;
import ca.uhn.fhir.util.ElementUtil;

/**
 * This is an example of a customized model class. Essentially we have taken the
 * built-in Organization resource class, and extended with a custom extension.
 */
@ResourceDef(name = "Organization")
public class MyOrganization extends Organization {

	/* *****************************
	 * Fields
	 * *****************************/

	/**
	 * This is a basic extension, with a DataType value (in this case, String)
	 */
	@Description(shortDefinition = "Contains a simple code indicating the billing code for this organization")
	@Extension(url = "http://foo#billingCode", isModifier = false, definedLocally = true)
	@Child(name = "billingCode")
	private CodeType myBillingCode;

	/**
	 * This is a composite extension, containing further extensions instead of
	 * a value. The class "EmergencyContact" is defined at the bottom
	 * of this file.
	 */
	@Description(shortDefinition="Contains emergency contact details")
	@Extension(url = "http://foo#emergencyContact", isModifier = false, definedLocally = true)
	@Child(name = "emergencyContact", min=0, max=Child.MAX_UNLIMITED)
	private List<EmergencyContact> myEmergencyContact;
	
	/* *****************************
	 * Getters and setters
	 * *****************************/

	public List<EmergencyContact> getEmergencyContact() {
		if (myEmergencyContact==null) {
			myEmergencyContact=new ArrayList<EmergencyContact>();
		}
		return myEmergencyContact;
	}

	public void setEmergencyContact(List<EmergencyContact> theEmergencyContact) {
		myEmergencyContact = theEmergencyContact;
	}

	public CodeType getBillingCode() {
		if (myBillingCode == null) {
			myBillingCode = new CodeType();
		}
		return myBillingCode;
	}

	public void setBillingCode(CodeType theBillingCode) {
		myBillingCode = theBillingCode;
	}

	/* *****************************
	 * Boilerplate methods- Hopefully these will be removed or made optional
	 * in a future version of HAPI but for now they need to be added to all block
	 * types. These two methods follow a simple pattern where a utility method from
	 * ElementUtil is called and all fields are passed in.
	 * *****************************/
	
//	@Override
//	public <T extends IElement> List<T> getAllPopulatedChildElementsOfType(Class<T> theType) {
//		return ElementUtil.allPopulatedChildElements(theType, super.getAllPopulatedChildElementsOfType(theType), myBillingCode, myEmergencyContact);
//	}

	@Override
	public boolean isEmpty() {
        return super.isEmpty() && ElementUtil.isEmpty(myBillingCode, myEmergencyContact);
	}

	/**
	 * This "block definition" defines an extension type with multiple child extensions.
	 * It is referenced by the field myEmergencyContact above.
	 */
	@Block
	public static class EmergencyContact extends BaseIdentifiableElement implements IExtension
	{
		/* *****************************
		 * Fields
		 * *****************************/	
		
		/**
		 * This is a primitive datatype extension
		 */
		@Description(shortDefinition = "Should be set to true if the contact is active")
		@Extension(url = "http://foo#emergencyContactActive", isModifier = false, definedLocally = true)
		@Child(name = "active")
		private BooleanType myActive;
				
		/**
		 * This is a composite datatype extension
		 */
		@Description(shortDefinition = "Contains the actual contact details")
		@Extension(url = "http://foo#emergencyContactContact", isModifier = false, definedLocally = true)
		@Child(name = "contact")
		private ContactPoint myContact;

		/* *****************************
		 * Getters and setters
		 * *****************************/

		public BooleanType getActive() {
			if (myActive == null) {
				myActive = new BooleanType();
			}
			return myActive;
		}

		public void setActive(BooleanType theActive) {
			myActive = theActive;
		}

		public ContactPoint getContact() {
			if (myContact == null) {
				myContact = new ContactPoint();
			}
			return myContact;
		}

		public void setContact(ContactPoint theContact) {
			myContact = theContact;
		}

		/* *****************************
		 * Boilerplate methods- Hopefully these will be removed or made optional
		 * in a future version of HAPI but for now they need to be added to all block
		 * types. These two methods follow a simple pattern where a utility method from
		 * ElementUtil is called and all fields are passed in.
		 * *****************************/
		
		@Override
		public <T extends IElement> List<T> getAllPopulatedChildElementsOfType(Class<T> theType) {
			return ElementUtil.allPopulatedChildElements(theType, myActive, myContact);
		}

		@Override
		public boolean isEmpty() {
			return ElementUtil.isEmpty(myActive, myContact);
		}

		
	}
	
}
