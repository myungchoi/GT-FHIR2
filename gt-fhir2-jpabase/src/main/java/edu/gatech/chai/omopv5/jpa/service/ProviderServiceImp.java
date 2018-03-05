package edu.gatech.chai.omopv5.jpa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.gatech.chai.omopv5.jpa.dao.ProviderDao;
import edu.gatech.chai.omopv5.jpa.entity.FPerson;
import edu.gatech.chai.omopv5.jpa.entity.Provider;

@Service
public class ProviderServiceImp implements ProviderService {

	@Autowired
	private ProviderDao providerDao;

	@Transactional(readOnly = true)
	@Override
	public Provider findById(Long id) {
		return providerDao.findById(id);
	}

	@Transactional(readOnly = true)
	@Override
	public List<Provider> searchByColumnString(String column, String value) {
		EntityManager em = providerDao.getEntityManager();
		
		String query = "SELECT t FROM Provider t WHERE "+column+" like :value";
		List<Provider> results = em.createQuery(query, Provider.class)
				.setParameter("value",  value).getResultList();
		if (results.size() > 0)
			return results;
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

	@Transactional(readOnly = true)
	@Override
	public Long getSize() {
		EntityManager em = providerDao.getEntityManager();
		
		String query = "SELECT COUNT(t) FROM Provider t";
		Long totalSize = em.createQuery(query, Long.class).getSingleResult();
		return totalSize;
	}

	@Transactional(readOnly = true)
	@Override
	public Long getSize(Map<String, List<ParameterWrapper>> paramMap) {
		// Construct predicate from this map.
		EntityManager em = providerDao.getEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Long> providerQuery = builder.createQuery(Long.class);
		Root<Provider> fPersonRoot = providerQuery.from(Provider.class);

		List<Predicate> predicates = ParameterWrapper.constructPredicate(builder, paramMap, fPersonRoot);		
		if (predicates == null || predicates.isEmpty()) return 0L;

		providerQuery.select(builder.count(fPersonRoot));
		providerQuery.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));
		
		return em.createQuery(providerQuery).getSingleResult();
	}

	@Override
	public List<Provider> searchWithoutParams(int fromIndex, int toIndex) {
		int length = toIndex - fromIndex;
		EntityManager em = providerDao.getEntityManager();
		
		String query = "SELECT p FROM Provider p ORDER BY id ASC";
		List<Provider> retvals = (List<Provider>) em.createQuery(query, Provider.class)
				.setFirstResult(fromIndex)
				.setMaxResults(length)
				.getResultList();
		
		return retvals;
	}

	@Override
	public List<Provider> searchWithParams(int fromIndex, int toIndex, Map<String, List<ParameterWrapper>> paramMap) {
		int length = toIndex - fromIndex;
		EntityManager em = providerDao.getEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Provider> providerQuery = builder.createQuery(Provider.class);
		Root<Provider> providerRoot = providerQuery.from(Provider.class);
		
		List<Provider> providers = new ArrayList<Provider>();
		List<Predicate> predicates = ParameterWrapper.constructPredicate(builder, paramMap, providerRoot);
		if (predicates == null || predicates.isEmpty()) return providers; // Nothing. return empty list
	
		providerQuery.select(providerRoot);
		providerQuery.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));

		List<Provider> retvals = em.createQuery(providerQuery)
				.setFirstResult(fromIndex)
				.setMaxResults(length)
				.getResultList();
		return retvals;
	}
	
}
