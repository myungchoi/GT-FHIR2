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
package edu.gatech.chai.omoponfhir.local.model;

public class FhirOmopVocabularyMapEntry {
	private String omopConceptCodeName;
	private String fhirUrlSystemName;
	private String otherSystemName;
	
	public FhirOmopVocabularyMapEntry() {
		
	}
	
	public String getOmopConceptCodeName() {
		return this.omopConceptCodeName;
	}
	
	public void setOmopConceptCodeName(String omopConceptCodeName) {
		this.omopConceptCodeName = omopConceptCodeName;
	}
	
	public String getFhirUrlSystemName() {
		return this.fhirUrlSystemName;
	}
	
	public void setFhirUrlSystemName(String fhirUrlSystemName) {
		this.fhirUrlSystemName = fhirUrlSystemName;
	}
	
	public String getOtherSystemName() {
		return this.otherSystemName;
	}
	
	public void setOtherSystemName(String otherSystemName) {
		this.otherSystemName = otherSystemName;
	}
}
