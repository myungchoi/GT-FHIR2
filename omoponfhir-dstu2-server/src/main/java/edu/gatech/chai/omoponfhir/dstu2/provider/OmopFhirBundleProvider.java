package edu.gatech.chai.omoponfhir.dstu2.provider;

import java.util.Date;
import java.util.List;

import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.instance.model.api.IPrimitiveType;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;

public abstract class OmopFhirBundleProvider implements IBundleProvider {
	InstantType searchTime;
	List<ParameterWrapper> paramList;
	Integer preferredPageSize;
	Integer totalSize;

	public OmopFhirBundleProvider (List<ParameterWrapper> paramList) {
		this.searchTime = InstantType.withCurrentTime();
		this.paramList = paramList;
	}
	
	public void setPreferredPageSize(Integer preferredPageSize) {
		this.preferredPageSize = preferredPageSize;
	}
	
	public void setTotalSize(Integer totalSize) {
		this.totalSize = totalSize;
	}

	@Override
	public IPrimitiveType<Date> getPublished() {
		return searchTime;
	}

	@Override
	public String getUuid() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer preferredPageSize() {
		return this.preferredPageSize;
	}

	@Override
	public Integer size() {
		return this.totalSize;
	}

}
