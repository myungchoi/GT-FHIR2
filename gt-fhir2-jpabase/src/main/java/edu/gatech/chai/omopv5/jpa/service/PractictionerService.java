package edu.gatech.chai.omopv5.jpa.service;

import edu.gatech.chai.omopv5.jpa.dao.ProviderDao;
import edu.gatech.chai.omopv5.jpa.entity.Provider;

public interface PractictionerService extends IService<Provider>{
	public ProviderDao getProviderDao();
}
