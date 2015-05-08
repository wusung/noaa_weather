package com.jfetek.common.data;

import org.json.JSONObject;

public interface JsonDescribable<T> extends Describable<T> {

	public JSONObject toJson();
	public T realize(JSONObject json);
	
}
