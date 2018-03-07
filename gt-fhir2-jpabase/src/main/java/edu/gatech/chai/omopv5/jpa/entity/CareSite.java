package edu.gatech.chai.omopv5.jpa.entity;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name="care_site")
//@NamedQueries(value={
//		@NamedQuery(name="loadCareSiteReference", query="select c.id from CareSite c WHERE c.organization.id = :serviceProvider AND c.location.id = :location")
//})
public class CareSite extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="caresite_seq_gen")
	@SequenceGenerator(name="caresite_seq_gen", sequenceName="caresite_id_seq", allocationSize=1)
	@Column(name="care_site_id")
	@Access(AccessType.PROPERTY)
	private Long id;
	
	@ManyToOne(cascade={CascadeType.ALL})
	@JoinColumn(name="location_id")
	private Location location;
	
	@ManyToOne(cascade={CascadeType.MERGE})
	@JoinColumn(name="place_of_service_concept_id")
	private Concept placeOfServiceConcept;
	
	@Column(name="care_site_name")
	private String careSiteName;
	
	@Column(name="care_site_source_value")
	private String careSiteSourceValue;
	
	@Column(name="place_of_service_source_value")
	private String placeOfServiceSourceValue;
	
	public CareSite() {
		super();
	}
	
	public CareSite(Long id, Location location, Concept placeOfServiceConcept, String careSiteName, String careSiteSourceValue, String placeOfServiceSourceValue) {
		super();
		
		this.id = id;
		this.location = location;
		this.placeOfServiceConcept = placeOfServiceConcept;
		this.careSiteName = careSiteName;
		this.careSiteSourceValue = careSiteSourceValue;
		this.placeOfServiceSourceValue = placeOfServiceSourceValue;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public Concept getPlaceOfServiceConcept() {
		return placeOfServiceConcept;
	}
	
	public void setPlaceOfServiceConcept(Concept placeOfServiceConcept) {
		this.placeOfServiceConcept = placeOfServiceConcept;
	}
	
	public String getCareSiteName() {
		return careSiteName;
	}
	
	public void setCareSiteName(String careSiteName) {
		this.careSiteName = careSiteName;
	}
	
	public String getCareSiteSourceValue() {
		return careSiteSourceValue;
	}
	
	public void setCareSiteSourceValue(String careSiteSourceValue) {
		this.careSiteSourceValue = careSiteSourceValue;
	}
	
	public String getPlaceOfServiceSourceValue() {
		return placeOfServiceSourceValue;
	}
	
	public void setPlaceOfServiceSourceValue(String placeOfServiceSourceValue) {
		this.placeOfServiceSourceValue = placeOfServiceSourceValue;
	}

	@Override
	public Long getIdAsLong() {
		return getId();
	}
	
}
