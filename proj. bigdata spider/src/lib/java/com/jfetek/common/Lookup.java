package com.jfetek.common;

import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;


public interface Lookup {

	public String lookup(String key);
	public String lookup(String key, String default_value);
	
//	public String pointer(String key);
	
	public Boolean lookupBoolean(String key);
	public Boolean lookupBoolean(String key, Boolean default_value);
//	public Boolean lookupBoolean(String key, boolean default_value);
	
	public Byte lookupByte(String key);
	public Byte lookupByte(String key, Byte default_value);
//	public Byte lookupByte(String key, byte default_value);
	public Byte lookupByte(String key, int radix, Byte default_value);
	
	public Character lookupChar(String key);
	public Character lookupChar(String key, Character default_value);
//	public Character lookupChar(String key, char default_value);
	
	public Integer lookupInt(String key);
	public Integer lookupInt(String key, Integer default_value);
//	public Integer lookupInt(String key, int default_value);
	public Integer lookupInt(String key, int radix, Integer default_value);
	
	public Long lookupLong(String key);
	public Long lookupLong(String key, Long default_value);
//	public Long lookupLong(String key, long default_value);
	public Long lookupLong(String key, int radix, Long default_value);
	
	public Float lookupFloat(String key);
	public Float lookupFloat(String key, Float default_value);
	
	public Double lookupDouble(String key);
	public Double lookupDouble(String key, Double default_value);
	
	public JSONObject lookupJson(String key);
	public JSONObject lookupJson(String key, JSONObject default_value);
	
	public JSONArray lookupJsonArray(String key);
	public JSONArray lookupJsonArray(String key, JSONArray default_value);
	
//	public BigInteger lookupInteger(String key);
//	public BigInteger lookupInteger(String key, BigInteger default_value);
//	
//	public BigDecimal lookupDecimal(String key);
//	public BigDecimal lookupDecimal(String key, BigDecimal default_value);
	
	public boolean has(String key);
	
//	public int size();
	
	public Properties toProperties();
	
	public JSONObject toJson();
}
