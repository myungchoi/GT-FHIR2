package edu.gatech.chai.omopv5.jpa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import edu.gatech.chai.omopv5.jpa.entity.BaseEntity;

public class ParameterWrapper {
	private String parameterType;
	private List<String> parameters;
	private String operator;
	private String value;
	
	public ParameterWrapper() {}
	
	public ParameterWrapper(String parameterType, List<String> parameters, String operator, String value) {
		this.parameterType = parameterType;
		this.parameters = parameters;
		this.operator = operator;
		this.value = value;
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
	
	public String getOperator() {
		return operator;
	}
	
	public void setOperator(String operator) {
		this.operator = operator;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public static List<Predicate> constructPredicate(CriteriaBuilder builder, Map<String, List<ParameterWrapper>> paramMap, Root<? extends BaseEntity> rootUser) {
		List<Predicate> predicates = new ArrayList<Predicate>();
		Predicate where = builder.conjunction();
		
		// paramMap has FHIR parameters mapped Omop parameters (or columns).
		for (Map.Entry<String, List<ParameterWrapper>>entry: paramMap.entrySet()) {
			List<ParameterWrapper> paramWrappers = entry.getValue();
			for (ParameterWrapper param: paramWrappers) {
				Predicate subWhere = builder.disjunction();
				switch (param.getParameterType()) {
				case "Short":
					// Convert the value to Short.
					Short shortValue = Short.valueOf(param.getValue());
					
					// We may have multiple columns to compare with 'or'. If so, get them now.
					for (String columnName : param.getParameters()) {
						if (param.getOperator().equalsIgnoreCase("="))
							subWhere = builder.or(subWhere, builder.equal(rootUser.get(columnName), shortValue));
						else if (param.getOperator().equalsIgnoreCase("<"))
							subWhere = builder.or(subWhere, builder.lessThan(rootUser.get(columnName), shortValue));
						else if (param.getOperator().equalsIgnoreCase("<="))
							subWhere = builder.or(subWhere, builder.lessThanOrEqualTo(rootUser.get(columnName), shortValue));
						else if (param.getOperator().equalsIgnoreCase(">"))
							subWhere = builder.or(subWhere, builder.greaterThan(rootUser.get(columnName), shortValue));
						else if (param.getOperator().equalsIgnoreCase(">="))
							subWhere = builder.or(subWhere, builder.greaterThanOrEqualTo(rootUser.get(columnName), shortValue));
					}
					break;
				case "String":
					// walk thru multiple columns if exists
					for (String columnName : param.getParameters()) {
						if (param.getOperator().equalsIgnoreCase("like"))
							subWhere = builder.or(subWhere, builder.like(rootUser.get(columnName), param.getValue()));
						else
							subWhere = builder.or(subWhere, builder.notLike(rootUser.get(columnName), param.getValue()));
					}
					break;
				case "Double":
					break;
				}
				
				where = builder.and(where,  subWhere);				
			}
		}
		predicates.add(where);

		return predicates;
	}
}
