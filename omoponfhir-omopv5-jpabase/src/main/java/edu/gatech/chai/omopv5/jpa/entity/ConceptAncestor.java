package edu.gatech.chai.omopv5.jpa.entity;

import java.io.Serializable;

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
@Table(name="concept_ancestor")
@Inheritance(strategy=InheritanceType.JOINED)
public class ConceptAncestor extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@ManyToOne
	@JoinColumn(name="ancestor_concept_id")
	private Concept ancestorConcept;

	@ManyToOne
	@JoinColumn(name="descendant_concept_id")
	private Concept descendantConcept;

	@Column(name="min_levels_of_separation")
	private Integer minLevelsOfSeparation;
	
	@Column(name="max_levels_of_separation")
	private Integer maxLevelsOfSeparation;
	
	public Concept getAncestorConcept() {
		return this.ancestorConcept;
	}
	
	public Concept getDescendantConcept() {
		return this.descendantConcept;
	}
	
	public Integer getMinLevelsOfSeparation() {
		return this.minLevelsOfSeparation;
	}
	
	public Integer getMaxLevelsOfSeparation() {
		return this.maxLevelsOfSeparation;
	}
	
	@Override
	public Long getIdAsLong() {
		// TODO Auto-generated method stub
		return null;
	}

}
