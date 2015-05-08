package com.jfetek.common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;




public abstract class AbstractLookup implements Lookup {

	protected abstract Object _lookup(String key);
	
	
	public boolean has(String key) {
		return null!=_lookup(key);
	}
	

	public String lookup(String key) {
		return lookup(key, null);
	}
	public String lookup(String key, String default_value) {
		Object data = _lookup(key);
		return null==data? default_value : data.toString();
	}

	public Boolean lookupBoolean(String key) {
		return lookupBoolean(key, SystemDefault.BOOLEAN);
	}
	public Boolean lookupBoolean(String key, Boolean default_value) {
		Object data = _lookup(key);
		if (null == data) return default_value;
		if (data instanceof Boolean) return ((Boolean) data);
		String str = data.toString();
		if ("true".equalsIgnoreCase(str) || "1".equals(str)) return Boolean.TRUE;
		if ("false".equalsIgnoreCase(str) || "0".equals(str)) return Boolean.FALSE;
		return default_value;
	}
//	public Boolean lookupBoolean(String key, boolean default_value) {
//		return lookupBoolean(key, Boolean.valueOf(default_value));
//	}
	

	public Byte lookupByte(String key) {
		return lookupByte(key, SystemDefault.BYTE);
	}
	public Byte lookupByte(String key, Byte default_value) {
		Object data = _lookup(key);
		if (null == data) return default_value;
		if (data instanceof Byte) return ((Byte) data);
		Byte value = null;
		try {
			value = Byte.decode( data.toString() );
		} catch(NumberFormatException e) { }
		return null==value? default_value : value;
	}
//	public Byte lookupByte(String key, byte default_value) {
//		return lookupByte(key, Byte.valueOf(default_value));
//	}
	public Byte lookupByte(String key, int radix, Byte default_value) {
		Object data = _lookup(key);
		if (null == data) return default_value;
		if (data instanceof Byte) return ((Byte) data);
		Byte value = null;
		try {
			value = Byte.valueOf( data.toString() , radix );
		} catch(NumberFormatException e) { }
		return null==value? default_value : value;
	}
	

	public Character lookupChar(String key) {
		return lookupChar(key, SystemDefault.CHARACTER);
	}
	public Character lookupChar(String key, Character default_value) {
		Object data = _lookup(key);
		if (null == data) return default_value;
		if (data instanceof Character) return ((Character) data);
		String str = data.toString();
		return 0==str.length()? default_value : Character.valueOf( str.charAt(0) );
	}
//	public Character lookupChar(String key, char default_value) {
//		return lookupChar(key, Character.valueOf(default_value));
//	}
	

	public Integer lookupInt(String key) {
		return lookupInt(key, SystemDefault.INTEGER);
	}
	public Integer lookupInt(String key, Integer default_value) {
		Object data = _lookup(key);
		if (null == data) return default_value;
		if (data instanceof Integer) return ((Integer) data);
		Integer value = null;
		try {
			value = Integer.decode( data.toString() );
		} catch(NumberFormatException e) { }
		return null==value? default_value : value;
	}
//	public Integer lookupInt(String key, int default_value) {
//		return lookupInt(key, Integer.valueOf(default_value));
//	}
	public Integer lookupInt(String key, int radix, Integer default_value) {
		Object data = _lookup(key);
		if (null == data) return default_value;
		if (data instanceof Integer) return ((Integer) data);
		Integer value = null;
		try {
			value = Integer.valueOf( data.toString() , radix );
		} catch(NumberFormatException e) { }
		return null==value? default_value : value;
	}

	
	public Long lookupLong(String key) {
		return lookupLong(key, SystemDefault.LONG);
	}
	public Long lookupLong(String key, Long default_value) {
		Object data = _lookup(key);
		if (null == data) return default_value;
		if (data instanceof Long) return ((Long) data);
		Long value = null;
		try {
			value = Long.decode( data.toString() );
		} catch(NumberFormatException e) { }
		return null==value? default_value : value;
	}
//	public Long lookupLong(String key, long default_value) {
//		return lookupLong(key, Long.valueOf(default_value));
//	}
	public Long lookupLong(String key, int radix, Long default_value) {
		Object data = _lookup(key);
		if (null == data) return default_value;
		if (data instanceof Long) return ((Long) data);
		Long value = null;
		try {
			value = Long.valueOf( data.toString() , radix );
		} catch(NumberFormatException e) { }
		return null==value? default_value : value;
	}


	public Float lookupFloat(String key) {
		return lookupFloat(key, SystemDefault.FLOAT);
	}
	public Float lookupFloat(String key, Float default_value) {
		Object data = _lookup(key);
		if (null == data) return default_value;
		if (data instanceof Float) return ((Float) data);
		Float value = null;
		try {
			value = Float.valueOf( data.toString() );
		} catch(NumberFormatException e) { }
		return null==value? default_value : value;
	}
	
	
	public Double lookupDouble(String key) {
		return lookupDouble(key, SystemDefault.DOUBLE);
	}
	public Double lookupDouble(String key, Double default_value) {
		Object data = _lookup(key);
		if (null == data) return default_value;
		if (data instanceof Double) return ((Double) data);
		Double value = null;
		try {
			value = Double.valueOf( data.toString() );
		} catch(NumberFormatException e) { }
		return null==value? default_value : value;
	}



	public JSONObject lookupJson(String key) {
		return lookupJson(key, null);
	}
	public JSONObject lookupJson(String key, JSONObject default_value) {
		Object data = _lookup(key);
		if (null == data) return default_value;
		if (data instanceof JSONObject) return ((JSONObject) data);
		JSONObject value = null;
		try {
			value = new JSONObject( data.toString() );
		} catch(JSONException e) { }
		return null==value? default_value : value;
	}
	
	
	public JSONArray lookupJsonArray(String key) {
		return lookupJsonArray(key, null);
	}
	public JSONArray lookupJsonArray(String key, JSONArray default_value) {
		Object data = _lookup(key);
		if (null == data) return default_value;
		if (data instanceof JSONArray) return ((JSONArray) data);
		JSONArray value = null;
		try {
			value = new JSONArray( data.toString() );
		} catch(JSONException e) { }
		return null==value? default_value : value;
	}

}
