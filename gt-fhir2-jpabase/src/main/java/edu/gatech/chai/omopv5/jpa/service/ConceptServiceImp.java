package edu.gatech.chai.omopv5.jpa.service;

import org.springframework.stereotype.Service;

import edu.gatech.chai.omopv5.jpa.dao.ConceptDao;
import edu.gatech.chai.omopv5.jpa.entity.Concept;

@Service
public class ConceptServiceImp extends BaseEntityServiceImp<Concept, ConceptDao> implements ConceptService {

}
