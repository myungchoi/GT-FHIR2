/**
 * 
 */
package edu.gatech.chai.smart.jpa.dao;

import edu.gatech.chai.omopv5.jpa.entity.BaseEntity;

/**
 * @author MC142
 *
 */

public interface ISmartFhirDao<T extends BaseEntity> {
	public T findOne(Long id);
	public T findOneAndDelete(Long id);
	public void saveAndDelete (T entity);
	public void save(T entity);
	public void update(T entity);
	public void delete(T entity);
	public void deleteById(Long entityId);
}
