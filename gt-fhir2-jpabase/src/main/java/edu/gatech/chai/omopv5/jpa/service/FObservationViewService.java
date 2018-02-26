package edu.gatech.chai.omopv5.jpa.service;

import java.util.Date;

import edu.gatech.chai.omopv5.jpa.entity.FObservationView;

public interface FObservationViewService extends IService<FObservationView> {
	public FObservationView findDiastolic (Long conceptId, Long personId, Date date, String time);

}
