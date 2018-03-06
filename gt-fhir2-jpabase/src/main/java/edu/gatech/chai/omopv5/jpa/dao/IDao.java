package edu.gatech.chai.omopv5.jpa.dao;

import edu.gatech.chai.omopv5.jpa.entity.BaseEntity;

public interface IDao<T extends BaseEntity> {
	public void add(T baseEntity);
	public void merge(T baseEntity);
	public void update(T baseEntity);
	public T findById(Long id);
	public T findById(Class<T> entityClass, Long id);
	public void delete(Long id);
	public void delete(Class<T> entityClass, Long id);
}