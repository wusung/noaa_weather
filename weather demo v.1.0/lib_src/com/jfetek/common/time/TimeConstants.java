package com.jfetek.common.time;

public class TimeConstants {

	public static final int		MONTHS_OF_YEAR			= 12;
	
	public static final int		DAYS_OF_WEEK			= 7;
	
	public static final int		HOURS_OF_DAY			= 24;
	public static final int		HOURS_OF_WEEK			= 168;
	
	public static final int		MINUTES_OF_HOUR			= 60;
	public static final int		MINUTES_OF_DAY			= 1440;
	public static final int		MINUTES_OF_WEEK			= 10080;
	
	public static final int		SECONDS_OF_MINUTE		= 60;
	public static final int		SEOCNDS_OF_HOUR			= 3600;
	public static final int		SECONDS_OF_DAY			= 86400;
	public static final int		SECONDS_OF_WEEK			= 604800;

	public static final long	MILLISECONDS_OF_SECOND	= 1000L;
	public static final long	MILLISECONDS_OF_MINUTE	= 60000L;
	public static final long	MILLISECONDS_OF_HOUR	= 3600000L;
	public static final long	MILLISECONDS_OF_DAY		= 86400000L;
	public static final long	MILLISECONDS_OF_WEEK	= 604800000L;
	
	
	
	public static final Date		EPOCH_DATE				= new Date(0);
//	public static final DateTime	EPOCH_DATETIME			= new DateTime(0);
	public static final DayOfWeek	DAY_OF_WEEK_OF_EPOCH	= DayOfWeek.THURSDAY;
	
	
	private TimeConstants() {
	}
}
