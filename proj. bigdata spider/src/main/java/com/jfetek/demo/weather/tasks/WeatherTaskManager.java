package com.jfetek.demo.weather.tasks;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumSet;

import com.jfetek.common.ErrorCode;
import com.jfetek.common.Lookup;
import com.jfetek.common.data.Result;
import com.jfetek.common.time.DateTime;
import com.jfetek.common.util.ArrayUtil;
import com.jfetek.common.util.CompareUtil;
import com.jfetek.common.util.EasyCodecUtil;
import com.jfetek.common.util.TextUtil;
import com.jfetek.demo.weather.BsonUtil;
import com.jfetek.demo.weather.Console;
import com.jfetek.demo.weather.Utils;
import com.jfetek.demo.weather.data.Filter;
import com.jfetek.demo.weather.data.FtpFileInfo;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.WriteResult;

public class WeatherTaskManager extends Thread {
	
	final Lookup setup;
	final DB db;
	final DBCollection task;
	final File rootDir;
	long tsStart;
	long tsAck;
	long cntTasks;
	int cntBuzy;
	public WeatherTaskManager(ThreadGroup tg) {
		super(tg, "weather.task-manager");
		
		this.setup = Console.setup.cates("weather:spider", "weather");
		this.db = Console.cache.getDB(setup.lookup("database"));
		this.task = db.getCollection(setup.lookup("collection"));
		this.rootDir = Console.createDir(setup.lookup("root.path"));
		this.tsAck = System.currentTimeMillis();
		this.cntTasks = 0;
		this.cntBuzy = 0;

		resetErrors();
		resetExecuting();
		resetCheck();
		
		this.start();
	}
	
	public DBCollection getYearRecord(int year, boolean drop_old) {
		Lookup setup = Console.setup.cates("weather");
		DB weather = Console.mongo.getDB(setup.lookup("database"));

		DBCollection records = weather.getCollection("record.y"+year);
		if (drop_old) {
			records.drop();
		}
		
		if (records.getIndexInfo().size() <= 1) {
			BasicDBObject primary = new BasicDBObject();
			primary
				.append("station", 1)
				.append("date", 1)
				.append("time", 1);
			records.createIndex(primary, new BasicDBObject("unique", true));
			records.createIndex(new BasicDBObject("station", 1));
			records.createIndex(new BasicDBObject("date", 1));
			records.createIndex(new BasicDBObject("station", 1).append("date", 1));
		}
		
		return records;
	}
	
	@Override
	public void run() {
		tsStart = System.currentTimeMillis();
		
		doParseMainTask();
		
		long cntLoop = 0;
		while (!Thread.interrupted()) {
			try {
				Thread.sleep(60000);	// check every 1 min
				if (isDone()) break;
				if (++cntLoop % 10 == 0) {	// log every 10 min
					printStatus();
					
					resetCheck();
				}
			} catch (InterruptedException e) {
				break;
			}
		}
		
		printStatus();
	}
	
	public void printStatus() {
		long total = task.count();
		long done = task.count(new BasicDBObject("status", "done"));
//		long yet = task.count(new BasicDBObject("status", "yet"));
		System.out.println(DateTime.now()+"["+Utils.timetext(System.currentTimeMillis()-tsStart)+"] "+cntTasks+":"+(total-done)+"/"+done+"/"+total+"");
	}
	
	public void emergencyStop(Throwable t) {
		System.out.println("EMERGENCY STOP!!!! cause "+t);
		ThreadGroup tgroup = this.getThreadGroup();
		tgroup.interrupt();
	}
	
	public void resetCheck() {
	}
	
	public WeatherTaskWatcher startTaskWatcher() {
		return startTaskWatcher(null);
	}
	public WeatherTaskWatcher startTaskWatcher(EnumSet<WeatherTask> types) {
		WeatherTaskWatcher watcher = new WeatherTaskWatcher(this, types);
		ThreadGroup tgroup = this.getThreadGroup();
		new Thread(tgroup, watcher, watcher.getName()).start();
		return watcher;
	}

	public Result<ArrayList<FtpFileInfo>> doParseMainTask() {
		String root = this.setup.lookup("url");

		Result<ArrayList<FtpFileInfo>> result = null;
		try {
			URI base = new URI(root);
//			result = Utils.getFtpFileList(base, Filter.YEAR_DIR_FILTER);
			result = Utils.listFtp(base, Filter.YEAR_DIR_FILTER);

			if (result.positive()) {
				addParseYearTask(result.data);
			}
		} catch (URISyntaxException e) {
			result = Result.failure(e);
		}
		
		return result;
	}

	public static boolean isArchiveLatest(File archive, File latest) {
		return isArchiveLatest(archive, null, latest, null);
	}
	public static boolean isArchiveLatest(File archive, File latest, boolean check_md5) {
		return check_md5
				? isArchiveLatest(archive, EasyCodecUtil.md5(archive), latest, EasyCodecUtil.md5(latest))
				: isArchiveLatest(archive, null, latest, null);
	}
	public static boolean isArchiveLatest(File archive, File latest, String latest_md5) {
		return isArchiveLatest(archive, null, latest, latest_md5);
	}
	public static boolean isArchiveLatest(File archive, String archive_md5, File latest, String latest_md5) {
		boolean isArchiveNull = (null==archive);
		boolean isLatestNull = (null==latest);
		if (isArchiveNull) return false;	// archive null, no archive yet
		if (isLatestNull) return true;	// no latest, archive is latest
		
		boolean isArchiveExists = archive.exists();
		boolean isLatestExists = latest.exists();
		if (!isArchiveExists) return false;	// archive not exists, no archive yet
		if (!isLatestExists) return true;	// no latest, archive is latest
		
		// both exists... compare file content
		if (archive.equals(latest)) return true;	// same file, archive is latest
		if (archive.length() != latest.length()) return false;	// different size, archive not latest
		
		boolean noArchiveMd5 = (null==archive_md5);
		boolean noLatestMd5 = (null==latest_md5);
		if (noArchiveMd5 && noLatestMd5) return true;	// no compare md5, length the same
		if (noArchiveMd5) archive_md5 = EasyCodecUtil.md5(archive);
		if (noLatestMd5) latest_md5 = EasyCodecUtil.md5(latest);
		return CompareUtil.isEqual(archive_md5, latest_md5);	// test md5
	}

	private boolean isFileLatest(int year, File dir, FtpFileInfo info) {
		File file = new File(dir, info.name);

		if (!file.exists()) {
//			System.out.println("["+info.size+"] "+info.uri+" -> "+file.getAbsolutePath());
//			System.out.println("> yet download");
			
			return false;
		}
		else if (file.length() != info.size) {
//			System.out.println("["+info.size+"] "+info.uri+" -> "+file.getAbsolutePath());
//			System.out.println("> file modified");
			
			return false;
		}
		
		return true;
	}
	
	
	public boolean isRawFileLatest(int year, FtpFileInfo info) {
		File rawDir = new File(this.rootDir, year+"/raw/");
		return isFileLatest(year, rawDir, info);
	}
	public boolean isRawFileLatest(int year, String station, File latest) {
		File raw = getRawFile(year, station);
		return isArchiveLatest(raw, latest, true);
	}
	public boolean isRawFileLatest(File raw, File latest, String latest_md5) {
		return isArchiveLatest(raw, latest, latest_md5);
	}
	
	public boolean isCsvFileLatest(int year, FtpFileInfo info) {
		File csvDir = new File(this.rootDir, year+"/csv/");
		return isFileLatest(year, csvDir, info);
	}
	public boolean isCsvFileLatest(int year, String station, File latest) {
		File csv = getCsvFile(year, station);
		return isArchiveLatest(csv, latest, true);
	}
	public boolean isCsvFileLatest(File csv, File latest, String latest_md5) {
		return isArchiveLatest(csv, latest, latest_md5);
	}
	
	public File getRawFile(int year, String station) {
		File rawDir = new File(this.rootDir, year+"/raw/");
		String name = station + "-" + year + ".gz";
		return new File(rawDir, name);
	}
	
	public File getCsvFile(int year, String station) {
		File csvDir = new File(this.rootDir, year+"/csv/");
		String name = station + "-" + year + ".gz";
		return new File(csvDir, name);
	}
	
	public synchronized BasicDBObject nextTask() {
		BasicDBObject query = new BasicDBObject("status", TaskStatus.YET.text);
		BasicDBObjectBuilder sort = BasicDBObjectBuilder.start()
				.add("priority", 1);
//				.add("year", -1);
//				.add("uri", -1)
//				.add("file", -1);
		BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
				.push("$set")
					.append("status", TaskStatus.EXECUTING.text)
				.pop();
		BasicDBObject data = (BasicDBObject) task.findAndModify(query, sort.get(), update.get());
//		System.out.println(query+"\n-> "+data);
		
		return data;
	}

	public synchronized BasicDBObject nextTask(WeatherTask type) {
		BasicDBObject query = new BasicDBObject("status", TaskStatus.YET.text)
				.append("task", type.text);
		BasicDBObjectBuilder sort = BasicDBObjectBuilder.start();
//				.add("priority", 1)
//				.add("year", -1);
//				.add("uri", -1)
//				.add("file", -1);
		BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
				.push("$set")
					.append("status", TaskStatus.EXECUTING.text)
				.pop();
		BasicDBObject data = (BasicDBObject) task.findAndModify(query, sort.get(), update.get());
//		System.out.println(query+"\n-> "+data);
		
		return data;
	}

	public synchronized BasicDBObject nextTask(EnumSet<WeatherTask> types) {
		BasicDBObjectBuilder query = BasicDBObjectBuilder.start()
				.add("status", TaskStatus.YET.text)
				.push("task")
					.add("$in", ArrayUtil.toStringArray(types))
				.pop();
		BasicDBObjectBuilder sort = BasicDBObjectBuilder.start()
				.add("priority", 1);
//				.add("year", -1);
//				.add("uri", -1)
//				.add("file", -1);
		BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
				.push("$set")
					.append("status", TaskStatus.EXECUTING.text)
				.pop();
		BasicDBObject data = (BasicDBObject) task.findAndModify(query.get(), sort.get(), update.get());
//		System.out.println(query.get()+"\n-> "+data);
		
		return data;
	}

	public BasicDBObject buildLog(ErrorCode ec) {
		BasicDBObjectBuilder log = BasicDBObjectBuilder.start()
				.append("timestamp", System.currentTimeMillis())
				.append("ok", ec.positive())
				.push("error")
					.append("code", ec.code)
					.append("message", ec.message)
				.pop();
		return (BasicDBObject) log.get();
	}
	
	public void resetExecuting() {
		BasicDBObjectBuilder query = BasicDBObjectBuilder.start()
				.append("status", TaskStatus.EXECUTING.text);
		BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
				.push("$set")
					.append("status", TaskStatus.YET.text)
				.pop()
				.push("$inc")
					.append("errorCount", 1)
				.pop()
				.push("$push")
					.append("log", buildLog(ErrorCode.error("reset task: execution interrupted...")))
				.pop();
		WriteResult result = this.task.update(query.get(), update.get(), false, true);
		System.out.println(result);
	}
	
	public void resetErrors() {
		BasicDBObjectBuilder query = BasicDBObjectBuilder.start()
				.append("status", TaskStatus.ERROR.text);
		BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
				.push("$set")
					.append("status", TaskStatus.YET.text)
				.pop();
		WriteResult result = this.task.update(query.get(), update.get(), false, true);
		System.out.println(result);
	}
//	
//	public void log(String task_id, ErrorCode ec) {
//		BasicDBObject log = buildLog(ec);
//		BasicDBObject update = new BasicDBObject("$push", new BasicDBObject("log", log));
//		this.task.update(new BasicDBObject("_id", task_id), update, false, false);
//	}

	public void updateStatus(BasicDBObject task, ErrorCode ec) {
		String task_id = task.getString("_id");
		BasicDBObject log = buildLog(ec);
		BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
				.push("$set")
					.add("status", ec.positive()? TaskStatus.DONE.text : TaskStatus.ERROR.text)
				.pop()
				.push("$push")
					.add("log", log)
				.pop();
		if (ec.negative()) {
			update.push("$inc")
				.append("errorCount", 1)
			.pop();
		}
		this.task.update(new BasicDBObject("_id", task_id), update.get(), false, false);
	}
	
	private BasicDBObject buildBasicTask(WeatherTask task) {
		BasicDBObjectBuilder builder = BasicDBObjectBuilder.start()
				.add("task", task.text)
				.add("status", TaskStatus.YET.text)
				.add("errorCount", 0)
				.add("log", new BasicDBList());
		return (BasicDBObject) builder.get();
	}
	
	private BasicDBObject buildParseYearTask(FtpFileInfo info) {
		int year = TextUtil.intValue(info.name);
		return buildBasicTask(WeatherTask.PARSE_YEAR)
				.append("_id", WeatherTask.PARSE_YEAR.getTaskId(info.uri))
				.append("year", year)
				.append("uri", info.uri.toString())
				.append("priority", 1);
	}
	public ArrayList<BasicDBObject> addParseYearTask(ArrayList<FtpFileInfo> files) {
		BulkWriteOperation bulk = task.initializeUnorderedBulkOperation();
		ArrayList<BasicDBObject> list = new ArrayList<BasicDBObject>(files.size());
		for (int i = 0, len = files.size(); i < len; ++i) {
			FtpFileInfo info = files.get(i);
			BasicDBObject data = buildParseYearTask(info);
			if (0 == task.count(new BasicDBObject("_id", data.getString("_id")))) {
				list.add(data);
				bulk.insert(data);
			}
		}
		if (list.size() > 0) {
			BulkWriteResult result = bulk.execute();
			int cntInserted = result.getInsertedCount();
			if (cntInserted > 0) {
				System.out.println("insert "+cntInserted+" parse-year tasks.");
			}
			int cntModified = result.getModifiedCount();
			if (cntModified > 0) {
				System.out.println("update "+cntModified+" parse-year tasks.");
			}
			synchronized (this) {
				this.notifyAll();
			}
		}
		return list;
	}


//	private BasicDBObject buildDownloadTask(FtpFileInfo info) {
//		String station = info.name.substring(0, 12);
//		int year = TextUtil.intValue(info.name.substring(13, 17), -1);
//		return buildBasicTask(WeatherTask.DOWNLOAD)
//				.append("_id", WeatherTask.DOWNLOAD.getTaskId(info.uri))
//				.append("year", year)
//				.append("station", station)
//				.append("uri", info.uri.toString())
//				.append("size", info.size)
//				.append("priority", 2);
//	}
	private BasicDBObject buildDownloadTaskQuery(FtpFileInfo info) {
		return new BasicDBObject("_id", WeatherTask.DOWNLOAD.getTaskId(info.uri));
	}
	private BasicDBObjectBuilder buildDownloadTask(FtpFileInfo info) {
		String station = info.name.substring(0, 12);
		int year = TextUtil.intValue(info.name.substring(13, 17), -1);
		return BasicDBObjectBuilder.start()
				.push("$setOnInsert")
					// basic
					.add("task", WeatherTask.DOWNLOAD.text)
					.add("errorCount", 0)
//					.add("log", new BasicDBList())
					// task
					.add("year", year)
					.add("station", station)
					.add("uri", info.uri.toString())
					.add("priority", 2)
				.pop();
	}
	public ArrayList<BasicDBObject> addDownloadTask(ArrayList<FtpFileInfo> files) {
		BulkWriteOperation bulk = task.initializeUnorderedBulkOperation();
		ArrayList<BasicDBObject> list = new ArrayList<BasicDBObject>(files.size());
		ArrayList<File> prepareTransform = new ArrayList<File>(files.size());
		for (int i = 0, len = files.size(); i < len; ++i) {
			FtpFileInfo info = files.get(i);
			BasicDBObject query = buildDownloadTaskQuery(info);
			BasicDBObjectBuilder update = buildDownloadTask(info);
			String station = info.name.substring(0, 12);
			int year = TextUtil.intValue(info.name.substring(13, 17), -1);
			int size = info.size;
			boolean needUpdate = true;
			if (isRawFileLatest(year, info)) {
				DBCursor cursor = null;
				try {
					cursor = task.find(query);
					BasicDBObject t = (BasicDBObject) cursor.one();
					if (null == t || (null != t && "yet".equals(t.getString("status")))) {
						update.push("$set")
							.add("status", "done")
							.add("size", size)
						.pop()
						.push("$push")
							.add("log", BsonUtil.getBasicBson(ErrorCode.ok("latest file already donwloaded")))
						.pop();
						
						// raw-file already exists, must ensure has transform-task
						File raw = getRawFile(year, station);
						prepareTransform.add(raw);
					}
					else {
						needUpdate = false;
					}
				} finally {
					cursor.close();;
				}
			}
			else {
				// create new
				update.push("$set")
					.add("status", "yet")
					.add("size", size)
				.pop();
			}
//			System.out.println((BasicDBObject)update.get());
			if (needUpdate) {
				list.add(query);
				bulk.find(query).upsert().update(update.get());
//				System.out.println(task.update(query, update.get(), true, true));
			}
		}
		if (list.size() > 0) {
			BulkWriteResult result = bulk.execute();
			int cntInserted = result.getInsertedCount();
			if (cntInserted > 0) {
				System.out.println("insert "+cntInserted+" download tasks.");
			}
			int cntModified = result.getModifiedCount();
			if (cntModified > 0) {
				System.out.println("update "+cntModified+" download tasks.");
			}
			synchronized (this) {
				this.notifyAll();
			}
		}
		if (prepareTransform.size() > 0) {
			addTransformTask(prepareTransform);
		}
		return list;
	}

	private BasicDBObject buildTransformTaskQuery(File file) {
		String name = file.getName();
		int year = TextUtil.intValue(name.substring(13, 17));
		return new BasicDBObject("_id", WeatherTask.TRANSFORM.getTaskId(year+"/raw/"+name));
	}
	private BasicDBObjectBuilder buildTransformTask(File file) {
		String name = file.getName();
		String station = name.substring(0, 12);
		int year = TextUtil.intValue(name.substring(13, 17));
		return BasicDBObjectBuilder.start()
				.push("$setOnInsert")
					// basic
					.add("task", WeatherTask.TRANSFORM.text)
//					.add("status", "yet")
					.add("errorCount", 0)
					.add("log", new BasicDBList())
					.add("priority", 2)
					// task
					.add("year", year)
					.add("station", station)
					.add("file", year+"/raw/"+name)
//					.add("size", file.length())
				.pop();
	}
	// TODO
	// return Result<_id>
	public BasicDBObject addTransformTask(File raw, File latest) {
		BasicDBObject query = buildTransformTaskQuery(raw);
		BasicDBObjectBuilder update = buildTransformTask(raw);
		int size = (int)latest.length();
		String md5 = EasyCodecUtil.md5(latest);
		if (0 == task.count(query) || !isRawFileLatest(raw, latest, md5)) {
			String name = raw.getName();
			String station = name.substring(0, 12);
			int year = TextUtil.intValue(name.substring(13, 17));
			File csv = getCsvFile(year, station);
			if (csv.exists()) {
				update.push("$set")
					.add("status", "done")
					.add("size", size)
					.add("md5", md5)
				.pop();

				// csv-file already exists, must ensure has insert-task
				addInsertTask(csv, csv);
			}
			else {
				update.push("$set")
					.add("status", "yet")
					.add("size", size)
					.add("md5", md5)
				.pop();
			}

			WriteResult result = task.update(query, update.get(), true, true);
		}
		else {
			// no change
		}
//		System.out.println(update.get()+"\n-> "+result);
		synchronized (this) {
			this.notifyAll();
		}
		return query;
	}
	public ArrayList<BasicDBObject> addTransformTask(ArrayList<File> files) {
		BulkWriteOperation bulk = task.initializeUnorderedBulkOperation();
		ArrayList<BasicDBObject> list = new ArrayList<BasicDBObject>(files.size());
		ArrayList<File> prepareInsert = new ArrayList<File>(files.size());
		for (int i = 0, len = files.size(); i < len; ++i) {
			File file = files.get(i);
			BasicDBObject query = buildTransformTaskQuery(file);
			BasicDBObjectBuilder update = buildTransformTask(file);
			int size = (int)file.length();
			String md5 = EasyCodecUtil.md5(file);
			if (0 == task.count(query)) {
				String name = file.getName();
				String station = name.substring(0, 12);
				int year = TextUtil.intValue(name.substring(13, 17));
				File csv = getCsvFile(year, station);
				if (csv.exists()) {
					update.push("$set")
						.add("status", "done")
						.add("size", size)
						.add("md5", md5)
					.pop();

					// csv-file already exists, must ensure has insert-task
//					addInsertTask(csv, csv);
					prepareInsert.add(csv);
				}
				else {
					update.push("$set")
						.add("status", "yet")
						.add("size", size)
						.add("md5", md5)
					.pop();
				}

				bulk.find(query).upsert().update(update.get());
				list.add(query);
			}
		}
		if (list.size() > 0) {
			BulkWriteResult result = bulk.execute();
			int cntInserted = result.getInsertedCount();
			if (cntInserted > 0) {
				System.out.println("insert "+cntInserted+" transform tasks.");
			}
			int cntModified = result.getModifiedCount();
			if (cntModified > 0) {
				System.out.println("update "+cntModified+" transform tasks.");
			}
			synchronized (this) {
				this.notifyAll();
			}
		}
		if (prepareInsert.size() > 0) {
			addInsertTask(prepareInsert);
		}
		return list;
	}

//	private BasicDBObject buildInsertTask(File file) {
//		String name = file.getName();
//		String station = name.substring(0, 12);
//		int year = TextUtil.intValue(name.substring(13, 17));
//		return buildBasicTask(WeatherTask.INSERT)
//				.append("_id", WeatherTask.INSERT.getTaskId(year+"/csv/"+name))
//				.append("year", year)
//				.append("station", station)
//				.append("file", year+"/csv/"+name)
//				.append("size", file.length())
//				.append("priority", 2);
//	}
	private BasicDBObject buildInsertTaskQuery(File file) {
		String name = file.getName();
		int year = TextUtil.intValue(name.substring(13, 17));
		return new BasicDBObject("_id", WeatherTask.INSERT.getTaskId(year+"/csv/"+name));
	}
	private BasicDBObjectBuilder buildInsertTask(File file) {
		String name = file.getName();
		String station = name.substring(0, 12);
		int year = TextUtil.intValue(name.substring(13, 17));
		return BasicDBObjectBuilder.start()
				.push("$setOnInsert")
					// basic
					.add("task", WeatherTask.INSERT.text)
//					.add("status", "yet")
					.add("errorCount", 0)
					.add("log", new BasicDBList())
					.add("priority", 2)
					// task
					.add("year", year)
					.add("station", station)
					.add("file", year+"/csv/"+name)
//					.add("size", file.length())
				.pop();
	}
	public BasicDBObject addInsertTask(File csv, File latest) {
		BasicDBObject query = buildInsertTaskQuery(csv);
		BasicDBObjectBuilder update = buildInsertTask(csv);

		int size = (int)latest.length();
		String md5 = EasyCodecUtil.md5(latest);
		if (0 == task.count(query) || !isRawFileLatest(csv, latest, md5)) {
			update.push("$set")
				.add("status", "yet")
				.add("size", size)
				.add("md5", md5)
			.pop();
		}
		WriteResult result = task.update(query, update.get(), true, true);
		synchronized (this) {
			this.notifyAll();
		}
		return query;
	}
	public ArrayList<BasicDBObject> addInsertTask(ArrayList<File> files) {
		BulkWriteOperation bulk = task.initializeUnorderedBulkOperation();
		ArrayList<BasicDBObject> list = new ArrayList<BasicDBObject>(files.size());
		for (int i = 0, len = files.size(); i < len; ++i) {
			File file = files.get(i);
			BasicDBObject query = buildInsertTaskQuery(file);
			BasicDBObjectBuilder update = buildInsertTask(file);

			int size = (int)file.length();
			String md5 = EasyCodecUtil.md5(file);
			if (0 == task.count(query)) {
				update.push("$set")
					.add("status", "yet")
					.add("size", size)
					.add("md5", md5)
				.pop();
			}
			bulk.find(query).upsert().update(update.get());
			list.add(query);
		}
		if (list.size() > 0) {
			BulkWriteResult result = bulk.execute();
			int cntInserted = result.getInsertedCount();
			if (cntInserted > 0) {
				System.out.println("insert "+cntInserted+" insert tasks.");
			}
			int cntModified = result.getModifiedCount();
			if (cntModified > 0) {
				System.out.println("update "+cntModified+" insert tasks.");
			}
			synchronized (this) {
				this.notifyAll();
			}
		}
		return list;
	}
	
	public synchronized void acknowledge() {
		this.tsAck = System.currentTimeMillis();
		++cntTasks;
	}

	public synchronized void ackIdle() {
		--cntBuzy;
	}
	
	public synchronized void ackBuzy() {
		++cntBuzy;
	}
	
	private synchronized boolean isDone() {
		long quiet = System.currentTimeMillis() - this.tsAck;
		
		if (quiet > 600000) {
			System.out.println("water quiet over "+Utils.timetext(quiet)+", "+cntBuzy+" watchers buzying");
			return 0 == cntBuzy;
		}
		return false;
	}
}
