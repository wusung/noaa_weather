package com.jfetek.demo.weather.api;

import java.util.ArrayList;

import com.jfetek.demo.weather.model.Station;

public interface StationService {

	/**
	 * Find station with country or lat/lng
	 * @param country
	 * @param g_lat
	 * @param g_lng
	 * @return
	 */
	ArrayList<Station> queryList(String country, double g_lat, double g_lng);
	
}
