package com.jfetek.common.time;


//public class Second extends Number {
public class Second implements TimeUnit, java.io.Serializable, Comparable<Second> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8170326970114159227L;
	
	
	private static final Second[]	INSTANCES = new Second[ TimeConstants.SECONDS_OF_MINUTE ];
	static {
		for (int i = 0; i < TimeConstants.SECONDS_OF_MINUTE; ++i) {
			INSTANCES[i] = new Second(i);
		}
	};
	
	
	public final int value;
	private final String text;
	private Second(int value) {
		this.value = value;

		StringBuilder s = new StringBuilder(2);
		if (this.value < 10) {
			s.append(0);
		}
		s.append(this.value);
		
		this.text = s.toString();
	}

	public Second next() {
		return INSTANCES[(this.value+1)%TimeConstants.SECONDS_OF_MINUTE];
	}
	
	public Second shift(int sec) {
		int idx = this.value+sec;
		idx = idx%TimeConstants.SECONDS_OF_MINUTE;
		if (idx < 0) idx += TimeConstants.SECONDS_OF_MINUTE;
		return INSTANCES[idx];
	}
	
	public Second previous() {
		return INSTANCES[(this.value+TimeConstants.SECONDS_OF_MINUTE-1)%TimeConstants.SECONDS_OF_MINUTE];
	}

//	public boolean after(Second sec) {
//		if (0 == this.value && TimeConstants.SECONDS_OF_MINUTE-1 == sec.value) return true;
//		return (this.value > sec.value);
//	}
//
//	public boolean before(Second sec) {
//		if (TimeConstants.SECONDS_OF_MINUTE-1 == this.value && 0 == sec.value) return true;
//		return (this.value < sec.value);
//	}

	public boolean equals(Second sec) {
		if (null == sec) return false;
		return this.value == sec.value;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Second && equals((Second) obj));
	}

	public int order() {
		return this.value;
	}
	
	public boolean isFirst() {
		return 0 == this.value;
	}
	
	public boolean isLast() {
		return TimeConstants.SECONDS_OF_MINUTE-1 == this.value;
	}

	public int compareTo(Second sec) {
		return this.value-sec.value;
	}
	
	@Override
	public String toString() {
		return this.text;
	}
	
	public String toText() {
		return this.text;
	}
	
	public static Second of(int sec) {
		if (sec < 0 || sec >= TimeConstants.SECONDS_OF_MINUTE) return null;
		return INSTANCES[sec];
	}
	
	public static Second first() {
		return INSTANCES[0];
	}

	public static Second last() {
		return INSTANCES[ TimeConstants.SECONDS_OF_MINUTE-1 ];
	}
	
//	public double doubleValue() {
//		return (double) this.value;
//	}
//
//	public float floatValue() {
//		return (float) this.value;
//	}
//
//	public int intValue() {
//		return this.value;
//	}
//
//	public long longValue() {
//		return (long) this.value;
//	}

}
