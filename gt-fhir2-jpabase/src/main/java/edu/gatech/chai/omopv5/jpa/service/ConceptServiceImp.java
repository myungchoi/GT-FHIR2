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

import edu.gatech.chai.omopv5.jpa.dao.CareSiteDao;
import edu.gatech.chai.omopv5.jpa.dao.ConceptDao;
import edu.gatech.chai.omopv5.jpa.entity.CareSite;
import edu.gatech.chai.omopv5.jpa.entity.Concept;
import edu.gatech.chai.omopv5.jpa.entity.FObservationView;

@Service
public class ConceptServiceImp implements ConceptService {

	@Autowired
	private ConceptDao conceptDao;

	@Transactional(readOnly = true)
	@Override
	public Concept findById(Long id) {
		return conceptDao.findById(id);
	}

	@Transactional(readOnly = true)
	@Override
	public List<Concept> searchByColumnString(String column, String value) {
		EntityManager em = conceptDao.getEntityManager();
		
		String query = "SELECT t FROM Concept t WHERE "+column+" like :value";
		List<Concept> results = em.createQuery(query, Concept.class)
				.setParameter("value", value).getResultList();
		return results;
	}

	@Override
	public List<Concept> searchWithoutParams(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Concept> searchWithParams(int fromIndex, int toIndex, Map<String, List<ParameterWrapper>> paramMap) {
		int length = toIndex - fromIndex;
		EntityManager em = conceptDao.getEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Concept> query = builder.createQuery(Concept.class);
		Root<Concept> root = query.from(Concept.class);
		
		List<Concept> retvals = new ArrayList<Concept>();
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

	@Override
	public Concept createOrUpdate(Concept entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getSize(Map<String, List<ParameterWrapper>> paramMap) {
		// TODO Auto-generated method stub
		return null;
	}

}
