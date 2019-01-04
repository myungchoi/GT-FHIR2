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

import java.util.List;

import org.hl7.fhir.dstu3.model.Patient;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import edu.gatech.chai.omoponfhir.omopv5.stu3.model.MyOrganization.EmergencyContact;

@ResourceDef(name="Patient", profile="http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient")
public class USCorePatient extends Patient {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Description(shortDefinition="Contains emergency contact details")
	@Extension(url = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-race", isModifier = false, definedLocally = true)
	@Child(name = "race", min=0, max=Child.MAX_UNLIMITED)
	private List<EmergencyContact> myEmergencyContact;

}
