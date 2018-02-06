package edu.gatech.chai.omopv5.jpa.dao;

import org.springframework.stereotype.Repository;

import edu.gatech.chai.omopv5.jpa.entity.Provider;

@Repository
public class ProviderDao extends BaseEntityDao<Provider> {

	@Override
	public void add(Provider baseEntity) {
		getEntityManager().persist(baseEntity);
	}

	@Override
	public void update(Provider baseEntity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Provider findById(Long id) {
		return getEntityManager().find(Provider.class, id);
	}

	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void merge(Provider baseEntity) {
		getEntityManager().merge(baseEntity);
	}

}
