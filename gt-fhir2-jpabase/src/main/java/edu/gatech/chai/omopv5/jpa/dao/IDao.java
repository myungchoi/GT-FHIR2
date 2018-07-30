package edu.gatech.chai.omopv5.jpa.dao;

import edu.gatech.chai.omopv5.jpa.entity.BaseEntity;

public interface IDao<T extends BaseEntity> {
	public void add(T baseEntity);
	public void merge(T baseEntity);
	public T findById(Class<T> entityClass, Long id);
	public Long delete(Class<T> entityClass, Long id);
}