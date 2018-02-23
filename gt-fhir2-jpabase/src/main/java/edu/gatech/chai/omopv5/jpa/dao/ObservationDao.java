package edu.gatech.chai.omopv5.jpa.dao;

import org.springframework.stereotype.Repository;

import edu.gatech.chai.omopv5.jpa.entity.Observation;

@Repository
public class ObservationDao extends BaseEntityDao<Observation> {

	@Override
	public void add(Observation baseEntity) {
		getEntityManager().persist(baseEntity);
	}

	@Override
	public void merge(Observation baseEntity) {
		getEntityManager().merge(baseEntity);
	}

	@Override
	public void update(Observation baseEntity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Observation findById(Long id) {
		return getEntityManager().find(Observation.class, id);
	}

	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub
		
	}

}
