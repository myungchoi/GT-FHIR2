package edu.gatech.chai.gtfhir2.provider;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Device;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import edu.gatech.chai.gtfhir2.mapping.OmopDevice;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;

public class DeviceResourceProvider implements IResourceProvider {

	private WebApplicationContext myAppCtx;
	private OmopDevice myMapper;
	private String myDbType;
	private int preferredPageSize = 30;

	public DeviceResourceProvider() {
		myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
		myDbType = myAppCtx.getServletContext().getInitParameter("backendDbType");
		if (myDbType.equalsIgnoreCase("omopv5") == true) {
			myMapper = new OmopDevice(myAppCtx);
		} else {
			myMapper = new OmopDevice(myAppCtx);
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
		return "Device";
	}

	/**
	 * The "@Create" annotation indicates that this method implements "create=type", which adds a 
	 * new instance of a resource to the server.
	 */
	@Create()
	public MethodOutcome createDevice(@ResourceParam Device theDevice) {
		validateResource(theDevice);
		
		Long id=null;
		try {
			id = myMapper.toDbase(theDevice, null);
		} catch (FHIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return new MethodOutcome(new IdDt(id));
	}

	@Search()
	public IBundleProvider findDevicesByParams(
			@OptionalParam(name=Device.SP_RES_ID) TokenParam theEncounterId
			) {
		final InstantType searchTime = InstantType.withCurrentTime();

		Map<String, List<ParameterWrapper>> paramMap = new HashMap<String, List<ParameterWrapper>> ();

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
			public List<IBaseResource> getResources(int arg0, int arg1) {
				// TODO Auto-generated method stub
				return null;
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
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Device.class;
	}

	// TODO: Add more validation code here.
	private void validateResource(Device theDevice) {
	}


}
