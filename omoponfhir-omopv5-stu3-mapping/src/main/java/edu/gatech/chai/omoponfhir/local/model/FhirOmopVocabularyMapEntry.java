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
