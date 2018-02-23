package edu.gatech.chai.omopv5.jpa.dao;

import org.springframework.stereotype.Repository;

import edu.gatech.chai.omopv5.jpa.entity.FObservationView;

@Repository
public class FObservationViewDao extends BaseEntityDao<FObservationView> {

	@Override
	public void add(FObservationView baseEntity) {
		getEntityManager().persist(baseEntity);
	}

	@Override
	public void merge(FObservationView baseEntity) {
		getEntityManager().merge(baseEntity);
	}

	@Override
	public void update(FObservationView baseEntity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FObservationView findById(Long id) {
		return getEntityManager().find(FObservationView.class, id);
	}

	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub
		
	}

}
