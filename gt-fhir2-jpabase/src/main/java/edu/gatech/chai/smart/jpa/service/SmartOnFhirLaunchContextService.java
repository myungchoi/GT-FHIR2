package edu.gatech.chai.smart.jpa.service;

import edu.gatech.chai.smart.jpa.entity.SmartLaunchContext;

public interface SmartOnFhirLaunchContextService {
	public SmartLaunchContext getContext(Long id);	
	public void setContext(SmartLaunchContext context);
}
