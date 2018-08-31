package edu.gatech.chai.omopv5.jpa.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.springframework.stereotype.Repository;

import edu.gatech.chai.omopv5.jpa.entity.FPerson;

@Repository
public class FPersonDao extends BaseEntityDao<FPerson> {
	@PersistenceContext(type=PersistenceContextType.EXTENDED)
	private EntityManager em;
}
