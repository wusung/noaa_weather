package com.jfetek.common.data;

import org.json.JSONArray;

public interface JsonArrayDescribable<T> extends Describable<T> {

	public JSONArray toJsonArray();
	public T realize(JSONArray json);
	
}
