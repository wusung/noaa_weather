package com.jfetek.common.data;

public interface Describable<T> {

	public String describe();
	public T realize(String describe); 
	
}
