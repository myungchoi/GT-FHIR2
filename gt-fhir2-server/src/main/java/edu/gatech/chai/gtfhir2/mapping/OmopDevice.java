package edu.gatech.chai.gtfhir2.mapping;

import java.util.List;

import org.hl7.fhir.dstu3.model.Device;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.exceptions.FHIRException;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import edu.gatech.chai.gtfhir2.provider.DeviceResourceProvider;
import edu.gatech.chai.omopv5.jpa.entity.DeviceExposure;
import edu.gatech.chai.omopv5.jpa.service.DeviceExposureService;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;

public class OmopDevice extends BaseOmopResource<Device, DeviceExposure, DeviceExposureService>
		implements IResourceMapping<Device, DeviceExposure> {
	
	public OmopDevice(WebApplicationContext context) {
		super(context, DeviceExposure.class, DeviceExposureService.class, DeviceResourceProvider.getType());
		initialize(context);
	}
	
	public OmopDevice() {
		super(ContextLoaderListener.getCurrentWebApplicationContext(), DeviceExposure.class, DeviceExposureService.class, DeviceResourceProvider.getType());
		initialize(ContextLoaderListener.getCurrentWebApplicationContext());
	}
	
	private void initialize(WebApplicationContext context) {
	}

	@Override
	public Long toDbase(Device fhirResource, IdType fhirId) throws FHIRException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ParameterWrapper> mapParameter(String parameter, Object value) {
		// TODO Auto-generated method stub
		return null;
	}
}
