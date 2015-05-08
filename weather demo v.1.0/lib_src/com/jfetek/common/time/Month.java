package com.jfetek.common.time;

public enum Month implements TimeUnit {
	JANUARY		(31),
	FEBRUARY	(28),
	MARCH		(31),
	APRIL		(30),
	MAY			(31),
	JUNE		(30),
	JULY		(31),
	AUGUST		(31),
	SEPTEMBER	(30),
	OCTOBER		(31),
	NOVEMVER	(30),
	DECEMBER	(31);
	

	private final int days;
	private final String text;
	Month(int days) {
		this.days = days;

		StringBuilder s = new StringBuilder(2);
		int order = this.ordinal();
		if (order < 9) s.append(0);
		s.append(order+1);
		this.text = s.toString();
	}
	

	public Month next() {
		return Month.values()[(this.ordinal()+1) % TimeConstants.MONTHS_OF_YEAR];
	}

	public Month previous() {
		return Month.values()[(this.ordinal()+TimeConstants.MONTHS_OF_YEAR-1)%TimeConstants.MONTHS_OF_YEAR];
	}
	
	public Month shift(int months) {
		int idx = this.ordinal()+months;
		idx = idx%TimeConstants.MONTHS_OF_YEAR;
		if (idx < 0) idx += TimeConstants.MONTHS_OF_YEAR;
		return Month.values()[idx];
	}
	
	public boolean isFirst() {
		return this==JANUARY;
	}
	
	public boolean isLast() {
		return this==DECEMBER;
	}
	
	public int days(Year year) {
		if (this == FEBRUARY && year.isLeapYear()) {
			return 1+this.days;
		}
		return this.days;
	}
	
	public MonthOfYear of(Year year) {
		return new MonthOfYear(year, this);
	}

	public String toText() {
		return this.text;
	}
	
	public static Month first() {
		return JANUARY;
	}
	
	public static Month last() {
		return DECEMBER;
	}

	public static Month get(int order) {
		if (order < 0 || order >= TimeConstants.MONTHS_OF_YEAR) return null;
		return Month.values()[ order ];
	}
}
