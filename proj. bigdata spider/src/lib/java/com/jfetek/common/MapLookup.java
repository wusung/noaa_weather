package com.jfetek.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.json.JSONObject;

import com.jfetek.common.json.JsonWrapper;
import com.jfetek.common.util.CompareUtil;

public class MapLookup extends AbstractLookup {
	
	protected HashMap<String,Object> m;
	
	public MapLookup(Map<String,String> map) {
		this.m = new HashMap<String,Object>(map);
	}

	
	@Override
	protected Object _lookup(String key) {
		return this.m.get(key);
	}

	@Override
	public boolean has(String key) {
		return this.m.containsKey(key);
	}


//	@Override
//	public String lookup(String key) {
//		return this.m.get(key);
//	}
//	@Override
//	public String lookup(String key, String default_value) {
//		String value = this.m.get(key);
//		return null==value? default_value : value;
//	}


	public Properties toProperties() {
		Properties p = new Properties();
		
		Iterator<Entry<String,Object>> it = this.m.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String,Object> e = it.next();
			String key = e.getKey();
			Object val = e.getValue();
			
			p.setProperty(key, CompareUtil.isNull(val)? null : String.valueOf(val));
		}

		return p;
	}

	public JSONObject toJson() {
		JsonWrapper wrapper = new JsonWrapper();
		
		Iterator<Entry<String,Object>> it = this.m.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String,Object> e = it.next();
			String key = e.getKey();
			Object val = e.getValue();
			
			wrapper.put(key, CompareUtil.isNull(val)? null : String.valueOf(val));
		}

		return wrapper.toJson();
	}

	
	public static void main(String[] args) {
		HashMap<String,String> m = new HashMap<String,String>();
		m.put("123", "123");
		m.put("123.456", "123.456");
		m.put("false", "trUe");
		m.put("test", "中文字123");
		m.put("json", "{a='a', b='b', n=123}");
		
		Lookup lookup = new MapLookup(m);
		System.out.println(lookup.lookup("123"));
		System.out.println(lookup.lookup("123.456"));
		System.out.println(lookup.lookup("false"));
		System.out.println(lookup.lookup("test"));
		System.out.println(lookup.lookup("json"));

		System.out.println("--- boolean -------------------------------------------");
		System.out.println(lookup.lookupBoolean("123"));
		System.out.println(lookup.lookupBoolean("123.456"));
		System.out.println(lookup.lookupBoolean("false"));
		System.out.println(lookup.lookupBoolean("test"));
		System.out.println(lookup.lookupBoolean("json"));

		System.out.println("--- byte -------------------------------------------");
		System.out.println(lookup.lookupByte("123"));
		System.out.println(lookup.lookupByte("123.456"));
		System.out.println(lookup.lookupByte("false"));
		System.out.println(lookup.lookupByte("test"));
		System.out.println(lookup.lookupByte("json"));

		System.out.println("--- char -------------------------------------------");
		System.out.println(lookup.lookupChar("123"));
		System.out.println(lookup.lookupChar("123.456"));
		System.out.println(lookup.lookupChar("false"));
		System.out.println(lookup.lookupChar("test"));
		System.out.println(lookup.lookupChar("json"));

		System.out.println("--- int -------------------------------------------");
		System.out.println(lookup.lookupInt("123"));
		System.out.println(lookup.lookupInt("123.456"));
		System.out.println(lookup.lookupInt("false"));
		System.out.println(lookup.lookupInt("test"));
		System.out.println(lookup.lookupInt("json"));

		System.out.println("--- long -------------------------------------------");
		System.out.println(lookup.lookupLong("123"));
		System.out.println(lookup.lookupLong("123.456"));
		System.out.println(lookup.lookupLong("false"));
		System.out.println(lookup.lookupLong("test"));
		System.out.println(lookup.lookupLong("json"));

		System.out.println("--- float -------------------------------------------");
		System.out.println(lookup.lookupFloat("123"));
		System.out.println(lookup.lookupFloat("123.456"));
		System.out.println(lookup.lookupFloat("false"));
		System.out.println(lookup.lookupFloat("test"));
		System.out.println(lookup.lookupFloat("json"));

		System.out.println("--- double -------------------------------------------");
		System.out.println(lookup.lookupDouble("123"));
		System.out.println(lookup.lookupDouble("123.456"));
		System.out.println(lookup.lookupDouble("false"));
		System.out.println(lookup.lookupDouble("test"));
		System.out.println(lookup.lookupDouble("json"));

		System.out.println("--- json -------------------------------------------");
		System.out.println(lookup.lookupJson("123"));
		System.out.println(lookup.lookupJson("123.456"));
		System.out.println(lookup.lookupJson("false"));
		System.out.println(lookup.lookupJson("test"));
		System.out.println(lookup.lookupJson("json"));
		
	}



}
