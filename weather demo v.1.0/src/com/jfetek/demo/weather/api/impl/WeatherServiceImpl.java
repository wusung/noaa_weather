package com.jfetek.demo.weather.api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import com.jfetek.common.data.Result;
import com.jfetek.common.time.DateRange;
import com.jfetek.demo.weather.Console;
import com.jfetek.demo.weather.api.MongoAPI;
import com.jfetek.demo.weather.api.WeatherQuery;
import com.jfetek.demo.weather.api.WeatherService;

public class WeatherServiceImpl implements WeatherService {
	private static final Logger logger = LoggerFactory.getLogger(WeatherServiceImpl.class);

	@Override
	public ArrayList<ArrayList<?>> queryDailyList(WeatherQuery query) {
		DateRange dateRange = query.getDateRange();
		String stations = query.getStations();
		String[] columns = query.getColumns();
		int offset = query.getOffset();
		int limit = query.getLimit();
		int endIndex = offset + limit;
		
		MongoAPI api = new MongoAPI(Console.mongo.getDB("weather1"));		
		ArrayList<ArrayList<?>> dataList = new ArrayList<ArrayList<?>>();
		for (int year = dateRange.first.year.value, end = dateRange.last.year.value; year <= end; ++year) {
			Result<ArrayList<ArrayList<Object>>> result = api.queryDailyWeatherByStation(year, stations, dateRange, columns);
			logger.debug("year={}, message={}", year, result.message);
			if (result.positive()) {
				dataList.addAll(result.data);
			}
		}
		
		return dataList.size() >= limit ? 
				new ArrayList<ArrayList<?>>(dataList.subList(offset, endIndex)):
				dataList;
	}

	@Override
	public ArrayList<ArrayList<?>> queryRawList(WeatherQuery query) {
		
		DateRange dateRange = query.getDateRange();
		String stations = query.getStations();
		String[] columns = query.getColumns();
		int offset = query.getOffset();
		int limit = query.getLimit();
		int endIndex = offset + limit;
		
		MongoAPI api = new MongoAPI(Console.mongo.getDB("weather1"));		
		ArrayList<ArrayList<?>> dataList = new ArrayList<ArrayList<?>>();
		logger.debug("{}~{}", dateRange.first.year.value, dateRange.last.year.value);
		for (int year = dateRange.first.year.value, end = dateRange.last.year.value; year <= end; ++year) {
			Result<ArrayList<ArrayList<Object>>> result = api.queryWeatherByStation(year, stations, dateRange, columns);
			logger.debug("year={}, message={}", year, result.message);
			if (result.positive()) {
				dataList.addAll(result.data);
			}
		}
		logger.debug("size={}", dataList.size());
		return dataList.size() >= limit ? 
				new ArrayList<ArrayList<?>>(dataList.subList(offset, endIndex)):
				dataList;
	}

	@Override
	public ArrayList<ArrayList<?>> queryWeeklyList(WeatherQuery query) {
		DateRange dateRange = query.getDateRange();
		String stations = query.getStations();
		String[] columns = query.getColumns();
		int offset = query.getOffset();
		int limit = query.getLimit();
		int endIndex = offset + limit;
		
		MongoAPI api = new MongoAPI(Console.mongo.getDB("weather1"));		
		ArrayList<ArrayList<?>> dataList = new ArrayList<ArrayList<?>>();
		
		for (int year = dateRange.first.year.value, end = dateRange.last.year.value; year <= end; ++year) {
			Result<ArrayList<ArrayList<Object>>> result = api.queryWeeklyWeatherByStation(year, stations, dateRange, columns);
			logger.debug("year={}, message={}", year, result.message);
			if (result.positive()) {
				dataList.addAll(result.data);
			}
		}
		
		return dataList.size() >= limit ? 
				new ArrayList<ArrayList<?>>(dataList.subList(offset, endIndex)):
				dataList;
	}

	@Override
	public ArrayList<ArrayList<?>> queryMonthlyList(WeatherQuery query) {
		
		DateRange dateRange = query.getDateRange();
		String stations = query.getStations();
		String[] columns = query.getColumns();
		int offset = query.getOffset();
		int limit = query.getLimit();
		int endIndex = offset + limit;
		
		MongoAPI api = new MongoAPI(Console.mongo.getDB("weather1"));		
		ArrayList<ArrayList<?>> dataList = new ArrayList<ArrayList<?>>();
		
		for (int year = dateRange.first.year.value, end = dateRange.last.year.value; year <= end; ++year) {
			Result<ArrayList<ArrayList<Object>>> result = api.queryMonthlyWeatherByStation(year, stations, dateRange, columns);
			logger.debug("year={}, message={}", year, result.message);
			if (result.positive()) {
				dataList.addAll(result.data);
			}
		}
		
		return dataList.size() >= limit ? 
				new ArrayList<ArrayList<?>>(dataList.subList(offset, endIndex)):
				dataList;
	}
}
