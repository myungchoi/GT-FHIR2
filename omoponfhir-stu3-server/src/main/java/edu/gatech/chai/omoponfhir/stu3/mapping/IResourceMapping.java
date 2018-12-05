package edu.gatech.chai.omoponfhir.stu3.mapping;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;

import edu.gatech.chai.omopv5.jpa.entity.BaseEntity;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;

public interface IResourceMapping<v extends Resource, t extends BaseEntity> {
	public v toFHIR(IdType id);
	public Long toDbase(v fhirResource, IdType fhirId) throws FHIRException;
	public void removeDbase(Long id);
	public Long removeByFhirId (IdType fhirId) throws FHIRException;
	public Long getSize();
	public Long getSize(List<ParameterWrapper> mapList);

	public v constructResource(Long fhirId, t entity, List<String> includes);
	public void searchWithoutParams(int fromIndex, int toIndex, List<IBaseResource> listResources, List<String> includes);
	public void searchWithParams(int fromIndex, int toIndex, List<ParameterWrapper> map, List<IBaseResource> listResources, List<String> includes);

	public List<ParameterWrapper> mapParameter(String parameter, Object value, boolean or);
	public v constructFHIR(Long fhirId, t entity);
	public t constructOmop(Long omopId, v fhirResource);
}
