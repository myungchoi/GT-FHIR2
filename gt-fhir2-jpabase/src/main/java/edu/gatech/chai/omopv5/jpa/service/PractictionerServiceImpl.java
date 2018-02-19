package edu.gatech.chai.omopv5.jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import edu.gatech.chai.omopv5.jpa.dao.FPersonDao;
import edu.gatech.chai.omopv5.jpa.dao.ProviderDao;
import edu.gatech.chai.omopv5.jpa.entity.FPerson;
import edu.gatech.chai.omopv5.jpa.entity.Location;
import edu.gatech.chai.omopv5.jpa.entity.Provider;

public class PractictionerServiceImpl implements PractictionerService {
	
	@Autowired
	private ProviderDao providerDao;
	
	@Transactional(readOnly = true)
	public Provider findById(Long id) {
		return providerDao.findById(id);
	}

	@Transactional(readOnly = true)
	public ProviderDao getProviderDao() {
		return this.providerDao;
	}

	@Transactional(readOnly = true)
	@Override
	public Provider searchByColumnString(String column, String value) {
		EntityManager em = providerDao.getEntityManager();
		
		String query = "SELECT t FROM Provider t WHERE "+column+" like :value";
		List<? extends Provider> results = em.createQuery(query, Provider.class)
				.setParameter("value",  value).getResultList();
		if (results.size() > 0)
			return results.get(0);
		else
			return null;	
	}

	@Transactional
	@Override
	public Provider createOrUpdate(Provider entity) {
		if (entity.getId() != null) {
			providerDao.merge(entity);
		} else {
			providerDao.add(entity);
		}
		return entity;
	}
}