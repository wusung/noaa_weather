package com.jfetek.demo.weather.tasks;

import java.util.EnumSet;

public enum WeatherTask {
	PARSE_COUNTRY("parse-country"),
	PARSE_STATION("parse-station"),
	PARSE_YEAR("parse-year"),
	DOWNLOAD("download"),
	TRANSFORM("transform"),
	INSERT("insert");
	
	public final String text;
	WeatherTask(String text) {
		this.text = text;
	}
	
	public String getTaskId(Object res) {
		return this.text + ":" + res;
	}
	
	public boolean equals(String text) {
		return this.text.equals(text);
	}
	
	public static WeatherTask of(String text) {
		for (WeatherTask t : WeatherTask.values()) {
			if (t.text.equals(text)) return t;
		}
		return null;
	}
	public static WeatherTask of(int ordinal) {
		WeatherTask[] values = WeatherTask.values();
		if (ordinal < 0 || ordinal >= values.length) return null;
		return values[ordinal];
	}
	
	public static EnumSet<WeatherTask> parseTask(String str) {
		String[] s = str.split(",");
		EnumSet<WeatherTask> set = EnumSet.noneOf(WeatherTask.class);
		for (int i = 0, len = s.length; i < len; ++i) {
			WeatherTask task = WeatherTask.of( s[i] );
			if (null != task) set.add(task);
		}
		return set;
	}
	
	@Override
	public String toString() {
		return this.text;
	}
}
