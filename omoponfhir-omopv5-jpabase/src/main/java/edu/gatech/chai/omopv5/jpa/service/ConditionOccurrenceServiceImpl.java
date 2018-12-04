package edu.gatech.chai.omopv5.jpa.service;

import edu.gatech.chai.omopv5.jpa.dao.ConditionOccurrenceDao;
import edu.gatech.chai.omopv5.jpa.entity.ConditionOccurrence;
import org.springframework.stereotype.Service;

@Service
public class ConditionOccurrenceServiceImpl extends BaseEntityServiceImp<ConditionOccurrence, ConditionOccurrenceDao>
        implements ConditionOccurrenceService{
    public ConditionOccurrenceServiceImpl() {
        super(ConditionOccurrence.class);
    }
}