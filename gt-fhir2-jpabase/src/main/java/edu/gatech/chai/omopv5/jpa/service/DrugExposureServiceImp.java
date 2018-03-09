package edu.gatech.chai.omopv5.jpa.service;

import org.springframework.stereotype.Service;

import edu.gatech.chai.omopv5.jpa.dao.DrugExposureDao;
import edu.gatech.chai.omopv5.jpa.entity.DrugExposure;

@Service
public class DrugExposureServiceImp extends BaseEntityServiceImp<DrugExposure, DrugExposureDao>
		implements DrugExposureService {
	public DrugExposureServiceImp() {
		super(DrugExposure.class);
	}
}
