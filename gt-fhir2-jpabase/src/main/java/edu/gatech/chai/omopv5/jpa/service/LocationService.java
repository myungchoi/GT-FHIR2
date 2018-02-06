package edu.gatech.chai.omopv5.jpa.service;

import edu.gatech.chai.omopv5.jpa.entity.Location;

public interface LocationService extends IService<Location> {
	public Location searchByAddress(String line1, String line2, String city, String state, String zipCode);
}
