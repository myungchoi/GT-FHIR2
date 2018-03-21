package edu.gatech.chai.omopv5.jpa.service;

import edu.gatech.chai.omopv5.jpa.entity.Concept;

public interface ConceptService extends IService<Concept> {
	public Concept getIngredient(Concept concept);
}
