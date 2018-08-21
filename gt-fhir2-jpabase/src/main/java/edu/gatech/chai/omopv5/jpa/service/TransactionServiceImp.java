package edu.gatech.chai.omopv5.jpa.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.gatech.chai.omopv5.jpa.dao.ConditionOccurrenceDao;
import edu.gatech.chai.omopv5.jpa.dao.FPersonDao;
import edu.gatech.chai.omopv5.jpa.dao.FResourcesDao;
import edu.gatech.chai.omopv5.jpa.dao.ProcedureOccurrenceDao;
import edu.gatech.chai.omopv5.jpa.entity.BaseEntity;
import edu.gatech.chai.omopv5.jpa.entity.FPerson;
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
	public int writeTransaction(Map<String, List<BaseEntity>> transactionMap) {
		System.out.println("At the writeTransaction");
		for (String key : transactionMap.keySet()) {
			String[] keyInfo = key.split("\\^");
			System.out.println("working on key="+key+" with keyInfo length="+keyInfo.length);
			if (keyInfo.length != 2) {
				// something is wrong.
				return -1;
			}

			// 2n part of keyInfo should be the entity table name.
			String entityName = keyInfo[1];
			System.out.println("About to process "+entityName+" from Service");
			// list of the entity classes
			try {
			List<BaseEntity> entityClasses = transactionMap.get(key);
			for (BaseEntity entityClass : entityClasses) {
				if (entityName.equals("FPerson")) {
					System.out.println("Adding fPerson to OMOP");
					fPersonDao.add((FPerson) entityClass);
				}
			}
			} catch (Exception ex) {
				ex.printStackTrace();
				return -1;
			}
		}

		return 0;
	}
}