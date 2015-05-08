package com.jfetek.common.time;

import com.jfetek.common.util.TextUtil;

public class MonthOfYear implements java.io.Serializable, Comparable<MonthOfYear> {

	public final Year year;
	public final Month month;
	public final int days;
	
	public final String text;
	
	public MonthOfYear(Year year, Month month) {
		this.year = year;
		this.month = month;
		this.days = this.month.days(this.year);

		StringBuilder s = new StringBuilder(7);
		s.append(this.year.toText()).append('-').append(this.month.toText());
		
		this.text = s.toString();
	}

	public MonthOfYear next() {
		Month mon = this.month.next();
		boolean ding = (mon == Month.first());
		return new MonthOfYear(ding? this.year.next() : this.year, mon);
	}
	
	public MonthOfYear previous() {
		Month mon = this.month.previous();
		boolean ding = (mon == Month.last());
		return new MonthOfYear(ding? this.year.previous() : this.year, mon);
	}
	
	public boolean hasDay(int day) {
		return (day >= 1 && day <= days);
	}
	
	public Date firstDay() {
		return new Date(this.year, this.month, 1);
	}
	
	public Date lastDay() {
		return new Date(this.year, this.month, this.month.days(this.year));
	}
	
	public Date day(int day) {
//		if (!this.hasDay(day)) throw new java.lang.IndexOutOfBoundsException(this+" no day "+day);
		if (day < 1 || day > days) return null;
		return new Date(this.year, this.month, day);
	}

	public boolean equals(MonthOfYear moy) {
		if (moy == null) return false;
		if (this.month != moy.month) return false;
		if (this.year.equals( moy.year )) return true;
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof MonthOfYear && equals((MonthOfYear) obj));
	}

	public int compareTo(MonthOfYear moy) {
		int c = this.year.compareTo( moy.year );
		if (0 == c) {
			c = this.month.compareTo( moy.month );
		}
		else {
			c *= 1000;
		}
		return c;
	}
	
	public DateRange getDateRange() {
		return new DateRange(firstDay(), lastDay());
	}

	@Override
	public String toString() {
		return this.text;
	}
	
	public String toText() {
		return this.text;
	}

	public static MonthOfYear valueOf(String s) {
		// format: yyyy-MM
		if (s != null && s.matches( "^\\d{4}\\-\\d{1,2}$" )) {
			int idxDash = s.indexOf('-');
			String yearPart = s.substring(0, idxDash);
			String monthPart = s.substring(idxDash+1);
			int year = TextUtil.intValue(yearPart, -1);
			int month = TextUtil.intValue(monthPart, -1);
			
			return new MonthOfYear(new Year(year), Month.get(month-1));
		}
		return null;
	}
	
}
