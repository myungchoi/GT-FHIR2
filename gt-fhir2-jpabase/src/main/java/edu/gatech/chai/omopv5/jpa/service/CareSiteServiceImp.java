package edu.gatech.chai.omopv5.jpa.service;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.gatech.chai.omopv5.jpa.dao.CareSiteDao;
import edu.gatech.chai.omopv5.jpa.entity.CareSite;

@Service
public class CareSiteServiceImp implements CareSiteService {

	@Autowired
	private CareSiteDao careSiteDao;
	
	@Transactional(readOnly = true)
	@Override
	public CareSite findById(Long id) {
		return careSiteDao.findById(id);
	}

	@Transactional(readOnly = true)
	@Override
	public CareSite searchByColumnString(String column, String value) {
		EntityManager em = careSiteDao.getEntityManager();
		
		String query = "SELECT t FROM CareSite t WHERE "+column+" like :value";
		List<? extends CareSite> results = em.createQuery(query, CareSite.class)
				.setParameter("value", value).getResultList();
		if (results.size() > 0)
			return results.get(0);
		else
			return null;	
	}

	@Transactional
	@Override
	public CareSite createOrUpdate(CareSite entity) {
		if (entity.getId() != null) {
			careSiteDao.merge(entity);
		} else {
			careSiteDao.add(entity);
		}
		return entity;
	}

	@Transactional(readOnly = true)
	@Override
	public Long getSize() {
		EntityManager em = careSiteDao.getEntityManager();
		
		String query = "SELECT COUNT(t) FROM CareSite t";
		Long totalSize = em.createQuery(query, Long.class).getSingleResult();
		return totalSize;
	}

	@Override
	public Long getSize(Map<String, List<ParameterWrapper>> paramMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CareSite> searchWithoutParams(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CareSite> searchWithParams(int fromIndex, int toIndex, Map<String, List<ParameterWrapper>> paramMap) {
		// TODO Auto-generated method stub
		return null;
	}

}
