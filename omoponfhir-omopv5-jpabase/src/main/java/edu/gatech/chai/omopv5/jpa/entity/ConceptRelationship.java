package edu.gatech.chai.omopv5.jpa.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import net.jcip.annotations.Immutable;

@Entity
@Immutable
@Table(name="concept_relationship")
@Inheritance(strategy=InheritanceType.JOINED)
public class ConceptRelationship extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@ManyToOne
	@JoinColumn(name="concept_id_1")
	private Concept conceptId1;
	
	@ManyToOne
	@JoinColumn(name="concept_id_2")
	private Concept conceptId2;
	
	@Column(name="relationship_id")
	private String relationshipId;
	
	@Column(name="valid_start_date")
	private Date validStartDate;
	
	@Column(name="valid_end_date")
	private Date validEndDate;
	
	@Column(name="invalid_reason")
	private String invalidReason;

	@Override
	public Long getIdAsLong() {
		// TODO Auto-generated method stub
		return null;
	}

}
