package edu.gatech.chai.omopv5.jpa.service;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import edu.gatech.chai.omopv5.jpa.dao.CareSiteDao;
import edu.gatech.chai.omopv5.jpa.dao.ConceptDao;
import edu.gatech.chai.omopv5.jpa.entity.CareSite;
import edu.gatech.chai.omopv5.jpa.entity.Concept;

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
		if (results.size() > 0)
			return results;
		else
			return null;	
	}

	@Override
	public List<Concept> searchWithoutParams(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Concept> searchWithParams(int fromIndex, int toIndex, Map<String, List<ParameterWrapper>> paramMap) {
		// TODO Auto-generated method stub
		return null;
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
