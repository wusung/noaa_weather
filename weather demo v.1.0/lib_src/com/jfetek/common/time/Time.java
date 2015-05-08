package com.jfetek.common.time;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.jfetek.common.util.TextUtil;

public class Time implements TimeData, java.io.Serializable, Comparable<Time> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6482731082974026416L;
	
	
	public static final Time	ZERO_HOUR	= Time.first();
	public static final Time	NOON		= new Time(12, 0, 0);


	public final Second second;
	public final Minute minute;
	public final Hour hour;
	public final int millisecond;
	
	public final long timestamp;
	
	static long adjustTZOffsetEffect(long ts) {
		long tss = (ts % TimeConstants.MILLISECONDS_OF_DAY);	// keep timestamp with time offset
		long tz = Time.getTimeZoneOffset();
		if (tz < 0) tz += TimeConstants.MILLISECONDS_OF_DAY;
		if (tss < 0-tz) {
			// yet change day, yesterday
			tss += TimeConstants.MILLISECONDS_OF_DAY;
		}
		else if (tss >= TimeConstants.MILLISECONDS_OF_DAY-tz) {
			// day changed, tomorrow
			tss -= TimeConstants.MILLISECONDS_OF_DAY;
		}
		return tss;
	}
	
	public Time(long ts) {
//		this.timestamp = (ts % TimeConstants.MILLISECONDS_OF_DAY);	// keep timestamp with time offset
		long tss = adjustTZOffsetEffect(ts);
		this.timestamp = tss;
		
		// count values add time offset <- that value will be correct
		ts = (ts + getTimeZoneOffset()) % TimeConstants.MILLISECONDS_OF_DAY;
		this.millisecond = (int) ts % 1000;
		ts = ts / 1000;	// to seconds
		this.second = Second.of((int)ts%60);
		ts = ts / 60;	// to minutes
		this.minute = Minute.of((int)ts%60);
		ts = ts / 60;	// to hours
		this.hour = Hour.of((int)ts);
	}
	
	public Time() {
		this(System.currentTimeMillis());
	}
	
	public Time(Hour hr, Minute min, Second sec) {
		this(hr, min, sec, 0);
	}
	
	public Time(Hour hr, Minute min, Second sec, int ms) {
		this.second = sec;
		this.minute = min;
		this.hour = hr;
		
		this.millisecond = ms;
		long ts = ((((hour.value * 60) + minute.value) * 60) + second.value) * 1000 + millisecond - getTimeZoneOffset();
		this.timestamp = adjustTZOffsetEffect(ts);
	}
	
	public Time(int hr, int min, int sec) {
		this(hr, min, sec, 0);
	}

	public Time(int hr, int min, int sec, int ms) {
		this.second = Second.of(sec);
		this.minute = Minute.of(min);
		this.hour = Hour.of(hr);
		
		this.millisecond = ms;
		long ts = ((((hr * 60) + min) * 60) + sec) * 1000 + millisecond - getTimeZoneOffset();
		this.timestamp = adjustTZOffsetEffect(ts);
	}
	
	public Time(java.util.Date date) {
		this(date.getTime());
	}
	

	public Time next() {
		Second sec = this.second.next();
		boolean ding = sec.equals(Second.first());
		Minute min = ding? this.minute.next() : this.minute;
		ding = (!min.equals(this.minute) && min.equals(Minute.first()));
		Hour hr = ding? this.hour.next() : this.hour;
		return new Time(hr, min, sec);
	}
	
	public Time previous() {
		Second sec = this.second.previous();
		boolean ding = sec.equals(Second.last());
		Minute min = ding? this.minute.previous() : this.minute;
		ding = (!min.equals(this.minute) && min.equals(Minute.last()));
		Hour hr = ding? this.hour.previous() : this.hour;
		return new Time(hr, min, sec);
	}
	
//	public boolean after(Time time) {
//		if (this.hour.after(time.hour)) return true;
//		if (this.minute.after(time.minute)) return true;
//		if (this.second.after(time.second)) return true;
//		if (previous().equals(time)) return true;
//		return false;
//	}
//
//	public boolean before(Time time) {
//		if (this.hour.before(time.hour)) return true;
//		if (this.minute.before(time.minute)) return true;
//		if (this.second.before(time.second)) return true;
//		if (next().equals(time)) return true;
//		return false;
//	}

	public boolean equals(Time time) {
		if (null == time) return false;
		return this.second.equals(time.second) && this.minute.equals(time.minute) && this.hour.equals(time.hour);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Time && equals((Time) obj));
	}

	@Override
	public int hashCode() {
        return (int) this.timestamp ^ (int) (this.timestamp >> 32);	// copy from java.util.Date
	}

	public int compareTo(Time time) {
		int c = this.hour.compareTo(time.hour);
		if (0 != c) return c;
		c = this.minute.compareTo(time.minute);
		if (0 != c) return c;
		return this.second.compareTo(time.second);
	}
	
	public Time ms(int ms) {
		return new Time(this.hour, this.minute, this.second, ms);
	}
	
	public DateTime of(Date date) {
		return new DateTime(date, this);
	}
	
	public java.sql.Time toSqlTime() {
		return new java.sql.Time(this.timestamp);
	}
	
	public boolean isAM() {
		long offset = getTimeZoneOffset();
		long tsNoon = (NOON.timestamp + offset) % TimeConstants.MILLISECONDS_OF_DAY;
		long tsThis = (this.timestamp + offset) % TimeConstants.MILLISECONDS_OF_DAY;
		return tsNoon - tsThis > 0;
	}
	
	public boolean isPM() {
		long offset = getTimeZoneOffset();
		long tsNoon = (NOON.timestamp + offset) % TimeConstants.MILLISECONDS_OF_DAY;
		long tsThis = (this.timestamp + offset) % TimeConstants.MILLISECONDS_OF_DAY;
		return tsThis - tsNoon >= 0;
	}
	
//	public String toHHmmss() {
//		StringBuilder s = new StringBuilder(8);
//		s.append(this.hour.str()).append(':')
//			.append(this.minute.str()).append(':')
//			.append(this.second.str());
//		return s.toString();
//	}
//	
//	public String toHHmmsszzz() {
//		StringBuilder s = new StringBuilder(8);
//		s.append(this.hour.str()).append(':')
//			.append(this.minute.str()).append(':')
//			.append(this.second.str());
//		if (0 != this.millisecond) s.append('.').append(this.millisecond);
//		return s.toString();
//	}
	
	@Override
	public String toString() {
		return toText(false);
	}

	public String toText(boolean ms) {
		StringBuilder s = new StringBuilder(8);
		s.append(this.hour.toText()).append(':')
			.append(this.minute.toText()).append(':')
			.append(this.second.toText());
		if (ms && 0 != this.millisecond) s.append('.').append(this.millisecond);
		return s.toString();
	}
	
	public static int getTimeZoneOffset() {
		TimeZone tz = TimeZone.getDefault();
		return tz.getRawOffset();
	}
	
	public static Time first() {
		return new Time(Hour.first(), Minute.first(), Second.first(), 0);
	}

	public static Time last() {
		return new Time(Hour.last(), Minute.last(), Second.last(), 999);
	}

	public static Time now() {
		return new Time();
	}

	public static Time valueOf(String s) {
		// format: HH:mm:ss
		if (s != null && s.matches( "^\\d{1,2}\\:\\d{1,2}\\:\\d{1,2}(.\\d{1,3})?$" )) {
			int idxColon = s.indexOf(':');
			int idxLastColon = s.lastIndexOf(':');
			int idxDot = s.indexOf('.');
			String hourPart = s.substring(0, idxColon);
			String minutePart = s.substring(idxColon+1, idxLastColon);
			String secondPart = -1==idxDot? s.substring(idxLastColon+1) : s.substring(idxLastColon+1, idxDot);
			String msPart = -1==idxDot? null : s.substring(idxDot+1);
			int hour = TextUtil.intValue(hourPart, -1);
			int minute = TextUtil.intValue(minutePart, -1);
			int second = TextUtil.intValue(secondPart, -1);
			int ms = null==msPart? 0 : TextUtil.intValue(msPart, -1);
			
			return new Time(hour, minute, second, ms);
		}
		return null;
	}
	public static Time valueOf(String s, String format) {
		if (TextUtil.noValueOrBlank(format)) return valueOf(s);
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			java.util.Date date = sdf.parse(s);
			return new Time(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		Time now = Time.now();
		System.out.println(now.isAM());
		System.out.println(now.isPM());
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
//		long ts = System.currentTimeMillis();
//		System.out.println(ts);
//		GregorianCalendar cal = new GregorianCalendar();
//		System.out.println(sdf.format(cal.getTime()));
//		long tsTime = ts % TimeConstants.MILLISECONDS_OF_DAY;
//		System.out.println(tsTime);
//		System.out.println(new Time(tsTime));
//		long ts2 = ts - tsTime;
//		System.out.println(ts2);
//		cal.setTimeInMillis(ts2);
//		System.out.println(sdf.format(cal.getTime()));
//		System.out.println(new java.sql.Time(tsTime));
//		System.out.println(new Timestamp(ts2));
		
		Calendar cal = new GregorianCalendar();
		long tsNow = cal.getTimeInMillis();
		int tzOffset = Time.getTimeZoneOffset();
		System.out.println(sdf.format(cal.getTime()));
		System.out.println(tzOffset);
		long tsTime = (tsNow % TimeConstants.MILLISECONDS_OF_DAY);
		long tsDate = tsNow - tsTime;
		cal.setTimeInMillis(tsDate + tzOffset);
		System.out.println(tsNow);
		System.out.println(tsTime);
		System.out.println(tsDate);
		System.out.println(new Time(tsTime));
		System.out.println(sdf.format(cal.getTime()));
//		System.out.println(new Timestamp(tsNow));
//		System.out.println(new Timestamp(tsTime));
		System.out.println(new Timestamp(tsDate));
		System.out.println(new Timestamp(tsDate + tzOffset));
		System.out.println("----------------------------");
		
		Date d = new Date(2014, 06, 10);
		Date today = Date.today();
		Time nowTime = Time.valueOf(Time.now().toText(true));
		System.out.println(d+"\t("+d.timestamp+")");
		System.out.println(today+"\t("+today.timestamp+")");
		System.out.println(nowTime+"\t("+nowTime.timestamp+")");
		DateTime dtNow = DateTime.now();
		DateTime dtNow2 = new DateTime(dtNow.date, nowTime);
		System.out.println("now> "+dtNow+"\t("+dtNow.timestamp+")");
		System.out.println("now2> "+dtNow2+"\t("+dtNow2.timestamp+")");
		System.out.println(dtNow.equals(dtNow2));
		System.out.println(DateTime.now());
		System.out.println(new DateTime(DateTime.now().timestamp + 23*TimeConstants.MILLISECONDS_OF_HOUR));
		
		cal.clear();
		System.out.println(sdf.format(cal.getTime()));
	}
}
