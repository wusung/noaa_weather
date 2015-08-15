package com.jfetek.demo.weather.api;

import com.jfetek.common.time.DateRange;

public class WeatherQuery {

	private String stations;
	private int limit;
	private DateRange dateRange; 
	private String[] columns;
	
	public String getStations() {
		return stations;
	}
	public void setStations(String stations) {
		this.stations = stations;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	public DateRange getDateRange() {
		return dateRange;
	}
	public void setDateRange(DateRange dateRange) {
		this.dateRange = dateRange;
	}
	public String[] getColumns() {
		return columns;
	}
	public void setColumns(String[] columns) {
		this.columns = columns;
	}	
}
