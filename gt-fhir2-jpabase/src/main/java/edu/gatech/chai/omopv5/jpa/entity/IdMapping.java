package edu.gatech.chai.omopv5.jpa.entity;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="id_mapping")
public class IdMapping {

	@Id
	@Column(name="id")
	@Access(AccessType.PROPERTY)
	private Long id;

	@Column(name="fhir_id")
	private Long fhirId;
	
	@Column(name="omop_id")
	private Long omopId;
	
	@Column(name="related_resource")
	private String relatedResource;
	
	public IdMapping() {}
	
	public Long getId() {
		return this.id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getFhirId() {
		return this.fhirId;
	}
	
	public void setFhirId(Long fhirId) {
		this.fhirId = fhirId;
	}
	
	public Long getOmopId() {
		return this.omopId;
	}
	
	public void setOmopId(Long omopId) {
		this.omopId = omopId;
	}
	
	public String getRelatedResource() {
		return this.relatedResource;
	}
	
	public void setRelatedResource(String relatedResource) {
		this.relatedResource = relatedResource;
	}
}
