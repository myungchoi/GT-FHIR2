package edu.gatech.chai.omopv5.jpa.service;

import edu.gatech.chai.omopv5.jpa.entity.CareSite;
import edu.gatech.chai.omopv5.jpa.entity.Location;

public interface CareSiteService extends IService<CareSite> {
	public CareSite searchByNameAndLocation(String careSiteName, Location location);
}
