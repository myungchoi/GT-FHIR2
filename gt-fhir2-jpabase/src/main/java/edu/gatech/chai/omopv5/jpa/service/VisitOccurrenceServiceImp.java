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

import edu.gatech.chai.omopv5.jpa.dao.VisitOccurrenceDao;
import edu.gatech.chai.omopv5.jpa.entity.VisitOccurrence;

@Service
public class VisitOccurrenceServiceImp implements VisitOccurrenceService {

	@Autowired
	private VisitOccurrenceDao visitOccurrenceDao;

	@Transactional(readOnly = true)
	@Override
	public VisitOccurrence findById(Long id) {
		return visitOccurrenceDao.findById(id);
	}

	@Transactional(readOnly = true)
	@Override
	public List<VisitOccurrence> searchByColumnString(String column, String value) {
		EntityManager em = visitOccurrenceDao.getEntityManager();

		String query = "SELECT t FROM VisitOccurrence t WHERE "+column+" like :value";
		List<VisitOccurrence> results = em.createQuery(query, VisitOccurrence.class)
				.setParameter("value", value).getResultList();
		if (results.size() > 0)
			return results;
		else
			return null;	
	}

	@Override
	public List<VisitOccurrence> searchWithoutParams(int fromIndex, int toIndex) {
		int length = toIndex - fromIndex;
		EntityManager em = visitOccurrenceDao.getEntityManager();
		
		String query = "SELECT p FROM VisitOccurrence p ORDER BY id ASC";
		List<VisitOccurrence> retvals = em.createQuery(query, VisitOccurrence.class)
				.setFirstResult(fromIndex)
				.setMaxResults(length)
				.getResultList();
		
		return retvals;
	}

	@Override
	public List<VisitOccurrence> searchWithParams(int fromIndex, int toIndex,
			Map<String, List<ParameterWrapper>> paramMap) {
		int length = toIndex - fromIndex;
		EntityManager em = visitOccurrenceDao.getEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<VisitOccurrence> query = builder.createQuery(VisitOccurrence.class);
		Root<VisitOccurrence> root = query.from(VisitOccurrence.class);
		
		List<VisitOccurrence> retvals = new ArrayList<VisitOccurrence>();
		List<Predicate> predicates = ParameterWrapper.constructPredicate(builder, paramMap, root);		
		if (predicates == null || predicates.isEmpty()) return retvals; // Nothing. return empty list
	
		query.select(root);
		query.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));

		if (length <= 0) {
			retvals = em.createQuery(query)
					.getResultList();
		} else {
			retvals = em.createQuery(query)
					.setFirstResult(fromIndex)
					.setMaxResults(length)
					.getResultList();
		}
		return retvals;
	}

	@Transactional
	@Override
	public VisitOccurrence createOrUpdate(VisitOccurrence entity) {
		if (entity.getId() != null) {
			visitOccurrenceDao.merge(entity);
		} else {
			visitOccurrenceDao.add(entity);
		}
		return entity;
	}

	@Override
	public Long getSize() {
		EntityManager em = visitOccurrenceDao.getEntityManager();
		
		String query = "SELECT COUNT(t) FROM VisitOccurrence t";
		Long totalSize = em.createQuery(query, Long.class).getSingleResult();
		return totalSize;
	}

	@Override
	public Long getSize(Map<String, List<ParameterWrapper>> paramMap) {
		// Construct predicate from this map.
		EntityManager em = visitOccurrenceDao.getEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<VisitOccurrence> root = query.from(VisitOccurrence.class);

		List<Predicate> predicates = ParameterWrapper.constructPredicate(builder, paramMap, root);		
		if (predicates == null || predicates.isEmpty()) return 0L;

		query.select(builder.count(root));
		query.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));
		
		return em.createQuery(query).getSingleResult();
	}

}
