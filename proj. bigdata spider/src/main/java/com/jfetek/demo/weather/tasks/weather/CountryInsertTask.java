package com.jfetek.demo.weather.tasks.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfetek.demo.weather.Console;
import com.jfetek.demo.weather.Utils;
import com.mongodb.DBCollection;

public class CountryInsertTask {
	
	private static final Logger LOGGER		= LoggerFactory.getLogger(CountryInsertTask.class);
	
	public static void main(String[] args) {
		try {
			Console.startup();
			Console.setup.cates("weather:spider", "weather");					
			DBCollection records = MassInsertTask.createCountryCollection(Utils.getWeatherDb(), true);			
			Console.shutdown();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			
		}
	}
}
