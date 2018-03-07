package edu.gatech.chai.gtfhir2.mapping;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.springframework.web.context.WebApplicationContext;

import edu.gatech.chai.omopv5.jpa.entity.BaseEntity;
import edu.gatech.chai.omopv5.jpa.service.IService;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;

public abstract class BaseOmopResource<v extends DomainResource, t extends BaseEntity, p extends IService<t>> implements IResourceMapping<v, t> {
	
	private p myOmopService;
	private Class<t> myEntityClass;
	
	public BaseOmopResource(WebApplicationContext context, Class<t> entityClass, Class<p> serviceClass) {
		myOmopService = context.getBean(serviceClass);
		myEntityClass = entityClass;
	}
	
	public p getMyOmopService() {
		return this.myOmopService;
	}
	
	public Class<t> getMyEntityClass() {
		return this.myEntityClass;
	}
	
	public Long getSize() {
		return myOmopService.getSize();
	}

	public Long getSize(Map<String, List<ParameterWrapper>> map) {
		return myOmopService.getSize(map);
	}

}
