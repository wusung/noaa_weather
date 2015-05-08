package com.jfetek.common.time;

public enum DayOfWeek {
	SUNDAY,
	MONDAY,
	TUESDAY,
	WEDNESDAY,
	THURSDAY,
	FRIDAY,
	SATURDAY;
	
	private static DayOfWeek	_first	= SUNDAY;
	private static DayOfWeek	_last	= SATURDAY;
	
	private final String text;
	DayOfWeek() {
		this.text = String.valueOf( this.ordinal() );
	}
	
	public DayOfWeek next() {
		return DayOfWeek.values()[(this.ordinal()+1) % TimeConstants.DAYS_OF_WEEK];
	}

	public DayOfWeek previous() {
		return DayOfWeek.values()[(this.ordinal()+TimeConstants.DAYS_OF_WEEK-1)%TimeConstants.DAYS_OF_WEEK];
	}
	
	public DayOfWeek shift(int days) {
		int idx = this.ordinal()+days;
		idx = idx%TimeConstants.DAYS_OF_WEEK;
		if (idx < 0) idx += TimeConstants.DAYS_OF_WEEK;
		return DayOfWeek.values()[idx];
	}
	
	public boolean isFirst() {
		return this==_first;
	}
	
	public boolean isLast() {
		return this==_last;
	}
	
	public void setFirst(DayOfWeek dow) {
		_first = dow;
		_last = _first.previous();
	}
	
	public boolean isWeekend() {
		return (this == SUNDAY || this == SATURDAY);
	}
	
	public String toText() {
		return this.text;
	}

	public static DayOfWeek first() {
		return _first;
	}
	
	public static DayOfWeek last() {
		return _last;
	}
	
	public static DayOfWeek get(int order) {
		if (order < 0 || order >= TimeConstants.DAYS_OF_WEEK) return null;
		return DayOfWeek.values()[ order ];
	}
}
