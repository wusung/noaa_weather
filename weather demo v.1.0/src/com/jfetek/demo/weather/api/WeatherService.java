package com.jfetek.demo.weather.api;

import java.util.ArrayList;
import java.util.List;

import com.jfetek.common.time.DateRange;

public interface WeatherService {
	
	public static String DEFAULT_FIELDS = "_id,usaf,uban,name,country,state,geo,elv,data_range";
	
	ArrayList<ArrayList<?>> queryRawList(String stations, DateRange dateRange, String[] columns);
	ArrayList<ArrayList<?>> queryWeeklyList(String stations, DateRange dateRange, String[] columns);
	ArrayList<ArrayList<?>> queryMonthlyList(String stations, DateRange dateRange, String[] columns);
	ArrayList<ArrayList<?>> queryDailyList(String stations, DateRange dateRange, String[] columns);
}
