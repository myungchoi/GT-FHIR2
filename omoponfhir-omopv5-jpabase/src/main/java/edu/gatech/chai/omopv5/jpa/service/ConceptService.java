package edu.gatech.chai.omopv5.jpa.service;

import java.util.List;

import edu.gatech.chai.omopv5.jpa.entity.Concept;

public interface ConceptService extends IService<Concept> {
	public List<Concept> getIngredient(Concept concept);
}
