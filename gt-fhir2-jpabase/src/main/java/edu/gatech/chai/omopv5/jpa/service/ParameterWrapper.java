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
	private String parameter;
	private String operator;
	private String value;
	
	public ParameterWrapper() {}
	
	public ParameterWrapper(String parameterType, String parameter, String operator, String value) {
		this.parameterType = parameterType;
		this.parameter = parameter;
		this.operator = operator;
		this.value = value;
	}
	
	public String getParameterType() {
		return parameterType;
	}
	
	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}

	public String getParameter() {
		return parameter;
	}
	
	public void setParameter(String parameter) {
		this.parameter = parameter;
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
				switch (param.getParameterType()) {
				case "Short":
					if (param.getOperator().equalsIgnoreCase("="))
						where= builder.and(where, builder.equal(rootUser.get(param.getParameter()), Short.valueOf(param.getValue())));
					else if (param.getOperator().equalsIgnoreCase("<"))
						where= builder.and(where, builder.lessThan(rootUser.get(param.getParameter()), Short.valueOf(param.getValue())));
					else if (param.getOperator().equalsIgnoreCase("<="))
						where= builder.and(where, builder.lessThanOrEqualTo(rootUser.get(param.getParameter()), Short.valueOf(param.getValue())));
					else if (param.getOperator().equalsIgnoreCase(">"))
						where= builder.and(where, builder.greaterThan(rootUser.get(param.getParameter()), Short.valueOf(param.getValue())));
					else if (param.getOperator().equalsIgnoreCase(">="))
						where= builder.and(where, builder.greaterThanOrEqualTo(rootUser.get(param.getParameter()), Short.valueOf(param.getValue())));
					break;
				case "String":
					if (param.getOperator().equalsIgnoreCase("like"))
						where = builder.and(where, builder.like(rootUser.get(param.getParameter()), param.getValue()));
					else
						where = builder.and(where, builder.notLike(rootUser.get(param.getParameter()), param.getValue()));
					break;
				case "Double":
					break;
				}
				
				predicates.add(where);
			}
		}
		
		return predicates;
	}
}
