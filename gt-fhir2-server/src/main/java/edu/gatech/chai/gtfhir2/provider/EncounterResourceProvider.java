package edu.gatech.chai.gtfhir2.provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
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
	
	public static String getType() {
		return "Encounter";
	}

    public OmopEncounter getMyMapper() {
    	return myMapper;
    }

	private Integer getTotalSize(List<ParameterWrapper> paramList) {
		final Long totalSize;
		if (paramList.size() == 0) {
			totalSize = getMyMapper().getSize();
		} else {
			totalSize = getMyMapper().getSize(paramList);
		}
		
		return totalSize.intValue();
	}

	/**
	 * The "@Create" annotation indicates that this method implements "create=type", which adds a 
	 * new instance of a resource to the server.
	 */
	@Create()
	public MethodOutcome createEncounter(@ResourceParam Encounter theEncounter) {
		validateResource(theEncounter);
		
		Long id=null;
		try {
			id = myMapper.toDbase(theEncounter, null);
		} catch (FHIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return new MethodOutcome(new IdDt(id));
	}

	@Delete()
	public void deleteEncounter(@IdParam IdType theId) {
		if (myMapper.removeByFhirId(theId) <= 0) {
			throw new ResourceNotFoundException(theId);
		}
	}


	@Search()
	public IBundleProvider findEncounterByParams(
			@OptionalParam(name=Encounter.SP_RES_ID) TokenParam theEncounterId,

			@IncludeParam(allow={"Encounter:appointment", "Encounter:diagnosis", 
					"Encounter:episodeofcare", "Encounter:incomingreferral", "Encounter:location", 
					"Encounter:part-of", "Encounter:participant", "Encounter:service-provider",
					"Encounter:patient", "Encounter:practitioner", "Encounter:subject"})
			final Set<Include> theIncludes,
			
			@IncludeParam(reverse=true)
            final Set<Include> theReverseIncludes
			) {
//		final InstantType searchTime = InstantType.withCurrentTime();
		
		List<ParameterWrapper> paramList = new ArrayList<ParameterWrapper> ();

		if (theEncounterId != null) {
			paramList.addAll(myMapper.mapParameter (Encounter.SP_RES_ID, theEncounterId, false));
		}

		// Now finalize the parameter map.
//		final Long totalSize;
//		if (paramList.size() == 0) {
//			totalSize = myMapper.getSize();
//		} else {
//			totalSize = myMapper.getSize(paramList);
//		}

		MyBundleProvider myBundleProvider = new MyBundleProvider(paramList, theIncludes, theReverseIncludes);
		myBundleProvider.setTotalSize(getTotalSize(paramList));
		myBundleProvider.setPreferredPageSize(preferredPageSize);
		
		return myBundleProvider;
		
//		return new IBundleProvider() {

//			@Override
//			public IPrimitiveType<Date> getPublished() {
//				return searchTime;
//			}
//
//			@Override
//			public List<IBaseResource> getResources(int fromIndex, int toIndex) {
//				List<IBaseResource> retv = new ArrayList<IBaseResource>();
//				
//				// _Include
//				List<String> includes = new ArrayList<String>();
//				
//				if (theIncludes.contains(new Include("Encounter:appointment"))) {
//					includes.add("Encounter:appointment");
//				}
//				
//				if (theIncludes.contains(new Include("Encounter:diagnosis"))) {
//					includes.add("Encounter:diagnosis");
//				}
//
//				if (theIncludes.contains(new Include("Encounter:incomingreferral"))) {
//					includes.add("Encounter:incomingreferral");
//				}
//				
//				if (theIncludes.contains(new Include("Encounter:location"))) {
//					includes.add("Encounter:location");
//				}
//
//				if (theIncludes.contains(new Include("Encounter:part-of"))) {
//					includes.add("Encounter:part-of");
//				}
//
//				if (theIncludes.contains(new Include("Encounter:participant"))) {
//					includes.add("Encounter:participant");
//				}
//
//				if (theIncludes.contains(new Include("Encounter:service-provider"))) {
//					includes.add("Encounter:service-provider");
//				}
//
//				if (theIncludes.contains(new Include("Encounter:patient"))) {
//					includes.add("Encounter:patient");
//				}
//
//				if (theIncludes.contains(new Include("Encounter:practitioner"))) {
//					includes.add("Encounter:practitioner");
//				}
//				
//				if (theIncludes.contains(new Include("Encounter:subject"))) {
//					includes.add("Encounter:subject");
//				}
//				
//				if (finalParamMap.size() == 0) {
//					myMapper.searchWithoutParams(fromIndex, toIndex, retv, includes);
//				} else {
//					myMapper.searchWithParams(fromIndex, toIndex, finalParamMap, retv, includes);
//				}
//
//				return retv;
//			}
//
//			@Override
//			public String getUuid() {
//				// TODO Auto-generated method stub
//				return null;
//			}
//
//			@Override
//			public Integer preferredPageSize() {
//				return preferredPageSize;
//			}
//
//			@Override
//			public Integer size() {
//				return totalSize.intValue();
//			}
		
//		};
	}
	
	/**
	 * This is the "read" operation. The "@Read" annotation indicates that this method supports the read and/or vread operation.
	 * <p>
	 * Read operations take a single parameter annotated with the {@link IdParam} paramater, and should return a single resource instance.
	 * </p>
	 * 
	 * @param theId
	 *            The read operation takes one parameter, which must be of type IdDt and must be annotated with the "@Read.IdParam" annotation.
	 * @return Returns a resource matching this identifier, or null if none exists.
	 */
	@Read()
	public Encounter readEncounter(@IdParam IdType theId) {
		Encounter retval = (Encounter) myMapper.toFHIR(theId);
		if (retval == null) {
			throw new ResourceNotFoundException(theId);
		}
			
		return retval;
	}

	/**
	 * The "@Update" annotation indicates that this method supports replacing an existing 
	 * resource (by ID) with a new instance of that resource.
	 * 
	 * @param theId
	 *            This is the ID of the patient to update
	 * @param thePatient
	 *            This is the actual resource to save
	 * @return This method returns a "MethodOutcome"
	 */
	@Update()
	public MethodOutcome updateObservation(@IdParam IdType theId, @ResourceParam Encounter theEncounter) {
		validateResource(theEncounter);

		Long fhirId = null;
		try {
			fhirId = myMapper.toDbase(theEncounter, theId);
		} catch (FHIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (fhirId == null) {
			throw new ResourceNotFoundException(theId);
		}
		
		return new MethodOutcome();
	}

	// TODO: Add more validation code here.
	private void validateResource(Encounter theEncounter) {
	}

	@Override
	public Class<Encounter> getResourceType() {
		// TODO Auto-generated method stub
		return Encounter.class;
	}

	class MyBundleProvider extends OmopFhirBundleProvider implements IBundleProvider {
		Set<Include> theIncludes;
		Set<Include> theReverseIncludes;

		public MyBundleProvider(List<ParameterWrapper> paramList, Set<Include> theIncludes, Set<Include>theReverseIncludes) {
			super(paramList);
			setPreferredPageSize (preferredPageSize);
			this.theIncludes = theIncludes;
			this.theReverseIncludes = theReverseIncludes;
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
			
			if (paramList.size() == 0) {
				myMapper.searchWithoutParams(fromIndex, toIndex, retv, includes);
			} else {
				myMapper.searchWithParams(fromIndex, toIndex, paramList, retv, includes);
			}

			return retv;
		}		
	}
}
