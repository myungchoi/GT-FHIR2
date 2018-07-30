package edu.gatech.chai.omopv5.jpa.entity;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="f_resources")
public class FResources extends BaseEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="fresources_seq_gen")
	@SequenceGenerator(name="fresources_seq_gen", sequenceName="fresources_id_seq", allocationSize=1)
	@Column(name="f_resources_id")
	@Access(AccessType.PROPERTY)
	private Long id;
	
	@Column(name="resource_name")
	private String resourceName;

	@Column(name="searchable")
	private String searchable;
	
	@Column(name="fhir_data")
	private String fhirData;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getResourceName() {
		return resourceName;
	}
	
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	
	public String getSearchable() {
		return searchable;
	}
	
	public void setSearchable(String searchable) {
		this.searchable = searchable;
	}
	
	public String getFhirData() {
		return fhirData;
	}
	
	public void setFhirData(String fhirData) {
		this.fhirData = fhirData;
	}
		
	@Override
	public Long getIdAsLong() {
		return getId();
	}

	// Any FHIR Search Utilities below ---
}
