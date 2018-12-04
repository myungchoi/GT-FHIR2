package edu.gatech.chai.omoponfhir.dstu2.utilities;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hl7.fhir.dstu3.model.HumanName;

public class NameUtility {
	private static final String stdNameTextFormat = "(\\w+), (\\w+)";
	private static final Pattern stdNamePattern =  Pattern.compile(stdNameTextFormat);
	private static final String stdNameFormat = "{0}, {1}";
	public static HumanName nametoFHIRHumanName(String family, String given) {
		HumanName retVal = new HumanName();
		retVal.setFamily(family);
		retVal.addGiven(given);
		return retVal;
	}
	
	public static HumanName nametoFHIRHumanName(String text) {
		Matcher stdNameMatcher = stdNamePattern.matcher(text);
		String family = null;
		String given = null;
		if(stdNameMatcher.group(1) != null) {
			family = stdNameMatcher.group(1);
		}
		if(stdNameMatcher.group(2) != null) {
			given = stdNameMatcher.group(2);
		}
		return nametoFHIRHumanName(family,given);
	}
	
	public static String givenFamilyToText(String family, String given) {
		return MessageFormat.format(stdNameFormat, family, given);
	}
}
