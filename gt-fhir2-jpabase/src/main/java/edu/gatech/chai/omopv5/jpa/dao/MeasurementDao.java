package edu.gatech.chai.omopv5.jpa.dao;

import org.springframework.stereotype.Repository;

import edu.gatech.chai.omopv5.jpa.entity.Measurement;

@Repository
public class MeasurementDao extends BaseEntityDao<Measurement> {

	@Override
	public void add(Measurement baseEntity) {
		getEntityManager().persist(baseEntity);
	}

	@Override
	public void merge(Measurement baseEntity) {
		getEntityManager().merge(baseEntity);
	}

	@Override
	public void update(Measurement baseEntity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Measurement findById(Long id) {
		return getEntityManager().find(Measurement.class, id);
	}

	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub
		
	}

}
