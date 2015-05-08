package com.jfetek.common.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.jfetek.common.util.TextUtil;

public class Date implements TimeData, java.io.Serializable, Comparable<Date> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -266381261084313554L;


	public static final Date	EPOCH	= new Date(0);
	
	
	private static Date	_today = null;

	
	public final Year year;
	public final Month month;
	public final int day;
	
	public final String text;
	
	public final long timestamp;
	
	private Date() {
		// today
		this(System.currentTimeMillis());
	}
	
//	public Date(long ts) {
//		GregorianCalendar cal = new GregorianCalendar();
//		cal.setTimeInMillis(ts);
//		this.year = new Year(cal.get(Calendar.YEAR));
//		this.month = Month.get(cal.get(Calendar.MONTH));
//		this.day = cal.get(Calendar.DAY_OF_MONTH);
//
//		StringBuilder s = new StringBuilder(10);
//		s.append(this.year.toText()).append('-').append(this.month.toText()).append('-');
//		if (this.day < 10) s.append(0);
//		s.append(this.day);
//		
//		this.text = s.toString();
//
//		cal.set(Calendar.HOUR_OF_DAY, 0);
//		cal.set(Calendar.MINUTE, 0);
//		cal.set(Calendar.SECOND, 0);
//		cal.set(Calendar.MILLISECOND, Time.getTimeZoneOffset());
//		cal.set(Calendar.MILLISECOND, 0);
//		this.timestamp = cal.getTimeInMillis();
//	}
	public Date(long ts) {
		// inner timestamp, in utc.
		long tss = Time.adjustTZOffsetEffect(ts);
		this.timestamp = ts - tss;
		
		// display, + timezone offset
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(ts);
		this.year = new Year(cal.get(Calendar.YEAR));
		this.month = Month.get(cal.get(Calendar.MONTH));
		this.day = cal.get(Calendar.DAY_OF_MONTH);

		StringBuilder s = new StringBuilder(10);
		s.append(this.year.toText()).append('-').append(this.month.toText()).append('-');
		if (this.day < 10) s.append(0);
		s.append(this.day);
		
		this.text = s.toString();
	}
	
	public Date(java.util.Date date) {
		this(date.getTime());
	}
	
	public Date(int year, int month, int day) {
		this.year = new Year(year);
		this.month = Month.get(month-1);
		MonthOfYear moy = this.month.of( this.year );
		if (!moy.hasDay(day)) throw new java.lang.IndexOutOfBoundsException(moy+" no day "+day);
		this.day = day;

		StringBuilder s = new StringBuilder(10);
		s.append(this.year.toText()).append('-').append(this.month.toText()).append('-');
		if (this.day < 10) s.append(0);
		s.append(this.day);
		
		this.text = s.toString();
		
		GregorianCalendar cal = new GregorianCalendar();
//		cal.clear();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month-1);
		cal.set(Calendar.DATE, day);
		
		long ts = cal.getTimeInMillis();
		this.timestamp = ts - Time.adjustTZOffsetEffect(ts);
	}
	
	public Date(Year year, Month month, int day) {
		MonthOfYear moy = month.of(year);
		if (!moy.hasDay(day)) throw new java.lang.IndexOutOfBoundsException(moy+" no day "+day);
		this.year = year;
		this.month = month;
		this.day = day;

		StringBuilder s = new StringBuilder(10);
		s.append(this.year.toText()).append('-').append(this.month.toText()).append('-');
		if (this.day < 10) s.append(0);
		s.append(this.day);
		
		this.text = s.toString();
		
		GregorianCalendar cal = new GregorianCalendar();
		cal.clear();
		cal.set(Calendar.YEAR, year.value);
		cal.set(Calendar.MONTH, month.ordinal());
		cal.set(Calendar.DAY_OF_MONTH, day);
		
		long ts = cal.getTimeInMillis();
		this.timestamp = ts - Time.adjustTZOffsetEffect(ts);
	}

	public Date next() {
		return new Date(this.timestamp + TimeConstants.MILLISECONDS_OF_DAY);
	}
	
	public Date previous() {
		return new Date(this.timestamp - TimeConstants.MILLISECONDS_OF_DAY);
	}
	
	public Date shift(int days) {
		if (0 == days) return this;
		return new Date(this.timestamp + days * TimeConstants.MILLISECONDS_OF_DAY);
	}

	public int diff(Date date) {
		return diff(this, date);
	}

	public int diff(java.util.Date date) {
		return diff(this, new Date(date));
	}

	public static int diff(Date base, Date another) {
		return (int) ((another.timestamp-base.timestamp)/TimeConstants.MILLISECONDS_OF_DAY);
	}
	
	public boolean before(Date date) {
		return this.timestamp < date.timestamp;
	}
	
	public boolean after(Date date) {
		return this.timestamp > date.timestamp;
	}
	
	public boolean equals(Date date) {
		if (null == date) return false;
		return this.timestamp == date.timestamp;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return obj instanceof Date ? equals((Date)obj) : false;
	}

	@Override
	public int hashCode() {
        return (int) this.timestamp ^ (int) (this.timestamp >> 32);	// copy from java.util.Date
	}

	public int compareTo(Date date) {
		return (int) (this.timestamp - date.timestamp);
	}
	
	public boolean sameDate(long ts) {
//		return equals(new Date(ts));
		// count values add time offset <- that value will be correct
//		ts = (ts + Time.getTimeZoneOffset());
		long diff = ts - this.timestamp;
		return (diff >= 0 && diff < TimeConstants.MILLISECONDS_OF_DAY);
	}
	
	public boolean sameDate(Date date) {
		return equals(date);
	}
	
	public boolean sameDate(java.util.Date date) {
		if (null == date) return false;
//		long ts = date.getTime();
//		// count values add time offset <- that value will be correct
////		TimeZone tz = TimeZone.getDefault();
////		ts = (ts + tz.getRawOffset());
//		long diff = ts - this.timestamp;
//		return (diff >= 0 && diff < TimeConstants.MILLISECONDS_OF_DAY);
////		return null==date? false : equals(new Date(date));
		
		return equals(new Date(date.getTime()));
	}

//	public boolean in(DateRange range) {
//		return range.include(this);
//	}
	
	public int getDayOfYear() {
		Date date = this.year.firstDay();
		return 1 + date.diff(this);
	}
	
//	public static void main(String[] args) {
//		System.out.println(Date.today().getDayOfYear());
//		System.out.println(Date.yesterday().getDayOfYear());
////		System.out.println(Date.valueOf("2011-02-31").getDayOfYear());
//		
//		System.out.println(Date.today().sameDate(System.currentTimeMillis()));
//		
//		System.out.println(Date.today().toSqlDate());
//	}

	public MonthOfYear getMonthOfYear() {
		return this.month.of( this.year );
	}
	
	public DayOfWeek getDayOfWeek() {
		int order = (int) (TimeConstants.DAY_OF_WEEK_OF_EPOCH.ordinal() + this.timestamp / TimeConstants.MILLISECONDS_OF_DAY) % TimeConstants.DAYS_OF_WEEK;
		return DayOfWeek.get(order);
	}
	
	public boolean isWeekend() {
		return getDayOfWeek().isWeekend();
	}
	
	public DateTime time(Time time) {
		return new DateTime(this, time);
	}
	
//	public DateRange to(Date end) {
//		return new DateRange(this, end, false);
//	}
//	
//	public DateRange to(Date end, boolean exclusive) {
//		return new DateRange(this, end, exclusive);
//	}

	public java.sql.Date toSqlDate() {
		return new java.sql.Date(this.timestamp);
	}
	
	public java.sql.Timestamp toSqlTimestamp() {
		return new java.sql.Timestamp(this.timestamp);
	}
	
	@Override
	public String toString() {
		return this.text;
	}

	public String toText() {
		return this.text;
	}

	public static synchronized Date today() {
		long ts = System.currentTimeMillis();
//		if (_today == null || Time.adjustTZOffsetEffect(System.currentTimeMillis()) - _today.timestamp >= TimeConstants.MILLISECONDS_OF_DAY) {
		if (_today == null || ts - Time.adjustTZOffsetEffect(ts) - _today.timestamp >= TimeConstants.MILLISECONDS_OF_DAY) {
			_today = new Date();
		}
		return _today;
	}
	
	public static Date tomorrow() {
		return today().next();
	}

	public static Date yesterday() {
		return today().previous();
	}

	public static Date valueOf(String s) {
		// format: yyyy-MM-dd
		if (s != null && s.matches( "^\\d{4}\\-\\d{1,2}\\-\\d{1,2}$" )) {
			int idxDash = s.indexOf('-');
			int idxLastDash = s.lastIndexOf('-');
			String yearPart = s.substring(0, idxDash);
			String monthPart = s.substring(idxDash+1, idxLastDash);
			String datePart = s.substring(idxLastDash+1);
			int year = TextUtil.intValue(yearPart, -1);
			int month = TextUtil.intValue(monthPart, -1);
			int date = TextUtil.intValue(datePart, -1);
			
			return new Date(year, month, date);
		}
		return null;
	}
	public static Date valueOf(String s, String format) {
		if (TextUtil.noValueOrBlank(format)) return valueOf(s);
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			java.util.Date date = sdf.parse(s);
			return new Date(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
//	public static void main(String[] args) {
////		Timestamp tsNow = new Timestamp(System.currentTimeMillis());
////		System.out.println(tsNow);
////		Date today = Date.today();
////		today = Date.valueOf(today.toString());
////		Time now = Time.now();
////		now = Time.valueOf(now.toString());
////		System.out.println(today+"\t"+today.timestamp);
////		System.out.println(now+"\t"+now.timestamp);
////		System.out.println(TimeSpan.ofMillis(now.timestamp).toHourText());
////		System.out.println(today.toSqlTimestamp());
////		System.out.println(new java.sql.Timestamp(today.timestamp+now.timestamp));
////		
////		DateTime datetime = new DateTime(today, now);
////		System.out.println(datetime);
////		System.out.println(datetime.toSqlTimestamp());
////		datetime = DateTime.valueOf(datetime.toString());
////		System.out.println(datetime);
////		System.out.println(datetime.toSqlTimestamp());
//		
//		java.sql.Date sqlDate = java.sql.Date.valueOf("2014-06-19");
//		sqlDate = new java.sql.Date(sqlDate.getTime());
////		java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
//		System.out.println(sqlDate);
//		System.out.println(sqlDate.getTime());
//		System.out.println(new Timestamp(sqlDate.getTime()));
//		Date date = new Date(sqlDate);
//		System.out.println(date);
//		System.out.println(date.timestamp);
//		System.out.println(date.toSqlDate());
//		System.out.println(date.toSqlDate().getTime());
//		System.out.println(date.toSqlTimestamp());
//		System.out.println(date.toSqlTimestamp().getTime());
//
////		java.sql.Date sqlDate1 = java.sql.Date.valueOf("2014-06-19");
////		java.sql.Date sqlDate2 = new java.sql.Date(System.currentTimeMillis());
////		long offset = sqlDate1.getTime() - sqlDate2.getTime();
////		System.out.println(offset);
////		System.out.println(TimeSpan.ofMillis(offset).toHourText());
//		
//	}

	public static void main(String[] args) throws InterruptedException {
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
		for (int i = 0; i < 30; ++i) {
			System.out.println(sdf.format(new java.util.Date())+ "> " + Date.today());
			Thread.sleep(1000);
		}
		System.out.println("over");
	}
}
