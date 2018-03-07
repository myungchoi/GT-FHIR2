package edu.gatech.chai.omopv5.jpa.service;

import org.springframework.stereotype.Service;

import edu.gatech.chai.omopv5.jpa.dao.MeasurementDao;
import edu.gatech.chai.omopv5.jpa.entity.Measurement;

@Service
public class MeasurementServiceImp extends BaseEntityServiceImp<Measurement, MeasurementDao> implements MeasurementService {

	public MeasurementServiceImp() {
		super(Measurement.class);
	}
}
