package edu.gatech.chai.omopv5.jpa.entity;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

public class ConditionOccurrence extends BaseEntity{
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="condition_occurrence_id_seq")
	@SequenceGenerator(name="condition_occurrence_id_seq", sequenceName="condition_occurrence_id_seq", allocationSize=1)
	@Column(name="person_id", nullable = false)
	@Access(AccessType.PROPERTY)
	private Long id;
	
	@ManyToOne(cascade={CascadeType.MERGE})
	@JoinColumn(name="person_id", nullable= false)
	private FPerson fPerson;
	
	@Column(name="condtion_concept_id", nullable=false)
	private Concept conceptId;
	
	@Column(name="condition_start_date")
	private Date startDate;
	
	@Column(name="day_of_birth")
	private Integer dayOfBirth;
	
	@Column(name="time_of_birth")
	private String timeOfBirth;
	
	@ManyToOne
	@JoinColumn(name="race_concept_id")
	private Concept raceConcept;
	
	@ManyToOne
	@JoinColumn(name="ethnicity_concept_id")
	private Concept ethnicityConcept;
	
	@ManyToOne(cascade={CascadeType.ALL})
	@JoinColumn(name="location_id")
	private Location location;
	
	@ManyToOne(cascade={CascadeType.ALL})
	@JoinColumn(name="provider_id")
	private Provider provider;

	@ManyToOne(cascade={CascadeType.MERGE})
	@JoinColumn(name="care_site_id")
	private CareSite careSite;
	
	@Column(name="person_source_value")
	private String personSourceValue;
	
	@Column(name="gender_source_value")
	private String genderSourceValue;
	
	@ManyToOne
	@JoinColumn(name="gender_source_concept_id")
	private Concept genderSourceConcept;
	
	@Column(name="race_source_value")
	private String raceSourceValue;
	
	@ManyToOne
	@JoinColumn(name="race_source_concept_id")
	private Concept raceSourceConcept;

	@Column(name="ethnicity_source_value")
	private String ethnicitySourceValue;
	
	@ManyToOne
	@JoinColumn(name="ethnicity_source_concept_id")
	private Concept ethnicitySourceConcept;

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public FPerson getFPerson() {
		return this.fPerson;
	}

	public void setFPerson(FPerson fPerson) {
		this.fPerson = fPerson;
	}
	
	@Override
	public Long getIdAsLong() {
		return getId();
	}
}
