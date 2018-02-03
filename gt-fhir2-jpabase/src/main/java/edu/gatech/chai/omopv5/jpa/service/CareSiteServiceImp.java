package edu.gatech.chai.omopv5.jpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.gatech.chai.omopv5.jpa.dao.CareSiteDao;
import edu.gatech.chai.omopv5.jpa.entity.CareSite;

@Service
public class CareSiteServiceImp implements CareSiteService {

	@Autowired
	private CareSiteDao careSiteDao;
	
	@Transactional(readOnly = true)
	@Override
	public CareSite findById(Long id) {
		return careSiteDao.findById(id);
	}

}
