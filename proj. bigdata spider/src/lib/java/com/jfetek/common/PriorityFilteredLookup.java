package com.jfetek.common;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.json.JSONObject;

import com.jfetek.common.json.JsonWrapper;

public class PriorityFilteredLookup extends AbstractLookup {

	private final VariableSetup setup;
	private final String[] categories;
	private final int lenCate;
	
	public PriorityFilteredLookup(VariableSetup setup, String[] categories) {
		this.setup = setup;
		if (null == categories || 0 == categories.length) {
			// list all category, order by alphabet
			categories = setup.categories().toArray(new String[0]);
		}
		if (0 == categories.length) {
			// empty category... use default category ""
			categories = new String[] {
				""
			};
		}
		this.categories = categories;
		this.lenCate = categories.length;
	}
	
	public PriorityFilteredLookup(VariableSetup setup, List<String> categories) {
		this.setup = setup;
		if (null == categories || 0 == categories.size()) {
			// list all category, order by alphabet
			this.categories = setup.categories().toArray(new String[0]);
		}
		else if (0 == categories.size()) {
			// empty category... use default category ""
			this.categories = new String[] {
				""
			};
		}
		else {
			this.categories = categories.toArray(new String[0]);
		}
		this.lenCate = categories.size();
	}

	@Override
	protected String _lookup(String key) {
		String value = null;
		// lookup first non-null value
		for (int i = 0; i < lenCate; ++i) {
			value = this.setup.val(this.categories[i], key);
			if (null != value) break;
		}
		return value;
	}

	@Override
	public boolean has(String key) {
		for (int i = 0; i < lenCate; ++i) {
			if (this.setup.has(this.categories[i], key)) return true;
		}
		return false;
	}

	@Override
	public String lookup(String key) {
		String value = null;
		// lookup first non-null value
		for (int i = 0; i < lenCate; ++i) {
			value = this.setup.val(this.categories[i], key);
			if (null != value) break;
		}
		return value;
	}

	@Override
	public String lookup(String key, String default_value) {
		String value = null;
		// lookup first non-null value
		for (int i = 0; i < lenCate; ++i) {
			value = this.setup.val(this.categories[i], key);
			if (null != value) break;
		}
		return value;
	}

	public Properties toProperties() {
		Properties p = null;
		
		for (int i = this.categories.length-1; i >= 0; --i) {
			p = new Properties( p );
			Set<String> keys = setup.aliases(this.categories[i]);
			Iterator<String> it = keys.iterator();
			while (it.hasNext()) {
				String key = it.next();
				String value = this.setup.val(this.categories[i], key);
				
				p.setProperty(key, value);
			}
		}
		
		return p;
	}

	public JSONObject toJson() {
		JsonWrapper wrapper = new JsonWrapper();
		
		for (int i = this.categories.length-1; i >= 0; --i) {
			Set<String> keys = setup.aliases(this.categories[i]);
			Iterator<String> it = keys.iterator();
			while (it.hasNext()) {
				String key = it.next();
				String value = this.setup.val(this.categories[i], key);
				
				wrapper.put(key, value);
			}
		}
		
		return wrapper.toJson();
	}

}
