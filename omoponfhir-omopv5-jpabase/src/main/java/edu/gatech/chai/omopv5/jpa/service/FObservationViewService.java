package edu.gatech.chai.omopv5.jpa.service;

import java.util.Date;

import org.springframework.transaction.annotation.Transactional;

import edu.gatech.chai.omopv5.jpa.entity.FObservationView;

@Transactional
public interface FObservationViewService extends IService<FObservationView> {
	public FObservationView findDiastolic (Long conceptId, Long personId, Date date, String time);

}
