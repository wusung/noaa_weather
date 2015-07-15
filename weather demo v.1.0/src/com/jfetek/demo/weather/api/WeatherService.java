package com.jfetek.demo.weather.api;

import java.util.List;

import com.jfetek.common.time.DateRange;
import com.jfetek.demo.weather.model.Weather;

public interface WeatherService {
	
	public static String DEFAULT_FIELDS = "_id,usaf,uban,name,country,state,geo,elv,data_range";
	List<Weather> queryList(int year, String country, DateRange dateRange, String[] columns);
	
}
