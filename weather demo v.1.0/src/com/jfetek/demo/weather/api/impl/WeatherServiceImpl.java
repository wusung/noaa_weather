package com.jfetek.demo.weather.api.impl;

import java.util.List;

import com.jfetek.common.time.DateRange;
import com.jfetek.demo.weather.api.WeatherService;
import com.jfetek.demo.weather.model.Weather;

public class WeatherServiceImpl implements WeatherService {

	@Override
	public List<Weather> queryList(int year, String country, DateRange dateRange, String[] columns) {
		return null;
	}
}
