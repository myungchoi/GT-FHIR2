package edu.gatech.chai.omopv5.jpa.dao;

import org.springframework.stereotype.Repository;

import edu.gatech.chai.omopv5.jpa.entity.Concept;

@Repository
public class ConceptDao extends BaseEntityDao<Concept> {

	@Override
	public void add(Concept baseEntity) {
		// Concept table is read-only.
	}

	@Override
	public void merge(Concept baseEntity) {
		// Concept table is read-only.
	}

	@Override
	public void update(Concept baseEntity) {
		// Concept table is read-only.
	}

	@Override
	public Concept findById(Long id) {
		return getEntityManager().find(Concept.class, id);
	}

	@Override
	public void delete(Long id) {
		// Concept table is read-only.
	}

}
