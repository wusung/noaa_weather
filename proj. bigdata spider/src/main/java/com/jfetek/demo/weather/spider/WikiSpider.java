package com.jfetek.demo.weather.spider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jcraft.jzlib.GZIPInputStream;
import com.jfetek.common.ErrorCode;
import com.jfetek.common.Lookup;
import com.jfetek.common.data.Pair;
import com.jfetek.common.data.Result;
import com.jfetek.common.time.Date;
import com.jfetek.common.time.DateTime;
import com.jfetek.common.time.Hour;
import com.jfetek.common.time.Month;
import com.jfetek.common.time.MonthOfYear;
import com.jfetek.common.time.Year;
import com.jfetek.common.util.ArrayUtil;
import com.jfetek.common.util.EasyCodecUtil;
import com.jfetek.common.util.TextUtil;
import com.jfetek.demo.weather.Console;
import com.jfetek.demo.weather.Utils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

public class WikiSpider {
	
	public enum WikiProject {
		WIKIPEDIA("wikipedia", ""),
		WIKIBOOKS("wikibooks", ".b"),
		WIKTIONARY("wiktionary", ".d"),
		WIKIMEDIA("wikimedia", ".m"),
		WIKIPEDIA_MOBILE("wikipedia mobile", ".mw"),
		WIKINEWS("wikinews", ".n"),
		WIKIQUOTE("wikiquote", ".q"),
		WIKISOURCE("wikisource", ".s"),
		WIKIVERSITY("wikiversity", ".v"),
		MEDIAWIKI("mediawiki", ".w"),
		WIKIVOYAGE("wikivoyage", ".voy");
		
		public final String text;
		public final String abbreviation;
		WikiProject(String text, String abb) {
			this.text = text;
			this.abbreviation = abb;
		}

		public boolean equals(String text) {
			return this.text.equals(text) || this.abbreviation.equals(text);
		}
		
		public static WikiProject of(String text) {
			for (WikiProject p : WikiProject.values()) {
				if (p.text.equals(text) || p.abbreviation.equals(text)) return p;
			}
			return null;
		}
		public static WikiProject of(int ordinal) {
			WikiProject[] values = WikiProject.values();
			if (ordinal < 0 || ordinal >= values.length) return null;
			return values[ordinal];
		}
		
		@Override
		public String toString() {
			return this.text;
		}
	}
	
	public enum Task {
		PARSE_YEAR("parse-year"),
		PARSE_MONTH("parse-month"),
		PARSE_MD5("parse-md5"),
		DOWNLOAD("download"),
		BULK_INSERT("bulk-insert");
		
		public final String text;
		Task(String text) {
			this.text = text;
		}
		
		public String getTaskId(String res) {
			return this.text + ":" +res;
		}
		
		public boolean equals(String text) {
			return this.text.equals(text);
		}
		
		public static Task of(String text) {
			for (Task t : Task.values()) {
				if (t.text.equals(text)) return t;
			}
			return null;
		}
		public static Task of(int ordinal) {
			Task[] values = Task.values();
			if (ordinal < 0 || ordinal >= values.length) return null;
			return values[ordinal];
		}
		
		@Override
		public String toString() {
			return this.text;
		}
	}
	
	public enum TaskStatus {
		YET("yet"),
		EXECUTING("executing"),
		DONE("done"),
		ERROR("error"),
		FETAL("fetal");
		
		public final String text;
		TaskStatus(String text) {
			this.text = text;
		}
		
		public boolean equals(String text) {
			return this.text.equals(text);
		}
		
		public static TaskStatus of(String text) {
			for (TaskStatus ts : TaskStatus.values()) {
				if (ts.text.equals(text)) return ts;
			}
			return null;
		}
		public static TaskStatus of(int ordinal) {
			TaskStatus[] values = TaskStatus.values();
			if (ordinal < 0 || ordinal >= values.length) return null;
			return values[ordinal];
		}
		
		@Override
		public String toString() {
			return this.text;
		}
	}
	
	public static class TaskManager extends Thread {
		final DB db;
		final DBCollection task;
		long tsStart;
		long tsAck;
		long cntTasks;
		int cntBuzy;
		public TaskManager(ThreadGroup tg) {
			super(tg, "wiki-spider.task-manager");
			
			Lookup setup = Console.setup.cate("wiki");
			
//			this.db = Console.cache.getDB("persistent");
//			this.task = db.getCollection("wiki.task");
			this.db = Console.cache.getDB( setup.lookup("task-database", "persistent") );
			this.task = db.getCollection( setup.lookup("task-collection", "wiki.task") );
			this.tsAck = System.currentTimeMillis();
			this.cntTasks = 0;
			this.cntBuzy = 0;

			resetErrors();
			resetExecuting();
			
			this.start();
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
					if (cntLoop % 1440 == 0) {	// log every 1 day
						doParseMainTask();
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
//			long yet = task.count(new BasicDBObject("status", "yet"));
			System.out.println(DateTime.now()+"["+Utils.timetext(System.currentTimeMillis()-tsStart)+"] "+cntTasks+":"+(total-done)+"/"+done+"/"+total+"");
		}

		public void resetCheck() {
		}
		
		public void emergencyStop(Throwable t) {
			System.out.println("EMERGENCY STOP!!!! cause "+t);
			THREAD_GROUP.interrupt();
		}
		
		public TaskWatcher startTaskWatcher() {
			return startTaskWatcher(null);
		}
		public TaskWatcher startTaskWatcher(EnumSet<Task> types) {
			return startTaskWatcher(types, true);
		}
		public TaskWatcher startTaskWatcher(EnumSet<Task> types, boolean desc) {
			TaskWatcher watcher = new TaskWatcher(manager, types, desc);
			new Thread(THREAD_GROUP, watcher).start();
			return watcher;
		}

//		public BulkInsertWatcher startBulkInsertWatcher() {
//			BulkInsertWatcher watcher = new BulkInsertWatcher(manager);
//			new Thread(THREAD_GROUP, watcher).start();
//			return watcher;
//		}

		public Result<ArrayList<String>> doParseMainTask() {
			String root = Console.setup.val("wiki", "url");

			ArrayList<String> list = new ArrayList<String>(20);
			Connection conn = setupConnection(root);
			try {
				Document doc = conn.get();
				Elements a = doc.select("ul").first().select("li a");
				for (Element e : a) {
					String href = e.absUrl("href");
//					System.out.println(href);
					list.add(href);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			manager.addParseYearTask(list, root);
			return Result.wrap(list);
		}
		

		public synchronized BasicDBObject nextTask() {
			return nextTask(true);
		}
		public synchronized BasicDBObject nextTask(boolean desc) {
			BasicDBObject query = new BasicDBObject("status", TaskStatus.YET.text);
			BasicDBObjectBuilder sort = BasicDBObjectBuilder.start()
					.add("priority", 1)
//					.add("url", desc? -1 : 1);
					.add("_id", desc? -1 : 1);
			BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
					.push("$set")
						.append("status", TaskStatus.EXECUTING.text)
					.pop();
			BasicDBObject data = (BasicDBObject) task.findAndModify(query, sort.get(), update.get());
			
			return data;
		}

		public synchronized BasicDBObject nextTask(Task type) {
			return nextTask(type, true);
		}
		public synchronized BasicDBObject nextTask(Task type, boolean desc) {
			BasicDBObject query = new BasicDBObject("status", TaskStatus.YET.text)
					.append("task", type.text);
			BasicDBObjectBuilder sort = BasicDBObjectBuilder.start()
					.add("priority", 1)
//					.add("url", desc? -1 : 1);
					.add("_id", desc? -1 : 1);
			BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
					.push("$set")
						.append("status", TaskStatus.EXECUTING.text)
					.pop();
			BasicDBObject data = (BasicDBObject) task.findAndModify(query, sort.get(), update.get());
			
			return data;
		}

		public synchronized BasicDBObject nextTask(EnumSet<Task> types) {
			return nextTask(types, true);
		}
		public synchronized BasicDBObject nextTask(EnumSet<Task> types, boolean desc) {
			BasicDBObjectBuilder query = BasicDBObjectBuilder.start()
					.add("status", TaskStatus.YET.text)
					.push("task")
						.add("$in", ArrayUtil.toStringArray(types))
					.pop();
			BasicDBObjectBuilder sort = BasicDBObjectBuilder.start()
					.add("priority", 1)
//					.add("url", desc? -1 : 1);
					.add("_id", desc? -1 : 1);
			BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
					.push("$set")
						.append("status", TaskStatus.EXECUTING.text)
					.pop();
			BasicDBObject data = (BasicDBObject) task.findAndModify(query.get(), sort.get(), update.get());
			
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
		
		private void resetDownloadOfErrorBulkInsert() {
			// if bulk-insert error, reset download task as error(descript reason), too
			BasicDBObjectBuilder queryBulkInsert = BasicDBObjectBuilder.start()
					.add("task", "bulk-insert")
					.add("status", "error");
			DBCursor c = this.task.find(queryBulkInsert.get(), new BasicDBObject("file", 1));
			if (c.size() > 0) {
				System.out.println("reset "+c.size()+" error of bulk-insert's download task, prevent file missing error");
				BulkWriteOperation bulk = this.task.initializeUnorderedBulkOperation();
				DBObject update = BasicDBObjectBuilder.start()
						.push("$set")
							.append("status", TaskStatus.ERROR.text)
						.pop()
						.push("$inc")
							.append("errorCount", 1)
						.pop()
						.push("$push")
							.append("log", buildLog(ErrorCode.error("reset task: bulk-insert error, may need re-download file...")))
						.pop()
					.get();
				for (DBObject o : c) {
					System.out.println("reset download-task of> "+o);
					String filename = o.get("file").toString();
					File file = new File(filename);
					String name = file.getName().substring(11, 27);
//					DateTime datetime = DateTime.valueOf(name, "yyyyMMdd-HHmmss");
					BasicDBObjectBuilder query = BasicDBObjectBuilder.start()
							.append("year", TextUtil.intValue(name.substring(0, 4)))
							.append("month", TextUtil.intValue(name.substring(4, 6)))
							.append("date", TextUtil.intValue(name.substring(6, 8)))
							.append("hour", TextUtil.intValue(name.substring(9, 11)));
					bulk.find(query.get()).update(update);
				}
				BulkWriteResult result = bulk.execute();
				System.out.println("update donwload-task> "+result);
				WriteResult rsRemove = this.task.remove(queryBulkInsert.get());
				System.out.println("remove bulk-insert-task> "+rsRemove);
			}
		}
		
		public void resetErrors() {
			resetDownloadOfErrorBulkInsert();
			
			BasicDBObjectBuilder query = BasicDBObjectBuilder.start()
					.append("status", TaskStatus.ERROR.text);
			BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
					.push("$set")
						.append("status", TaskStatus.YET.text)
					.pop();
			WriteResult result = this.task.update(query.get(), update.get(), false, true);
			System.out.println(result);
			
		}
		
		public void log(String task_id, ErrorCode ec) {
			BasicDBObject log = buildLog(ec);
			BasicDBObject update = new BasicDBObject("$push", new BasicDBObject("log", log));
			this.task.update(new BasicDBObject("_id", task_id), update, false, true);
		}

		public void updateStatus(BasicDBObject task, ErrorCode ec) {
			String task_id = task.getString("_id");
			BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
					.push("$set")
						.add("status", ec.positive()? TaskStatus.DONE.text : TaskStatus.ERROR.text)
					.pop();
			if (ec.negative()) {
				update.push("$inc")
					.append("errorCount", 1)
				.pop();
			}
			this.task.update(new BasicDBObject("_id", task_id), update.get(), false, true);
		}
		
		private BasicDBObject buildBasicTask(Task task) {
			BasicDBObjectBuilder builder = BasicDBObjectBuilder.start()
					.add("task", task.text)
					.add("status", TaskStatus.YET.text)
					.add("errorCount", 0)
					.add("log", new BasicDBList());
			return (BasicDBObject) builder.get();
		}
		
		private BasicDBObject buildParseYearTask(String url, String referer) {
			int idxSlash = url.lastIndexOf("/");
			int year = TextUtil.intValue(url.substring(1+idxSlash), -1);
			return buildBasicTask(Task.PARSE_YEAR)
					.append("_id", Task.PARSE_YEAR.getTaskId(url))
					.append("year", year)
					.append("url", url)
					.append("referer", referer)
					.append("priority", 1);
		}
		public BasicDBObject addParseYearTask(String url, String referer) {
			BasicDBObject data = buildParseYearTask(url, referer);
			this.task.insert(data);
			synchronized (this) {
				this.notifyAll();
			}
			return data;
		}
		public ArrayList<BasicDBObject> addParseYearTask(ArrayList<String> urls, String referer) {
			BulkWriteOperation bulk = task.initializeUnorderedBulkOperation();
			ArrayList<BasicDBObject> list = new ArrayList<BasicDBObject>();
			for (int i = 0, len = urls.size(); i < len; ++i) {
				String url = urls.get(i);
				BasicDBObject data = buildParseYearTask(url, referer);
				if (0 == task.count(new BasicDBObject("_id", data.getString("_id")))) {
					list.add(data);
					bulk.insert(data);
				}
			}
			if (list.size() > 0) {
				BulkWriteResult result = bulk.execute();
				synchronized (this) {
					this.notifyAll();
				}
			}
			return list;
		}

		private BasicDBObject buildParseMonthTask(String url, String referer) {
			int idxSlash = url.lastIndexOf("/");
			String tmp = url.substring(1+idxSlash);
			int year = TextUtil.intValue(tmp.substring(0, 4), -1);
			int month = TextUtil.intValue(tmp.substring(5), -1);
			return buildBasicTask(Task.PARSE_MONTH)
					.append("_id", Task.PARSE_MONTH.getTaskId(url))
					.append("year", year)
					.append("month", month)
					.append("url", url)
					.append("referer", referer)
					.append("priority", 1);
		}
		public BasicDBObject addParseMonthTask(String url, String referer) {
			BasicDBObject data = buildParseMonthTask(url, referer);
			this.task.insert(data);
			synchronized (this) {
				this.notifyAll();
			}
			return data;
		}
		public ArrayList<BasicDBObject> addParseMonthTask(ArrayList<String> urls, String referer) {
			BulkWriteOperation bulk = task.initializeUnorderedBulkOperation();
			ArrayList<BasicDBObject> list = new ArrayList<BasicDBObject>();
			for (int i = 0, len = urls.size(); i < len; ++i) {
				String url = urls.get(i);
				BasicDBObject data = buildParseMonthTask(url, referer);
				if (0 == task.count(new BasicDBObject("_id", data.getString("_id")))) {
					list.add(data);
					bulk.insert(data);
				}
			}
			if (list.size() > 0) {
				BulkWriteResult result = bulk.execute();
				synchronized (this) {
					this.notifyAll();
				}
			}
			return list;
		}

		private BasicDBObject buildParseMd5Task(String url, String referer) {
//			int idxSlash = url.lastIndexOf("/");
//			String tmp = url.substring(1+idxSlash);
//			int year = TextUtil.intValue(tmp.substring(11, 15), -1);
//			int month = TextUtil.intValue(tmp.substring(15, 17), -1);
//			int date = TextUtil.intValue(tmp.substring(17, 19), -1);
//			int hour = TextUtil.intValue(tmp.substring(20, 22), -1);
			return buildBasicTask(Task.PARSE_MD5)
					.append("_id", Task.PARSE_MD5.getTaskId(url))
//					.append("year", year)
//					.append("month", month)
//					.append("date", date)
//					.append("hour", hour)
					.append("url", url)
					.append("referer", referer)
					.append("priority", 1);
		}
		public BasicDBObject addParseMd5Task(String url, String referer) {
			BasicDBObject data = buildParseMd5Task(url, referer);
			if (0 == task.count(new BasicDBObject("_id", data.getString("_id")))) {
				this.task.insert(data);
			}
			synchronized (this) {
				this.notifyAll();
			}
			return data;
		}
		private BasicDBObject buildDownloadTask(String url, String referer) {
			int idxSlash = url.lastIndexOf("/");
			String tmp = url.substring(1+idxSlash);
			int year = TextUtil.intValue(tmp.substring(11, 15), -1);
			int month = TextUtil.intValue(tmp.substring(15, 17), -1);
			int date = TextUtil.intValue(tmp.substring(17, 19), -1);
			int hour = TextUtil.intValue(tmp.substring(20, 22), -1);
			return buildBasicTask(Task.DOWNLOAD)
					.append("_id", Task.DOWNLOAD.getTaskId(url))
					.append("year", year)
					.append("month", month)
					.append("date", date)
					.append("hour", hour)
					.append("url", url)
					.append("referer", referer)
					.append("priority", 4);
		}
		public BasicDBObject addDownloadTask(String url, String referer) {
			BasicDBObject data = buildDownloadTask(url, referer);
			this.task.insert(data);
			synchronized (this) {
				this.notifyAll();
			}
			return data;
		}
		public ArrayList<BasicDBObject> addDownloadTask(ArrayList<String> urls, String referer) {
			BulkWriteOperation bulk = task.initializeUnorderedBulkOperation();
			ArrayList<BasicDBObject> list = new ArrayList<BasicDBObject>();
			for (int i = 0, len = urls.size(); i < len; ++i) {
				String url = urls.get(i);
				BasicDBObject data = buildDownloadTask(url, referer);
				if (0 == task.count(new BasicDBObject("_id", data.getString("_id")))) {
					list.add(data);
					bulk.insert(data);
				}
			}
			if (list.size() > 0) {
				BulkWriteResult result = bulk.execute();
				synchronized (this) {
					this.notifyAll();
				}
			}
			return list;
		}

		private BasicDBObject buildBulkInsertTask(String dirname, File file) {
			String relname = dirname + file.getName();	// save the relative-filepath
			return buildBasicTask(Task.BULK_INSERT)
					.append("_id", Task.BULK_INSERT.getTaskId(relname))
//					.append("file", file.getAbsolutePath())
					.append("file", relname)
					.append("priority", 3);
		}
//		public BasicDBObject addBulkInsertTask(File file) {
		public Result<BasicDBObject> addBulkInsertTask(String dirname, File file) {
			BasicDBObject data = buildBulkInsertTask(dirname, file);
			Result<BasicDBObject> result = Result.wrap(data);
			BasicDBObject t = new BasicDBObject("_id", data.getString("_id"));
			if (0 == task.count(t)) {
				WriteResult rs = this.task.insert(data);
				synchronized (this) {
					this.notifyAll();
				}
			}
			if (0 == task.count(t)) {
				result = Result.failure("create bulk-insert task of ["+file.getName()+"] failure.");
			}
			return result;
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
					
			long echoInterval = 600000;
			if (quiet > echoInterval) {
				boolean done = 0 == cntBuzy;
				if (1 == quiet/echoInterval) {
					System.out.println("watcher quiet over "+Utils.timetext(quiet)+", "+cntBuzy+" watchers buzying");
				}
				return done;
			}
			return false;
		}
	}
	
//	public static class BulkInsertWatcher implements Runnable {
//		final DB db;
//		final TaskManager manager;
//		final File rawDir;
//		public BulkInsertWatcher(TaskManager manager) {
//			this.db = Console.mongo.getDB("wiki1");
//			this.manager = manager;
//
//			this.rawDir = Console.createDir("wiki");
//		}
//		@Override
//		public void run() {
//			long tsIdle = 0;
//			while (true) {
//				try {
//					BasicDBObject task = manager.nextTask(Task.BULK_INSERT);
//					while (null == task) {
//						if (0 == tsIdle) {
//							System.out.println("no bulk-insert task... wait");
//							tsIdle = System.currentTimeMillis();
//						}
//						synchronized (manager) {
//							manager.wait();
//						}
//						task = manager.nextTask(Task.BULK_INSERT);
//					}
//					if (0 != tsIdle) {
//						System.out.println("bulk-insert task resume from idle("+Utils.timetext(System.currentTimeMillis()-tsIdle)+")");
//						tsIdle = 0;
//					}
//
//					String id = task.getString("_id");
//					String name = task.getString("task");
//					System.out.println("bulk-insert-task["+id+"]...");
//					Result<?> ec;
//					try {
//						Result<Pair<Integer,Integer>> result = doBulkInsertTask(task);
//						ec = result;
//					} catch(RuntimeException e) {
//						e.printStackTrace();
//						ec = Result.failure(e);
//						if (String.valueOf(e.getMessage()).toLowerCase().indexOf("no space") != -1) {
//							manager.emergencyStop(e);
//						}
//					} catch(Throwable t) {
//						t.printStackTrace();
//						ec = Result.failure(t);
//					}
//					manager.log(id, ec);
//					manager.updateStatus(task, ec);
//					
//					manager.acknowledge();
//					
//				} catch (InterruptedException e) {
//					break;
//				}
//			}
//		}
//		
//		public Result<Pair<Integer,Integer>> doBulkInsertTask(BasicDBObject data) throws IOException {
//			int cntLine = 0;
//			int cntRow = 0;
//			//String id = data.getString("_id");
//			String filename = data.getString("file");
//			File file = new File(this.rawDir, filename);
//			filename = file.getName();
//			String dtstr = filename.substring(11, 26);
//			String dstr = dtstr.substring(0, 8);
//			DateTime datetime = DateTime.valueOf(dtstr, "yyyyMMdd-HHmmss");
//
//			DB mongo = Console.mongo.getDB("wiki1");
//			DBCollection records = mongo.getCollection("record.y"+datetime.date.year.value);
//			
//			// delete old first
//			System.out.println("clear date["+datetime.date.toSqlDate()+"] hour["+datetime.time.hour.value+"] pagecount first...");
//			records.remove(new BasicDBObject("date", datetime.date.toSqlDate())
//							.append("hour", datetime.time.hour.value)
//						);
//			
//			// do insert/update
//			BulkWriteOperation bulk = records.initializeUnorderedBulkOperation();
//			Result<Pair<Integer,Integer>> result = Result.wrap(Pair.of(0, 0));
//			BufferedReader in = null;
//			long ts = System.currentTimeMillis();
//			try {
//				in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
//				String line;
//				int cntBulk = 0;
//				while (null != (line = in.readLine())) {
//					++cntLine;
//					line = line.trim();
//					if (0 == line.length()) continue;
//					
//					String[] col = line.split(" ");
//					int idxDot = col[0].indexOf(".");
//					String lang = col[0];
////					WikiProject project = WikiProject.WIKIPEDIA;
//					String proj = "";	// wikipedia
//					if (idxDot >= 0) {
//						lang = col[0].substring(0, idxDot).toLowerCase();
////						project = WikiProject.of(col[0].substring(idxDot));
//						proj = col[0].substring(idxDot).toLowerCase();
//					}
//					
////					System.out.println("line#"+(cntLine)+"> "+col[1]);
//					String title = Utils.urldecode(col[1]);
////					System.out.println("line#"+(cntLine)+"> "+title);
//					long cntPage = TextUtil.longValue(col[2]);
//					double size = TextUtil.doubleValue(col[3]);
//
//					String id = col[0] + ":" + dstr + ":" + EasyCodecUtil.md5(title);
//					BasicDBObject query = new BasicDBObject("_id", id);
//					BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
//						.push("$setOnInsert")
//							.add("lang", lang)
//							.add("proj", proj)
//							.add("title", title)
//							.add("date", datetime.date.toSqlDate())
//							.add("hour", datetime.time.hour.value)
//						.pop()
//						.push("$inc")
//							.add("count", cntPage)
//							.add("size", size)
//						.pop();
//					bulk.find(query).upsert().update(update.get());
//
//					if (++cntBulk >= 10000) {
//						BulkWriteResult rsBulk = bulk.execute();
//						cntRow += rsBulk.getUpserts().size();
//						System.out.println("bulk-insert["+filename+"]: line["+cntLine+"] insert["+cntRow+"] update["+(cntLine-cntRow)+"] ... ("+Utils.timetext(System.currentTimeMillis()-ts)+")");
//
//						Thread.yield();
//						
//						bulk = records.initializeUnorderedBulkOperation();
//						cntBulk = 0;
//					}
//				}
//				if (cntBulk > 0) {
//					BulkWriteResult rsBulk = bulk.execute();
//					cntRow += rsBulk.getUpserts().size();
//					System.out.println("bulk-insert["+filename+"]: line["+cntLine+"] insert["+cntRow+"] update["+(cntLine-cntRow)+"] ... ("+Utils.timetext(System.currentTimeMillis()-ts)+")");
//				}
//				
//				result = Result.wrap(Pair.of(cntLine, cntRow));
//			} catch(IOException e) {
//				e.printStackTrace();
//				result = Result.failure(e);
//			} finally {
//				if (null != in) {
//					try {
//						in.close();
//					} catch(Exception e) {}
//					in = null;
//				}
//				
//				if (result.positive()) {
//					FileUtils.deleteQuietly(file);
//				}
//			}
//			
//			System.out.println(result);
//			return result;
//		}
//	}

	public static class TaskWatcher implements Runnable {
		final DB db;
		final DBCollection task;
		final TaskManager manager;
		final File rawDir;
		EnumSet<Task> types;
		boolean desc;
		int pageCountThreshold;
		public TaskWatcher(TaskManager manager) {
			this(manager, null);
		}
		public TaskWatcher(TaskManager manager, EnumSet<Task> types) {
			this(manager, types, true);
		}
		public TaskWatcher(TaskManager manager, EnumSet<Task> types, boolean desc) {
			Lookup setup = Console.setup.cate("wiki");
			
//			this.db = Console.cache.getDB("persistent");
//			this.task = db.getCollection("wiki.task");
			this.db = Console.cache.getDB( setup.lookup("task-database", "persistent") );
			this.task = db.getCollection( setup.lookup("task-collection", "wiki.task") );
			this.manager = manager;

			String rawPath = Console.setup.val("wiki", "root.path");
			this.rawDir = Console.createDir(rawPath);
			this.types = types;
			this.desc = desc;
			this.pageCountThreshold = TextUtil.intValue(Console.setup.val("wiki", "page-count.threshold"), 0);
		}
		
		protected BasicDBObject nextTask() {
			if (null == this.types || 0 == this.types.size()) {
				return manager.nextTask(this.desc);
			}
			else if (1 == this.types.size()) {
				Task[] type = new Task[1];
				this.types.toArray(type);
				return manager.nextTask(type[0], this.desc);
			}
			else {
				return manager.nextTask(this.types, this.desc);
			}
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
							System.out.println("no task... wait");
							tsIdle = System.currentTimeMillis();
							manager.ackIdle();
						}
						synchronized (manager) {
							manager.wait(60000);
						}
						task = nextTask();
					}
					if (0 != tsIdle) {
						System.out.println("resume from idle("+Utils.timetext(System.currentTimeMillis()-tsIdle)+")");
						tsIdle = 0;
						manager.ackBuzy();
					}

					String id = task.getString("_id");
					String name = task.getString("task");
//					System.out.println("task["+id+"]...");
					Result<?> result;
					try {
						Task t = Task.of(name);
//System.out.println("task> "+t);
						switch (t) {
							case PARSE_YEAR:
								result = doParseYearTask(task);
								break;
							case PARSE_MONTH:
								result = doParseMonthTask(task);
								break;
							case PARSE_MD5:
								result = doParseMd5Task(task);
								break;
							case DOWNLOAD:
								result = doDownloadTask(task);
								break;
							case BULK_INSERT:
								result = doBulkInsertTask(task);
								break;	
							default:
								System.out.println("UNKNOWN TASK: "+name+" id:"+id);
								result = Result.failure("unknown task: "+name);
						}
//					} catch (InterruptedException e) {
//						throw e;
					} catch(RuntimeException e) {
						e.printStackTrace();
						result = Result.failure(e);
						if (String.valueOf(e.getMessage()).toLowerCase().indexOf("no space") != -1) {
							manager.emergencyStop(e);
						}
					} catch(Throwable t) {
						t.printStackTrace();
						result = Result.failure(t);
					}
//System.out.println("result> "+result);
					manager.log(id, result);
					manager.updateStatus(task, result);
					
					manager.acknowledge();
					
				} catch (InterruptedException e) {
					break;
				}
			}
		}
		
		public Result<ArrayList<String>> doParseYearTask(BasicDBObject data) {
			// parse all year
			String id = data.getString("_id");
			String url = data.getString("url");
			String referer = data.getString("referer");
//			System.out.println("parse year: "+url);

			ArrayList<String> list = new ArrayList<String>(12);
			Connection conn = setupConnection(url, referer);
			
			Result<ArrayList<String>> result;
			try {
				Document doc = conn.get();
				
				Elements a = doc.select("ul").first().select("li a");
				for (Element e : a) {
					list.add(e.absUrl("href"));
				}
				
				manager.addParseMonthTask(list, url);
				result = Result.wrap(list);

			} catch(RuntimeException e) {
				throw e;
			} catch (Exception e1) {
				System.out.println("ERROR> "+data);
				e1.printStackTrace();
				result = Result.failure(e1);
			}
			
			return result;
		}

		public Result<ArrayList<String>> doParseMonthTask(BasicDBObject data) {
			// parse all month 
			String id = data.getString("_id");
			String url = data.getString("url");
			String referer = data.getString("referer");
//			System.out.println("parse month: "+url);

			ArrayList<String> list = new ArrayList<String>(750);
			Connection conn = setupConnection(url, referer);

			Result<ArrayList<String>> result;
			try {
				Document doc = conn.get();
				Element md5 = doc.select("a").first();
				manager.addParseMd5Task(md5.absUrl("href"), url);
				
				Elements a = doc.select("ul").first().select("li a");
				for (Element e : a) {
					list.add(e.absUrl("href"));
				}
				
				manager.addDownloadTask(list, url);
				result = Result.wrap(list);

			} catch(RuntimeException e) {
				throw e;
			} catch (Exception e1) {
				System.out.println("ERROR> "+data);
				e1.printStackTrace();
				result = Result.failure(e1);
			}
			
			return result;
		}
		
		public Result<File> doParseMd5Task(BasicDBObject data) throws IOException {
			// parse all month 
			String id = data.getString("_id");
			String url = data.getString("url");
			String referer = data.getString("referer");

			long ts = System.currentTimeMillis();
			Result<File> result = download(url, referer);
			DecimalFormat df = new DecimalFormat("0.##");
			long diff = System.currentTimeMillis() - ts;
			double speed = 1000.0*result.data.length()/1024.0/diff;
			String unit = "KB";
			if (speed > 1024.0) {
				speed /= 1024;
				unit = "MB";
			}
			System.out.println("download["+df.format(speed)+unit+"/s]("+timetext(diff)+"): "+url);
			if (result.positive()) {
				String md5_collection = Console.setup.val("wiki", "md5-collection", "wiki.md5");
				DBCollection md5 = this.db.getCollection(md5_collection);
//				DBCollection md5 = this.db.getCollection("wiki.md5");
				BulkWriteOperation bulk = md5.initializeUnorderedBulkOperation();
				BufferedReader in = null;
				try {
					in = new BufferedReader(new InputStreamReader(new FileInputStream(result.data)));
					String line;
					int counter = 0;
					while (null != (line = in.readLine())) {
						line = line.trim();
						if (0 == line.length()) continue;
						
						String hash = line.substring(0, 32);
						String filename = line.substring(34);
						int year = TextUtil.intValue(filename.substring(11, 15), -1);
						int month = TextUtil.intValue(filename.substring(15, 17), -1);
						int date = TextUtil.intValue(filename.substring(17, 19), -1);
						int hour = TextUtil.intValue(filename.substring(20, 22), -1);
						
						BasicDBObject query = new BasicDBObject("_id", filename);
						BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
								.push("$setOnInsert")
									.append("year", year)
									.append("month", month)
									.append("date", date)
									.append("hour", hour)
								.pop()
								.push("$set")
									.append("md5", hash)
								.pop();
						
						++counter;
						bulk.find(query).upsert().update(update.get());
					}

					if (counter > 0) {
						bulk.execute();
					}
					
				} catch(IOException e) {
					result = Result.failure(e);
				} finally {
					if (null != in) {
						try {
							in.close();
						} catch(Exception e) {}
						in = null;
					}
				}
			}
			
			return result;
		
		}

		public Result<File> doDownloadTask(BasicDBObject data) throws IOException {
			// parse all month 
			String id = data.getString("_id");
			String url = data.getString("url");
			String referer = data.getString("referer");
			
			String name = url.substring(url.lastIndexOf('/')+1);
			DateTime datetime = DateTime.valueOf(name.substring(11, 26), "yyyyMMdd-HHmmss");
			Date date = datetime.date;
			String dirname = date.year.toString() + "/" + date.month.toText() + "/";
			File dir = new File(this.rawDir, dirname);
			dir.mkdirs();
			File file = new File(dir, name);

			boolean mustDownload = true;
			if (file.exists()) {
				// check md5
				String md5 = EasyCodecUtil.md5(file);
				String md5_collection = Console.setup.val("wiki", "md5-collection", "wiki.md5");
				DBCollection hashtable = this.db.getCollection(md5_collection);
//				DBCollection hashtable = this.db.getCollection("wiki.md5");
				long count = hashtable.count(BasicDBObjectBuilder.start().add("_id", name).add("md5", md5).get());
				if (1 == count) {
					mustDownload = false;
				}
				else {
					FileUtils.deleteQuietly(file);
				}
			}

			Result<File> result;
			if (mustDownload) {
				long ts = System.currentTimeMillis();
				result = download(url, referer);
				if (result.positive()) {
					DecimalFormat df = new DecimalFormat("0.##");
					long diff = System.currentTimeMillis() - ts;
					double speed = 1000.0*result.data.length()/1024.0/diff;
					String unit = "KB";
					if (speed > 1024.0) {
						speed /= 1024;
						unit = "MB";
					}
					System.out.println("download["+df.format(speed)+unit+"/s]("+timetext(diff)+"): "+url);
					// TODO
					// check file md5
					String md5 = EasyCodecUtil.md5(result.data);
					String md5_collection = Console.setup.val("wiki", "md5-collection", "wiki.md5");
					DBCollection hashtable = this.db.getCollection(md5_collection);
//					DBCollection hashtable = this.db.getCollection("wiki.md5");
					long count = hashtable.count(BasicDBObjectBuilder.start().add("_id", name).add("md5", md5).get());
					data.append("md5", md5)
						.append("checksum", (count>0));
					
//					if (result.data.renameTo(file)) {
//						manager.addBulkInsertTask(dirname, file);
//					}
//					else {
//						result = Result.failure("can not rename file: "+name);
//					}
					try {
						FileUtils.moveFile(result.data, file);
						Result<BasicDBObject> rsInsert = manager.addBulkInsertTask(dirname, file);
						if (rsInsert.negative()) {
							System.out.println("ERROR!! insert new bulk-insert task fail... "+rsInsert);
						}
					} catch(Exception e) {
						e.printStackTrace();
						result = Result.failure("can not rename file: "+name);
					} finally {
						result.data.delete();
					}
				}
				else {
					System.out.println("download fail("+timetext(System.currentTimeMillis()-ts)+"): "+url);
				}
			}
			else {
				result = Result.wrap(file, "latest file already donwloaded");
				System.out.println("latest file already exists: "+url);
				Result<BasicDBObject> rsInsert = manager.addBulkInsertTask(dirname, file);
				if (rsInsert.negative()) {
					System.out.println("ERROR!! insert new bulk-insert task fail... "+rsInsert);
				}
			}
			
//			manager.updateStatus(data, result);
		
			return result;
		}
//
//		public Result<Pair<Integer,Integer>> doBulkInsertTask(BasicDBObject data) throws IOException {
//			int cntLine = 0;
//			int cntRow = 0;
//			//String id = data.getString("_id");
//			String filename = data.getString("file");
//			File file = new File(this.rawDir, filename);
//			filename = file.getName();
//			String dtstr = filename.substring(11, 26);
//			String dstr = dtstr.substring(0, 11);	// yyyyMMdd-HH
//			DateTime datetime = DateTime.valueOf(dtstr, "yyyyMMdd-HHmmss");
//
//			DB mongo = Console.mongo.getDB("wiki1");
//			DBCollection records = mongo.getCollection("record.y"+datetime.date.year.value);
//			
//			// delete old first
//			System.out.println("clear date["+datetime.date.toSqlDate()+"] hour["+datetime.time.hour.value+"] pagecount first...");
//			records.remove(new BasicDBObject("date", datetime.date.toSqlDate())
//							.append("hour", datetime.time.hour.value)
//						);
//			
//			// do insert/update
//			BulkWriteOperation bulk = records.initializeUnorderedBulkOperation();
//			Result<Pair<Integer,Integer>> result = Result.failure("init.");
//			BufferedReader in = null;
//			long ts = System.currentTimeMillis();
//			long step = 60000; // 1min
//			long tsNext = step;
//			try {
//				in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
//				String line;
//				int cntBulk = 0;
//				while (null != (line = in.readLine())) {
//					++cntLine;
//					line = line.trim();
//					if (0 == line.length()) continue;
//					
//					String[] col = line.split(" ");
//					int idxDot = col[0].indexOf(".");
//					String lang = col[0];
////					WikiProject project = WikiProject.WIKIPEDIA;
//					String proj = "";	// wikipedia
//					if (idxDot >= 0) {
//						lang = col[0].substring(0, idxDot).toLowerCase();
////						project = WikiProject.of(col[0].substring(idxDot));
//						proj = col[0].substring(idxDot).toLowerCase();
//					}
//					
////					System.out.println("line#"+(cntLine)+"> "+col[1]);
//					String title = Utils.urldecode(col[1]);
//					String key = title.trim().toLowerCase();
////					System.out.println("line#"+(cntLine)+"> "+title);
//					long cntPage = TextUtil.longValue(col[2]);
//					double size = TextUtil.doubleValue(col[3]);
//
//					String id = col[0] + ":" + dstr + ":" + EasyCodecUtil.md5(title);
//					BasicDBObject query = new BasicDBObject("_id", id);
//					BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
//						.push("$setOnInsert")
//							.add("lang", lang)
//							.add("proj", proj)
//							.add("key", key)
//							.add("title", title)
//							.add("date", datetime.date.toSqlDate())
//							.add("hour", datetime.time.hour.value)
//						.pop()
//						.push("$inc")
//							.add("count", cntPage)
//							.add("size", size)
//						.pop();
//					bulk.find(query).upsert().update(update.get());
//
//					if (++cntBulk >= 10000) {
//						BulkWriteResult rsBulk = bulk.execute();
//						cntRow += rsBulk.getUpserts().size();
//
//						Thread.yield();
//						
//						bulk = records.initializeUnorderedBulkOperation();
//						cntBulk = 0;
//					}
//					if (System.currentTimeMillis() - ts > tsNext) {
//						System.out.println("bulk-insert["+filename+"]: line["+cntLine+"] insert["+cntRow+"] update["+(cntLine-cntRow)+"] ... ("+Utils.timetext(System.currentTimeMillis()-ts)+")");
//						tsNext += step;
//					}
//				}
//				if (cntBulk > 0) {
//					BulkWriteResult rsBulk = bulk.execute();
//					cntRow += rsBulk.getUpserts().size();
//					System.out.println("bulk-insert["+filename+"]: line["+cntLine+"] insert["+cntRow+"] update["+(cntLine-cntRow)+"] ... ("+Utils.timetext(System.currentTimeMillis()-ts)+")");
//				}
//				
//				result = Result.wrap(Pair.of(cntLine, cntRow));
//			} catch(IOException e) {
//				e.printStackTrace();
//				result = Result.failure(e);
//			} finally {
//				if (null != in) {
//					try {
//						in.close();
//					} catch(Exception e) {}
//					in = null;
//				}
//				
//				if (result.positive()) {
//					FileUtils.deleteQuietly(file);	// delete raw file for save disk-space
//				}
//			}
//			
//			System.out.println(result);
//			return result;
//		}
//	}

		public Result<Pair<Integer,Integer>> doBulkInsertTask2(BasicDBObject data) throws IOException {
			int cntLine = 0;
			int cntRow = 0;
			//String id = data.getString("_id");
			String filename = data.getString("file");
			File file = new File(this.rawDir, filename);
			filename = file.getName();
			String dtstr = filename.substring(11, 26);
			String dstr = dtstr.substring(0, 11);	// yyyyMMdd-HH
			DateTime datetime = DateTime.valueOf(dtstr, "yyyyMMdd-HHmmss");

//			DB mongo = Console.mongo.getDB("wiki1");
			String record_db = Console.setup.val("wiki", "record-database", "wiki1");
			DB mongo = Console.mongo.getDB(record_db);
			DBCollection records = mongo.getCollection("record.of"+datetime.toText("yyyyMMddHH"));
			
			// delete old first
			if (records.count() > 0) {
				System.out.println("drop date["+datetime.date.toSqlDate()+"] hour["+datetime.time.hour.value+"] pagecount first...");
				records.drop();
			}
			
			// do insert/update
			BulkWriteOperation bulk = records.initializeUnorderedBulkOperation();
			Result<Pair<Integer,Integer>> result = Result.failure("init.");
			BufferedReader in = null;
			long ts = System.currentTimeMillis();
			long step = 60000; // 1min
			long tsNext = step;
			try {
				GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
				if (0 == gzip.available()) {
					result = Result.wrap(Pair.of(0, 0));
				}
				else {
					in = new BufferedReader(new InputStreamReader(gzip));
					String line;
					int cntBulk = 0;
					while (null != (line = in.readLine())) {
						++cntLine;
						line = line.trim();
						if (0 == line.length()) continue;
						
						String[] col = line.split(" ");
						int idxDot = col[0].indexOf(".");
						String lang = col[0];
						String proj = "";	// wikipedia
						if (idxDot >= 0) {
							lang = col[0].substring(0, idxDot).toLowerCase();
							proj = col[0].substring(idxDot).toLowerCase();
						}
						
						String title = Utils.urldecode(col[1]);
						String key = title.trim().toLowerCase();
						long cntPage = TextUtil.longValue(col[2]);
						double size = TextUtil.doubleValue(col[3]);

						String id = col[0] + ":" + dstr + ":" + EasyCodecUtil.md5(title);
						BasicDBObject query = new BasicDBObject("_id", id);
						BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
							.push("$setOnInsert")
								.add("lang", lang)
								.add("proj", proj)
								.add("key", key)
								.add("title", title)
								.add("date", datetime.toSqlTimestamp())
							.pop()
							.push("$inc")
								.add("count", cntPage)
								.add("size", size)
							.pop();
						bulk.find(query).upsert().update(update.get());

						if (++cntBulk >= 10000) {
							BulkWriteResult rsBulk = bulk.execute();
							cntRow += rsBulk.getUpserts().size();

							Thread.sleep(10);
							
							bulk = records.initializeUnorderedBulkOperation();
							cntBulk = 0;
						}
						if (System.currentTimeMillis() - ts > tsNext) {
							System.out.println("bulk-insert["+filename+"]: line["+cntLine+"] insert["+cntRow+"] update["+(cntLine-cntRow)+"] ... ("+Utils.timetext(System.currentTimeMillis()-ts)+")");
							tsNext += step;
						}
					}
					if (cntBulk > 0) {
						BulkWriteResult rsBulk = bulk.execute();
						cntRow += rsBulk.getUpserts().size();
						System.out.println("bulk-insert["+filename+"]: line["+cntLine+"] insert["+cntRow+"] update["+(cntLine-cntRow)+"] ... ("+Utils.timetext(System.currentTimeMillis()-ts)+")");
					}

					records.createIndex(new BasicDBObject("key", 1));
					System.out.println("bulk-insert["+filename+"]: create indeces ... ("+Utils.timetext(System.currentTimeMillis()-ts)+")");
					
					result = Result.wrap(Pair.of(cntLine, cntRow));
				}
			} catch(IOException e) {
				e.printStackTrace();
				result = Result.failure(e);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				if (null != in) {
					try {
						in.close();
					} catch(Exception e) {}
					in = null;
				}
				
				if (result.positive()) {
					FileUtils.deleteQuietly(file);	// delete raw file for save disk-space
				}
			}
			
			System.out.println(result);
			return result;
		}

		public Result<Pair<Integer,Integer>> doBulkInsertTask3(BasicDBObject data) throws IOException {
			int cntLine = 0;
			int cntRow = 0;
			//String id = data.getString("_id");
			String filename = data.getString("file");
			File file = new File(this.rawDir, filename);
			filename = file.getName();
			String dtstr = filename.substring(11, 26);
			String dstr = dtstr.substring(0, 11);	// yyyyMMdd-HH
			DateTime datetime = DateTime.valueOf(dtstr, "yyyyMMdd-HHmmss");

//			DB mongo = Console.mongo.getDB("wiki1");
			String record_db = Console.setup.val("wiki", "record-database", "wiki1");
			DB mongo = Console.mongo.getDB(record_db);
			DBCollection records = mongo.getCollection("record.of"+datetime.toText("yyyyMM"));


			long ts = System.currentTimeMillis();
			// if is current year-month... index...
			// if not current year-month... not index...
			// if not current year-month and first day of month... index after insert
			records.createIndex(new BasicDBObject("date", 1));
			if (Date.today().getMonthOfYear().equals( datetime.date.getMonthOfYear() )) {
				records.createIndex(new BasicDBObject("key", 1));
//				records.createIndex(new BasicDBObject("date", 1));
//				records.createIndex(new BasicDBObject("date", 1).append("key", 1));
				System.out.println("bulk-insert["+filename+"]: current yaer-month must create indeces ... ("+Utils.timetext(System.currentTimeMillis()-ts)+")");
			}
			
			
			// delete old first
			BasicDBObject queryDate = new BasicDBObject("date", datetime.toSqlTimestamp());
			DBCursor c = records.find(queryDate);
			if (c.size() > 0) {
//				System.out.println("drop date["+datetime.date.toSqlDate()+"] hour["+datetime.time.hour.value+"] pagecount first...");
//				records.drop();
				System.out.println("remove date["+datetime.date.toSqlDate()+"] hour["+datetime.time.hour.value+"] pagecount ["+c.size()+"]... ("+Utils.timetext(System.currentTimeMillis()-ts)+")");
				records.remove(queryDate);
			}
			
			// do insert/update
			BulkWriteOperation bulk = records.initializeUnorderedBulkOperation();
			Result<Pair<Integer,Integer>> result = Result.failure("init.");
			BufferedReader in = null;
			long step = 60000; // 1min
			long tsNext = step;
			try {
				GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
				if (0 == gzip.available()) {
					result = Result.wrap(Pair.of(0, 0));
				}
				else {
					in = new BufferedReader(new InputStreamReader(gzip));
					String line;
					int cntBulk = 0;
					DecimalFormat df = new DecimalFormat("0.0");
					while (null != (line = in.readLine())) {
						++cntLine;
						line = line.trim();
						if (0 == line.length()) continue;
						
						String[] col = line.split(" ");
						int idxDot = col[0].indexOf(".");
						String lang = col[0];
						String proj = "";	// wikipedia
						if (idxDot >= 0) {
							lang = col[0].substring(0, idxDot).toLowerCase();
							proj = col[0].substring(idxDot).toLowerCase();
						}

						long cntPage = TextUtil.longValue(col[2]);
						// 20150304: add page count threshold, only larger then theshold insert to database
						if (cntPage <= pageCountThreshold) {
							continue;
						}
						String title = Utils.urldecode(col[1]);
						String key = title.trim().toLowerCase();
						double size = TextUtil.doubleValue(col[3]);

						BasicDBObjectBuilder ins = BasicDBObjectBuilder.start()
							.add("lang", lang)
							.add("proj", proj)
							.add("key", key)
							.add("title", title)
							
							.add("date", datetime.toSqlTimestamp())
							
							.add("count", cntPage)
							.add("size", size);
//						bulk.find(query).upsert().update(update.get());
						bulk.insert(ins.get());

						if (++cntBulk >= 10000) {
							BulkWriteResult rsBulk = bulk.execute();
							//cntRow += rsBulk.getUpserts().size();
							cntRow += rsBulk.getInsertedCount();

							Thread.sleep(10);
							
							bulk = records.initializeUnorderedBulkOperation();
							cntBulk = 0;
						}
						if (System.currentTimeMillis() - ts > tsNext) {
							System.out.println("bulk-insert["+filename+"]: line["+cntLine+"] insert["+cntRow+"]("+df.format(100.0*cntRow/cntLine)+"%) ignore["+(cntLine-cntRow)+"] ... ("+Utils.timetext(System.currentTimeMillis()-ts)+")");
							tsNext += step;
						}
					}
					if (cntBulk > 0) {
						BulkWriteResult rsBulk = bulk.execute();
//						cntRow += rsBulk.getUpserts().size();
						cntRow += rsBulk.getInsertedCount();
						System.out.println("bulk-insert["+filename+"]: line["+cntLine+"] insert["+cntRow+"]("+df.format(100.0*cntRow/cntLine)+"%) ignore["+(cntLine-cntRow)+"] ... ("+Utils.timetext(System.currentTimeMillis()-ts)+")");
					}

					result = Result.wrap(Pair.of(cntLine, cntRow));
				}
			} catch(IOException e) {
				e.printStackTrace();
				result = Result.failure(e);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				if (null != in) {
					try {
						in.close();
					} catch(Exception e) {}
					in = null;
				}
				
				if (result.positive()) {
					FileUtils.deleteQuietly(file);	// delete raw file for save disk-space
				}
			}
			
//			System.out.println(result);
			System.out.println("bulk-insert["+filename+"]: "+(1000*cntRow/(System.currentTimeMillis()-ts))+" line/sec");
			

			// if is current year-month... index...
			// if not current year-month... not index...
			// if not current year-month and 
			//		asc: first day zero hour of month... index after insert
			//		desc: last day 23 hour of month... index after insert
			if ((this.desc && datetime.date.day == 1 && Hour.first() == datetime.time.hour) ||
				(!this.desc && datetime.date.getMonthOfYear().lastDay().equals(datetime.date) && Hour.last() == datetime.time.hour)
				) {
				records.createIndex(new BasicDBObject("key", 1));
//				records.createIndex(new BasicDBObject("date", 1));
//				records.createIndex(new BasicDBObject("date", 1).append("key", 1));
				System.out.println("bulk-insert["+filename+"]: ["+(this.desc?"DESC":"ASC")+"] last task of month must create indeces ... ("+Utils.timetext(System.currentTimeMillis()-ts)+")");
			}
			
			return result;
		}
		

		public Result<Pair<Integer,Integer>> doBulkInsertTask(BasicDBObject data) throws IOException {
			int cntLine = 0;
			int cntRow = 0;
			//String id = data.getString("_id");
			String filename = data.getString("file");
			File file = new File(this.rawDir, filename);
			filename = file.getName();
			String dtstr = filename.substring(11, 26);
//			String dstr = dtstr.substring(0, 11);	// yyyyMMdd-HH
//			int day = datetime.date.day;
//			int hour = datetime.time.hour.value;
			int year = TextUtil.intValue( dtstr.substring(0, 4) );
			int month = TextUtil.intValue( dtstr.substring(4, 6) );
			String moy = dtstr.substring(0, 6);
			String date = dtstr.substring(0, 4) + "-" + dtstr.substring(4, 6) + "-" + dtstr.substring(6, 8);
			String day = dtstr.substring(6, 8);
			String hour = dtstr.substring(9, 11);

			MonthOfYear monthOfYear = new MonthOfYear(new Year(year), Month.get(month-1));

//			DB mongo = Console.mongo.getDB("wiki1");
			String record_db = Console.setup.val("wiki", "record-database", "wiki1");
			DB mongo = Console.mongo.getDB(record_db);
			DBCollection records = mongo.getCollection("record.of"+moy);


			long ts = System.currentTimeMillis();
			// if is current year-month... index...
			// if not current year-month... not index...
			// if not current year-month and first day of month... index after insert
//			records.createIndex(new BasicDBObject("day", 1));
//			long tsoff = System.currentTimeMillis() - ts;
//			if (tsoff > 100) {
//				System.out.println("create index: {day: 1} ..."+tsoff+"ms");
//				ts = System.currentTimeMillis();
//			}
//			records.createIndex(BasicDBObjectBuilder.start().add("day", 1).add("hour", 1).get());
//			long tsoff = System.currentTimeMillis() - ts;
//			if (tsoff > 100) {
//				System.out.println("create index: {day: 1, hour: 1} ..."+tsoff+"ms");
//				ts = System.currentTimeMillis();
//			}
			records.createIndex(BasicDBObjectBuilder.start().add("dayhour", 1).get());
			long tsoff = System.currentTimeMillis() - ts;
			if (tsoff > 100) {
				System.out.println("create index: {dayhour: 1} ..."+tsoff+"ms");
				ts = System.currentTimeMillis();
			}
			
			
			// delete old first
			DBObject queryDateHour = BasicDBObjectBuilder.start()
//						.add("day", day)
//						.add("hour", hour)
						.add("dayhour", day+" "+hour)
					.get();
			DBCursor c = records.find(queryDateHour);
			if (c.size() > 0) {
//				System.out.println("drop date["+datetime.date.toSqlDate()+"] hour["+datetime.time.hour.value+"] pagecount first...");
//				records.drop();
				records.remove(queryDateHour);
				System.out.println("remove date["+date+"] hour["+hour+"] pagecount ["+c.size()+"]... ("+Utils.timetext(System.currentTimeMillis()-ts)+")");
			}
			
			// do insert/update
			BulkWriteOperation bulk = records.initializeUnorderedBulkOperation();
			Result<Pair<Integer,Integer>> result = Result.failure("init.");
			BufferedReader in = null;
			long step = 60000; // 1min
			long tsNext = step;
			try {
				GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
				if (0 == gzip.available()) {
					result = Result.wrap(Pair.of(0, 0));
				}
				else {
					in = new BufferedReader(new InputStreamReader(gzip));
					String line;
					int cntBulk = 0;
					DecimalFormat df = new DecimalFormat("0.0");
					while (null != (line = in.readLine())) {
						++cntLine;
//						line = line.trim();
						if (0 == line.length()) continue;
						
						String[] col = line.split(" ");
						if (col.length != 4) {
							System.out.println("uknown format line: "+line);
							continue;
						}

						long cntPage = TextUtil.longValue(col[2]);
						// 20150304: add page count threshold, only larger then theshold insert to database
						if (cntPage <= pageCountThreshold) {
							continue;
						}
						
						int idxDot = col[0].indexOf(".");
						String lang = col[0];
						String proj = "";	// wikipedia
						if (idxDot >= 0) {
							lang = col[0].substring(0, idxDot).toLowerCase();
							proj = col[0].substring(idxDot).toLowerCase();
						}
						String title = Utils.urldecode(col[1]);
						String key = title.trim().toLowerCase();
						double size = TextUtil.doubleValue(col[3]);

						BasicDBObjectBuilder ins = BasicDBObjectBuilder.start()
							.add("lang", lang)
							.add("proj", proj)
							.add("key", key)
							.add("title", title)
							
////							.add("date", datetime.toSqlTimestamp())
//							.add("year", datetime.date.year.value)
//							.add("month", (1+datetime.date.month.ordinal()))
//							.add("day", day)
//							.add("hour", hour)
							.add("dayhour", day+" "+hour)
							
							.add("count", cntPage)
							.add("size", size);
//						bulk.find(query).upsert().update(update.get());
						bulk.insert(ins.get());

						if (++cntBulk >= 10000) {
							BulkWriteResult rsBulk = bulk.execute();
							//cntRow += rsBulk.getUpserts().size();
							cntRow += rsBulk.getInsertedCount();

							Thread.sleep(10);
							
							bulk = records.initializeUnorderedBulkOperation();
							cntBulk = 0;
						}
						if (System.currentTimeMillis() - ts > tsNext) {
							System.out.println("bulk-insert["+filename+"]: line["+cntLine+"] insert["+cntRow+"]("+df.format(100.0*cntRow/cntLine)+"%) ignore["+(cntLine-cntRow)+"] ... ("+Utils.timetext(System.currentTimeMillis()-ts)+")");
							tsNext += step;
						}
					}
					if (cntBulk > 0) {
						BulkWriteResult rsBulk = bulk.execute();
//						cntRow += rsBulk.getUpserts().size();
						cntRow += rsBulk.getInsertedCount();
						System.out.println("bulk-insert["+filename+"]: line["+cntLine+"] insert["+cntRow+"]("+df.format(100.0*cntRow/cntLine)+"%) ignore["+(cntLine-cntRow)+"] ... ("+Utils.timetext(System.currentTimeMillis()-ts)+")");
					}

					result = Result.wrap(Pair.of(cntLine, cntRow));
				}
			} catch(IOException e) {
				e.printStackTrace();
				result = Result.failure(e);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				if (null != in) {
					try {
						in.close();
					} catch(Exception e) {}
					in = null;
				}
				
				if (result.positive()) {
					FileUtils.deleteQuietly(file);	// delete raw file for save disk-space
				}
			}
			
//			System.out.println(result);
			System.out.println("bulk-insert["+filename+"]: "+(1000*cntRow/(System.currentTimeMillis()-ts))+" line/sec");
			

			// if is current year-month... index...
			// if not current year-month... not index...
			// if not current year-month and 
			//		asc: first day zero hour of month... index after insert
			//		desc: last day 23 hour of month... index after insert
			if ((this.desc && TextUtil.intValue(day) == 1 && Hour.first().value == TextUtil.intValue(hour)) ||
				(!this.desc && monthOfYear.lastDay().toText().equals(date) && Hour.last().value == TextUtil.intValue(hour))
				) {
				
				System.out.println("bulk-insert["+filename+"]: ["+(this.desc?"DESC":"ASC")+"] last task of month must create indeces ...");

//				ts = System.currentTimeMillis();
//				records.createIndex(new BasicDBObject("key", 1));
//				tsoff = System.currentTimeMillis() - ts;
//				System.out.println("create index: {key: 1} ...("+Utils.timetext(tsoff)+")");
//				
//				ts = System.currentTimeMillis();
//				records.createIndex(BasicDBObjectBuilder.start().add("key", 1).add("day", 1).get());
//				tsoff = System.currentTimeMillis() - ts;
//				System.out.println("create index: {key: 1, day: 1} ...("+Utils.timetext(tsoff)+")");
				
				ts = System.currentTimeMillis();
				records.createIndex(BasicDBObjectBuilder.start().add("key", 1).add("dayhour", 1).get());
				tsoff = System.currentTimeMillis() - ts;
				System.out.println("create index: {key: 1, dayhour: 1} ...("+Utils.timetext(tsoff)+")");
				
			}
			
			return result;
		}
	}


	public static String timetext(long ms) {
		int sec = (int) (ms / 1000);
		int min = sec / 60;
		int hr = min / 60;
		min %= 60;
		sec %= 60;
		
		StringBuilder tmp = new StringBuilder(20);
		if (hr > 0) {
			tmp.append(hr).append("Hours");
			if (sec > 0) {
				tmp.append(' ').append(min).append("Miunuts ")
					.append(sec).append("Seconds");
			}
			else if (min > 0) {
				tmp.append(' ').append(min).append("Miunuts");
			}
		}
		else if (min > 0) {
			tmp.append(min).append("Miunuts");
			if (sec > 0) tmp.append(' ').append(sec).append("Seconds");
		}
		else {
			tmp.append(sec).append("Seconds");
		}
		return tmp.toString();
	}
	
	public static ThreadGroup THREAD_GROUP = null;
	public static TaskManager manager = null;
	

	public static void main(String[] args) throws Throwable {
//		String s = "/com/jfetek/demo/weather/system.setup";
//		URL url = Console.class.getResource(s);
//		System.out.println("["+url+"] "+s);
//
//		s = "/resources/com/jfetek/demo/weather/system.setup";
//		url = Console.class.getResource(s);
//		System.out.println("["+url+"] "+s);
//
//		s = "/resources/system.setup";
//		url = Console.class.getResource(s);
//		System.out.println("["+url+"] "+s);
//
//		s = "/system.setup";
//		url = Console.class.getResource(s);
//		System.out.println("["+url+"] "+s);
//
//		s = "com/jfetek/demo/weather/system.setup";
//		url = Console.class.getResource(s);
//		System.out.println("["+url+"] "+s);
//
//		s = "resources/com/jfetek/demo/weather/system.setup";
//		url = Console.class.getResource(s);
//		System.out.println("["+url+"] "+s);
//
//		s = "resources/system.setup";
//		url = Console.class.getResource(s);
//		System.out.println("["+url+"] "+s);
//
//		s = "system.setup";
//		url = Console.class.getResource(s);
//		System.out.println("["+url+"] "+s);

		Console.startup();

		THREAD_GROUP = new ThreadGroup("wiki-spider.thread-group");
		manager = new TaskManager(THREAD_GROUP);

		Lookup setup = Console.setup.cate("wiki");
		int counter = 0;
		
//		EnumSet<Task> types = EnumSet.of(Task.PARSE_YEAR, Task.PARSE_MONTH, Task.PARSE_MD5);
		EnumSet<Task> types = EnumSet.allOf(Task.class);
		int cntWatcher = setup.lookupInt("watcher", 0);
		for (int i = 0; i < cntWatcher; ++i) {
//			manager.startTaskWatcher(types, 0==(i%2)?true:false);
			manager.startTaskWatcher(types, false);
//			manager.startTaskWatcher(types);
			++counter;
		}
		
//		manager.startBulkInsertWatcher();
//		manager.startBulkInsertWatcher();

		if (counter > 0) {
			manager.join();
		}
		System.out.println("all watchers idle, no task to go... exit");
		THREAD_GROUP.interrupt();
//		THREAD_GROUP.destroy();
		
		Console.shutdown();
	}
	
	public static Connection setupConnection(String url) {
		return setupConnection(url, null);
	}
	public static Connection setupConnection(String url, String referer) {
//		System.out.println("analysising... "+url);
		Connection conn = Jsoup.connect(url);
		if (TextUtil.hasValue(referer)) conn.referrer(referer);
		Properties prop = Console.setup.cate("http-client").toProperties();
		Iterator<Entry<Object,Object>> it = prop.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Object,Object> e = it.next();
			conn.header(e.getKey().toString(), e.getValue().toString());
		}
		return conn
				.ignoreContentType(true)
				.timeout(5000);
	}
	
	public static Result<File> download(String url, String referer) throws IOException {
		Result<File> result;
		File tmp = Console.createTempFile(".wiki.download");
		FileOutputStream out = null;
		ReadableByteChannel ch = null;
		HttpURLConnection conn = null;
		try {
//			ch = Channels.newChannel(new URL(url).openStream());
			URL u = new URL(url);
//			String name = u.getFile();
			conn = (HttpURLConnection) u.openConnection();
			conn.setInstanceFollowRedirects( true );
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(60000);
			conn.setUseCaches(false);
			conn.setRequestProperty("Referer", referer);
			conn.setRequestProperty("User-Agent", Console.setup.val("http-client", "User-Agent"));
//			conn.setRequestProperty("Accept-Encoding", "gzip");
			ch = Channels.newChannel(conn.getInputStream());
			out = new FileOutputStream(tmp);
			out.getChannel().transferFrom(ch, 0, Long.MAX_VALUE);
			out.flush();
			
			result = Result.wrap(tmp);
		} catch(IOException e) {
			e.printStackTrace();
			result = Result.failure(e);
			if (String.valueOf(e.getMessage()).toLowerCase().indexOf("no space") != -1) {
				throw new RuntimeException(e.getMessage(), e);
			}
		} catch(Exception e) {
			e.printStackTrace();
			result = Result.failure(e);
		} finally {
			if (null != ch) {
				try {
					ch.close();
				} catch(Exception e) {}
				ch = null;
			}
			if (null != out) {
				try {
					out.close();
				} catch(Exception e) {}
				out = null;
			}
			if (null != conn) {
				try {
					conn.disconnect();
				} catch(Exception e) {}
				conn = null;
			}
		}
		if (result.negative()) {
			tmp.delete();
			tmp = null;
		}
		return result;
	}
}
