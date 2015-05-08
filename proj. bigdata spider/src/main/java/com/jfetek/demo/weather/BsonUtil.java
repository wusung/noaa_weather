package com.jfetek.demo.weather;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;
import org.json.JSONArray;
import org.json.JSONObject;

import com.jfetek.common.ErrorCode;
import com.jfetek.common.Lookup;
import com.jfetek.common.VariableSetup;
import com.jfetek.common.data.JsonArrayDescribable;
import com.jfetek.common.data.JsonDescribable;
import com.jfetek.common.data.Pair;
import com.jfetek.common.data.Result;
import com.jfetek.common.db.DatabaseEnum;
import com.jfetek.common.time.DateRange;
import com.mongodb.util.JSON;

public class BsonUtil {
	
	private BsonUtil() {
	}
	
	public static BasicBSONObject bsonFromJson(JSONObject json) {
		BasicBSONObject bson = new BasicBSONObject();
		@SuppressWarnings("unchecked")
		Iterator<String> keys = json.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			Object value = json.opt(key);
			bson.put(key, value);
		}
		return bson;
	}
	public static BasicBSONList bsonFromJson(JSONArray json) {
		BasicBSONList bson = new BasicBSONList();
		for (int i = 0, len = json.length(); i < len; ++i) {
			Object value = json.opt(i);
			bson.put(i, value);
		}
		return bson;
	}

	public static Object makeBsonize(Object obj) {
		if (null == obj) return null;
//System.out.println("_helper: object");
		if (obj instanceof ErrorCode) {
			return bsonFromJson(((ErrorCode)obj).toJson());
		}
		else if (obj instanceof Enum) {
			return ((Enum<?>)obj).ordinal();
		}
		else if (obj instanceof DatabaseEnum) {
			return ((DatabaseEnum<?>)obj).toNumber();
		}
		else if (obj instanceof Pair) {
			return bsonFromJson(((Pair<?,?>)obj).toJsonArray());
		}
		else if (obj instanceof Lookup) {
			return bsonFromJson(((Lookup)obj).toJson());
		}
		else if (obj instanceof VariableSetup) {
			return bsonFromJson(((VariableSetup)obj).toJson());
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
			return bsonFromJson(((DateRange)obj).toJsonArray());
		}
		else if (obj instanceof Object[]) {
			return makeBsonize((Object[])obj);
		}
		else if (obj instanceof Collection) {
			return makeBsonize((Collection<?>)obj);
		}
		else if (obj instanceof Map) {
			return makeBsonize((Map<?,?>)obj);
		}
		else if (obj instanceof JsonDescribable) {
			return bsonFromJson(((JsonDescribable<?>)obj).toJson());
		}
		else if (obj instanceof JsonArrayDescribable) {
			return bsonFromJson(((JsonArrayDescribable<?>)obj).toJsonArray());
		}
		return obj;
	}
	public static BasicBSONList makeBsonize(Object[] array) {
		BasicBSONList bson = new BasicBSONList();
		for (int i = 0, len = array.length; i < len; ++i) {
			Object obj = array[i];
			bson.put(i, makeBsonize(obj));
		}
		return bson;
	}
	public static BasicBSONList makeBsonize(Collection<?> collection) {
//System.out.println("_helper: collection");
		BasicBSONList array = new BasicBSONList();
		Iterator<?> it = collection.iterator();
		for (int i = 0; it.hasNext(); ++i) {
			Object obj = it.next();
			array.put(i, makeBsonize(obj));
		}
		return array;
	}
	public static BasicBSONObject makeBsonize(Map<?,?> map) {
//System.out.println("_helper: map");
		BasicBSONObject bson = new BasicBSONObject();
		Iterator<?> it = map.keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			Object value = map.get(key);
			bson.put(String.valueOf(key), makeBsonize(value));
		}
		return bson;
	}
	
	public static <T> BasicBSONList makeArray(Collection<? extends JsonDescribable<T>> data) {
		BasicBSONList array = new BasicBSONList();
		if (null == data) return array;
		Iterator<? extends JsonDescribable<T>> it = data.iterator();
		for (int i = 0; it.hasNext(); ++i) {
			JsonDescribable<T> t = it.next();
			array.put(i, bsonFromJson(t.toJson()));
		}
		return array;
	}
	public static <T> BasicBSONObject makeObject(Collection<? extends JsonDescribable<T>> data, String key_field) {
		BasicBSONObject bson = new BasicBSONObject();
		if (data.size() > 0) {
			Iterator<? extends JsonDescribable<T>> it = data.iterator();
			while (it.hasNext()) {
				JsonDescribable<T> t = it.next();
				JSONObject obj = t.toJson();
				String key = obj.optString(key_field);
				bson.put(key, bsonFromJson(obj));
			}
		}
		return bson;
	}
	public static <K,T> BasicBSONObject makeObject(Map<K,? extends JsonDescribable<T>> data) {
		BasicBSONObject bson = new BasicBSONObject();
		if (data.size() > 0) {
			Iterator<K> it = data.keySet().iterator();
			while (it.hasNext()) {
				K key = it.next();
				JsonDescribable<T> t = data.get(key);
				bson.put(String.valueOf(key), bsonFromJson(t.toJson()));
			}
		}
		return bson;
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
	
	public static BasicBSONObject getBasicBson(ErrorCode ec) {
		if (null == ec) return null;
		BasicBSONObject bson = new BasicBSONObject();
		bson.put("ok", ec.positive());
		bson.put("error", bsonFromJson(ec.toJson()));
		bson.put("timestamp", System.currentTimeMillis());
		return bson;
	}
	public static BasicBSONObject getBasicBson(Result<?> result) {
		if (null == result) return null;
		BasicBSONObject bson = new BasicBSONObject();
		bson.put("ok", result.positive());
		bson.put("error", result.toJson());
		bson.put("result", makeBsonize(result.data));
		bson.put("timestamp", System.currentTimeMillis());
		return bson;
	}
	
	public static BasicBSONObject addBasicBsonFields(BasicBSONObject bson, ErrorCode ec) {
		if (null == ec) return bson;
		bson.put("ok", ec.positive());
		bson.put("error", ec.toJson());
		bson.put("timestamp", System.currentTimeMillis());
		return bson;
	}
	public static BasicBSONObject addBasicJsonFields(BasicBSONObject bson, Result<?> result) {
		if (null == result) return bson;
		bson.put("ok", result.positive());
		bson.put("error", result.toJson());
		bson.put("result", makeBsonize(result.data));
		bson.put("timestamp", System.currentTimeMillis());
		return bson;
	}
	
	// optional field: exectime
	public static BasicBSONObject addExecTimeField(BasicBSONObject bson, long ts_start, long ts_end) {
		bson.put("exectime", (ts_end - ts_start));
		return bson;
	}
	public static BasicBSONObject addExecTimeField(BasicBSONObject bson, long exec_time) {
		bson.put("exectime", exec_time);
		return bson;
	}

	public static BasicBSONObject addResultField(BasicBSONObject bson, Object result) {
		bson.put("result", makeBsonize(result));
		return bson;
	}
	public static <T> BasicBSONObject addResultField(BasicBSONObject bson, Collection<? extends JsonDescribable<T>> result) {
		bson.put("result", makeArray(result));
		return bson;
	}
	public static BasicBSONObject addResultField(BasicBSONObject bson, JSONObject result) {
		bson.put("result", bsonFromJson(result));
		return bson;
	}
	public static BasicBSONObject addResultField(BasicBSONObject bson, JSONArray result) {
		bson.put("result", bsonFromJson(result));
		return bson;
	}
	
	public static Object fromString(String s) {
		return fromString(s, null);
	}
	public static Object fromString(String s, BasicBSONObject default_value) {
		return JSON.parse(s);
	}
}
