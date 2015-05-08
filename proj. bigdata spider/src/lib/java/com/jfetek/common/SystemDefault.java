package com.jfetek.common;

import java.nio.charset.Charset;
import java.sql.Time;

import com.jfetek.common.time.Date;
import com.jfetek.common.time.DateTime;

public final class SystemDefault {
	
	// charset
	public static final 			String	CHARSET_VALUE			= "UTF-8";
	public static final 		   Charset	CHARSET					= Charset.forName(CHARSET_VALUE);
	
	// null
//	public static final				Object	NULL					= new Object() {
//		@Override public			String	toString()				{	return String.valueOf((Object) null);	}
//		@Override public		   boolean	equals(Object obj)		{	return (null == obj || this == obj);	}
//		@Override public			   int	hashCode()				{	return 0;								}
//	};
//	public static class NullObject {
//		private static final NullObject INSTANCE;
//		static {
//			INSTANCE = new NullObject();
//		}
//		private NullObject() {}
//		@Override
//		public boolean equals(Object obj) {
//			return (null == obj || this == obj);
//		}
//		@Override
//		public String toString() {
//			return String.valueOf((Object) null);
//		}
//		static synchronized NullObject getNullObject() {
//			return INSTANCE;
//		}	
//	}
//	public static final 		NullObject	NULL					= NullObject.getNullObject();
	public static final class Null {
		private static final String	STRING_VALUE	= String.valueOf((Object) null);
		private Null() {
		}
		@Override
		protected final Object clone() {
			return this;
		}
		@Override
		public final boolean equals(Object obj) {
			return null==obj || this==obj;
		}
		@Override
		public final int hashCode() {
			return 0;
		}
		@Override
		public final String toString() {
			return STRING_VALUE;
		}
		
		public final <T> T of(T object) {
			return (T) null;
		}
		public final <T> T of(Class<T> clazz) {
			return clazz.cast(null);
		}
	}
	public static final				  Null	NULL					= new Null();
	public static final 			String	NULL_STRING				= NULL.toString();
	

	public static final class None {
		private None() {
		}
		@Override
		protected final Object clone() {
			return this;
		}
		@Override
		public final boolean equals(Object obj) {
			return this==obj;
		}
		@Override
		public final int hashCode() {
			return Integer.MIN_VALUE;
		}
		@Override
		public final String toString() {
			return "N/A";
		}
	}
	public static final				  None	NA						= new None();
	
	// sql
	public static final 			String	SQL_TIMESTAMP_VALUE		= "0001-01-01 00:00:00";
	public static final java.sql.Timestamp	SQL_TIMESTAMP			= java.sql.Timestamp.valueOf(SQL_TIMESTAMP_VALUE);
	public static final				  long	SQL_TIMESTAMP_MILLIS	= SQL_TIMESTAMP.getTime();
	public static final 			String	SQL_DATE_VALUE			= SQL_TIMESTAMP_VALUE.substring(0, 10);
	public static final 	 java.sql.Date	SQL_DATE				= java.sql.Date.valueOf(SQL_DATE_VALUE);
	public static final				  long	SQL_DATE_MILLIS			= SQL_DATE.getTime();
	public static final 			String	SQL_TIME_VALUE			= SQL_TIMESTAMP_VALUE.substring(11);
	public static final		 java.sql.Time	SQL_TIME				= java.sql.Time.valueOf(SQL_TIME_VALUE);
	public static final				  long	SQL_TIME_MILLIS			= SQL_TIME.getTime();
	
	// date-time
	public static final			  DateTime	DATE_TIME				= DateTime.valueOf(SQL_TIMESTAMP_VALUE);
	public static final				  Date	DATE					= Date.valueOf(SQL_DATE_VALUE);
	public static final				  Time	TIME					= Time.valueOf(SQL_TIME_VALUE);
	
	// common
	public static final				   int	UNKNOWN_CODE			= 0;
	public static final				String	UNKNOWN_VALUE			= "unknown";
	public static final				   int	NEGATIVE_VALUE			= -1;
	public static final				   int	ERROR_VALUE				= -1;
	public static final				   int	NO_VALUE				= -1;
	
	// type
//	public static final			   Boolean	BOOLEAN					= (Boolean) null;
	public static final			   Boolean	BOOLEAN					= Boolean.FALSE;
//	public static final				  Byte	BYTE					= (Byte) null;
//	public static final			 Character	CHARACTER				= (Character) null;
//	public static final				 Short	SHORT					= (Short) null;
//	public static final			   Integer	INTEGER					= (Integer) null;
//	public static final				  Long	LONG					= (Long) null;
//	public static final				 Float	FLOAT					= (Float) null;
//	public static final				Double	DOUBLE					= (Double) null;
	public static final				  Byte	BYTE					= NULL.of(Byte.class);
	public static final			 Character	CHARACTER				= NULL.of(Character.class);
	public static final				 Short	SHORT					= NULL.of(Short.class);
	public static final			   Integer	INTEGER					= NULL.of(Integer.class);
	public static final				  Long	LONG					= NULL.of(Long.class);
	public static final				 Float	FLOAT					= NULL.of(Float.class);
	public static final				Double	DOUBLE					= NULL.of(Double.class);

	// java-type
	public static final		java.util.Date	JAVA_DATE				= (java.util.Date) null;
	
	public static final				String	STRING					= null;
	
	
	// premitive type
	public static final			   boolean	BOOLEAN_VALUE			= false;
	public static final				  byte	BYTE_VALUE				= (byte) 0x00;
	public static final				  char	CHAR_VALUE				= (char) 0x00;
	public static final				 short	SHORT_VALUE				= -1;
	public static final				   int	INT_VALUE				= -1;
	public static final 			  long	LONG_VALUE				= -1L;
	public static final				 float	FLOAT_VALUE				= -1.0f;
	public static final				double	DOUBLE_VALUE			= -1.0;

	public static final				  byte	UNSIGNED_BYTE_VALUE		= (byte) 0x00;
	public static final				  char	UNSIGNED_CHAR_VALUE		= (char) 0x00;
	public static final				 short	UNSIGNED_SHORT_VALUE	= 0;
	public static final				   int	UNSIGNED_INT_VALUE		= 0;
	public static final 			  long	UNSIGNED_LONG_VALUE		= 0L;
	public static final				 float	UNSIGNED_FLOAT_VALUE	= 0f;
	public static final				double	UNSIGNED_DOUBLE_VALUE	= 0;
	
	
	// error value
//	public static final			   boolean	BOOLEAN_ERROR_VALUE		= false;
//	public static final				  byte	BYTE_ERROR_VLAUE		= (byte) -1;
//	public static final				  char	CHAR_ERROR_VALUE		= 0x00;
//	public static final				   int	INT_ERROR_VALUE			= -1;
//	public static final				  long	LONG_ERROR_VALUE		= -1L;
//	public static final				double	DOUBLE_ERROR_VALUE		= -0;
	
	// buffer
	public static final				   int	BUFFER_SIZE				= 1024;
	public static final				   int	LARGE_BUFFER_SIZE		= 4096;
	public static final				   int	EOF						= -1;
	public static final				String	EOL						= "\n";
	
	// Objects
//	public static final			    Random	RANDOM					= new Random();
	
	public static final				   int	RADIX					= 10;
	public static final				String	MIME_TYPE				= "application/octet-stream";
	
	private SystemDefault() {
	}

	// extendable
//	public static final				VariableSetup	EXT					= new VariableSetup();

	
	
	public static final class Patterns {
		
		
		
		private Patterns() {
		}
	}
}
