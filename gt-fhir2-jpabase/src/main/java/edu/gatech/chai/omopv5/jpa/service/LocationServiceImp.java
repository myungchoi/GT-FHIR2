package edu.gatech.chai.omopv5.jpa.service;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.gatech.chai.omopv5.jpa.dao.LocationDao;
import edu.gatech.chai.omopv5.jpa.entity.Location;

@Service
public class LocationServiceImp extends BaseEntityServiceImp<Location, LocationDao> implements LocationService {

	@Transactional(readOnly = true)
	@Override
	public Location searchByAddress(String line1, String line2, String city, String state, String zipCode) {
		EntityManager em = getEntityDao().getEntityManager();
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

}
