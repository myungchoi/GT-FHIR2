package edu.gatech.chai.omopv5.jpa.service;


import edu.gatech.chai.omopv5.jpa.entity.FPerson;
import edu.gatech.chai.omopv5.jpa.entity.Location;

public interface FPersonService extends IService<FPerson> {
	public FPerson searchByNameAndLocation(String familyName, String given1Name, String given2Name, Location location);
}
