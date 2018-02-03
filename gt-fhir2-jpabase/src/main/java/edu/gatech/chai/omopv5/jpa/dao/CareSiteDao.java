package edu.gatech.chai.omopv5.jpa.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import edu.gatech.chai.omopv5.jpa.entity.CareSite;

@Repository
public class CareSiteDao implements IDao<CareSite> {
	@PersistenceContext
	   private EntityManager em;
	
	@Override
	public void add(CareSite baseEntity) {
		em.persist(baseEntity);
	}

	@Override
	public void update(CareSite baseEntity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CareSite findById(Long id) {
		return em.find(CareSite.class, id);
	}

	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub
		
	}
}
