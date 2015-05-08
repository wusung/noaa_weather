package com.jfetek.common.json;

import java.util.Collection;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.jfetek.common.data.JsonDescribable;
import com.jfetek.common.db.DatabaseEnum;
import com.jfetek.common.util.JsonUtil;

public class JsonWrapper implements JsonDescribable<JsonWrapper> {

	protected JSONObject json;
	
	public JsonWrapper() {
		this.json = new JSONObject();
	}
//	public JsonWrapper(String s) throws JSONException {
//		this.json = new JSONObject(s);
//	}
	public JsonWrapper(JSONObject json) {
		this.json = json;
	}
	
	
	public JsonWrapper put(String key, boolean value) {
		try {
			this.json.put(key, value);
		} catch (JSONException e) { }
		return this;
	}
	public JsonWrapper put(String key, byte value) {
		try {
			this.json.put(key, value);
		} catch (JSONException e) { }
		return this;
	}
	public JsonWrapper put(String key, char value) {
		try {
			this.json.put(key, value);
		} catch (JSONException e) { }
		return this;
	}
	public JsonWrapper put(String key, short value) {
		try {
			this.json.put(key, value);
		} catch (JSONException e) { }
		return this;
	}
	public JsonWrapper put(String key, int value) {
		try {
			this.json.put(key, value);
		} catch (JSONException e) { }
		return this;
	}
	public JsonWrapper put(String key, long value) {
		try {
			this.json.put(key, value);
		} catch (JSONException e) { }
		return this;
	}
	public JsonWrapper put(String key, float value) {
		try {
			this.json.put(key, value);
		} catch (JSONException e) { }
		return this;
	}
	public JsonWrapper put(String key, double value) {
		try {
			this.json.put(key, value);
		} catch (JSONException e) { }
		return this;
	}
	public JsonWrapper put(String key, Object value) {
		try {
			this.json.put(key, JsonUtil.makeJsonize(value));
		} catch(JSONException e) {}
		return this;
	}
	public <T> JsonWrapper put(String key, JsonDescribable<T> value) {
		try {
			this.json.put(key, value.toJson());
		} catch(JSONException e) {}
		return this;
	}
	public <T> JsonWrapper put(String key, Collection<? extends JsonDescribable<T>> value) {
		try {
			this.json.put(key, JsonUtil.makeArray(value));
		} catch (JSONException e) { }
		return this;
	}
	public <K,T> JsonWrapper put(String key, Map<K,? extends JsonDescribable<T>> value) {
		try {
			this.json.put(key, JsonUtil.makeJsonize(value));
		} catch (JSONException e) { }
		return this;
	}
	public JsonWrapper put(String key, java.util.Date value) {
		try {
			this.json.put(key, null==value? null : value.getTime());
		} catch (JSONException e) {}
		return this;
	}
	public JsonWrapper put(String key, Enum<?> e) {
		if (e instanceof DatabaseEnum) {
			DatabaseEnum<?> de = (DatabaseEnum<?>) e;
			try {
				this.json.put(key, 0==e.ordinal()&&!de.isEmptyAvailable()? null : e.ordinal());
			} catch (JSONException ex) {}
		}
		else {
			try {
				this.json.put(key, null==e? null : e.ordinal());
			} catch (JSONException ex) {}
		}
		return this;
	}


	public JsonWrapper putUnsigned(String key, byte value) {
		if (value < 0) return this;
		try {
			this.json.put(key, value);
		} catch (JSONException e) { }
		return this;
	}
	public JsonWrapper putUnsigned(String key, short value) {
		if (value < 0) return this;
		try {
			this.json.put(key, value);
		} catch (JSONException e) { }
		return this;
	}
	public JsonWrapper putUnsigned(String key, int value) {
		if (value < 0) return this;
		try {
			this.json.put(key, value);
		} catch (JSONException e) { }
		return this;
	}
	public JsonWrapper putUnsigned(String key, long value) {
		if (value < 0) return this;
		try {
			this.json.put(key, value);
		} catch (JSONException e) { }
		return this;
	}
	public JsonWrapper putUnsigned(String key, float value) {
		if (value < 0) return this;
		try {
			this.json.put(key, value);
		} catch (JSONException e) { }
		return this;
	}
	public JsonWrapper putUnsigned(String key, double value) {
		if (value < 0) return this;
		try {
			this.json.put(key, value);
		} catch (JSONException e) { }
		return this;
	}
	
	
	
	
//	public Object get(String key) {
//		return this.json.opt(key);
//	}
//	
//	public String getString(String key) {
//		return this.json.optString(key);
//	}
//	public String getString(String key, String default_value) {
//		return this.json.optString(key, default_value);
//	}
//	
//	public Boolean getBoolean(String key) {
//		return this.json.optBoolean(key);
//	}
//	public Boolean getBoolean(String key, boolean default_value) {
//		return this.json.optBoolean(key, default_value);
//	}
//	
//	public <E> E getEnum(Class<? extends Enum<E>> e, String key) {
//		int idx = this.json.optInt(key, -1);
//		if (idx < 0) return null;
//	}
	
	
	
	public JSONObject toJson() {
		return this.json;
	}
	public String describe() {
		return this.json.toString();
	}
	
	public JsonWrapper realize(String describe) {
		try {
			return realize(new JSONObject(describe));
		} catch (JSONException e) { }
		return null;
	}
	public JsonWrapper realize(JSONObject json) {
		this.json = json;
		return this;
	}
	
	@Override
	public String toString() {
		return this.describe();
	}
}
