package edu.gatech.chai.omoponfhir.stu3.utilities;

import java.util.List;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.StringType;

import edu.gatech.chai.omopv5.jpa.entity.Location;
import edu.gatech.chai.omopv5.jpa.service.LocationService;

public class AddressUtil {
	/**
	 * 
	 * @param locationService : Class to JPA service
	 * @param address : FHIR address data
	 * @param location : Location entity class in OMOP
	 * @return : Location class found. Null if not found
	 */
	public static Location searchAndUpdate(LocationService locationService, Address address, Location location) {
		if (address == null)
			return null;

		List<StringType> addressLines = address.getLine();
		String line1 = null;
		String line2 = null;
		if (addressLines.size() > 0) {
			line1 = addressLines.get(0).getValue();
			if (address.getLine().size() > 1) {
				line2 = address.getLine().get(1).getValue();
			}
		}
		String zipCode = address.getPostalCode();
		String city = address.getCity();
		String state = address.getState();

		Location existingLocation = locationService.searchByAddress(line1, line2, city, state, zipCode);
		if (existingLocation != null) {
			return existingLocation;
		} else {
			// We will return new Location. But, if Location is provided,
			// then we update the parameters here.
			if (location != null) {
				location.setAddress1(line1);
				if (line2 != null)
					location.setAddress2(line2);
				location.setZipCode(zipCode);
				location.setCity(city);
				location.setState(state);
			} else {
				return new Location(line1, line2, city, state, zipCode);
			}
		}

		return null;
	}
}
