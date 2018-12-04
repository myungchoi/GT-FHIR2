package edu.gatech.chai.omoponfhir.dstu2.provider;

import org.hl7.fhir.dstu3.model.Resource;

import edu.gatech.chai.omoponfhir.dstu2.mapping.BaseOmopResource;
import edu.gatech.chai.omopv5.jpa.entity.BaseEntity;
import edu.gatech.chai.omopv5.jpa.service.IService;

public class GtFhirBaseResourceProvider
	<v extends Resource, t extends BaseEntity, p extends IService<t>, x extends BaseOmopResource<v, t, p>> {
		private x myMapper;
		
		public x getMyMapper() {
			return myMapper;			
		}
}
