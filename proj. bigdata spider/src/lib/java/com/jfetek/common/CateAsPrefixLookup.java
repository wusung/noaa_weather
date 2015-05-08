package com.jfetek.common;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.json.JSONObject;

import com.jfetek.common.json.JsonWrapper;

public class CateAsPrefixLookup extends AbstractLookup {
	
	private VariableSetup _setup;
	private String adhere;
	public CateAsPrefixLookup(VariableSetup setup, String adhere) {
		this._setup = setup;
		this.adhere = adhere;
	}

	public Properties toProperties() {
		Properties p = new Properties();
		
		Iterator<String> it = this._setup.categories().iterator();
		while (it.hasNext()) {
			String category = it.next();

			Set<String> keys = _setup.aliases(category);
			Iterator<String> it2 = keys.iterator();
			while (it2.hasNext()) {
				String key = it2.next();
				String value = this._setup.val(category, key);
				
				p.setProperty(category + adhere + key, value);
			}
		}
		
		return p;
	}

	@Override
	protected String _lookup(String key) {
		int idx = key.indexOf(adhere);
		String cate = key.substring(0, idx);
		String name = key.substring(adhere.length()+idx);
		return _setup.val(cate, name);
	}


	public JSONObject toJson() {
		JsonWrapper json = new JsonWrapper();
		
		Iterator<String> it = this._setup.categories().iterator();
		while (it.hasNext()) {
			String category = it.next();

			Set<String> keys = _setup.aliases(category);
			Iterator<String> it2 = keys.iterator();
			while (it2.hasNext()) {
				String key = it2.next();
				String value = this._setup.val(category, key);

				json.put(category+this.adhere+key, value);
			}
		}
		
		return json.toJson();
	}
}
