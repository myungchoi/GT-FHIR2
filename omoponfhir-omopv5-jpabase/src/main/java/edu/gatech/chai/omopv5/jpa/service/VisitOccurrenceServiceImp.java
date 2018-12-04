package edu.gatech.chai.omopv5.jpa.service;

import org.springframework.stereotype.Service;

import edu.gatech.chai.omopv5.jpa.dao.VisitOccurrenceDao;
import edu.gatech.chai.omopv5.jpa.entity.VisitOccurrence;

@Service
public class VisitOccurrenceServiceImp extends BaseEntityServiceImp<VisitOccurrence, VisitOccurrenceDao> implements VisitOccurrenceService {

	public VisitOccurrenceServiceImp() {
		super(VisitOccurrence.class);
	}
}
