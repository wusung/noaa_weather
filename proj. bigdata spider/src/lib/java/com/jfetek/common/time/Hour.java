package com.jfetek.common.time;


public class Hour implements TimeUnit, java.io.Serializable, Comparable<Hour> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7800713980499951328L;
	
	
	private static final Hour[]	INSTANCES = new Hour[ TimeConstants.HOURS_OF_DAY ];
	static {
		for (int i = 0; i < TimeConstants.HOURS_OF_DAY; ++i) {
			INSTANCES[i] = new Hour(i);
		}
	};
	
	
	public final int value;
	private final String text;
	private Hour(int value) {
		this.value = value;

		StringBuilder s = new StringBuilder(2);
		if (this.value < 10) {
			s.append(0);
		}
		s.append(this.value);
		
		this.text = s.toString();
	}

	public Hour next() {
		return INSTANCES[(this.value+1)%TimeConstants.HOURS_OF_DAY];
	}

	public Hour previous() {
		return INSTANCES[(this.value+TimeConstants.HOURS_OF_DAY-1)%TimeConstants.HOURS_OF_DAY];
	}
	
	public Hour shift(int hr) {
		int idx = this.value+hr;
		idx = idx%TimeConstants.HOURS_OF_DAY;
		if (idx < 0) idx += TimeConstants.HOURS_OF_DAY;
		return INSTANCES[idx];
	}
	
//	public boolean after(Hour hr) {
//		if (0 == this.value && TimeConstants.HOURS_OF_DAY-1 == hr.value) return true;
//		return (this.value > hr.value);
//	}
//
//	public boolean before(Hour hr) {
//		if (TimeConstants.HOURS_OF_DAY-1 == this.value && 0 == hr.value) return true;
//		return (this.value < hr.value);
//	}

	public boolean equals(Hour hr) {
		return this.value == hr.value;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Hour && equals((Hour) obj));
	}

	public int order() {
		return this.value;
	}
	
	public boolean isFirst() {
		return 0 == this.value;
	}
	
	public boolean isLast() {
		return TimeConstants.HOURS_OF_DAY-1 == this.value;
	}

	public int compareTo(Hour hr) {
		return this.value-hr.value;
	}

	@Override
	public String toString() {
		return this.text;
	}
	
	public String toText() {
		return this.text;
	}
	

	public static Hour of(int hour) {
		if (hour < 0 || hour >= TimeConstants.HOURS_OF_DAY) return null;
		return INSTANCES[hour];
	}

	public static Hour first() {
		return INSTANCES[0];
	}

	public static Hour last() {
		return INSTANCES[ TimeConstants.HOURS_OF_DAY-1 ];
	}

}
