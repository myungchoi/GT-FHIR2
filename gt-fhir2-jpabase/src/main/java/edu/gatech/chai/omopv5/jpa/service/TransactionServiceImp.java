package edu.gatech.chai.omopv5.jpa.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.gatech.chai.omopv5.jpa.dao.ConditionOccurrenceDao;
import edu.gatech.chai.omopv5.jpa.dao.FPersonDao;
import edu.gatech.chai.omopv5.jpa.dao.FResourcesDao;
import edu.gatech.chai.omopv5.jpa.dao.ProcedureOccurrenceDao;
import edu.gatech.chai.omopv5.jpa.entity.FResources;

@Service
public class TransactionServiceImp implements TransactionService {

	@Autowired
	private FPersonDao fPersonDao;
	@Autowired
	private ConditionOccurrenceDao conditionDao;
	@Autowired
	private ProcedureOccurrenceDao procedureDao;

	@Transactional
	public void performTransaction (Map<Object, Object> transactionMap) {
		
	}
}
