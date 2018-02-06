package edu.gatech.chai.omopv5.jpa.dao;

import org.springframework.stereotype.Repository;

import edu.gatech.chai.omopv5.jpa.entity.FPerson;

@Repository
public class FPersonDao extends BaseEntityDao<FPerson> {
	@Override
	public void add(FPerson baseEntity) {
		getEntityManager().persist(baseEntity);
	}

	@Override
	public void update(FPerson baseEntity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FPerson findById(Long id) {
		return getEntityManager().find(FPerson.class, id);
	}

	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void merge(FPerson baseEntity) {
		getEntityManager().merge(baseEntity);
	}

}
