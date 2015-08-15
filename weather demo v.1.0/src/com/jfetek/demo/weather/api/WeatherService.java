package com.jfetek.demo.weather.api;

import java.util.ArrayList;

public interface WeatherService {
	
	public static String DEFAULT_FIELDS = "_id,usaf,uban,name,country,state,geo,elv,data_range";
	
	ArrayList<ArrayList<?>> queryRawList(WeatherQuery query);
	ArrayList<ArrayList<?>> queryWeeklyList(WeatherQuery query);
	ArrayList<ArrayList<?>> queryMonthlyList(WeatherQuery query);
	ArrayList<ArrayList<?>> queryDailyList(WeatherQuery query);
}
