package edu.gatech.chai.gtfhir2.mapping;

import org.hl7.fhir.dstu3.model.codesystems.AdministrativeGender;
import org.hl7.fhir.dstu3.model.codesystems.OrganizationType;
import org.hl7.fhir.exceptions.FHIRException;

public enum OmopConceptMapping {
	
	/**
	 * AdministrativeGender Mapping to OMOP Gender Concept ID.
	 */
	MALE(AdministrativeGender.MALE.toCode(), 8507L),
	FEMALE(AdministrativeGender.FEMALE.toCode(), 8532L),
	UNKNOWN(AdministrativeGender.UNKNOWN.toCode(), 8551L),
	OTHER(AdministrativeGender.OTHER.toCode(), 8521L),
	NULL(AdministrativeGender.NULL.toCode(),8570L),
	/*
	 * OrganizationType Mapping
	 */
	PROV(OrganizationType.PROV.toCode(), 4107295L), 
	DEPT(OrganizationType.DEPT.toCode(), 4318944L), 
	TEAM(OrganizationType.TEAM.toCode(), 4217012L), 
	GOVT(OrganizationType.GOVT.toCode(), 4195901L), 
	INS(OrganizationType.INS.toCode(), 8844L), 
	EDU(OrganizationType.EDU.toCode(), 4030303L), 
	RELI(OrganizationType.RELI.toCode(), 8844L), 
	CRS(OrganizationType.CRS.toCode(), 8844L), 
	CG(OrganizationType.CG.toCode(), 4127377L), 
	BUS(OrganizationType.BUS.toCode(), 8844L), 
	ORG_OTHER(OrganizationType.OTHER.toCode(), 8844L), 
	ORG_NULL(OrganizationType.NULL.toCode(), 8844L);
	
	
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
	
	public static Long omopForOrganizationTypeCode(String organizationTypeCode) throws FHIRException {
		if (organizationTypeCode == null || organizationTypeCode.isEmpty()) {
			throw new FHIRException("Unknow Organization Type code: '"+organizationTypeCode+"'");
		}
		
		if ("prov".equals(organizationTypeCode)) {
			return 4107295L;
		}
		if ("dept".equals(organizationTypeCode)) {
			return 4318944L;
		}
		if ("team".equals(organizationTypeCode)) {
			return 4217012L;
		}
		if ("govt".equals(organizationTypeCode)) {
			return 4195901L;
		}
		if ("ins".equals(organizationTypeCode)) {
			return 8844L; // Other place of service... can't find right one.
		}
		if ("edu".equals(organizationTypeCode)) {
			return 4030303L;
		}
		if ("reli".equals(organizationTypeCode)) {
			return 8844L;
		}
		if ("crs".equals(organizationTypeCode)) {
			return 8844L;
		}
		if ("cg".equals(organizationTypeCode)) {
			return 4127377L;
		}
		if ("bus".equals(organizationTypeCode)) {
			return 8844L;
		}
		if ("other".equals(organizationTypeCode)) {
			return 8844L;
		} else {
			return 8844L;
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
