package com.jfetek.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jfetek.common.time.Date;
import com.jfetek.common.time.DateTime;
import com.jfetek.common.time.Time;


public class TimeUtil {
	
	public static final String	FORMAL_DATE_TIME_PATTERN_STR	= "(\\d{4})\\-(\\d{1,2})\\-(\\d{1,2}) (\\d{1,2})\\:(\\d{1,2})\\:(\\d{1,2})(\\.(\\d{1,3}))?";
	public static final Pattern	PATTERN_FORMAL_DATE_TIME	= Pattern.compile(FORMAL_DATE_TIME_PATTERN_STR);
	
	//                                                     1         2            3            4         5            6         7   8           9   10          11  12
	public static final String	DATE_TIME_PATTERN_STR	= "(\\d{2,4})([\\/\\-\\.])(\\d{1,2})\\2(\\d{1,2})([ \\/\\:\\_](\\d{1,2})(\\:(\\d{1,2}))?(\\:(\\d{1,2}))?(\\.(\\d{1,3}))?)?";
	public static final Pattern	PATTERN_DATE_TIME	= Pattern.compile(DATE_TIME_PATTERN_STR);

	
	public static final String	FORMAL_DATE_PATTERN_STR	= "(\\d{4})\\-(\\d{1,2})\\-(\\d{1,2})";
	public static final Pattern	PATTERN_FORMAL_DATE	= Pattern.compile(FORMAL_DATE_PATTERN_STR);
	
	public static final String	DATE_PATTERN_STR	= "(\\d{2,4})([\\/\\-\\.])(\\d{1,2})\\2(\\d{1,2})";
	public static final Pattern	PATTERN_DATE	= Pattern.compile(DATE_PATTERN_STR);
	
	
	public static final String	FORMAL_TIME_PATTERN_STR	= "(\\d{1,2})\\:(\\d{1,2})\\:(\\d{1,2})(\\.(\\d{1,3}))";
	public static final Pattern	PATTERN_FORMAL_TIME	= Pattern.compile(FORMAL_TIME_PATTERN_STR);
	
	public static final String	TIME_PATTERN_STR	= "(\\d{1,2})\\:(\\d{1,2})(\\:(\\d{1,2}))?(\\.(\\d{1,3}))?";
	public static final Pattern	PATTERN_TIME	= Pattern.compile(TIME_PATTERN_STR);
	
	
	

	private TimeUtil() {
	}
	
//	public static DateRange getDateRange(int year) {
//	}
//	
//	public static DateTimeRange getDateTimeRange(int year) {
//	}
//
	public static boolean isLeapYear(int year) {
		if ((year & 3) != 0) return false;
		return (year % 100 != 0) || (year % 400 == 0); // Gregorian
	}
//
//	public static boolean isTheSameDay(long ts1, long ts2) {
//		Date d1 = new Date(ts1);
//		Date d2 = new Date(ts2);
//		return d1.equals( d2 );
//	}
//	
//	public static boolean isTheSameDay(java.util.Date d1, java.util.Date d2) {
//		Date day1 = new Date(d1);
//		Date day2 = new Date(d2);
//		return day1.equals( day2 );
//	}
	
	
	public static DateTime parse(String s) {
		if (null == s || s.length() < 8) return null;
		Matcher m = PATTERN_DATE_TIME.matcher(s);
		DateTime datetime = null;
		if (m.find()) {
			int year = TextUtil.intValue( m.group(1) );
			int month = TextUtil.intValue( m.group(3) );
			int day = TextUtil.intValue( m.group(4) );
			int hour = TextUtil.intValue( m.group(6) );
			int minute = TextUtil.intValue( m.group(8) , 0 );
			int second = TextUtil.intValue( m.group(10) , 0 );
			int millis = TextUtil.intValue( m.group(12) , 0 );

			if (year < 100) year += 2000;	// base 2000
			datetime = new DateTime(year, month, day, hour, minute, second, millis);
		}
		return datetime;
	}
	
	public static void main(String[] args) {
		DateTime dt = DateTime.now();
		System.out.println(dt);
		dt = parse(dt.toString());
		System.out.println(dt);
		
		String s = "13/2/2 4:5.100";
		System.out.println(parse(s).toText(true));
		System.out.println(parseDate(s).toText());
		System.out.println(parseTime(s).toText(true));
	}


	public static Date parseDate(String s) {
		if (null == s || s.length() < 5) return null;
		Matcher m = PATTERN_DATE.matcher(s);
		Date date = null;
		if (m.find()) {
			int year = TextUtil.intValue( m.group(1) );
			int month = TextUtil.intValue( m.group(3) );
			int day = TextUtil.intValue( m.group(4) );
			
			if (year < 100) year += 2000;	// base 2000
			date = new Date(year, month, day);
		}
		return date;
	}
	
	public static Time parseTime(String s) {
		if (null == s || s.length() < 5) return null;
		Matcher m = PATTERN_TIME.matcher(s);
		Time time = null;
		if (m.find()) {
			int hour = TextUtil.intValue( m.group(1) );
			int minute = TextUtil.intValue( m.group(2) );
			int second = TextUtil.intValue( m.group(4) , 0 );
			int millis = TextUtil.intValue( m.group(6) , 0 );
			
			time = new Time(hour, minute, second, millis);
		}
		return time;
	}
	
}
