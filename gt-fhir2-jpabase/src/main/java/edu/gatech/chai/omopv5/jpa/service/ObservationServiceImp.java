package edu.gatech.chai.omopv5.jpa.service;

import org.springframework.stereotype.Service;

import edu.gatech.chai.omopv5.jpa.dao.ObservationDao;
import edu.gatech.chai.omopv5.jpa.entity.Observation;

@Service
public class ObservationServiceImp extends BaseEntityServiceImp<Observation, ObservationDao> implements ObservationService {


}
