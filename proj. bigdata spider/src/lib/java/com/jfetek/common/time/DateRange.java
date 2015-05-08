package com.jfetek.common.time;

import java.text.DateFormat;
import java.util.regex.Matcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jfetek.common.data.JsonArrayDescribable;
import com.jfetek.common.data.JsonDescribable;
import com.jfetek.common.util.TextUtil;
import com.jfetek.common.util.TimeUtil;

public class DateRange implements JsonDescribable<DateRange>, JsonArrayDescribable<DateRange> {
	
	public static final DateRange	NONE	= new DateRange(Date.EPOCH, Date.EPOCH);
	

	public final Date first;
	public final Date last;	// inclusive
	protected DateRange(Date from, Date to) {
		if (from.timestamp > to.timestamp) {
			this.first = to;
			this.last = from;
		}
		else {
			this.first = from;
			this.last = to;
		}
	}
	
	public boolean contains(Date date) {
//		return !this.from.before(date) && !this.to.after(date);
		return this.first.timestamp <= date.timestamp && this.last.timestamp >= date.timestamp;
	}
	public boolean contains(java.util.Date date) {
		return contains(new Date(date));
	}
	public boolean contains(DateTime datetime) {
		return contains(datetime.date);
	}
	
	public boolean after(Date date) {
		return this.first.timestamp > date.timestamp;
	}
	public boolean after(java.util.Date date) {
		return this.first.timestamp > date.getTime();
	}

	public boolean before(Date date) {
		return this.last.timestamp < date.timestamp;
	}
	public boolean before(java.util.Date date) {
		return this.last.timestamp < date.getTime();
	}
	
	public int days() {
		int days = 1 + (int)((this.last.timestamp - this.first.timestamp) / TimeConstants.MILLISECONDS_OF_DAY);
		return days;
	}
	
	public DateRange expandTo(Date date) {
		if (this.after(date)) {
			return new DateRange(date, this.last);
		}
		else if (this.before(date)) {
			return new DateRange(this.first, date);
		}
		return this;
	}
	public DateRange expandWith(DateRange range) {
		if (NONE == range) return this;
		if (this == NONE) return range;
		return new DateRange(
				this.first.timestamp<range.first.timestamp? this.first : range.first ,
				this.last.timestamp>range.last.timestamp? this.last : range.last
		);
	}
	
	public Date[] toArray() {
		Date[] arr = new Date[ days() ];
		int idx = 0;
		for (Date d = this.first; !d.after(this.last); d = d.next()) {
			arr[ idx++ ] = d;
		}
		return arr;
	}
	
	public java.sql.Date[] toSqlDateArray() {
		java.sql.Date[] arr = new java.sql.Date[ days() ];
		int idx = 0;
		for (Date d = this.first; !d.after(this.last); d = d.next()) {
			arr[ idx++ ] = d.toSqlDate();
		}
		return arr;
	}
	
	@Override
	public String toString() {
		return toText();
	}
	
	public String toText() {
		StringBuilder s = new StringBuilder(14);
		s.append('[').append(this.first.toString()).append("..").append(this.last.toString()).append(']');
		return s.toString();
	}
	public String toText(DateFormat format) {
		StringBuilder s = new StringBuilder(14);
		s.append('[').append(format.format(this.first.toSqlDate())).append("..").append(format.format(this.last.toSqlDate())).append(']');
		return s.toString();
	}

	public JSONObject toJson() {
		try {
			JSONObject json = new JSONObject();
			json.put("first", this.first.timestamp).put("last", this.last.timestamp);
			return json;
		} catch (JSONException e) { }
		return null;
	}

	public JSONArray toJsonArray() {
		return new JSONArray().put(this.first.timestamp).put(this.last.timestamp);
	}
	
	public String describe() {
		return toText();
	}

	public DateRange realize(JSONObject json) {
		long first = json.optLong("first");
		long last = json.optLong("last");
		return DateRange.between(new Date(first), new Date(last));
	}

	public DateRange realize(String describe) {
		return parse(describe);
	}

	public DateRange realize(JSONArray json) {
		return DateRange.between(new Date(json.optLong(0)), new Date(json.optLong(1)));
	}

	public static DateRange parse(String s) {
		Matcher m = TimeUtil.PATTERN_DATE.matcher(s);
		Date from = null;
		Date to = null;
		if (m.find()) {
			int year = TextUtil.intValue( m.group(1) );
			int month = TextUtil.intValue( m.group(3) );
			int day = TextUtil.intValue( m.group(4) );
			
			if (year < 100) year += 2000;	// base 2000
			from = new Date(year, month, day);
		}
		if (m.find()) {
			int year = TextUtil.intValue( m.group(1) );
			int month = TextUtil.intValue( m.group(3) );
			int day = TextUtil.intValue( m.group(4) );
			
			if (year < 100) year += 2000;	// base 2000
			to = new Date(year, month, day);
		}
		return new DateRange(from, to);
	}
	
	public static DateRange between(Date from, Date to) {
		return new DateRange(from, to);
	}
	public static DateRange between(java.util.Date from, java.util.Date to) {
		return new DateRange(new Date(from), new Date(to));
	}
	
	public static DateRange is(Date date) {
		return new DateRange(date, date);
	}
	public static DateRange is(java.util.Date date) {
		Date d = new Date(date);
		return new DateRange(d, d);
	}
	
	public static DateRange till(Date date) {
		return new DateRange(Date.today(), date);
	}
	public static DateRange till(java.util.Date date) {
		return new DateRange(Date.today(), new Date(date));
	}
	
	public static DateRange since(Date date) {
		return new DateRange(date, Date.today());
	}
	public static DateRange since(java.util.Date date) {
		return new DateRange(new Date(date), Date.today());
	}

	public static DateRange from(Date begin, int days) {
		return new DateRange(begin, begin.shift(days));
	}
	public static DateRange from(java.util.Date begin, int days) {
		Date b = new Date(begin);
		return new DateRange(b, b.shift(days));
	}
	
	public static DateRange to(Date end, int days) {
		return new DateRange(end.shift(-days), end);
	}
	public static DateRange to(java.util.Date end, int days) {
		Date e = new Date(end);
		return new DateRange(e.shift(-days), e);
	}
	
	public static DateRange today() {
		Date today = Date.today();
		return new DateRange(today, today);
	}
	
	public static DateRange thisWeek() {
		Date today = Date.today();
		DayOfWeek dow = today.getDayOfWeek();
		Date first = today.shift( DayOfWeek.first().ordinal() - dow.ordinal() );
		Date last = today.shift( DayOfWeek.last().ordinal() - dow.ordinal() );
		return new DateRange(first, last);
	}
	
	public static DateRange thisMonth() {
		MonthOfYear moy = Date.today().getMonthOfYear();
		return new DateRange(moy.firstDay(), moy.lastDay());
	}
	
	public static DateRange of(MonthOfYear moy) {
		return new DateRange(moy.firstDay(), moy.lastDay());
	}
	
	public static DateRange thisYear() {
		Date today = Date.today();
		Year year = today.year;
		return new DateRange(year.firstDay(), year.lastDay());
	}
	
	public static DateRange of(Year year) {
		return new DateRange(year.firstDay(), year.lastDay());
	}
	
//	public static void main(String[] args) {
////		DateRange range = DateRange.between(Date.today(), Date.today());
//		DateRange range = DateRange.since(Date.yesterday());
//		System.out.println(range);
//		System.out.println(ArrayUtil.join(range.toArray()));
//		System.out.println(range.contains(Date.today()));
//		System.out.println(range.contains(Date.tomorrow()));
//		System.out.println(range.contains(Date.yesterday()));
//		System.out.println(range.contains(DateTime.now()));
//		System.out.println(DateRange.parse(range.toString()));
//		
//		for (int i = 0; i <= 7; ++i) {
//			DateRange range2 = DateRange.from(Date.valueOf("2013-06-19"), i);
//			System.out.println(range2+" {"+range2.days()+"}");
//		}
//		
//		System.out.println(DateRange.today());
//		System.out.println(DateRange.thisWeek());
//		System.out.println(DateRange.thisMonth());
//		System.out.println(DateRange.thisYear());
//		
//		Range<Date> r = Range.between(Date.today(), Date.tomorrow());
//		r.isNaturalOrdering();
////		r.isAfter(date)
//		
//		DateRange r1 = DateRange.thisMonth();
//		DateRange r2 = DateRange.between(Date.valueOf("2013-06-21"), Date.valueOf("2013-06-27"));
//		System.out.println(r1);
//		System.out.println(r2);
//		System.out.println(r1.expandWith(r2));
//		System.out.println(r2.expandWith(r1));
//	}

}
