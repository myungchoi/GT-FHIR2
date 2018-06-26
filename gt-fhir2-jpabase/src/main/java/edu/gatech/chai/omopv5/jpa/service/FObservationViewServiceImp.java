package edu.gatech.chai.omopv5.jpa.service;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Service;

import edu.gatech.chai.omopv5.jpa.dao.FObservationViewDao;
import edu.gatech.chai.omopv5.jpa.entity.FObservationView;

@Service
public class FObservationViewServiceImp extends BaseEntityServiceImp<FObservationView, FObservationViewDao> implements FObservationViewService {

	public FObservationViewServiceImp() {
		super(FObservationView.class);
	}
	
	@Override
	public FObservationView findDiastolic(Long conceptId, Long personId, Date date, String time) {
		EntityManager em = getEntityDao().getEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<FObservationView> criteria = builder.createQuery(FObservationView.class);
		Root<FObservationView> from = criteria.from(FObservationView.class);
		criteria.select(from).where(
				builder.equal(from.get("observationConcept").get("id"), conceptId),
				builder.equal(from.get("fPerson").get("id"), personId),
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
