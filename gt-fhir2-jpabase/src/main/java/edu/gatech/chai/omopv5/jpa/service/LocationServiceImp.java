package edu.gatech.chai.omopv5.jpa.service;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.gatech.chai.omopv5.jpa.dao.LocationDao;
import edu.gatech.chai.omopv5.jpa.entity.Location;

@Service
public class LocationServiceImp implements LocationService {

	@Autowired
	private LocationDao locationDao;
	
	@Transactional(readOnly = true)
	@Override
	public Location findById(Long id) {
		return locationDao.findById(id);
	}

	@Transactional(readOnly = true)
	@Override
	public Location searchByAddress(String line1, String line2, String city, String state, String zipCode) {
		EntityManager em = locationDao.getEntityManager();
		String query;
		List<Location> results;
		
		if (line2 != null) {
			query = "SELECT t FROM Location t WHERE address1 LIKE :line1 AND address2 LIKE :line2 AND city LIKE :city AND state LIKE :state AND zipCode LIKE :zip";
			results = em.createQuery(query, Location.class)
					.setParameter("line1", line1)
					.setParameter("line2", line2)
					.setParameter("city", city)
					.setParameter("state", state)
					.setParameter("zip", zipCode)
					.getResultList();
		} else { 
			query = "SELECT t FROM Location t WHERE address1 LIKE :line1 AND city LIKE :city AND state LIKE :state AND zipCode LIKE :zip";
			results = em.createQuery(query, Location.class)
					.setParameter("line1", line1)
					.setParameter("city", city)
					.setParameter("state", state)
					.setParameter("zip", zipCode)
					.getResultList();
		}

		System.out.println("loadEntityByLocaiton:"+query+" result size:"+results.size());
		if (results.size() > 0)
			return results.get(0);
		else
			return null;

	}

	@Transactional(readOnly = true)
	@Override
	public Location searchByColumnString(String column, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Transactional
	@Override
	public Location createOrUpdate(Location entity) {
		if (entity.getId() != null) {
			locationDao.merge(entity);
		} else {
			locationDao.add(entity);
		}
		return entity;
	}

	@Transactional(readOnly = true)
	@Override
	public Long getSize() {
		EntityManager em = locationDao.getEntityManager();
		
		String query = "SELECT COUNT(t) FROM Location t";
		Long totalSize = em.createQuery(query, Long.class).getSingleResult();
		return totalSize;
	}

}
