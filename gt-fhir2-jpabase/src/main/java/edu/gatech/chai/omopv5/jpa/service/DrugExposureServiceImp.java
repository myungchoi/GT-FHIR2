package edu.gatech.chai.omopv5.jpa.service;

import edu.gatech.chai.omopv5.jpa.dao.DrugExposureDao;
import edu.gatech.chai.omopv5.jpa.entity.DrugExposure;

public class DrugExposureServiceImp extends BaseEntityServiceImp<DrugExposure, DrugExposureDao>
		implements DrugExposureService {
	public DrugExposureServiceImp() {
		super(DrugExposure.class);
	}
}
