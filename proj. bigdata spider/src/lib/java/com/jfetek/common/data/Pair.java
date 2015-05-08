package com.jfetek.common.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jfetek.common.util.CompareUtil;
import com.jfetek.common.util.JsonUtil;

public class Pair<T1, T2> {
	
	public static <T1,T2> Pair<T1,T2> of(T1 first, T2 second) {
		// short then constructor
		return new Pair<T1,T2>(first, second);
	}

	protected T1 first;
	protected T2 second;
	
	public Pair(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}
	
	public T1 getFirst() {
		return first;
	}
	
	public void setFirst(T1 first) {
		this.first = first;
	}
	
	public T2 getSecond() {
		return second;
	}
	
	public void setSecond(T2 second) {
		this.second = second;
	}
	
	@Override
	public int hashCode() {
		int hashcode = (null==first? 0 : first.hashCode()) * (null==second? 0 : second.hashCode()) + 173644928;
		return hashcode;
	}
	

//	public boolean equals(Pair<T1,T2> p) {
//		if (null == p) return false;
//		if (this == p) return true;
//		return CompareUtil.isEqual(this.first, p.first) && CompareUtil.isEqual(this.second, p.second);
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		// override Object.equals(Object)
//		//	if not VariableExpress return false
//		return (obj instanceof Pair && equals((Pair<T1,T2>) obj));
//	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Pair)) return false;
		try {
			Pair p = (Pair) obj;
			return CompareUtil.isEqual(this.first, p.first) && CompareUtil.isEqual(this.second, p.second);
		} catch(ClassCastException e) {
			return false;
		} catch(Exception e) {
			return false;
		}
	}
	

	
	public JSONObject toJson() {
		return toJson("first", "second");
	}
	public JSONObject toJson(String first_name, String second_name) {
		JSONObject json = new JSONObject();
		try {
			json.put(first_name, JsonUtil.makeJsonize(this.first)).put(second_name, JsonUtil.makeJsonize(this.second));
		} catch (JSONException e) {}
		JsonUtil.addField(json, second_name, this.second);
		return json;
	}
	
	public JSONArray toJsonArray() {
		JSONArray array = new JSONArray();
		array.put(JsonUtil.makeJsonize(this.first)).put(JsonUtil.makeJsonize(this.second));
		return array;
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append('(').append(this.first).append(',').append(this.second).append(')');
		return s.toString();
	}
}
