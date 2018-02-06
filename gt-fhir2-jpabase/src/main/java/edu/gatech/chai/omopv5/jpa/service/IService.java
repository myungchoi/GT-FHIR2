package edu.gatech.chai.omopv5.jpa.service;

import edu.gatech.chai.omopv5.jpa.entity.BaseEntity;

public interface IService<v extends BaseEntity> {
	static String version = "5.x";
	
	v findById (Long id);
	v searchByColumnString (String column, String value);
	v createOrUpdate (v entity);
}
