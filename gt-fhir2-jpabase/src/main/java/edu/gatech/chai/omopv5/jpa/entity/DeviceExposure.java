package edu.gatech.chai.omopv5.jpa.entity;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="device_exposure")
public class DeviceExposure extends BaseEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="device_exposure_id")
	@Access(AccessType.PROPERTY)
	private Long id;

	@ManyToOne
	@JoinColumn(name="person_id", nullable=false)
	private Person person;

	@ManyToOne
	@JoinColumn(name="device_concept_id", nullable=false)
	private Concept deviceConcept;

	@Column(name="device_exposure_start_date", nullable=false)
	private Date deviceExposureStartDate;
	
	@Column(name="device_exposure_end_date")
	private Date deviceExposureEndDate;

	@Column(name="unique_device_id", nullable=false)
	private String uniqueDeviceId;
	
	@ManyToOne
	@JoinColumn(name="device_type_concept_id", nullable=false)
	private Concept deviceTypeConcept;

	@ManyToOne
	@JoinColumn(name="provider_id")
	private Provider provider;
	
	@ManyToOne
	@JoinColumn(name="visit_occurrence_id")
	private VisitOccurrence visitOccurrence;
	
	@ManyToOne
	@JoinColumn(name="device_source_concept_id")
	private Concept deviceSourceConcept;
	
	@Column(name="device_source_value")
	private String deviceSourceValue;
	
	@Column(name="quantity")
	private Integer quqntity;
	
	public Long getId() {
		return id;
	}
	
	public void setId (Long id) {
		this.id = id;
	}

	public Person getPerson () {
		return person;
	}
	
	public void setPerson (Person person) {
		this.person = person;
	}
	
	public Concept getDeviceConcept () {
		return deviceConcept;
	}
	
	public void setDeviceConcept (Concept deviceConcept) {
		this.deviceConcept = deviceConcept;
	}
	
	public Date getDeviceExposureStartDate () {
		return deviceExposureStartDate;
	}
	
	public void setDeviceExposureStartDate (Date deviceExposureStartDate) {
		this.deviceExposureStartDate = deviceExposureStartDate;
	}
	
	public Date getDeviceExposureEndDate () {
		return deviceExposureEndDate;
	}
	
	public void setDeviceExposureEndDate (Date deviceExposureEndDate) {
		this.deviceExposureEndDate = deviceExposureEndDate;
	}
	
	public String getUniqueDeviceId () {
		return uniqueDeviceId;
	}
	
	public void setUniqueDeviceId (String uniqueDeviceId) {
		this.uniqueDeviceId = uniqueDeviceId;
	}
	
	public Concept getDeviceTypeConcept () {
		return deviceTypeConcept;
	}
	
	public void setDeviceTypeConcept (Concept deviceTypeConcept) {
		this.deviceTypeConcept = deviceTypeConcept;
	}
	
	public Provider getProvider () {
		return provider;
	}
	
	public void setProvider (Provider provider) {
		this.provider = provider;
	}
	
	public VisitOccurrence getVisitOccurrence () {
		return visitOccurrence;
	}
	
	public void setVisitOccurrence (VisitOccurrence visitOccurrence) {
		this.visitOccurrence = visitOccurrence;
	}
	
	public Concept getDeviceSourceConcept () {
		return this.deviceSourceConcept;
	}
	
	public void setDeviceSourceConcept (Concept deviceSourceConcept) {
		this.deviceSourceConcept = deviceSourceConcept;
	}
	
	public String getDeviceSourceValue () {
		return this.deviceSourceValue;
	}
	
	public void setDeviceSourceValue (String deviceSourceValue) {
		this.deviceSourceValue = deviceSourceValue;
	}
	
	public Integer getQuantity() {
		return this.quqntity;
	}
	
	public void setQuantity(Integer quantity) {
		this.quqntity = quantity;
	}
	
	@Override
	public Long getIdAsLong() {
		return getId();
	}

}
