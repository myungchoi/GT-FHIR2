package edu.gatech.chai.omopv5.jpa.service;

import java.util.List;

import edu.gatech.chai.omopv5.jpa.entity.BaseEntity;

public interface IService<v extends BaseEntity> {
	static String version = "5.x";
	
	v findById (Long id);
	public Long removeById (Long id);
	List<v> searchByColumnString (String column, String value);
	public List<v> searchWithParams(int fromIndex, int toIndex, List<ParameterWrapper> paramList);
	public List<v> searchWithoutParams(int fromIndex, int toIndex);
	v create(v entity);
	v update(v entity);
	Long getSize();
	Long getSize(List<ParameterWrapper> paramList);
}
