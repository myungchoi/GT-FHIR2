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
 *
 *******************************************************************************/
package edu.gatech.chai.omopv5.model.entity;

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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = "device_exposure_id")
public class DeviceExposure extends BaseEntity {
	public static String tableName = "device_exposure";

	@JsonProperty("device_exposure_id")
	private Long id;

	@JsonProperty("person_id")
	private FPerson fPerson;

	@JsonProperty("device_concept_id")
	private Concept deviceConcept;

	@JsonProperty("device_exposure_start_date")
	private Date deviceExposureStartDate;
	
	@JsonProperty("device_exposure_end_date")
	private Date deviceExposureEndDate;

	@JsonProperty("unique_device_id")
	private String uniqueDeviceId;
	
	@JsonProperty("device_type_concept_id")
	private Concept deviceTypeConcept;

	@JsonProperty("provider_id")
	private Provider provider;
	
	@JsonProperty("visit_occurrence_id")
	private VisitOccurrence visitOccurrence;
	
	@JsonProperty("device_source_concept_id")
	private Concept deviceSourceConcept;
	
	@JsonProperty("device_source_value")
	private String deviceSourceValue;
	
	@JsonProperty("quantity")
	private Integer quantity;
	
	public Long getId() {
		return id;
	}
	
	public void setId (Long id) {
		this.id = id;
	}

	public FPerson getFPerson () {
		return fPerson;
	}
	
	public void setFPerson (FPerson fPerson) {
		this.fPerson = fPerson;
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
		return this.quantity;
	}
	
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	@Override
	public Long getIdAsLong() {
		return getId();
	}

}
