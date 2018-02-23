package edu.gatech.chai.omopv5.jpa.dao;

import org.springframework.stereotype.Repository;

import edu.gatech.chai.omopv5.jpa.entity.VisitOccurrence;

@Repository
public class VisitOccurrenceDao extends BaseEntityDao<VisitOccurrence> {

	@Override
	public void add(VisitOccurrence baseEntity) {
		getEntityManager().persist(baseEntity);
	}

	@Override
	public void merge(VisitOccurrence baseEntity) {
		getEntityManager().merge(baseEntity);
	}

	@Override
	public void update(VisitOccurrence baseEntity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public VisitOccurrence findById(Long id) {
		return getEntityManager().find(VisitOccurrence.class, id);
	}

	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub
		
	}

}
