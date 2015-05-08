package com.jfetek.common;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.json.JSONObject;

import com.jfetek.common.json.JsonWrapper;

public class CategoryFilteredLookup extends AbstractLookup {
	
	private final VariableSetup setup;
	private final String category;
	
	public CategoryFilteredLookup(VariableSetup setup, String category) {
		this.setup = setup;
		this.category = category;
	}

	@Override
	protected String _lookup(String key) {
		return this.setup.val(this.category, key);
	}

	@Override
	public boolean has(String key) {
		return this.setup.has(this.category, key);
	}

	@Override
	public String lookup(String key) {
		return this.setup.val(this.category, key);
	}

	@Override
	public String lookup(String key, String default_value) {
		return this.setup.val(this.category, key, default_value);
	}

	public Properties toProperties() {
		Properties p = new Properties();
		
		Set<String> keys = setup.aliases(this.category);
		if (null != keys) {
			Iterator<String> it = keys.iterator();
			while (it.hasNext()) {
				String key = it.next();
				String value = this.setup.val(this.category, key);
				
				p.setProperty(key, value);
			}
		}
		
		return p;
	}

	public JSONObject toJson() {
		JsonWrapper wrapper = new JsonWrapper();
		
		Set<String> keys = setup.aliases(this.category);
		if (null != keys) {
			Iterator<String> it = keys.iterator();
			while (it.hasNext()) {
				String key = it.next();
				String value = this.setup.val(this.category, key);
				
				wrapper.put(key, value);
			}
		}
		
		return wrapper.toJson();
	}

}
