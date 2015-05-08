package com.jfetek.common.db;

public interface DatabaseEnum<E extends Enum<E>> {

	public E getEnum();
	public String toString();
	public Number toNumber();
	
	public boolean isEmptyAvailable();

//	public E getEnumOf(int order);
//	public E getEnumOf(String value);
	
//	public static class Helper {
//		private Helper() {}
//		
//		public
//	}
}
