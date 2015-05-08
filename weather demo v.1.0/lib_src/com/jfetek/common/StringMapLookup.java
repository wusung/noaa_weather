package com.jfetek.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.json.JSONObject;

import com.jfetek.common.json.JsonWrapper;

public class StringMapLookup extends AbstractLookup {
	
	protected HashMap<String,String> m;
	
	public StringMapLookup(Map<String,String> map) {
		this.m = new HashMap<String,String>(map);
	}

	
	@Override
	protected String _lookup(String key) {
		return this.m.get(key);
	}

	@Override
	public boolean has(String key) {
		return this.m.containsKey(key);
	}


	@Override
	public String lookup(String key) {
		return this.m.get(key);
	}
	@Override
	public String lookup(String key, String default_value) {
		String value = this.m.get(key);
		return null==value? default_value : value;
	}

	public Properties toProperties() {
		Properties p = new Properties();
		
		Iterator<Entry<String,String>> it = this.m.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String,String> e = it.next();
			String key = e.getKey();
			String value = e.getValue();
			
			p.setProperty(key, value);
		}

		return p;
	}

	public JSONObject toJson() {
		JsonWrapper wrapper = new JsonWrapper();
		
		Iterator<Entry<String,String>> it = this.m.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String,String> e = it.next();
			String key = e.getKey();
			String value = e.getValue();
			
			wrapper.put(key, value);
		}

		return wrapper.toJson();
	}

}
