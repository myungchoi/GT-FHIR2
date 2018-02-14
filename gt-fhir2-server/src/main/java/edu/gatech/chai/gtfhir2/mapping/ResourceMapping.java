package edu.gatech.chai.gtfhir2.mapping;

import java.util.List;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.IdType;

public interface ResourceMapping<v extends DomainResource> {
	public v toFHIR(IdType id);
	public Long toDbase(v Fhir);
	public Long getSize();
}
