package com.jfetek.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import com.jfetek.common.ErrorCode;
import com.jfetek.common.Lookup;
import com.jfetek.common.VariableSetup;
import com.jfetek.common.data.JsonArrayDescribable;
import com.jfetek.common.data.JsonDescribable;
import com.jfetek.common.data.Pair;
import com.jfetek.common.data.Result;
import com.jfetek.common.db.DatabaseEnum;
import com.jfetek.common.time.DateRange;

public class JsonUtil {
	
	private JsonUtil() {
	}

	public static Object makeJsonize(Object obj) {
		if (null == obj) return null;
//System.out.println("_helper: object");
		if (obj instanceof ErrorCode) {
			return ((ErrorCode)obj).toJson();
		}
		else if (obj instanceof Enum) {
			return ((Enum<?>)obj).ordinal();
		}
		else if (obj instanceof DatabaseEnum) {
			return ((DatabaseEnum<?>)obj).toNumber();
		}
		else if (obj instanceof Pair) {
			return ((Pair<?,?>)obj).toJsonArray();
		}
		else if (obj instanceof Lookup) {
			return ((Lookup)obj).toJson();
		}
		else if (obj instanceof VariableSetup) {
			return ((VariableSetup)obj).toJson();
		}
		else if (obj instanceof java.util.Date) {
			return((java.util.Date)obj).getTime();
		}
		else if (obj instanceof com.jfetek.common.time.Time) {
			return ((com.jfetek.common.time.Time)obj).timestamp;
		}
		else if (obj instanceof com.jfetek.common.time.Date) {
			return ((com.jfetek.common.time.Date)obj).timestamp;
		}
		else if (obj instanceof com.jfetek.common.time.DateTime) {
			return ((com.jfetek.common.time.DateTime)obj).timestamp;
		}
		else if (obj instanceof DateRange) {
			return ((DateRange)obj).toJsonArray();
		}
		else if (obj instanceof Object[]) {
			return makeJsonize((Object[])obj);
		}
		else if (obj instanceof Collection) {
			return makeJsonize((Collection<?>)obj);
		}
		else if (obj instanceof Map) {
			return makeJsonize((Map<?,?>)obj);
		}
		else if (obj instanceof JsonDescribable) {
			return ((JsonDescribable<?>)obj).toJson();
		}
		else if (obj instanceof JsonArrayDescribable) {
			return ((JsonArrayDescribable<?>)obj).toJsonArray();
		}
		return obj;
	}
	public static JSONArray makeJsonize(Object[] array) {
		JSONArray json = new JSONArray();
		for (int i = 0, len = array.length; i < len; ++i) {
			Object obj = array[i];
			json.put(makeJsonize(obj));
		}
		return json;
	}
	public static JSONArray makeJsonize(Collection<?> collection) {
//System.out.println("_helper: collection");
		JSONArray array = new JSONArray();
		Iterator<?> it = collection.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			array.put(makeJsonize(obj));
		}
		return array;
	}
	public static JSONObject makeJsonize(Map<?,?> map) {
//System.out.println("_helper: map");
		JSONObject json = new JSONObject();
		Iterator<?> it = map.keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			Object value = map.get(key);
			try {
				json.put(String.valueOf(key), makeJsonize(value));
			} catch (JSONException e) {}
		}
		return json;
	}
	
	public static <T> JSONArray makeArray(Collection<? extends JsonDescribable<T>> data) {
		JSONArray array = new JSONArray();
		if (null == data) return array;
		Iterator<? extends JsonDescribable<T>> it = data.iterator();
		while (it.hasNext()) {
			JsonDescribable<T> t = it.next();
			array.put(t.toJson());
		}
		return array;
	}
	public static <T> JSONObject makeObject(Collection<? extends JsonDescribable<T>> data, String key_field) {
		JSONObject json = new JSONObject();
		if (data.size() > 0) {
			Iterator<? extends JsonDescribable<T>> it = data.iterator();
			while (it.hasNext()) {
				try {
					JsonDescribable<T> t = it.next();
					JSONObject obj = t.toJson();
					String key = obj.optString(key_field);
					json.put(key, obj);
				} catch(JSONException e) {}
			}
		}
		return json;
	}
	public static <K,T> JSONObject makeObject(Map<K,? extends JsonDescribable<T>> data) {
		JSONObject json = new JSONObject();
		if (data.size() > 0) {
			Iterator<K> it = data.keySet().iterator();
			while (it.hasNext()) {
				K key = it.next();
				JsonDescribable<T> t = data.get(key);
				try {
					json.put(String.valueOf(key), t.toJson());
				} catch(JSONException e) {}
			}
		}
		return json;
	}
//	private static <T> JSONArray _helper(Collection<? extends JsonDescribable<T>> data) {
//System.out.println("_helper: collection");
//		JSONArray array = new JSONArray();
//		if (data.size() > 0) {
//			Iterator<? extends JsonDescribable<T>> it = data.iterator();
//			while (it.hasNext()) {
//				JsonDescribable<T> t = it.next();
//				array.put(t.toJson());
//			}
//		}
//		return array;
//	}
//	private static <K,T> JSONObject _helper(Map<K,? extends JsonDescribable<T>> map) {
//		JSONObject json = new JSONObject();
//		if (map.size() > 0) {
//			Iterator<Entry<K,? extends JsonDescribable<T>>> it2 = map.entrySet().iterator();
//			while (it2.hasNext()) {
//				Entry<K, ? extends JsonDescribable<T>> entry = it2.next();
//				try {
//					json.put(String.valueOf(entry.getKey()), entry.getValue().toJson());
//				} catch (JSONException e) {}
//			}
//			
////			Iterator<K> it = map.keySet().iterator();
////			while (it.hasNext()) {
////				K k = it.next();
////				JsonDescribable<T> t = map.get(k);
////				try {
////					json.put(String.valueOf(k), t.toJson());
////				} catch (JSONException e) {}
////			}
//		}
//		return json;
//	}
	
	public static ArrayList<Object> toList(JSONArray array) {
		if (null == array) return null;
		int len = array.length();
		ArrayList<Object> list = new ArrayList<Object>(len);
		if (0 == len) return list;
		for (int i = 0; i < len; ++i) {
			list.add( array.opt(i) );
		}
		return list;
	}

	public static ArrayList<String> toStringArray(JSONArray array) {
		if (null == array) return null;
		int len = array.length();
		ArrayList<String> list = new ArrayList<String>(len);
		if (0 == len) return list;
		for (int i = 0; i < len; ++i) {
			list.add( array.optString(i) );
		}
		return list;
	}
	
	public static HashMap<String,Object> toMap(JSONObject json) {
		if (null == json) return null;
		int len = json.length();
		HashMap<String,Object> map = new HashMap<String,Object>();
		if (0 == len) return map;
		Iterator<?> it = json.keys();
		while (it.hasNext()) {
			String key = String.valueOf(it.next());
			Object val = json.opt(key);
			map.put(key, val);
		}
		return map;
	}
	
	public static HashMap<String,String> toStringMap(JSONObject json) {
		if (null == json) return null;
		int len = json.length();
		HashMap<String,String> map = new HashMap<String,String>();
		if (0 == len) return map;
		Iterator<?> it = json.keys();
		while (it.hasNext()) {
			String key = String.valueOf(it.next());
			String val = json.optString(key);
			map.put(key, val);
		}
		return map;
	}
	
	public static JSONObject getBasicJson(ErrorCode ec) {
		if (null == ec) return null;
		JSONObject json = new JSONObject();
		try {
			json.put("ok", ec.positive())
				.put("error", ec.toJson())
				.put("timestamp", System.currentTimeMillis());
		} catch (JSONException e) {}
		return json;
	}
	public static JSONObject getBasicJson(Result<?> result) {
		if (null == result) return null;
		JSONObject json = new JSONObject();
		try {
			json.put("ok", result.positive())
				.put("error", result.toJson())
				.put("result", makeJsonize(result.data))
				.put("timestamp", System.currentTimeMillis());
		} catch (JSONException e) {}
		return json;
	}
	
	public static JSONObject addBasicJsonFields(JSONObject json, ErrorCode ec) {
		if (null == ec) return json;
		try {
			json.put("ok", ec.positive())
				.put("error", ec.toJson())
				.put("timestamp", System.currentTimeMillis());
		} catch (JSONException e) {}
		return json;
	}
	public static JSONObject addBasicJsonFields(JSONObject json, Result<?> result) {
		if (null == result) return json;
		try {
			json.put("ok", result.positive())
				.put("error", result.toJson())
				.put("result", makeJsonize(result.data))
				.put("timestamp", System.currentTimeMillis());
		} catch (JSONException e) {}
		return json;
	}
	public static JSONStringer addBasicJsonFields(JSONStringer json, ErrorCode ec) {
		if (null == ec) return json;
		try {
			json.key("ok").value(ec.positive())
				.key("error").value(ec.toJson())
				.key("timestamp").value(System.currentTimeMillis());
		} catch (JSONException e) {}
		return json;
	}
	public static JSONStringer addBasicJsonFields(JSONStringer json, Result<?> result) {
		if (null == result) return json;
		try {
			json.key("ok").value(result.positive())
				.key("error").value(result.toJson())
				.key("result").value(makeJsonize(result.data))
				.key("timestamp").value(System.currentTimeMillis());
		} catch (JSONException e) {}
		return json;
	}

	// optional field: exectime
	public static JSONObject addExecTimeField(JSONObject json, long ts_start, long ts_end) {
		try {
			// optional field: exectime
			json.put("exectime", (ts_end - ts_start));
		} catch (JSONException e) {}
		return json;
	}
	public static JSONObject addExecTimeField(JSONObject json, long exec_time) {
		try {
			// optional field: exectime
			json.put("exectime", exec_time);
		} catch (JSONException e) {}
		return json;
	}
	public static JSONStringer addExecTimeField(JSONStringer json, long ts_start, long ts_end) {
		try {
			// optional field: exectime
			json.key("exectime").value(ts_end - ts_start);
		} catch (JSONException e) {}
		return json;
	}
	public static JSONStringer addExecTimeField(JSONStringer json, long exec_time) {
		try {
			// optional field: exectime
			json.key("exectime").value(exec_time);
		} catch (JSONException e) {}
		return json;
	}

	// optional field: ext	(extra information)
	public static JSONObject addExtraInfoField(JSONObject json, String ext_selector, String ext_value) {
		try {
			// optional field: ext
			JSONArray ext = json.optJSONArray("ext");
			if (ext == null) {
				ext = new JSONArray();
				json.put("ext", ext);
			}
			
			JSONObject extObj = new JSONObject();
			extObj.put( ext_selector , ext_value );
			ext.put(extObj);
		} catch (JSONException e) {}
		return json;
	}
	
	public static JSONObject addMessageField(JSONObject json, String message) {
		try {
			// optional field: message
			json.append("message", message);
		} catch (JSONException e) {}
		return json;
	}
	public static JSONStringer addExecTimeField(JSONStringer json, String message) {
		try {
			// optional field: exectime
			json.key("message").value(message);
		} catch (JSONException e) {}
		return json;
	}

//	public static JSONObject addSigninField(JSONObject json, SigninInfo info) {
//		try {
//			json.put("signin", info.toJson());
//		} catch(JSONException e) {}
//		return json;
//	}
	
	public static JSONObject addSigninField(JSONObject json, JSONObject info) {
		try {
			json.put("signin", info);
		} catch(JSONException e) {}
		return json;
	}
	
	public static JSONObject addResultField(JSONObject json, Object result) {
		try {
			json.put("result", makeJsonize(result));
		} catch(JSONException e) {}
		return json;
	}
	public static <T> JSONObject addResultField(JSONObject json, Collection<? extends JsonDescribable<T>> result) {
		try {
			json.put("result", makeArray(result));
		} catch (JSONException e) { }
		return json;
	}
	public static JSONObject addResultField(JSONObject json, JSONObject result) {
		try {
			json.put("result", result);
		} catch(JSONException e) {}
		return json;
	}
	public static JSONObject addResultField(JSONObject json, JSONArray result) {
		try {
			json.put("result", result);
		} catch(JSONException e) {}
		return json;
	}
	
	public static JSONObject addRefField(JSONObject json, String name, Object data) {
		try {
			JSONObject ref = json.optJSONObject("ref");
			if (null == ref) {
				ref = new JSONObject();
				json.put("ref", ref);
			}
			ref.put(name, makeJsonize(data));
		} catch(JSONException e) {}
		return json;
	}
	public static <T> JSONObject addRefField(JSONObject json, String name, JsonDescribable<T> data) {
		try {
			JSONObject ref = json.optJSONObject("ref");
			if (null == ref) {
				ref = new JSONObject();
				json.put("ref", ref);
			}
			ref.put(name, data.toJson());
		} catch(JSONException e) {}
		return json;
	}
	public static <T> JSONObject addRefField(JSONObject json, String name, Collection<? extends JsonDescribable<T>> data) {
		try {
			JSONObject ref = json.optJSONObject("ref");
			if (null == ref) {
				ref = new JSONObject();
				json.put("ref", ref);
			}
			ref.put(name, makeArray(data));
		} catch(JSONException e) {}
		return json;
	}
	public static <T> JSONObject addRefField(JSONObject json, String name, Collection<? extends JsonDescribable<T>> data, String key_field) {
		try {
			JSONObject ref = json.optJSONObject("ref");
			if (null == ref) {
				ref = new JSONObject();
				json.put("ref", ref);
			}
			ref.put(name, makeObject(data, key_field));
		} catch(JSONException e) {}
		return json;
	}
	
	public static JSONObject addField(JSONObject json, String field_name, Object data) {
		try {
			json.put(field_name, makeJsonize(data));
		} catch(JSONException e) {}
		return json;
	}
	public static <T> JSONObject addField(JSONObject json, String field_name, JsonDescribable<T> data) {
		try {
			json.put(field_name, data.toJson());
		} catch(JSONException e) {}
		return json;
	}
	public static <T> JSONObject addField(JSONObject json, String field_name, Collection<? extends JsonDescribable<T>> data) {
		try {
			json.put(field_name, makeArray(data));
		} catch (JSONException e) { }
		return json;
	}
	public static <T> JSONObject addField(JSONObject json, String field_name, Collection<? extends JsonDescribable<T>> data, String key_field) {
		try {
			json.put(field_name, makeObject(data, key_field));
		} catch (JSONException e) { }
		return json;
	}

	public static JSONObject appendField(JSONObject json, String field_name, Object data) {
		try {
			json.append(field_name, makeJsonize(data));
		} catch(JSONException e) {}
		return json;
	}
	public static <T> JSONObject appendField(JSONObject json, String field_name, JsonDescribable<T> data) {
		try {
			json.append(field_name, data.toJson());
		} catch(JSONException e) {}
		return json;
	}
	public static <T> JSONObject appendField(JSONObject json, String field_name, Collection<? extends JsonDescribable<T>> data) {
		try {
			json.append(field_name, makeArray(data));
		} catch (JSONException e) { }
		return json;
	}
	public static <T> JSONObject appendField(JSONObject json, String field_name, Collection<? extends JsonDescribable<T>> data, String key_field) {
		try {
			json.append(field_name, makeObject(data, key_field));
		} catch (JSONException e) { }
		return json;
	}

	public static JSONObject extendsByJson(JSONObject base_json, JSONObject addition_json) {
		if (null != addition_json && 0 != addition_json.length()) {
			try {
				Iterator<?> it = addition_json.keys();
				while (it.hasNext()) {
					String k = String.valueOf( it.next() );
					Object v = addition_json.opt(k);
					base_json.put(k, v);
				}
			} catch (JSONException e) { }
		}
		return base_json;	
	}
	
	
	public static JSONObject fromString(String s) {
		return fromString(s, null);
	}
	public static JSONObject fromString(String s, JSONObject default_value) {
		try {
			return new JSONObject(s);
		} catch (JSONException e) { }
		return default_value;
	}
}
