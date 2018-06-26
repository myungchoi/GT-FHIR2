package edu.gatech.chai.gtfhir2.mapping;

import org.hl7.fhir.exceptions.FHIRException;

/***
 * 
 * @author mc142
 *
 * URI information for coding system is obtained from 
 *   https://www.hl7.org/fhir/terminologies-systems.html
 * 
 * 
 */
public enum OmopCodeableConceptMapping {
	ATC("http://www.whocc.no/atc", "ATC"),
	CPT("http://www.ama-assn.org/go/cpt", "CPT4"),
	HCPCS("2.16.840.1.113883.6.14", "HCPCS"),
	ICD9CM("http://hl7.org/fhir/sid/icd-9-cm", "ICD9CM"),
	ICD9PROC("http://hl7.org/fhir/sid/icd-9-proc", "ICD9Proc"),
	ICD10("http://hl7.org/fhir/sid/icd-10", "ICD10"),
	ICD10CM("http://hl7.org/fhir/sid/icd-10-cm", "ICD10CM"),
	LOINC("http://loinc.org", "LOINC"),
	NDC("http://hl7.org/fhir/sid/ndc", "NDC"),
	NDFRT("http://hl7.org/fhir/ndfrt", "NDFRT"),
	RXNORM("http://www.nlm.nih.gov/research/umls/rxnorm", "RxNorm"),
	SCT("http://snomed.info/sct", "SNOMED"),
	UCUM("http://unitsofmeasure.org", "UCUM");
	
	
	public static String omopVocabularyforFhirUri(String fhirUri) throws FHIRException {
		if (fhirUri == null || fhirUri.isEmpty()) {
			throw new FHIRException("FHIR URI cannot be null or empty: '"+fhirUri+"'");
		}

		if ("http://www.whocc.no/atc".equals(fhirUri)) 
			return "ATC";
		if ("http://www.ama-assn.org/go/cpt".equals(fhirUri))
			return "CPT4";
		if ("2.16.840.1.113883.6.14".equals(fhirUri))
			return "HCPCS";
		if ("http://hl7.org/fhir/sid/icd-9-cm".equals(fhirUri))
			return "ICD9CM";
		if ("http://hl7.org/fhir/sid/icd-9-proc".equals(fhirUri))
			return "ICD9Proc";
		if ("http://hl7.org/fhir/sid/icd-10".equals(fhirUri))
			return "ICD10";
		if ("http://hl7.org/fhir/sid/icd-10-cm".equals(fhirUri))
			return "ICD10CM";
		if ("http://loinc.org".equals(fhirUri))
			return "LOINC";
		if ("http://hl7.org/fhir/sid/ndc".equals(fhirUri))
			return "NDC";
		if ("http://hl7.org/fhir/ndfrt".equals(fhirUri))
			return "NDFRT";
		if ("http://www.nlm.nih.gov/research/umls/rxnorm".equals(fhirUri))
			return "RxNorm";
		if ("http://snomed.info/sct".equals(fhirUri))
			return "SNOMED";
		if ("http://unitsofmeasure.org".equals(fhirUri))
			return "UCUM";
		
		return "None";
		
	}

	public static String fhirUriforOmopVocabulary(String omopVocabulary) throws FHIRException {
		if (omopVocabulary == null || omopVocabulary.isEmpty()) {
			throw new FHIRException("Omop Vocabulary ID cannot be null or empty: '"+omopVocabulary+"'");
		}

		if ("ATC".equals(omopVocabulary)) 
			return "http://www.whocc.no/atc";
		if ("CPT4".equals(omopVocabulary))
			return "http://www.ama-assn.org/go/cpt";
		if ("HCPCS".equals(omopVocabulary))
			return "2.16.840.1.113883.6.14";
		if ("ICD9CM".equals(omopVocabulary))
			return "http://hl7.org/fhir/sid/icd-9-cm";
		if ("ICD9Proc".equals(omopVocabulary))
			return "http://hl7.org/fhir/sid/icd-9-proc";
		if ("ICD10".equals(omopVocabulary))
			return "http://hl7.org/fhir/sid/icd-10";
		if ("ICD10CM".equals(omopVocabulary))
			return "http://hl7.org/fhir/sid/icd-10-cm";
		if ("LOINC".equals(omopVocabulary))
			return "http://loinc.org";
		if ("NDC".equals(omopVocabulary))
			return "http://hl7.org/fhir/sid/ndc";
		if ("NDFRT".equals(omopVocabulary))
			return "http://hl7.org/fhir/ndfrt";
		if ("RxNorm".equals(omopVocabulary))
			return "http://www.nlm.nih.gov/research/umls/rxnorm";
		if ("SNOMED".equals(omopVocabulary))
			return "http://snomed.info/sct";
		if ("UCUM".equals(omopVocabulary))
			return "http://unitsofmeasure.org";
		
		return "None";
		
	}

	String fhirUri;
	String omopVocabulary;
	
	OmopCodeableConceptMapping(String fhirUri, String omopVocabulary) {
		this.fhirUri = fhirUri;
		this.omopVocabulary = omopVocabulary;
	}
	
	public String getFhirUri() {
		return fhirUri;
	}
	
	public void setFhirUri(String fhirUri) {
		this.fhirUri = fhirUri;
	}
	
	public String getOmopVocabulary() {
		return omopVocabulary;
	}
	
	public void setOmopVocabulary(String omopVocabulary) {
		this.omopVocabulary = omopVocabulary;
	}
}
