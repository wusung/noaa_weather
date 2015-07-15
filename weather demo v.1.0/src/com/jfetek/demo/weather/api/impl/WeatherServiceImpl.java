package com.jfetek.demo.weather.api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import com.jfetek.common.data.Result;
import com.jfetek.common.time.DateRange;
import com.jfetek.demo.weather.Console;
import com.jfetek.demo.weather.api.MongoAPI;
import com.jfetek.demo.weather.api.WeatherService;

public class WeatherServiceImpl implements WeatherService {
	private static final Logger logger = LoggerFactory.getLogger(WeatherServiceImpl.class);

	@Override
	public ArrayList<ArrayList<?>> queryDailyList(String stations, DateRange dateRange, String[] columns) {

		MongoAPI api = new MongoAPI(Console.mongo.getDB("weather1"));		
		ArrayList<ArrayList<?>> dataList = new ArrayList<ArrayList<?>>();
		for (int year = dateRange.first.year.value, end = dateRange.last.year.value; year <= end; ++year) {
			Result<ArrayList<ArrayList<Object>>> result = api.queryDailyWeatherByStation(year, stations, dateRange, columns);
			logger.debug("year={}, message={}", year, result.message);
			if (result.positive()) {
				dataList.addAll(result.data);
			}
		}
		
		return dataList;
	}

	@Override
	public ArrayList<ArrayList<?>> queryRawList(String stations, DateRange dateRange, String[] columns) {
		
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
		return dataList;
	}

	@Override
	public ArrayList<ArrayList<?>> queryWeeklyList(String stations, DateRange dateRange, String[] columns) {
		MongoAPI api = new MongoAPI(Console.mongo.getDB("weather1"));		
		ArrayList<ArrayList<?>> dataList = new ArrayList<ArrayList<?>>();
		
		for (int year = dateRange.first.year.value, end = dateRange.last.year.value; year <= end; ++year) {
			Result<ArrayList<ArrayList<Object>>> result = api.queryWeeklyWeatherByStation(year, stations, dateRange, columns);
			logger.debug("year={}, message={}", year, result.message);
			if (result.positive()) {
				dataList.addAll(result.data);
			}
		}
		
		return dataList;
	}

	@Override
	public ArrayList<ArrayList<?>> queryMonthlyList(String stations, DateRange dateRange, String[] columns) {
		MongoAPI api = new MongoAPI(Console.mongo.getDB("weather1"));		
		ArrayList<ArrayList<?>> dataList = new ArrayList<ArrayList<?>>();
		
		for (int year = dateRange.first.year.value, end = dateRange.last.year.value; year <= end; ++year) {
			Result<ArrayList<ArrayList<Object>>> result = api.queryMonthlyWeatherByStation(year, stations, dateRange, columns);
			logger.debug("year={}, message={}", year, result.message);
			if (result.positive()) {
				dataList.addAll(result.data);
			}
		}
		
		return dataList;
	}
}
