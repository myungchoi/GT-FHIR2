package edu.gatech.chai.omopv5.jpa.service;

import java.util.List;
import java.util.Map;

import edu.gatech.chai.omopv5.jpa.entity.BaseEntity;

public interface IService<v extends BaseEntity> {
	static String version = "5.x";
	
	v findById (Long id);
	v searchByColumnString (String column, String value);
	public List<v> searchWithoutParams(int fromIndex, int toIndex);
	public List<v> searchWithParams(int fromIndex, int toIndex, Map<String, List<ParameterWrapper>> paramMap);
	v createOrUpdate (v entity);
	Long getSize();
	Long getSize(Map<String, List<ParameterWrapper>> paramMap);
}
