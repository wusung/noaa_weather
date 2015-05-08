package com.jfetek.common.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.jfetek.common.util.TextUtil;

public class DateTime implements TimeData, java.io.Serializable, Comparable<DateTime> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5235552640044335212L;


	public static final DateTime	EPOCH	= new DateTime(new Date(0), new Time(0));
//	public static final DateTime	EPOCH	= new DateTime(0);
	
	
	public final Date date;
	public final Time time;
	public final long timestamp;
	
	public DateTime(long ts) {
		this.date = new Date(ts);
		this.time = new Time(ts);
		this.timestamp = ts;
	}
	
//	private DateTime() {
//		this(System.currentTimeMillis());
//	}
	
	public DateTime(Date date, Time time) {
		this.date = date;
		this.time = time;
		this.timestamp = date.timestamp + time.timestamp;
	}
	
	public DateTime(Date date) {
		this(date, Time.first());
	}
	
	public DateTime(Time time) {
		this(Date.today(), time);
	}
	
	public DateTime(java.util.Date d) {
		this.date = new Date(d);
		this.time = new Time(d);
		this.timestamp = d.getTime();
	}

	public DateTime(int year, int month, int day) {
		this(new Date(year, month, day), Time.first());
	}
	
	public DateTime(Year year, Month month, int day) {
		this(new Date(year, month, day), Time.first());
	}

	public DateTime(Year year, Month month, int day, Hour hr, Minute min, Second sec) {
		this(new Date(year, month, day), new Time(hr, min, sec));
	}
	
	public DateTime(Year year, Month month, int day, Hour hr, Minute min, Second sec, int ms) {
		this(new Date(year, month, day), new Time(hr, min, sec, ms));
	}
	
	public DateTime(int year, int month, int day, int hr, int min, int sec) {
		this(new Date(year, month, day), new Time(hr, min, sec));
	}

	public DateTime(int year, int month, int day, int hr, int min, int sec, int ms) {
		this(new Date(year, month, day), new Time(hr, min, sec, ms));
	}
	
	
	public DateTime time(Time time) {
		return new DateTime(this.date, time);
	}
	

	public boolean before(DateTime datetime) {
		return this.timestamp < date.timestamp;
	}
	
	public boolean after(DateTime datetime) {
		return this.timestamp > date.timestamp;
	}
	
	public boolean equals(DateTime datetime) {
		if (null == date) return false;
		return this.timestamp == datetime.timestamp;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return obj instanceof Date ? equals((DateTime)obj) : false;
	}

	@Override
	public int hashCode() {
        return (int) this.timestamp ^ (int) (this.timestamp >> 32);	// copy from java.util.Date
	}

	public int compareTo(DateTime datetime) {
		return (int) (this.timestamp - datetime.timestamp);
	}

	
	public java.sql.Timestamp toSqlTimestamp() {
		return new java.sql.Timestamp(this.timestamp);
	}
	
	public java.sql.Date toSqlDate() {
//		return new java.sql.Date(this.timestamp);
		return this.date.toSqlDate();
	}
	
	public java.sql.Time toSqlTime() {
//		return new java.sql.Time(this.timestamp);
		return this.time.toSqlTime();
	}
	
	public static DateTime now() {
		return new DateTime(Date.today(), new Time());
	}
	
	public String toString() {
		return toText(false);
	}

	public String toText(boolean ms) {
		StringBuilder s = new StringBuilder(19);
		s.append(this.date).append(' ').append(this.time.toText(ms));
		return s.toString();
	}
	
	public String toText(String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(this.toSqlTimestamp());
	}
	

	public static DateTime valueOf(String s) {
		// format: yyyy-MM-dd HH:mm:ss
		if (s != null && s.matches( "^\\d{4}\\-\\d{1,2}\\-\\d{1,2} \\d{1,2}\\:\\d{1,2}\\:\\d{1,2}(.\\d{1,3})?$" )) {
			int idxDash = s.indexOf('-');
			int idxLastDash = s.lastIndexOf('-');
			int idxColon = s.indexOf(':');
			int idxSpace = s.indexOf(' ');
			int idxLastColon = s.lastIndexOf(':');
			int idxDot = s.indexOf('.');
			String yearPart = s.substring(0, idxDash);
			String monthPart = s.substring(idxDash+1, idxLastDash);
			String datePart = s.substring(idxLastDash+1, idxSpace);
			String hourPart = s.substring(idxSpace+1, idxColon);
			String minutePart = s.substring(idxColon+1, idxLastColon);
			String secondPart = -1==idxDot? s.substring(idxLastColon+1) : s.substring(idxLastColon+1, idxDot);
			String msPart = -1==idxDot? null : s.substring(idxDot+1);
			int year = TextUtil.intValue(yearPart, -1);
			int month = TextUtil.intValue(monthPart, -1);
			int date = TextUtil.intValue(datePart, -1);
			int hour = TextUtil.intValue(hourPart, -1);
			int minute = TextUtil.intValue(minutePart, -1);
			int second = TextUtil.intValue(secondPart, -1);
			int ms = null==msPart? 0 : TextUtil.intValue(msPart, -1);

			return new DateTime(year, month, date, hour, minute, second, ms);
		}
		return null;
	}
	public static DateTime valueOf(String s, String format) {
		if (TextUtil.noValueOrBlank(format)) return valueOf(s);
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			java.util.Date date = sdf.parse(s);
			return new DateTime(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
}
