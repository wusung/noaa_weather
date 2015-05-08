package com.jfetek.demo.weather.spider;

import java.io.File;
import java.util.EnumSet;

import com.jfetek.common.Lookup;
import com.jfetek.demo.weather.Console;
import com.jfetek.demo.weather.tasks.WeatherTask;
import com.jfetek.demo.weather.tasks.WeatherTaskManager;

public class WeatherInsertManager {

	public static final ThreadGroup	THREAD_GROUP = new ThreadGroup("weather-insert.thread-group");

	public static void main(String[] args) throws Throwable {
		File file = new File("system.setup");
//		System.out.println(file.delete());
		
		Console.startup();

		Lookup setup = Console.setup.cates("weather");
		
		WeatherTaskManager manager = new WeatherTaskManager(THREAD_GROUP);
		int counter = 0;

//		EnumSet<WeatherTask> types = EnumSet.of(WeatherTask.PARSE_COUNTRY, WeatherTask.PARSE_STATION, WeatherTask.PARSE_YEAR, WeatherTask.DOWNLOAD);
		EnumSet<WeatherTask> types = EnumSet.of(
				WeatherTask.INSERT
			);
		int cntWatcher = setup.lookupInt("watcher", 0);
		for (int i = 0; i < cntWatcher; ++i) {
			manager.startTaskWatcher(types);
			++counter;
		}

//		Integer val = setup.lookupInt("transform.watcher", 0);
//		int cntTransformWatcher = val.intValue();
//		for (int i = 0; i < cntTransformWatcher; ++i) {
//			manager.startTaskWatcher(EnumSet.of(WeatherTask.TRANSFORM));
//			++counter;
//		}
	
//		int cntInsertWatcher = setup.lookupInt("insert.watcher", 0);
//		for (int i = 0; i < cntInsertWatcher; ++i) {
//			manager.startTaskWatcher(EnumSet.of(WeatherTask.INSERT));
//			++counter;
//		}
	
		if (counter > 0) {
			manager.join();
		}
		THREAD_GROUP.interrupt();
		
		Console.shutdown();
	}
	
}
