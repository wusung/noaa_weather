package com.jfetek.demo.weather.spider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jfetek.common.ErrorCode;
import com.jfetek.common.data.Result;
import com.jfetek.common.time.Date;
import com.jfetek.common.time.DateTime;
import com.jfetek.common.util.TextUtil;
import com.jfetek.demo.weather.Console;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
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
		MEDIAWIKI("mediawiki", ".w");
		
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
		public TaskManager(ThreadGroup tg) {
			super(tg, "wiki-spider.task-manager");
			this.db = Console.mongo.getDB("persistent");
			this.task = db.getCollection("wiki.task");
			this.tsAck = System.currentTimeMillis();
			
			this.start();
		}
		
		public void run() {
			tsStart = System.currentTimeMillis();
			
			resetErrors();
			resetExecuting();
			
			doParseMainTask();
			
			new Thread(THREAD_GROUP, new TaskWatcher(manager)).start();
			new Thread(THREAD_GROUP, new TaskWatcher(manager)).start();

//			new Thread(THREAD_GROUP, new BulkInsertWatcher(manager)).start();
//			new Thread(THREAD_GROUP, new BulkInsertWatcher(manager)).start();
			
			long cntLoop = 0;
			while (!Thread.interrupted()) {
				try {
					Thread.sleep(60000);	// check every 1 min
					if (isDonw()) break;
					if (++cntLoop % 30 == 0) {	// log every 10 min
						long total = task.count();
						long done = task.count(new BasicDBObject("status", "done"));
						System.out.println(DateTime.now()+"["+timetext(System.currentTimeMillis()-tsStart)+"] "+done+"/"+total+"");
					}
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		public Result<ArrayList<String>> doParseMainTask() {
			String root = Console.setup.val("wiki", "url");

			ArrayList<String> list = new ArrayList<String>(10);
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
			BasicDBObject query = new BasicDBObject("status", TaskStatus.YET.text);
			BasicDBObjectBuilder sort = BasicDBObjectBuilder.start()
					.add("priority", 1)
					.add("url", -1);
			BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
					.push("$set")
						.append("status", TaskStatus.EXECUTING.text)
					.pop();
			BasicDBObject data = (BasicDBObject) task.findAndModify(query, sort.get(), update.get());
			
			return data;
		}

		public synchronized BasicDBObject nextTask(Task type) {
			BasicDBObject query = new BasicDBObject("status", TaskStatus.YET.text)
					.append("task", type.text);
			BasicDBObjectBuilder sort = BasicDBObjectBuilder.start()
					.add("priority", 1)
					.add("url", -1);
			BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
					.push("$set")
						.append("status", TaskStatus.EXECUTING.text)
					.pop();
			BasicDBObject data = (BasicDBObject) task.findAndModify(query, sort.get(), update.get());
			
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
			return buildBasicTask(Task.PARSE_YEAR)
					.append("_id", Task.PARSE_YEAR.getTaskId(url))
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
			return buildBasicTask(Task.PARSE_MONTH)
					.append("_id", Task.PARSE_MONTH.getTaskId(url))
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
			return buildBasicTask(Task.PARSE_MD5)
					.append("_id", Task.PARSE_MD5.getTaskId(url))
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
			return buildBasicTask(Task.DOWNLOAD)
					.append("_id", Task.DOWNLOAD.getTaskId(url))
					.append("url", url)
					.append("referer", referer)
					.append("priority", 2);
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
		public BasicDBObject addBulkInsertTask(String dirname, File file) {
			BasicDBObject data = buildBulkInsertTask(dirname, file);
			if (0 == task.count(new BasicDBObject("_id", data.getString("_id")))) {
				this.task.insert(data);
			}
			return data;
		}
		
		public synchronized void acknowledge() {
			this.tsAck = System.currentTimeMillis();
		}
		
		private synchronized boolean isDonw() {
			return (System.currentTimeMillis() - this.tsAck > 600000);
		}
	}
	
	public static class BulkInsertWatcher implements Runnable {
		final DB db;
		final TaskManager manager;
		final File rawDir;
		public BulkInsertWatcher(TaskManager manager) {
			this.db = Console.mongo.getDB("wiki1");
			this.manager = manager;

			this.rawDir = Console.createDir("wiki");
		}
		@Override
		public void run() {
			while (true) {
				try {
					BasicDBObject task = manager.nextTask(Task.BULK_INSERT);
					while (null == task) {
						System.out.println("no bulk-insert task... wait");
						synchronized (manager) {
							manager.wait();
						}
						task = manager.nextTask();
					}

					String id = task.getString("_id");
					String name = task.getString("task");
//					System.out.println("bulk-insert-task["+id+"]...");
					Result<?> ec;
					try {
						Result<BulkWriteResult> result = doBulkInsertTask(task);
						ec = result;
					} catch(Throwable t) {
						t.printStackTrace();
						ec = Result.failure(t);
					}
					manager.log(id, ec);
					manager.updateStatus(task, ec);
					
					manager.acknowledge();
					
				} catch (InterruptedException e) {
					break;
				}
			}
		}
		
		public Result<BulkWriteResult> doBulkInsertTask(BasicDBObject data) throws IOException {
			// parse all month 
			String id = data.getString("_id");
			String name = data.getString("file");
			File file = new File(this.rawDir, name);
			name = file.getName();

//			BufferedReader in = null;
//			long ts = System.currentTimeMillis();
//			try {
//				in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
//				String line;
//				int cntLine = 0;
//				while (null != (line = in.readLine())) {
//					++cntLine;
//					line = line.trim();
//					if (0 == line.length()) continue;
//					
//					String[] col = line.split(" ");
//					int idxDot = col[0].indexOf(".");
//					String lang = col[0];
//					WikiProject project = WikiProject.WIKIPEDIA;
//					if (idxDot >= 0) {
//						lang = col[0].substring(0, idxDot);
//						project = WikiProject.of(col[0].substring(idxDot));
//					}
//					
//					String title = HttpUtil.decode(col[1]);
//					long cntPage = TextUtil.longValue(col[2]);
//					double size = TextUtil.doubleValue(col[3]);
//					
//				}
//				
//			} catch(IOException e) {
//				e.printStackTrace();
//			} finally {
//				if (null != in) {
//					try {
//						in.close();
//					} catch(Exception e) {}
//					in = null;
//				}
//			}
//			
//			return result;
			return Result.failure();
		}
	}

	public static class TaskWatcher implements Runnable {
		final DB db;
		final DBCollection task;
		final TaskManager manager;
		final File rawDir;
		public TaskWatcher(TaskManager manager) {
			this.db = Console.mongo.getDB("persistent");
			this.task = db.getCollection("wiki.task");
			this.manager = manager;

			this.rawDir = Console.createDir("wiki");
		}
		@Override
		public void run() {
			while (true) {
				try {
					BasicDBObject task = manager.nextTask();
					while (null == task) {
						System.out.println("no task... wait");
						synchronized (manager) {
							manager.wait();
						}
						task = manager.nextTask();
					}

					String id = task.getString("_id");
					String name = task.getString("task");
//					System.out.println("task["+id+"]...");
					Result<?> result;
					try {
						Task t = Task.of(name);
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
							default:
								System.out.println("UNKNOWN TASK: "+name+" id:"+id);
								result = Result.failure("unknown task: "+name);
						}
//					} catch (InterruptedException e) {
//						throw e;
					} catch(Throwable t) {
						t.printStackTrace();
						result = Result.failure(t);
					}
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
				Element md5 = doc.select("a").first();
				manager.addParseMd5Task(md5.absUrl("href"), url);
				
				Elements a = doc.select("ul").first().select("li a");
				for (Element e : a) {
					list.add(e.absUrl("href"));
				}
				
				manager.addParseMonthTask(list, url);
				result = Result.wrap(list);
				
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
				Elements a = doc.select("ul").first().select("li a");
				for (Element e : a) {
					list.add(e.absUrl("href"));
				}
				
				manager.addDownloadTask(list, url);
				result = Result.wrap(list);
				
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
			System.out.println("download("+timetext(System.currentTimeMillis()-ts)+"): "+url);
			if (result.positive()) {
				String name = url.substring(url.lastIndexOf('/')+1);
				DateTime datetime = DateTime.valueOf(name.substring(11, 26), "yyyyMMdd-HHmmss");
				File dir = new File(this.rawDir, datetime.date.year.toString());
				dir.mkdirs();
				File file = new File(dir, name);
				if (result.data.renameTo(file)) {
//					doParseMd5Task(file);
				}
				else {
					result = Result.failure("can not rename md5-file: "+name);
				}
			}
			
			return result;
		
		}

		public Result<File> doDownloadTask(BasicDBObject data) throws IOException {
			// parse all month 
			String id = data.getString("_id");
			String url = data.getString("url");
			String referer = data.getString("referer");

			long ts = System.currentTimeMillis();
			Result<File> result = download(url, referer);
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
				String name = url.substring(url.lastIndexOf('/')+1);
				DateTime datetime = DateTime.valueOf(name.substring(11, 26), "yyyyMMdd-HHmmss");
				Date date = datetime.date;
				String dirname = date.year.toString() + "/" + date.month.toText() + "/";
				File dir = new File(this.rawDir, dirname);
				dir.mkdirs();
				File file = new File(dir, name);
				if (result.data.renameTo(file)) {
					manager.addBulkInsertTask(dirname, file);
				}
				else {
					result = Result.failure("can not rename file: "+name);
				}
			}
			else {
				System.out.println("download fail("+timetext(System.currentTimeMillis()-ts)+"): "+url);
			}
			
//			manager.updateStatus(data, result);
		
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
		Console.startup();

		THREAD_GROUP = new ThreadGroup("wiki-spider.thread-group");
		manager = new TaskManager(THREAD_GROUP);

		manager.join();
		THREAD_GROUP.interrupt();
		THREAD_GROUP.destroy();
		
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
