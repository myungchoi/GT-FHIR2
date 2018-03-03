package edu.gatech.chai.omopv5.jpa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.gatech.chai.omopv5.jpa.dao.FObservationViewDao;
import edu.gatech.chai.omopv5.jpa.entity.FObservationView;

@Service
public class FObservationViewServiceImp implements FObservationViewService {

	@Autowired
	private FObservationViewDao fObservationViewDao;

	@Transactional(readOnly = true)
	@Override
	public FObservationView findById(Long id) {
		return fObservationViewDao.findById(id);
	}

	@Transactional(readOnly = true)
	@Override
	public List<FObservationView> searchByColumnString(String column, String value) {
		EntityManager em = fObservationViewDao.getEntityManager();
		
		String query = "SELECT t FROM FObservationView t WHERE "+column+" like :value";
		List<FObservationView> results = em.createQuery(query, FObservationView.class)
				.setParameter("value", value).getResultList();
		return results;
	}

	@Override
	public List<FObservationView> searchWithoutParams(int fromIndex, int toIndex) {
		int length = toIndex - fromIndex;
		EntityManager em = fObservationViewDao.getEntityManager();
		
		String query = "SELECT p FROM FObservationView p ORDER BY id ASC";
		List<FObservationView> retvals = (List<FObservationView>) em.createQuery(query, FObservationView.class)
				.setFirstResult(fromIndex)
				.setMaxResults(length)
				.getResultList();
		
		return retvals;
	}

	@Override
	public List<FObservationView> searchWithParams(int fromIndex, int toIndex,
			Map<String, List<ParameterWrapper>> paramMap) {
		int length = toIndex - fromIndex;
		EntityManager em = fObservationViewDao.getEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<FObservationView> query = builder.createQuery(FObservationView.class);
		Root<FObservationView> root = query.from(FObservationView.class);
		
		List<FObservationView> retvals = new ArrayList<FObservationView>();
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
	public FObservationView createOrUpdate(FObservationView entity) {
		if (entity.getId() != null) {
			fObservationViewDao.merge(entity);
		} else {
			fObservationViewDao.add(entity);
		}
		return entity;
	}

	@Transactional(readOnly = true)
	@Override
	public Long getSize() {
		EntityManager em = fObservationViewDao.getEntityManager();
		
		String query = "SELECT COUNT(t) FROM FObservationView t";
		Long totalSize = em.createQuery(query, Long.class).getSingleResult();
		return totalSize;
	}

	@Transactional(readOnly = true)
	@Override
	public Long getSize(Map<String, List<ParameterWrapper>> paramMap) {
		// Construct predicate from this map.
		EntityManager em = fObservationViewDao.getEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Long> fObservationViewQuery = builder.createQuery(Long.class);
		Root<FObservationView> fObservationViewRoot = fObservationViewQuery.from(FObservationView.class);

		List<Predicate> predicates = ParameterWrapper.constructPredicate(builder, paramMap, fObservationViewRoot);		
		if (predicates == null || predicates.isEmpty()) return 0L;

		fObservationViewQuery.select(builder.count(fObservationViewRoot));
		fObservationViewQuery.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));
		
		return em.createQuery(fObservationViewQuery).getSingleResult();
	}

	@Override
	public FObservationView findDiastolic(Long conceptId, Long personId, Date date, String time) {
		EntityManager em = fObservationViewDao.getEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<FObservationView> criteria = builder.createQuery(FObservationView.class);
		Root<FObservationView> from = criteria.from(FObservationView.class);
		criteria.select(from).where(
				builder.equal(from.get("observationConcept").get("id"), conceptId),
				builder.equal(from.get("person").get("id"), personId),
				builder.equal(from.get("date"), date),
				builder.equal(from.get("time"),  time)
				);
		TypedQuery<FObservationView> query = em.createQuery(criteria);
		List<FObservationView> results = query.getResultList();
		if (results.size() > 0) {
			return results.get(0);				
		} else {
			return null;
		}
	}
}
