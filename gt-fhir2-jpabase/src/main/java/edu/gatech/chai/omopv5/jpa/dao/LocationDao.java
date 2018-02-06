package edu.gatech.chai.omopv5.jpa.dao;

import org.springframework.stereotype.Repository;

import edu.gatech.chai.omopv5.jpa.entity.Location;

@Repository
public class LocationDao extends BaseEntityDao<Location> {
	@Override
	public void add(Location baseEntity) {
		getEntityManager().persist(baseEntity);
	}

	@Override
	public void update(Location baseEntity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Location findById(Long id) {
		return getEntityManager().find(Location.class, id);
	}

	@Override
	public void merge(Location baseEntity) {
		getEntityManager().merge(baseEntity);
	}
	
}
