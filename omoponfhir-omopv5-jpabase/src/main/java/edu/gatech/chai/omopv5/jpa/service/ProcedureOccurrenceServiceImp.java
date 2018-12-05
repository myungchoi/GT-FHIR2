package edu.gatech.chai.omopv5.jpa.service;

import org.springframework.stereotype.Service;

import edu.gatech.chai.omopv5.jpa.dao.ProcedureOccurrenceDao;
import edu.gatech.chai.omopv5.jpa.entity.ProcedureOccurrence;

@Service
public class ProcedureOccurrenceServiceImp extends BaseEntityServiceImp<ProcedureOccurrence, ProcedureOccurrenceDao>
		implements ProcedureOccurrenceService {

	public ProcedureOccurrenceServiceImp() {
		super(ProcedureOccurrence.class);
	}

}
