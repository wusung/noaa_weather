package com.jfetek.demo.weather.tasks;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;

import com.jfetek.common.data.Result;
import com.jfetek.demo.weather.Utils;
import com.jfetek.demo.weather.data.FtpFileInfo;
import com.jfetek.demo.weather.tasks.weather.ISHDataTransformTask;
import com.jfetek.demo.weather.tasks.weather.MassInsertTask;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteResult;
import com.mongodb.DBCollection;

public class WeatherTaskWatcher implements Runnable {
	
	private static final	AtomicLong	WATCHER_SN	= new AtomicLong();
	
	final String name;
	final WeatherTaskManager manager;
	EnumSet<WeatherTask> types;
	public WeatherTaskWatcher(WeatherTaskManager manager) {
		this(manager, null);
	}
	public WeatherTaskWatcher(WeatherTaskManager manager, EnumSet<WeatherTask> types) {
		this.name = "weather-watcher"+types+"#"+WATCHER_SN.getAndIncrement();
		this.manager = manager;
		this.types = types;
		
		System.out.println("create: "+this.name);
	}
	
	public String getName() {
		return this.name;
	}
	
	protected BasicDBObject nextTask() {
		long ts = System.currentTimeMillis();
		BasicDBObject task;
		if (null == this.types || 0 == this.types.size()) {
			task = manager.nextTask();
		}
		else if (1 == this.types.size()) {
			WeatherTask[] type = new WeatherTask[1];
			this.types.toArray(type);
			task = manager.nextTask(type[0]);
		}
		else {
			task = manager.nextTask(this.types);
		}
//		System.out.println((null==task? "no " : "")+"next task... "+(System.currentTimeMillis()-ts)+"ms");
		return task;
	}
	
	@Override
	public void run() {
		long tsIdle = 0;
		manager.ackBuzy();
		while (true) {
			try {
				BasicDBObject task = nextTask();
				while (null == task) {
					if (0 == tsIdle) {
						System.out.println(this.name+": no task... wait");
						tsIdle = System.currentTimeMillis();
						manager.ackIdle();
					}
					synchronized (manager) {
						manager.wait(60000);
					}
					task = nextTask();
				}
				if (0 != tsIdle) {
					System.out.println(this.name+": resume from idle("+Utils.timetext(System.currentTimeMillis()-tsIdle)+")");
					tsIdle = 0;
					manager.ackBuzy();
				}

				String id = task.getString("_id");
				String name = task.getString("task");
//				System.out.println("task["+id+"]...");
				Result<?> result;
				try {
					WeatherTask t = WeatherTask.of(name);
					switch (t) {
						case PARSE_COUNTRY:
							result = doParseCountryTask(task);
							break;
						case PARSE_STATION:
							result = doParseStationTask(task);
							break;
						case PARSE_YEAR:
							result = doParseYearTask(task);
							break;
						case DOWNLOAD:
							result = doDownloadTask(task);
							break;
						case TRANSFORM:
							result = doTransformTask(task);
							break;
						case INSERT:
							result = doInsertTask(task);
							break;
						default:
							System.out.println("UNKNOWN TASK: "+name+" id:"+id);
							result = Result.failure("unknown task: "+name);
					}
//				} catch (InterruptedException e) {
//					throw e;
				} catch(RuntimeException e) {
					result = Result.failure(e);
					if (String.valueOf(e.getMessage()).toLowerCase().indexOf("no space") != -1) {
						manager.emergencyStop(e);
					}
				} catch(Throwable t) {
					t.printStackTrace();
					result = Result.failure(t);
				}
				
				long ts = System.currentTimeMillis();
//				manager.log(id, result);
				manager.updateStatus(task, result);
				if (result.negative()) {
					System.out.println(result);
				}
//				System.out.println("update task... "+(System.currentTimeMillis()-ts)+"ms");
				
				manager.acknowledge();
				
			} catch (InterruptedException e) {
				break;
			}
		}
	}
	
	public Result<ArrayList<FtpFileInfo>> doParseYearTask(BasicDBObject data) {
		String dir = data.getString("uri");
		
		Result<ArrayList<FtpFileInfo>> result = null;
		try {
			URI uri = new URI(dir);
//			result = Utils.getFtpFileList(uri);
			long ts = System.currentTimeMillis();
			result = Utils.listFtp(uri);

			if (result.positive()) {
				long ts1 = System.currentTimeMillis() - ts;
				ts = System.currentTimeMillis();
				manager.addDownloadTask(result.data);
				System.out.println("parse-year["+result.data.size()+"]("+Utils.timetext(ts1)+"+"+Utils.timetext(System.currentTimeMillis()-ts)+"): "+uri+" "+(result.positive()? "OK" : "FAILURE"));
			}
			else {
				System.out.println("parse-year["+result.data.size()+"]("+Utils.timetext(System.currentTimeMillis()-ts)+"): "+uri+" "+(result.positive()? "OK" : "FAILURE"));
			}

		} catch (URISyntaxException e) {
			result = Result.failure(e);
		} catch (RuntimeException e) {
			throw e;
		}
		
		return result;
	}

	public Result<File> doDownloadTask(BasicDBObject data) throws IOException {
		int year = data.getInt("year");
		String station = data.getString("station");
		URI uri = URI.create(data.getString("uri"));

		Result<File> result = Utils.downloadFtp(uri);
		if (result.positive()) {
			File tmp = result.data;
			File raw = manager.getRawFile(year, station);
			try {
				manager.addTransformTask(raw, tmp);
			} finally {
				if (raw.exists()) {
					raw.delete();
				}
				else {
					raw.getParentFile().mkdirs();
				}
//				if (!tmp.renameTo(raw)) {
//					System.out.println("rename raw file fail: "+raw.getAbsolutePath());
//				}
				try {
					FileUtils.moveFile(tmp, raw);
				} catch(Exception e) {
					System.out.println("rename raw file fail: "+raw.getAbsolutePath());
					e.printStackTrace();
				}
				
				// remove temp-file
				tmp.delete();
				tmp = null;
			}
		}
		
		return result;
	}
	
	public Result<File> doTransformTask(BasicDBObject data) {
		int year = data.getInt("year");
		String station = data.getString("station");
		File raw = manager.getRawFile(year, station);
		if (!raw.exists()) {
			return Result.failure("raw-file not exists: "+raw.getAbsolutePath());
		}
		
		long ts = System.currentTimeMillis();
		ISHDataTransformTask transformTask = new ISHDataTransformTask(raw);
		Result<File> result = transformTask.transfer();
		System.out.println("transform["+transformTask.getTransformCount()+"]("+Utils.timetext(System.currentTimeMillis()-ts)+"): "+raw.getName()+" "+(result.positive()? "OK" : "FAILURE"));
		if (result.positive()) {
			File tmp = result.data;
			File csv = manager.getCsvFile(year, station);
			try {
				manager.addInsertTask(csv, tmp);
			} finally {
				if (csv.exists()) {
					csv.delete();
				}
				else {
					csv.getParentFile().mkdirs();
				}
//				if (!tmp.renameTo(csv)) {
//					System.out.println("rename csv file fail: ["+csv.isAbsolute()+"]"+csv.getAbsolutePath());
//				}
				try {
					FileUtils.moveFile(tmp, csv);
				} catch(Exception e) {
					System.out.println("rename csv file fail: ["+csv.isAbsolute()+"]"+csv.getAbsolutePath());
					e.printStackTrace();
				}
				
				// remove temp-file
				tmp.delete();
				tmp = null;
			}
		}
		
		return result;
	}
	
	public Result<BulkWriteResult> doInsertTask(BasicDBObject data) {
		int year = data.getInt("year");
		String station = data.getString("station");
		File csv = manager.getCsvFile(year, station);
		
		boolean drop_old = false;
		DBCollection records = manager.getYearRecord(year, drop_old);
		long ts = System.currentTimeMillis();
		BulkWriteResult result = MassInsertTask.processRecord(records, csv, drop_old);
		if (null == result) {
			System.out.println("insert("+Utils.timetext(System.currentTimeMillis()-ts)+"): already inserted, ignore "+csv.getName());
		}
		else {
			System.out.println("insert["+(result.getInsertedCount()+result.getModifiedCount())+"]("+Utils.timetext(System.currentTimeMillis()-ts)+"): "+csv.getName());
		}
		
		return Result.wrap(result);
	}
	
	public Result<ArrayList<String>> doParseCountryTask(BasicDBObject data) {
		return Result.failure();
	}
	
	public Result<ArrayList<String>> doParseStationTask(BasicDBObject data) {
		return Result.failure();
	}
}
