package edu.gatech.chai.omopv5.jpa.service;

import java.util.List;
import java.util.Map;

import edu.gatech.chai.omopv5.jpa.entity.BaseEntity;

public interface TransactionService {
	public int writeTransaction (Map<String, List<BaseEntity>> transactionMap);
}
