package com.jfetek.common.time;

public class Year implements TimeData, java.io.Serializable, Comparable<Year> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3022528427988311337L;
	
	
	public final int value;
	public final int days;
	private final String text;
	public Year(int value) {
		this.value = value;
		this.days = isLeapYear()? 366 : 365;

		StringBuilder s = new StringBuilder(4);
		if (this.value < 10) {
			s.append("000");
		}
		else if (this.value < 100) {
			s.append("00");
		}
		else if (this.value < 1000) {
			s.append('0');
		}
		s.append(this.value);
		
		this.text = s.toString();
	}
	
	public Year next() {
		return new Year(1 + this.value);
	}
	
	public Year previous() {
		return new Year(this.value - 1);
	}

	public Date firstDay() {
		return new Date(this, Month.JANUARY, 1);
	}
	
	public Date lastDay() {
		return new Date(this, Month.DECEMBER, Month.DECEMBER.days(this));
	}
	
	public Date day(int day) {
		if (day < 1 || day > days) return null;
		return firstDay().shift(day-1);
	}
	
	public static void main(String[] args) {
		System.out.println(new Year(2013).day(1));
		System.out.println(new Year(2013).day(100));
		System.out.println(new Year(2013).day(200));
		System.out.println(new Year(2013).day(365/2));
	}
	
	public boolean isLeapYear() {
		if ((this.value & 3) != 0) return false;
		return (this.value % 100 != 0) || (this.value % 400 == 0); // Gregorian
	}

	public boolean equals(Year year) {
		if (year == null) return false;
		return this.value == year.value;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Year && equals((Year) obj));
	}

	public int compareTo(Year year) {
		return this.value - year.value;
	}

	@Override
	public String toString() {
		return this.text;
	}
	
	public String toText() {
		return this.text;
	}
	
	public static Year of(int year) {
		return new Year(year);
	}
}
