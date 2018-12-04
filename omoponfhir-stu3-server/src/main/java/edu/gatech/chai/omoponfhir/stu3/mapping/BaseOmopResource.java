package edu.gatech.chai.omoponfhir.stu3.mapping;

import java.util.Arrays;
import java.util.List;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.web.context.WebApplicationContext;

import edu.gatech.chai.omopv5.jpa.entity.BaseEntity;
import edu.gatech.chai.omopv5.jpa.service.IService;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;

public abstract class BaseOmopResource<v extends Resource, t extends BaseEntity, p extends IService<t>>
		implements IResourceMapping<v, t> {

	private p myOmopService;
	private Class<t> myEntityClass;
	private Class<p> myServiceClass;
	private String myFhirResourceType;

	public static String MAP_EXCEPTION_FILTER = "FILTER";
	public static String MAP_EXCEPTION_EXCLUDE = "EXCLUDE";

	public BaseOmopResource(WebApplicationContext context, Class<t> entityClass, Class<p> serviceClass,
			String fhirResourceType) {
		myOmopService = context.getBean(serviceClass);
		myEntityClass = entityClass;
		myFhirResourceType = fhirResourceType;
	}

	public String getMyFhirResourceType() {
		return this.myFhirResourceType;
	}

	public p getMyOmopService() {
		return this.myOmopService;
	}

	public void setMyOmopService(WebApplicationContext context) {
		this.myOmopService = context.getBean(myServiceClass);
	}

	public Class<t> getMyEntityClass() {
		return this.myEntityClass;
	}

	public void removeDbase(Long id) {
		myOmopService.removeById(id);
	}

	public Long removeByFhirId(IdType fhirId) {
		Long id_long_part = fhirId.getIdPartAsLong();
		Long myId = IdMapping.getOMOPfromFHIR(id_long_part, getMyFhirResourceType());

		return myOmopService.removeById(myId);
	}

	public Long getSize() {
		return myOmopService.getSize();
	}

	public Long getSize(List<ParameterWrapper> mapList) {
		return myOmopService.getSize(mapList);
	}

	/***
	 * constructResource: Overwrite this if you want to implement includes.
	 */
	public v constructResource(Long fhirId, t entity, List<String> includes) {
		v fhirResource = constructFHIR(fhirId, entity);

		return fhirResource;
	}

	// This needs to be overridden at every Omop[x] class.
//	public v constructFHIR(Long fhirId, t entity) {
//		return null;
//	}

	/***
	 * toFHIR this is called from FHIR provider for read operation.
	 */
	public v toFHIR(IdType id) {
		Long id_long_part = id.getIdPartAsLong();
		Long myId = IdMapping.getOMOPfromFHIR(id_long_part, getMyFhirResourceType());

		t entityClass = (t) getMyOmopService().findById(myId);
		if (entityClass == null)
			return null;

		Long fhirId = IdMapping.getFHIRfromOMOP(myId, getMyFhirResourceType());

		return constructFHIR(fhirId, entityClass);
	}

	public void searchWithoutParams(int fromIndex, int toIndex, List<IBaseResource> listResources,
			List<String> includes) {
		List<t> entities = getMyOmopService().searchWithoutParams(fromIndex, toIndex);

		// We got the results back from OMOP database. Now, we need to construct
		// the list of
		// FHIR Patient resources to be included in the bundle.
		for (t entity : entities) {
			Long omopId = entity.getIdAsLong();
			Long fhirId = IdMapping.getFHIRfromOMOP(omopId, getMyFhirResourceType());
			v fhirResource = constructResource(fhirId, entity, includes);
			if (fhirResource != null) {
				listResources.add(fhirResource);
				addRevIncludes(omopId, includes, listResources);
			}
		}
	}

	public void searchWithParams(int fromIndex, int toIndex, List<ParameterWrapper> mapList,
			List<IBaseResource> listResources, List<String> includes) {
		List<t> entities = getMyOmopService().searchWithParams(fromIndex, toIndex, mapList);

		for (t entity : entities) {
			Long omopId = entity.getIdAsLong();
			Long fhirId = IdMapping.getFHIRfromOMOP(omopId, getMyFhirResourceType());
			v fhirResource = constructResource(fhirId, entity, includes);
			if (fhirResource != null) {
				listResources.add(fhirResource);
				// Do the rev_include and add the resource to the list.
				addRevIncludes(omopId, includes, listResources);
			}
		}
	}

	// Override the this method to provide rev_includes.
	public void addRevIncludes(Long omopId, List<String> includes, List<IBaseResource> listResources) {

	}

	// Some common functions that are repetitively used.
	protected void addParamlistForPatientIDName(String parameter, String value, ParameterWrapper paramWrapper,
			List<ParameterWrapper> mapList) {
		switch (parameter) {
		case "Patient:" + Patient.SP_RES_ID:
			String pId = value;
			paramWrapper.setParameterType("Long");
			paramWrapper.setParameters(Arrays.asList("fPerson.id"));
			paramWrapper.setOperators(Arrays.asList("="));
			paramWrapper.setValues(Arrays.asList(pId));
			paramWrapper.setRelationship("or");
			mapList.add(paramWrapper);
			break;
		case "Patient:" + Patient.SP_NAME:
			String patientName = value.replace("\"", "");
			patientName = patientName.replace("'", "");
			
			paramWrapper.setParameterType("String");
			paramWrapper.setParameters(Arrays.asList("fPerson.familyName", "fPerson.givenName1", "fPerson.givenName2",
					"fPerson.prefixName", "fPerson.suffixName"));
			paramWrapper.setOperators(Arrays.asList("like", "like", "like", "like", "like"));
			paramWrapper.setValues(Arrays.asList("%" + patientName + "%"));
			paramWrapper.setRelationship("or");
			mapList.add(paramWrapper);
			break;
		default:
			return;
		}
	}
}
