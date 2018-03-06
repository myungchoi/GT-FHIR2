package edu.gatech.chai.omopv5.jpa.service;

import org.springframework.stereotype.Service;

import edu.gatech.chai.omopv5.jpa.dao.ProviderDao;
import edu.gatech.chai.omopv5.jpa.entity.Provider;

@Service
public class ProviderServiceImp extends BaseEntityServiceImp<Provider, ProviderDao> implements ProviderService {
	
}
