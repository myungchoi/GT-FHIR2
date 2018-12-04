package edu.gatech.chai.smart.jpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.gatech.chai.smart.jpa.dao.SmartOnFhirLaunchContextDao;
import edu.gatech.chai.smart.jpa.entity.SmartLaunchContext;

@Service
public class SmartOnFhirLaunchContextServiceImp implements SmartOnFhirLaunchContextService {

	@Autowired
	private SmartOnFhirLaunchContextDao vDao;
	private Class<SmartLaunchContext> entityClass;

	public SmartOnFhirLaunchContextServiceImp() {
		this.entityClass = SmartLaunchContext.class;
	}
	
	public SmartOnFhirLaunchContextDao getEntityDao() {
		return vDao;
	}
	
	public Class<SmartLaunchContext> getEntityClass() {
		return this.entityClass;
	}

	@Override
	public SmartLaunchContext getContext(Long id) {
		return vDao.findOneAndDelete(id);
	}

	@Override
	public void setContext(SmartLaunchContext context) {
		vDao.saveAndDelete(context);
	}
}
