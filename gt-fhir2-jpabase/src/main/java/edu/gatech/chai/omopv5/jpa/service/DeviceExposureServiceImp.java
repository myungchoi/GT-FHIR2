package edu.gatech.chai.omopv5.jpa.service;

import org.springframework.stereotype.Service;

import edu.gatech.chai.omopv5.jpa.dao.DeviceExposureDao;
import edu.gatech.chai.omopv5.jpa.entity.DeviceExposure;

@Service
public class DeviceExposureServiceImp extends BaseEntityServiceImp<DeviceExposure, DeviceExposureDao>
		implements DeviceExposureService {

	public DeviceExposureServiceImp() {
		super(DeviceExposure.class);
	}

}
