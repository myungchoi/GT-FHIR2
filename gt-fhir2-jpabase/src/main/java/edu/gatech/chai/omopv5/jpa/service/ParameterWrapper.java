package edu.gatech.chai.omopv5.jpa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.chai.omopv5.jpa.entity.BaseEntity;

/**
 * ParameterWrapper for database operations.
 * 
 * paramterType stores variable type such as String, Short, etc.
 * constructPredicate() method should convert this to appropriate type for the
 * database.
 * 
 * parameters store column name(s).
 * 
 * operators store SQL comparisons
 * 
 * values store value(s) to be compared.
 * 
 * relationship store either "or" or "and" - relationship between parameters and
 * values default is "or"
 * 
 * Parameters and values both cannot be multiple at the same time. Either one
 * should be single.
 * 
 * eg) parameters: "givenName1", "givenName2" values: "TOM" operator: "like"
 * relationship: "or"
 * 
 * This should be read as, Column_givenName1 like "TOM" or Column_givenName2
 * like "TOM"
 * 
 * The operators should match with largest number of parameters or values. It
 * means, if there are 3 parameters (there should be only one value), then 3
 * operators are needed for each parameter. If there are 3 values (there should
 * be only one parameter), then 3 operators are needed for each value.
 * 
 * The order should be parameter(left)-operator-value(right). So, put the
 * operator in this order.
 * 
 * @author mc142
 *
 */
public class ParameterWrapper {
	
	private static final Logger logger = LoggerFactory.getLogger(ParameterWrapper.class);
	private String parameterType;
	private List<String> parameters;
	private List<String> operators;
	private List<String> values;
	private String relationship;

	public ParameterWrapper() {
	}

	public ParameterWrapper(String parameterType, List<String> parameters, List<String> operators,
			List<String> values, String relationship) {
		this.parameterType = parameterType;
		this.parameters = parameters;
		this.operators = operators;
		this.values = values;
		this.relationship = relationship;
	}

	public String getParameterType() {
		return parameterType;
	}

	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	public List<String> getOperators() {
		return operators;
	}

	public void setOperators(List<String> operators) {
		this.operators = operators;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public static List<Predicate> constructPredicate(CriteriaBuilder builder,
			Map<String, List<ParameterWrapper>> paramMap, Root<? extends BaseEntity> rootUser) {
		List<Predicate> predicates = new ArrayList<Predicate>();
		Predicate where = builder.conjunction();

		// paramMap has FHIR parameters mapped Omop parameters (or columns).
		for (Map.Entry<String, List<ParameterWrapper>> entry : paramMap.entrySet()) {
			List<ParameterWrapper> paramWrappers = entry.getValue();
			for (ParameterWrapper param : paramWrappers) {
				Predicate subWhere;
				if (param.getRelationship() == null || param.getRelationship().equalsIgnoreCase("or"))
					subWhere = builder.disjunction();
				else
					subWhere = builder.conjunction();

				switch (param.getParameterType()) {
				case "String":
					logger.debug("String parameter type found.");
					String attributeName = null;
					String valueName = null;
					for (Iterator<String> attributeIter = param.getParameters().iterator(), 
							operIter = param.getOperators().iterator(),
							valueIter = param.getValues().iterator();
							(attributeIter.hasNext() || valueIter.hasNext()) && operIter.hasNext();
						) {

						if (attributeIter.hasNext())
							attributeName = attributeIter.next();
						if (valueIter.hasNext())
							valueName = valueIter.next();
						String oper = operIter.next();						
						logger.debug("--- Attribute name:"+attributeName);
						logger.debug("--- value:"+valueName);
						logger.debug("--- operator:"+oper);
						Path<String> path;
						String[] columnPath = attributeName.split("\\.");
						if (columnPath.length == 2) {
							path = rootUser.get(columnPath[0]).get(columnPath[1]);
						} else if (columnPath.length == 3) {
							path = rootUser.get(columnPath[0]).get(columnPath[1]).get(columnPath[2]);
						} else {
							path = rootUser.get(attributeName);
						}

						if (oper.equalsIgnoreCase("like"))
							if (param.getRelationship() == null || param.getRelationship().equals("or")) {
								subWhere = builder.or(subWhere, builder.like(path, valueName));
							} else {
								subWhere = builder.and(subWhere, builder.like(path, valueName));
							}
						else
							if (param.getRelationship() == null || param.getRelationship().equals("or")) {
								subWhere = builder.or(subWhere, builder.notLike(path, valueName));
							} else {
								subWhere = builder.and(subWhere, builder.notLike(path, valueName));
							}
					}
					break;
				case "Date":
					logger.debug("Numeric parameter type found.");
					attributeName = null;
					valueName = null;
					for (Iterator<String> attributeIter = param.getParameters().iterator(), 
							operIter = param.getOperators().iterator(),
							valueIter = param.getValues().iterator(); 
							(attributeIter.hasNext() || valueIter.hasNext()) && operIter.hasNext();) {
						
						if (attributeIter.hasNext())
							attributeName = attributeIter.next();
						if (valueIter.hasNext())
							valueName = valueIter.next();
						String oper = operIter.next();
						logger.debug("--- Attribute name:"+attributeName);
						logger.debug("--- value:"+valueName);
						logger.debug("--- operator:"+oper);
						Long dateInMili = Long.valueOf(valueName);
						Date value = new Date(dateInMili);
						
						Path<Date> path;
						String[] columnPath = attributeName.split("\\.");
						if (columnPath.length == 2) {
							path = rootUser.get(columnPath[0]).get(columnPath[1]);
						} else if (columnPath.length == 3) {
							path = rootUser.get(columnPath[0]).get(columnPath[1]).get(columnPath[2]);
						} else {
							path = rootUser.get(attributeName);
						}
						
						if (oper.equalsIgnoreCase("=")) {
							if (param.getRelationship() == null || param.getRelationship().equals("or")) {
								subWhere = builder.or(subWhere, builder.equal(path, value));
							} else {
								subWhere = builder.and(subWhere, builder.equal(path, value));
							}
						} else if (oper.equalsIgnoreCase("!=")) {
							if (param.getRelationship() == null || param.getRelationship().equals("or")) {
								subWhere = builder.or(subWhere, builder.notEqual(path, value));
							} else {
								subWhere = builder.and(subWhere, builder.notEqual(path, value));
							}
						} else if (oper.equalsIgnoreCase("<"))
							if (param.getRelationship() == null || param.getRelationship().equals("or")) {
								subWhere = builder.or(subWhere, builder.lessThan(path, value));
							} else {
								subWhere = builder.and(subWhere, builder.lessThan(path, value));
							}
						else if (oper.equalsIgnoreCase("<="))
							if (param.getRelationship() == null || param.getRelationship().equals("or")) {
								subWhere = builder.or(subWhere, builder.lessThanOrEqualTo(path, value));
							} else {
								subWhere = builder.and(subWhere, builder.lessThanOrEqualTo(path, value));
							}
						else if (oper.equalsIgnoreCase(">")) {
							if (param.getRelationship() == null || param.getRelationship().equals("or")) {
								subWhere = builder.or(subWhere, builder.greaterThan(path, value));
							} else {
								subWhere = builder.and(subWhere, builder.greaterThan(path, value));
							}
						} else { // (param.getOperator().equalsIgnoreCase(">="))
							if (param.getRelationship() == null || param.getRelationship().equals("or")) {
								subWhere = builder.or(subWhere, builder.greaterThanOrEqualTo(path, value));
							} else {
								subWhere = builder.and(subWhere, builder.greaterThanOrEqualTo(path, value));
							}
						}
					}
					break;
				case "Short":
				case "Long":
				case "Double":
				case "Integer":
					subWhere = numbericPredicateBuidler(builder, param, rootUser, subWhere, param.getParameterType());
					break;
				}

				where = builder.and(where, subWhere);
			}
		}
		predicates.add(where);

		return predicates;
	}
	
	public static Predicate numbericPredicateBuidler (
			CriteriaBuilder builder,
			ParameterWrapper param, 
			Root<? extends BaseEntity> rootUser,
			Predicate subWhere,
			String paramType
			) {
		logger.debug("Numeric parameter type found.");
		// We may have multiple columns to compare with 'or'. If
		// so, get them now.
		// for (String columnName : param.getParameters(),
		// String oper: param.getOperators()) {
		String attributeName = null;
		String valueName = null;
		for (Iterator<String> attributeIter = param.getParameters().iterator(), 
				operIter = param.getOperators().iterator(),
				valueIter = param.getValues().iterator(); 
				(attributeIter.hasNext() || valueIter.hasNext()) && operIter.hasNext();) {
			
			if (attributeIter.hasNext())
				attributeName = attributeIter.next();
			if (valueIter.hasNext())
				valueName = valueIter.next();
			String oper = operIter.next();
			logger.debug("--- Attribute name:"+attributeName);
			logger.debug("--- value:"+valueName);
			logger.debug("--- operator:"+oper);
			Number value;
			if (paramType.equals("Short")) {
				value = Short.valueOf(valueName);
			} else if (paramType.equals("Long")) {
				value = Long.valueOf(valueName);
			} else if (paramType.equals("Double")) {
				value = Double.valueOf(valueName);
			} else {
				value = Integer.valueOf(valueName);
			}
			
			Path<Number> path;
			String[] columnPath = attributeName.split("\\.");
			if (columnPath.length == 2) {
				path = rootUser.get(columnPath[0]).get(columnPath[1]);
			} else if (columnPath.length == 3) {
				path = rootUser.get(columnPath[0]).get(columnPath[1]).get(columnPath[2]);
			} else {
				path = rootUser.get(attributeName);
			}
			
			if (oper.equalsIgnoreCase("=")) {
				if (param.getRelationship() == null || param.getRelationship().equals("or")) {
					subWhere = builder.or(subWhere, builder.equal(path, value));
				} else {
					subWhere = builder.and(subWhere, builder.equal(path, value));
				}
			} else if (oper.equalsIgnoreCase("!=")) {
				if (param.getRelationship() == null || param.getRelationship().equals("or")) {
					subWhere = builder.or(subWhere, builder.notEqual(path, value));
				} else {
					subWhere = builder.and(subWhere, builder.notEqual(path, value));
				}
			} else if (oper.equalsIgnoreCase("<"))
				if (param.getRelationship() == null || param.getRelationship().equals("or")) {
					subWhere = builder.or(subWhere, builder.lt(path, value));
				} else {
					subWhere = builder.and(subWhere, builder.lt(path, value));
				}
			else if (oper.equalsIgnoreCase("<="))
				if (param.getRelationship() == null || param.getRelationship().equals("or")) {
					subWhere = builder.or(subWhere, builder.le(path, value));
				} else {
					subWhere = builder.and(subWhere, builder.le(path, value));
				}
			else if (oper.equalsIgnoreCase(">")) {
				if (param.getRelationship() == null || param.getRelationship().equals("or")) {
					subWhere = builder.or(subWhere, builder.gt(path, value));
				} else {
					subWhere = builder.and(subWhere, builder.gt(path, value));
				}
			} else { // (param.getOperator().equalsIgnoreCase(">="))
				if (param.getRelationship() == null || param.getRelationship().equals("or")) {
					subWhere = builder.or(subWhere, builder.ge(path, value));
				} else {
					subWhere = builder.and(subWhere, builder.ge(path, value));
				}
			}
		}
		
		return subWhere;
	}

}
