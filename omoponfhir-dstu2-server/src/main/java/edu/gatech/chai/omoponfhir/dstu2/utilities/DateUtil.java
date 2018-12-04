package edu.gatech.chai.omoponfhir.dstu2.utilities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	public static Date constructDateTime(Date date, String time) {
		DateFormat dateOnlyFormat = new SimpleDateFormat("yyyy/MM/dd");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		
		String timeString = "00:00:00";
		if (time != null && !time.isEmpty()) {
			timeString = time;
		}
		
		String dateTimeString = dateOnlyFormat.format(date) + " " + timeString;
		Date dateTime = null;
		try {
			dateTime = dateFormat.parse(dateTimeString);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		
		return dateTime;
	}
}
