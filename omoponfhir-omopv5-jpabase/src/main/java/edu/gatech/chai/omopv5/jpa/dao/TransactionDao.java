package edu.gatech.chai.omopv5.jpa.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

@Repository
public class TransactionDao {
	@PersistenceContext
	private EntityManager em;

	public EntityManager getEntityManager() {
		return em;
	}
}
