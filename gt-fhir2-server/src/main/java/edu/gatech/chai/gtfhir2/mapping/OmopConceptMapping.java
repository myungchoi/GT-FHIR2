package edu.gatech.chai.gtfhir2.mapping;

import org.hl7.fhir.dstu3.model.codesystems.AdministrativeGender;
import org.hl7.fhir.exceptions.FHIRException;

public enum OmopConceptMapping {
	
	/**
	 * AdministrativeGender Mapping to OMOP Gender Concept ID.
	 */
	MALE(AdministrativeGender.MALE.toCode(), 8507L),
	FEMALE(AdministrativeGender.FEMALE.toCode(), 8532L),
	UNKNOWN(AdministrativeGender.UNKNOWN.toCode(), 8551L),
	OTHER(AdministrativeGender.OTHER.toCode(), 8521L),
	NULL(AdministrativeGender.NULL.toCode(),8570L);
	
	
	public static Long omopForAdministrativeGenderCode(String administrativeGenderCode) throws FHIRException {
		if (administrativeGenderCode == null || administrativeGenderCode.isEmpty()) {
			throw new FHIRException("Unknow Administrative Gender code: '"+administrativeGenderCode+"'");
		}
		
		if ("male".equals(administrativeGenderCode)) {
			return 8507L; // MALE in OMOP
		}
		if ("female".equals(administrativeGenderCode)) {
			return 8532L; // FEMALE in OMOP
		}
		if ("unknown".equals(administrativeGenderCode)) {
			return 8551L;
		}
		if ("other".equals(administrativeGenderCode)) {
			return 8521L;
		} else {
			return 8570L;
		}
	}
	
	String fhirCode;
	Long omopConceptId;

	OmopConceptMapping(String fhirCode, Long omopConceptId) {
		this.fhirCode = fhirCode;
		this.omopConceptId = omopConceptId;
	}

	public Long getOmopConceptId() {
		return omopConceptId;
	}

	public void setOmopConceptId(Long omopConceptId) {
		this.omopConceptId = omopConceptId;
	}

	public String getFhirCode() {
		return fhirCode;
	}

	public void setFhirCode(String fhirCode) {
		this.fhirCode = fhirCode;
	}
}
