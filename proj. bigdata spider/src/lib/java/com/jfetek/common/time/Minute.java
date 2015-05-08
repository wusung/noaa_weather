package com.jfetek.common.time;


public class Minute implements TimeUnit, java.io.Serializable, Comparable<Minute> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3711771762078502558L;
	
	
	private static final Minute[]	INSTANCES = new Minute[ TimeConstants.MINUTES_OF_HOUR ];
	static {
		for (int i = 0; i < TimeConstants.MINUTES_OF_HOUR; ++i) {
			INSTANCES[i] = new Minute(i);
		}
	};
	
	
	public final int value;
	private final String text;
	private Minute(int value) {
		this.value = value;

		StringBuilder s = new StringBuilder(2);
		if (this.value < 10) {
			s.append(0);
		}
		s.append(this.value);
		
		this.text = s.toString();
	}

	public Minute next() {
		return INSTANCES[(this.value+1)%TimeConstants.MINUTES_OF_HOUR];
	}

	public Minute previous() {
		return INSTANCES[(this.value+TimeConstants.MINUTES_OF_HOUR-1)%TimeConstants.MINUTES_OF_HOUR];
	}

	public Minute shift(int min) {
		int idx = this.value+min;
		idx = idx%TimeConstants.MINUTES_OF_HOUR;
		if (idx < 0) idx += TimeConstants.MINUTES_OF_HOUR;
		return INSTANCES[idx];
	}
	
//	public boolean after(Minute hr) {
//		if (0 == this.value && TimeConstants.MINUTES_OF_HOUR-1 == hr.value) return true;
//		return (this.value > hr.value);
//	}
//
//	public boolean before(Minute hr) {
//		if (TimeConstants.MINUTES_OF_HOUR-1 == this.value && 0 == hr.value) return true;
//		return (this.value < hr.value);
//	}

	public boolean equals(Minute min) {
		return this.value == min.value;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Minute && equals((Minute) obj));
	}

	public int order() {
		return this.value;
	}
	
	public boolean isFirst() {
		return 0 == this.value;
	}
	
	public boolean isLast() {
		return TimeConstants.MINUTES_OF_HOUR-1 == this.value;
	}

	public int compareTo(Minute min) {
		return this.value-min.value;
	}
	
	@Override
	public String toString() {
		return this.text;
	}
	
	public String toText() {
		return this.text;
	}
	
	public static Minute of(int min) {
		if (min < 0 || min >= TimeConstants.MINUTES_OF_HOUR) return null;
		return INSTANCES[min];
	}
	
	public static Minute first() {
		return INSTANCES[0];
	}

	public static Minute last() {
		return INSTANCES[ TimeConstants.MINUTES_OF_HOUR-1 ];
	}

}
