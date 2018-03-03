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

import edu.gatech.chai.omopv5.jpa.dao.MeasurementDao;
import edu.gatech.chai.omopv5.jpa.entity.FObservationView;
import edu.gatech.chai.omopv5.jpa.entity.Measurement;
import edu.gatech.chai.omopv5.jpa.entity.Observation;

@Service
public class MeasurementServiceImp implements MeasurementService {

	@Autowired
	private MeasurementDao measurementDao;
	
	@Transactional(readOnly = true)
	@Override
	public Measurement findById(Long id) {
		return measurementDao.findById(id);
	}

	@Override
	public List<Measurement> searchByColumnString(String column, String value) {
		EntityManager em = measurementDao.getEntityManager();
		
		String query = "SELECT t FROM Measurement t WHERE "+column+" like :value";
		List<Measurement> results = em.createQuery(query, Measurement.class)
				.setParameter("value",  value).getResultList();
		return results;
	}

	@Override
	public List<Measurement> searchWithoutParams(int fromIndex, int toIndex) {
		int length = toIndex - fromIndex;
		EntityManager em = measurementDao.getEntityManager();
		
		String query = "SELECT p FROM Measurement p ORDER BY id ASC";
		List<Measurement> retvals = (List<Measurement>) em.createQuery(query, Measurement.class)
				.setFirstResult(fromIndex)
				.setMaxResults(length)
				.getResultList();
		
		return retvals;
	}

	@Override
	public List<Measurement> searchWithParams(int fromIndex, int toIndex,
			Map<String, List<ParameterWrapper>> paramMap) {
		int length = toIndex - fromIndex;
		EntityManager em = measurementDao.getEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Measurement> query = builder.createQuery(Measurement.class);
		Root<Measurement> root = query.from(Measurement.class);
		
		List<Measurement> retvals = new ArrayList<Measurement>();
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
	public Measurement createOrUpdate(Measurement entity) {
		if (entity.getId() != null) {
			measurementDao.merge(entity);
		} else {
			measurementDao.add(entity);
		}
		return entity;
	}

	@Transactional(readOnly = true)
	@Override
	public Long getSize() {
		EntityManager em = measurementDao.getEntityManager();
		
		String query = "SELECT COUNT(t) FROM Measurement t";
		Long totalSize = em.createQuery(query, Long.class).getSingleResult();
		return totalSize;
	}

	@Override
	public Long getSize(Map<String, List<ParameterWrapper>> paramMap) {
		// Construct predicate from this map.
		EntityManager em = measurementDao.getEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<Measurement> root = query.from(Measurement.class);

		List<Predicate> predicates = ParameterWrapper.constructPredicate(builder, paramMap, root);		
		if (predicates == null || predicates.isEmpty()) return 0L;

		query.select(builder.count(root));
		query.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));
		
		return em.createQuery(query).getSingleResult();
	}

}
