package com.jfetek.demo.weather.tasks.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfetek.demo.weather.Console;
import com.jfetek.demo.weather.Utils;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.DBCollection;

public class StationInsertTask {
	private static final Logger LOGGER		= LoggerFactory.getLogger(CountryInsertTask.class);
	
	public static void main(String[] args) {
		try {
			Console.startup();
			Console.setup.cates("weather:spider", "weather");
			
			DBCollection countries = Utils.getWeatherDb().getCollection("country");
			BulkWriteOperation bulk = countries.initializeUnorderedBulkOperation();
			DBCollection stations = MassInsertTask.createStationCollection(Utils.getWeatherDb(), bulk, true);
			BulkWriteResult rsBulk = bulk.execute();
			Console.shutdown();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			
		}
	}
}
