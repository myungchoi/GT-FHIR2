package edu.gatech.chai.gtfhir2.mapping;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import edu.gatech.chai.omopv5.jpa.entity.BaseEntity;
import edu.gatech.chai.omopv5.jpa.entity.FObservationView;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;

public interface IResourceMapping<v extends DomainResource, t extends BaseEntity> {
	public v toFHIR(IdType id);
	public Long toDbase(v Fhir);
	public Long getSize();
	public Long getSize(Map<String, List<ParameterWrapper>> map);

	public v constructResource(Long fhirId, t entity, List<String> includes);
	public void searchWithoutParams(int fromIndex, int toIndex, List<IBaseResource> listResources, List<String> includes);
	public void searchWithParams(int fromIndex, int toIndex, Map<String, List<ParameterWrapper>> map, List<IBaseResource> listResources, List<String> includes);

}
