package edu.gatech.chai.gtfhir2.provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;

import edu.gatech.chai.gtfhir2.mapping.OmopEncounter;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;

public class EncounterResourceProvider implements IResourceProvider {

	private WebApplicationContext myAppCtx;
	private String myDbType;
	private OmopEncounter myMapper;
	private int preferredPageSize = 30;

	public EncounterResourceProvider() {
		myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
		myDbType = myAppCtx.getServletContext().getInitParameter("backendDbType");
		if (myDbType.equalsIgnoreCase("omopv5") == true) {
			myMapper = new OmopEncounter(myAppCtx);
		} else {
			myMapper = new OmopEncounter(myAppCtx);
		}
		
		String pageSizeStr = myAppCtx.getServletContext().getInitParameter("preferredPageSize");
		if (pageSizeStr != null && pageSizeStr.isEmpty() == false) {
			int pageSize = Integer.parseInt(pageSizeStr);
			if (pageSize > 0) {
				preferredPageSize = pageSize;
			} 
		}		
	}
	
	/**
	 * The "@Create" annotation indicates that this method implements "create=type", which adds a 
	 * new instance of a resource to the server.
	 */
	@Create()
	public MethodOutcome createPatient(@ResourceParam Encounter theEncounter) {
		validateResource(theEncounter);
		
		Long id = myMapper.toDbase(theEncounter);		
		return new MethodOutcome(new IdDt(id));
	}

	@Search()
	public IBundleProvider findPatientsByParams(
			@OptionalParam(name=Encounter.SP_RES_ID) TokenParam theEncounterId,

			@IncludeParam(allow={"Encounter:appointment", "Encounter:diagnosis", 
					"Encounter:episodeofcare", "Encounter:incomingreferral", "Encounter:location", 
					"Encounter:part-of", "Encounter:participant", "Encounter:service-provider",
					"Encounter:patient", "Encounter:practitioner", "Encounter:subject"})
			final Set<Include> theIncludes,
			
			@IncludeParam(reverse=true)
            final Set<Include> theReverseIncludes
			) {
		final InstantType searchTime = InstantType.withCurrentTime();
		
		Map<String, List<ParameterWrapper>> paramMap = new HashMap<String, List<ParameterWrapper>> ();

		if (theEncounterId != null) {
			mapParameter (paramMap, Encounter.SP_RES_ID, theEncounterId);
		}

		// Now finalize the parameter map.
		final Map<String, List<ParameterWrapper>> finalParamMap = paramMap;
		final Long totalSize;
		if (paramMap.size() == 0) {
			totalSize = myMapper.getSize();
		} else {
			totalSize = myMapper.getSize(finalParamMap);
		}

		return new IBundleProvider() {

			@Override
			public IPrimitiveType<Date> getPublished() {
				return searchTime;
			}

			@Override
			public List<IBaseResource> getResources(int fromIndex, int toIndex) {
				List<IBaseResource> retv = new ArrayList<IBaseResource>();
				
				// _Include
				List<String> includes = new ArrayList<String>();
				
				if (theIncludes.contains(new Include("Encounter:appointment"))) {
					includes.add("Encounter:appointment");
				}
				
				if (theIncludes.contains(new Include("Encounter:diagnosis"))) {
					includes.add("Encounter:diagnosis");
				}

				if (theIncludes.contains(new Include("Encounter:incomingreferral"))) {
					includes.add("Encounter:incomingreferral");
				}
				
				if (theIncludes.contains(new Include("Encounter:location"))) {
					includes.add("Encounter:location");
				}

				if (theIncludes.contains(new Include("Encounter:part-of"))) {
					includes.add("Encounter:part-of");
				}

				if (theIncludes.contains(new Include("Encounter:participant"))) {
					includes.add("Encounter:participant");
				}

				if (theIncludes.contains(new Include("Encounter:service-provider"))) {
					includes.add("Encounter:service-provider");
				}

				if (theIncludes.contains(new Include("Encounter:patient"))) {
					includes.add("Encounter:patient");
				}

				if (theIncludes.contains(new Include("Encounter:practitioner"))) {
					includes.add("Encounter:practitioner");
				}
				
				if (theIncludes.contains(new Include("Encounter:subject"))) {
					includes.add("Encounter:subject");
				}
				
				if (finalParamMap.size() == 0) {
					myMapper.searchWithoutParams(fromIndex, toIndex, retv, includes);
				} else {
					myMapper.searchWithParams(fromIndex, toIndex, finalParamMap, retv, includes);
				}

				return retv;
			}

			@Override
			public String getUuid() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Integer preferredPageSize() {
				return preferredPageSize;
			}

			@Override
			public Integer size() {
				return totalSize.intValue();
			}
		
		};
	}
	
	private void mapParameter(Map<String, List<ParameterWrapper>> paramMap, String FHIRparam, Object value) {
		List<ParameterWrapper> paramList = myMapper.mapParameter(FHIRparam, value);
		if (paramList != null) {
			paramMap.put(FHIRparam, paramList);
		}
	}
	
	// TODO: Add more validation code here.
	private void validateResource(Encounter theEncounter) {
	}

	@Override
	public Class<Encounter> getResourceType() {
		// TODO Auto-generated method stub
		return Encounter.class;
	}

}
