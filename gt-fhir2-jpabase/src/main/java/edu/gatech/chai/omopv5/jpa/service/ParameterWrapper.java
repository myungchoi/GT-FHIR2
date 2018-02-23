package edu.gatech.chai.omopv5.jpa.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
	private String parameterType;
	private List<String> parameters;
	private List<String> operators;
	private List<String> values;
	private String relationship;

	public ParameterWrapper() {
	}

	public ParameterWrapper(String parameterType, List<String> parameters, List<String> operators,
			List<String> values) {
		this.parameterType = parameterType;
		this.parameters = parameters;
		this.operators = operators;
		this.values = values;
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
				case "Short":
					if (param.getParameters().size() > 1) {
						Short shortValue = Short.valueOf(param.getValues().get(0));

						// We may have multiple columns to compare with 'or'. If
						// so, get them now.
						// for (String columnName : param.getParameters(),
						// String oper: param.getOperators()) {
						for (Iterator<String> columnIter = param.getParameters().iterator(), operIter = param
								.getOperators().iterator(); columnIter.hasNext() && operIter.hasNext();) {
							String columnName = columnIter.next();
							String oper = operIter.next();

							Path<Short> path;
							String[] columnPath = columnName.split("\\.");
							if (columnPath.length == 2) {
								path = rootUser.get(columnPath[0]).get(columnPath[1]);
							} else {
								path = rootUser.get(columnName);
							}
							
							if (oper.equalsIgnoreCase("=")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.equal(path, shortValue));
								} else {
									subWhere = builder.and(subWhere, builder.equal(path, shortValue));
								}
							} else if (oper.equalsIgnoreCase("!=")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.notEqual(path, shortValue));
								} else {
									subWhere = builder.and(subWhere, builder.notEqual(path, shortValue));
								}
							} else if (oper.equalsIgnoreCase("<"))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.lessThan(path, shortValue));
								} else {
									subWhere = builder.and(subWhere, builder.lessThan(path, shortValue));
								}
							else if (oper.equalsIgnoreCase("<="))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.lessThanOrEqualTo(path, shortValue));
								} else {
									subWhere = builder.and(subWhere, builder.lessThanOrEqualTo(path, shortValue));
								}
							else if (oper.equalsIgnoreCase(">")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.greaterThan(path, shortValue));
								} else {
									subWhere = builder.and(subWhere, builder.greaterThan(path, shortValue));
								}
							} else { // (param.getOperator().equalsIgnoreCase(">="))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.greaterThanOrEqualTo(path, shortValue));
								} else {
									subWhere = builder.and(subWhere, builder.greaterThanOrEqualTo(path, shortValue));
								}
							}
						}
					} else {
						String columnName = param.getParameters().get(0);
						Path<Short> path;
						String[] columnPath = columnName.split("\\.");
						if (columnPath.length == 2) {
							path = rootUser.get(columnPath[0]).get(columnPath[1]);
						} else {
							path = rootUser.get(columnName);
						}

						for (Iterator<String> valueIter = param.getValues().iterator(), operIter = param.getOperators()
								.iterator(); valueIter.hasNext() && operIter.hasNext();) {
							String oper = operIter.next();
							String valueString = valueIter.next();
							if (oper.equalsIgnoreCase("=")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.equal(path, Short.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.equal(path, Short.valueOf(valueString)));
								}
							} else if (oper.equalsIgnoreCase("!=")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.notEqual(path, Short.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.notEqual(path, Short.valueOf(valueString)));
								}
							} else if (oper.equalsIgnoreCase("<"))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.lessThan(path, Short.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.lessThan(path, Short.valueOf(valueString)));
								}
							else if (oper.equalsIgnoreCase("<="))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.lessThanOrEqualTo(path, Short.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.lessThanOrEqualTo(path, Short.valueOf(valueString)));
								}
							else if (oper.equalsIgnoreCase(">")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.greaterThan(path, Short.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.greaterThan(path, Short.valueOf(valueString)));
								}
							} else { // (param.getOperator().equalsIgnoreCase(">="))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.greaterThanOrEqualTo(path, Short.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.greaterThanOrEqualTo(path, Short.valueOf(valueString)));
								}
							}
						}
					}
					break;
				case "String":
					if (param.getParameters().size() > 1) {
						String valueString = param.getValues().get(0);
						
						for (Iterator<String> columnIter = param.getParameters().iterator(), operIter = param.getOperators().iterator();
								columnIter.hasNext() && operIter.hasNext();) {
							String columnName = columnIter.next();
							String oper = operIter.next();
							
							Path<String> path;
							String[] columnPath = columnName.split("\\.");
							if (columnPath.length == 2) {
								path = rootUser.get(columnPath[0]).get(columnPath[1]);
							} else {
								path = rootUser.get(columnName);
							}

							if (oper.equalsIgnoreCase("like"))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.like(path, valueString));
								} else {
									subWhere = builder.and(subWhere, builder.like(path, valueString));
								}
							else
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.notLike(path, valueString));
								} else {
									subWhere = builder.and(subWhere, builder.notLike(path, valueString));
								}
						}
					} else {
						String columnName = param.getParameters().get(0);
						Path<String> path;
						String[] columnPath = columnName.split("\\.");
						if (columnPath.length == 2) {
							path = rootUser.get(columnPath[0]).get(columnPath[1]);
						} else {
							path = rootUser.get(columnName);
						}

						for (Iterator<String> valueIter = param.getValues().iterator(), operIter = param.getOperators().iterator();
								valueIter.hasNext() && operIter.hasNext();) {
							String valueString = valueIter.next();
							String oper = operIter.next();
							
							if (oper.equalsIgnoreCase("like"))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.like(path, valueString));
								} else {
									subWhere = builder.and(subWhere, builder.like(path, valueString));
								}
							else {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.notLike(path, valueString));
								} else {
									subWhere = builder.and(subWhere, builder.notLike(path, valueString));
								}
							}
						}
					}
					break;
				case "Long":
					if (param.getParameters().size() > 1) {
						Long longValue = Long.valueOf(param.getValues().get(0));

						// We may have multiple columns to compare with 'or'. If
						// so, get them now.
						// for (String columnName : param.getParameters(),
						// String oper: param.getOperators()) {
						for (Iterator<String> columnIter = param.getParameters().iterator(), operIter = param
								.getOperators().iterator(); columnIter.hasNext() && operIter.hasNext();) {
							String columnName = columnIter.next();
							String oper = operIter.next();

							Path<Long> path;
							String[] columnPath = columnName.split("\\.");
							if (columnPath.length == 2) {
								path = rootUser.get(columnPath[0]).get(columnPath[1]);
							} else {
								path = rootUser.get(columnName);
							}
							
							if (oper.equalsIgnoreCase("=")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.equal(path, longValue));
								} else {
									subWhere = builder.and(subWhere, builder.equal(path, longValue));
								}
							} else if (oper.equalsIgnoreCase("!=")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.notEqual(path, longValue));
								} else {
									subWhere = builder.and(subWhere, builder.notEqual(path, longValue));
								}
							} else if (oper.equalsIgnoreCase("<"))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.lessThan(path, longValue));
								} else {
									subWhere = builder.and(subWhere, builder.lessThan(path, longValue));
								}
							else if (oper.equalsIgnoreCase("<="))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.lessThanOrEqualTo(path, longValue));
								} else {
									subWhere = builder.and(subWhere, builder.lessThanOrEqualTo(path, longValue));
								}
							else if (oper.equalsIgnoreCase(">")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.greaterThan(path, longValue));
								} else {
									subWhere = builder.and(subWhere, builder.greaterThan(path, longValue));
								}
							} else { // (param.getOperator().equalsIgnoreCase(">="))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.greaterThanOrEqualTo(path, longValue));
								} else {
									subWhere = builder.and(subWhere, builder.greaterThanOrEqualTo(path, longValue));
								}
							}
						}
					} else {
						String columnName = param.getParameters().get(0);
						Path<Long> path;
						String[] columnPath = columnName.split("\\.");
						if (columnPath.length == 2) {
							path = rootUser.get(columnPath[0]).get(columnPath[1]);
						} else {
							path = rootUser.get(columnName);
						}

						for (Iterator<String> valueIter = param.getValues().iterator(), operIter = param.getOperators()
								.iterator(); valueIter.hasNext() && operIter.hasNext();) {
							String oper = operIter.next();
							String valueString = valueIter.next();
							if (oper.equalsIgnoreCase("=")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.equal(path, Long.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.equal(path, Long.valueOf(valueString)));
								}
							} else if (oper.equalsIgnoreCase("!=")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.notEqual(path, Long.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.notEqual(path, Long.valueOf(valueString)));
								}
							} else if (oper.equalsIgnoreCase("<"))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.lessThan(path, Long.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.lessThan(path, Long.valueOf(valueString)));
								}
							else if (oper.equalsIgnoreCase("<="))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.lessThanOrEqualTo(path, Long.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.lessThanOrEqualTo(path, Long.valueOf(valueString)));
								}
							else if (oper.equalsIgnoreCase(">")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.greaterThan(path, Long.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.greaterThan(path, Long.valueOf(valueString)));
								}
							} else { // (param.getOperator().equalsIgnoreCase(">="))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.greaterThanOrEqualTo(path, Long.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.greaterThanOrEqualTo(path, Long.valueOf(valueString)));
								}
							}
						}
					}
					break;
				case "Double":
					if (param.getParameters().size() > 1) {
						Double doubleValue = Double.valueOf(param.getValues().get(0));

						// We may have multiple columns to compare with 'or'. If
						// so, get them now.
						// for (String columnName : param.getParameters(),
						// String oper: param.getOperators()) {
						for (Iterator<String> columnIter = param.getParameters().iterator(), operIter = param
								.getOperators().iterator(); columnIter.hasNext() && operIter.hasNext();) {
							String columnName = columnIter.next();
							String oper = operIter.next();

							Path<Double> path;
							String[] columnPath = columnName.split("\\.");
							if (columnPath.length == 2) {
								path = rootUser.get(columnPath[0]).get(columnPath[1]);
							} else {
								path = rootUser.get(columnName);
							}
							
							if (oper.equalsIgnoreCase("=")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.equal(path, doubleValue));
								} else {
									subWhere = builder.and(subWhere, builder.equal(path, doubleValue));
								}
							} else if (oper.equalsIgnoreCase("!=")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.notEqual(path, doubleValue));
								} else {
									subWhere = builder.and(subWhere, builder.notEqual(path, doubleValue));
								}
							} else if (oper.equalsIgnoreCase("<"))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.lessThan(path, doubleValue));
								} else {
									subWhere = builder.and(subWhere, builder.lessThan(path, doubleValue));
								}
							else if (oper.equalsIgnoreCase("<="))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.lessThanOrEqualTo(path, doubleValue));
								} else {
									subWhere = builder.and(subWhere, builder.lessThanOrEqualTo(path, doubleValue));
								}
							else if (oper.equalsIgnoreCase(">")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.greaterThan(path, doubleValue));
								} else {
									subWhere = builder.and(subWhere, builder.greaterThan(path, doubleValue));
								}
							} else { // (param.getOperator().equalsIgnoreCase(">="))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.greaterThanOrEqualTo(path, doubleValue));
								} else {
									subWhere = builder.and(subWhere, builder.greaterThanOrEqualTo(path, doubleValue));
								}
							}
						}
					} else {
						String columnName = param.getParameters().get(0);
						Path<Double> path;
						String[] columnPath = columnName.split("\\.");
						if (columnPath.length == 2) {
							path = rootUser.get(columnPath[0]).get(columnPath[1]);
						} else {
							path = rootUser.get(columnName);
						}

						for (Iterator<String> valueIter = param.getValues().iterator(), operIter = param.getOperators()
								.iterator(); valueIter.hasNext() && operIter.hasNext();) {
							String oper = operIter.next();
							String valueString = valueIter.next();
							if (oper.equalsIgnoreCase("=")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.equal(path, Double.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.equal(path, Double.valueOf(valueString)));
								}
							} else if (oper.equalsIgnoreCase("!=")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.notEqual(path, Double.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.notEqual(path, Double.valueOf(valueString)));
								}
							} else if (oper.equalsIgnoreCase("<"))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.lessThan(path, Double.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.lessThan(path, Double.valueOf(valueString)));
								}
							else if (oper.equalsIgnoreCase("<="))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.lessThanOrEqualTo(path, Double.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.lessThanOrEqualTo(path, Double.valueOf(valueString)));
								}
							else if (oper.equalsIgnoreCase(">")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.greaterThan(path, Double.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.greaterThan(path, Double.valueOf(valueString)));
								}
							} else { // (param.getOperator().equalsIgnoreCase(">="))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.greaterThanOrEqualTo(path, Double.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.greaterThanOrEqualTo(path, Double.valueOf(valueString)));
								}
							}
						}
					}
					break;
				case "Integer":
					if (param.getParameters().size() > 1) {
						Integer integerValue = Integer.valueOf(param.getValues().get(0));

						// We may have multiple columns to compare with 'or'. If
						// so, get them now.
						// for (String columnName : param.getParameters(),
						// String oper: param.getOperators()) {
						for (Iterator<String> columnIter = param.getParameters().iterator(), operIter = param
								.getOperators().iterator(); columnIter.hasNext() && operIter.hasNext();) {
							String columnName = columnIter.next();
							String oper = operIter.next();

							Path<Integer> path;
							String[] columnPath = columnName.split("\\.");
							if (columnPath.length == 2) {
								path = rootUser.get(columnPath[0]).get(columnPath[1]);
							} else {
								path = rootUser.get(columnName);
							}
							
							if (oper.equalsIgnoreCase("=")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.equal(path, integerValue));
								} else {
									subWhere = builder.and(subWhere, builder.equal(path, integerValue));
								}
							} else if (oper.equalsIgnoreCase("!=")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.notEqual(path, integerValue));
								} else {
									subWhere = builder.and(subWhere, builder.notEqual(path, integerValue));
								}
							} else if (oper.equalsIgnoreCase("<"))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.lessThan(path, integerValue));
								} else {
									subWhere = builder.and(subWhere, builder.lessThan(path, integerValue));
								}
							else if (oper.equalsIgnoreCase("<="))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.lessThanOrEqualTo(path, integerValue));
								} else {
									subWhere = builder.and(subWhere, builder.lessThanOrEqualTo(path, integerValue));
								}
							else if (oper.equalsIgnoreCase(">")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.greaterThan(path, integerValue));
								} else {
									subWhere = builder.and(subWhere, builder.greaterThan(path, integerValue));
								}
							} else { // (param.getOperator().equalsIgnoreCase(">="))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.greaterThanOrEqualTo(path, integerValue));
								} else {
									subWhere = builder.and(subWhere, builder.greaterThanOrEqualTo(path, integerValue));
								}
							}
						}
					} else {
						String columnName = param.getParameters().get(0);
						Path<Integer> path;
						String[] columnPath = columnName.split("\\.");
						if (columnPath.length == 2) {
							path = rootUser.get(columnPath[0]).get(columnPath[1]);
						} else {
							path = rootUser.get(columnName);
						}

						for (Iterator<String> valueIter = param.getValues().iterator(), operIter = param.getOperators()
								.iterator(); valueIter.hasNext() && operIter.hasNext();) {
							String oper = operIter.next();
							String valueString = valueIter.next();
							if (oper.equalsIgnoreCase("=")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.equal(path, Integer.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.equal(path, Integer.valueOf(valueString)));
								}
							} else if (oper.equalsIgnoreCase("!=")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.notEqual(path, Integer.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.notEqual(path, Integer.valueOf(valueString)));
								}
							} else if (oper.equalsIgnoreCase("<"))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.lessThan(path, Integer.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.lessThan(path, Integer.valueOf(valueString)));
								}
							else if (oper.equalsIgnoreCase("<="))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.lessThanOrEqualTo(path, Integer.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.lessThanOrEqualTo(path, Integer.valueOf(valueString)));
								}
							else if (oper.equalsIgnoreCase(">")) {
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.greaterThan(path, Integer.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.greaterThan(path, Integer.valueOf(valueString)));
								}
							} else { // (param.getOperator().equalsIgnoreCase(">="))
								if (param.getRelationship() == null || param.getRelationship().equals("or")) {
									subWhere = builder.or(subWhere, builder.greaterThanOrEqualTo(path, Integer.valueOf(valueString)));
								} else {
									subWhere = builder.and(subWhere, builder.greaterThanOrEqualTo(path, Integer.valueOf(valueString)));
								}
							}
						}
					}					
					break;
				}

				where = builder.and(where, subWhere);
			}
		}
		predicates.add(where);

		return predicates;
	}

}
