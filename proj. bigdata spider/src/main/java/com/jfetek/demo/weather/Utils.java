package com.jfetek.demo.weather;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import com.jfetek.common.Lookup;
import com.jfetek.common.SystemDefault;
import com.jfetek.common.data.Result;
import com.jfetek.common.util.ResourceUtil;
import com.jfetek.common.util.TextUtil;
import com.jfetek.demo.weather.data.Filter;
import com.jfetek.demo.weather.data.FtpFileInfo;
import com.mongodb.DB;

public class Utils {

	private Utils() {
	}
	
//	public static final String	URLENCODE_PATTERN	= "^([0-9a-zA-Z\\.\\-\\*\\_\\+\\:\\/\'\" ]|\\%([0-9a-fA-F][0-9a-fA-F])+)+$";
	public static final Pattern	URLENCODE_PATTERN	= Pattern.compile("\\%[0-9a-fA-F][0-9a-fA-F]");
	
	public static void main(String[] args) throws Exception {
//		String s = "Ала";
//		String s = "%C3%83%C6%92%C3%8B%C5%93%C3%83%E2%80%9A%C3%82%C2%B3%C3%83%C6%92%C3%A2%E2%80%9E%C2%A2%C3%83%E2%80%A6%C3%82%C2%A0%C3%83%C6%92%C3%8B%C5%93%...";
//		String s = "%25d0%2590%25d0%25bb%25d0%25b0%25d1%2585%25d3%2599%25d1%258b%25d0%25bb%25d0%25b0:derhexer";
//		String s = "User_talk:%E4%B8%96%E7%88%B5%E6%97%B6%E6%97%B6%E5%BD%A9%E3%80%90+%E6%98%8E%E7%8F%A0%E6%97%B6%E6%97%B6%E5%BD%A9%E5%B9%B3%E5%8F%B0%3A+www.370u.com+%E3%80%91%E5%85%A8%E7%BD%91%E6%9C%80%E9%AB%98";
		String s = "Category_talk:Singles_certified_triple_platinum_by_the_Productores_de_M%C3%BAsica_de_Espa%C3%B1a";
		System.out.println(urldecode(s));
		System.out.println(URLDecoder.decode(s, SystemDefault.CHARSET_VALUE));
	}
	
	public static String urldecode(String s) {
		int count = 0;
		String tmp = s;
		try {
			while (URLENCODE_PATTERN.matcher(tmp).find()) {
				tmp = _urldecode(tmp);
				++count;
			}
		} catch (Exception e) {
			System.out.println(s+" -"+count+"-> "+tmp);
			e.printStackTrace();
		}
//		System.out.println("decode "+count+" times.");
		return tmp;
	}
	
	public static String _urldecode(String s) throws UnsupportedEncodingException {
		StringParser parser = new StringParser(s);
		StringBuilder tmp = new StringBuilder(s.length()/2);
		for (char c = parser.current(); c != StringParser.DONE; c = parser.next()) {
			switch (c) {
				case '+':
					tmp.append(' ');
					break;
				case '%':
					String str = _parseHelper(parser);
					if (null == str) {
						tmp.append(c);
					}
					else {
						tmp.append(str);
					}
					break;
				default:
					tmp.append(c);
					break;
			}
		}
		return tmp.toString();
	}
	public static String _parseHelper(StringParser parser) throws UnsupportedEncodingException {
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		StringBuilder tmp = new StringBuilder(3);
		for (char c = parser.current(); c != StringParser.DONE; c = parser.next()) {
			if (c != '%') {
				parser.previous();
				break;
			}
			int mark = parser.getIndex();
			char c1 = parser.next();
			if (StringParser.DONE == c1) {
				String s = new String(buff.toByteArray(), SystemDefault.CHARSET_VALUE);
				return s + '%';
			}
			char c2 = parser.next();
			if (StringParser.DONE == c2) {
				String s = new String(buff.toByteArray(), SystemDefault.CHARSET_VALUE);
				return s + '%' + c1;
			}
			tmp.delete(0, 2);
			tmp.append(c1);
			tmp.append(c2);
			try {
				byte b = (byte)Integer.parseInt(tmp.toString(), 16);
				buff.write(b);
			} catch(NumberFormatException e) {
				String s = new String(buff.toByteArray(), SystemDefault.CHARSET_VALUE);
				parser.setIndex(1+mark);
				return s + '%';
			}
		}
		return new String(buff.toByteArray(), SystemDefault.CHARSET_VALUE);
	}

	public static String masstext(double size) {
		DecimalFormat df = new DecimalFormat("0.##");
		String unit = "Byte";
		if (size > 1024.0) {
			size /= 1024;
			unit = "KB";
		}
		if (size > 1024.0) {
			size /= 1024;
			unit = "MB";
		}
		if (size > 1024.0) {
			size /= 1024;
			unit = "GB";
		}
		if (size > 1024.0) {
			size /= 1024;
			unit = "TB";
		}
		return df.format(size) + unit;
	}
	public static String timetext(long ms) {
		int sec = (int) (ms / 1000);
		int min = sec / 60;
		int hr = min / 60;
		int day = hr / 24;
		hr %= 24;
		min %= 60;
		sec %= 60;
		
		StringBuilder tmp = new StringBuilder(20);
		if (day > 0) {
			tmp.append(day).append("Days");
			if (sec > 0) {
				tmp.append(' ').append(hr).append("Hours ")
				.append(min).append("Minutes ")
				.append(sec).append("Seconds");
			}
			else if (min > 0) {
				tmp.append(' ').append(hr).append("Hours ")
					.append(min).append("Minutes ");
			}
			else if (hr > 0) {
				tmp.append(' ').append(hr).append("Hours");
			}
		}
		else if (hr > 0) {
			tmp.append(hr).append("Hours");
			if (sec > 0) {
				tmp.append(' ').append(min).append("Minutes ")
					.append(sec).append("Seconds");
			}
			else if (min > 0) {
				tmp.append(' ').append(min).append("Minutes");
			}
		}
		else if (min > 0) {
			tmp.append(min).append("Minutes");
			if (sec > 0) tmp.append(' ').append(sec).append("Seconds");
		}
		else {
			tmp.append(sec).append("Seconds");
		}
		return tmp.toString();
	}

	public static Connection setupJsoupConnection(String url) {
		return setupJsoupConnection(url, null);
	}
	public static Connection setupJsoupConnection(String url, String referer) {
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
	
	public static Result<File> downloadHttp(String url, String referer) {
		Result<File> result;
		File tmp = null;
		FileOutputStream out = null;
		ReadableByteChannel ch = null;
		HttpURLConnection conn = null;
		long ts = System.currentTimeMillis();
		try {
			tmp = Console.createTempFile(".download");
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
		if (result.positive()) {
			long diff = System.currentTimeMillis() - ts;
			double speed = 1000.0*tmp.length()/diff;
			System.out.println("download["+Utils.masstext(speed)+"/s]("+Utils.timetext(diff)+"): "+url);
		}
		else {
			System.out.println("download fail("+Utils.timetext(System.currentTimeMillis()-ts)+"): "+url);
		}
		if (null != tmp && result.negative()) {
			tmp.delete();
			tmp = null;
		}
		return result;
	}

	public static Result<File> downloadUrl(String url) {
		Result<File> result;
		File tmp = null;
		URLConnection conn = null;
		OutputStream os = null;
		FileOutputStream out = null;
		ReadableByteChannel ch = null;
		long ts = System.currentTimeMillis();
		try {
			tmp = Console.createTempFile(".download");
			URL u = new URL(url);
			conn = u.openConnection();
			os = conn.getOutputStream();
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
			if (null != os) {
				try {
					os.close();
				} catch(Exception e) {}
				os = null;
			}
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
			conn = null;
		}
		if (result.positive()) {
			long diff = System.currentTimeMillis() - ts;
			double speed = 1000.0*tmp.length()/diff;
			System.out.println("download["+Utils.masstext(speed)+"/s]("+Utils.timetext(diff)+"): "+url);
		}
		else {
			System.out.println("download fail("+Utils.timetext(System.currentTimeMillis()-ts)+"): "+url);
		}
		if (null != tmp && result.negative()) {
			tmp.delete();
			tmp = null;
		}
		return result;
	}

//	public static Result<ArrayList<FtpFileInfo>> getFtpFileList(URI uri) {
//		Result<File> result = Utils.downloadUrl(uri.toString());
//		if (result.negative()) return Result.failure(result);
//		
//		File tmp = result.data;
//		ArrayList<FtpFileInfo> list = new ArrayList<FtpFileInfo>();
//		BufferedReader in = null;
//		try {
//			in = new BufferedReader(new InputStreamReader(new FileInputStream(tmp)));
//			String line;
//			while (null != (line = in.readLine())) {
//				FtpFileInfo info = FtpFileInfo.parse(uri, line);
//				if (null != info) list.add(info);
//			}
//		} catch(FileNotFoundException e) {
//			e.printStackTrace();
//		} catch(IOException e) {
//			e.printStackTrace();
//			if (String.valueOf(e.getMessage()).toLowerCase().indexOf("no space") != -1) {
//				throw new RuntimeException(e.getMessage(), e);
//			}
//		} finally {
//			if (null != in) {
//				try {
//					in.close();
//				} catch(Exception e) {}
//				in = null;
//			}
//		}
//		if (null != tmp) {
//			tmp.delete();
//			tmp = null;
//		}
//		return Result.wrap(list);
//	}
//
//	public static Result<ArrayList<FtpFileInfo>> getFtpFileList(URI uri, Filter<FtpFileInfo> filter) {
//		if (null == filter) return getFtpFileList(uri);
//		
//		Result<File> result = Utils.downloadUrl(uri.toString());
//		if (result.negative()) return Result.failure(result);
//		
//		File tmp = result.data;
//		ArrayList<FtpFileInfo> list = new ArrayList<FtpFileInfo>();
//		BufferedReader in = null;
//		try {
//			in = new BufferedReader(new InputStreamReader(new FileInputStream(tmp)));
//			String line;
//			while (null != (line = in.readLine())) {
//				FtpFileInfo info = FtpFileInfo.parse(uri, line);
//				if (null != info && filter.accept(info)) list.add(info);
//			}
//		} catch(FileNotFoundException e) {
//			e.printStackTrace();
//		} catch(IOException e) {
//			e.printStackTrace();
//			if (String.valueOf(e.getMessage()).toLowerCase().indexOf("no space") != -1) {
//				throw new RuntimeException(e.getMessage(), e);
//			}
//		} finally {
//			if (null != in) {
//				try {
//					in.close();
//				} catch(Exception e) {}
//				in = null;
//			}
//		}
//		if (null != tmp) {
//			tmp.delete();
//			tmp = null;
//		}
//		return Result.wrap(list);
//	}
	
	public static Result<File> downloadFtp(URI uri) {
		FTPClient ftp = createFtpConnection(uri);
		if (null == ftp) return Result.failure("can not open ftp: "+uri);
		
		Result<File> result;
		File tmp = null;
		long ts = System.currentTimeMillis();
		try {
			tmp = Console.createTempFile(".ftp.download");
			InputStream in = null;
			OutputStream out = null;
			try {
				out = new BufferedOutputStream(new FileOutputStream(tmp));
				boolean ok = ftp.retrieveFile(uri.getPath(), out);
				if (ok) {
					result = Result.wrap(tmp);
				}
				else {
					result = Result.failure(ftp.getReplyString());
				}
			} catch(IOException e) {
				e.printStackTrace();
				result = Result.failure(e);
			} finally {
				ResourceUtil.close(in);
				in = null;
				ResourceUtil.close(out);
				out = null;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			result = Result.failure(e);
			if (String.valueOf(e.getMessage()).toLowerCase().indexOf("no space") != -1) {
				throw new RuntimeException(e.getMessage(), e);
			}
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.abor();
				} catch(Exception e) {}
				try {
					ftp.logout();
				} catch(Exception e) {}
				try {
					ftp.disconnect();
				} catch(Exception e) {}
				ftp = null;
			}
		}
		if (result.positive()) {
			long diff = System.currentTimeMillis() - ts;
			double speed = 1000.0*tmp.length()/diff;
			System.out.println("download["+Utils.masstext(speed)+"/s]("+Utils.timetext(diff)+"): "+uri);
		}
		else {
			System.out.println("download fail("+Utils.timetext(System.currentTimeMillis()-ts)+"): "+uri);
		}
		if (result.negative() && null != tmp && tmp.exists()) {
			tmp.delete();
			tmp = null;
		}
		return result;
	}

	public static Result<ArrayList<FtpFileInfo>> listFtp(URI uri) {
		FTPClient ftp = createFtpConnection(uri);
		if (null == ftp) return Result.failure("can not open ftp: "+uri);
		
		Result<ArrayList<FtpFileInfo>> result;
		try {
			ArrayList<FtpFileInfo> list = new ArrayList<FtpFileInfo>();
			FTPFile[] files = ftp.listFiles( uri.getPath() );
			ftp.getReplyCode();
			int len = files.length;
			for (int i = 0; i < len; ++i) {
				FTPFile file = files[i];
				list.add(FtpFileInfo.of(uri, file));
			}
			
			result = Result.wrap(list);
			
		} catch (IOException e) {
			e.printStackTrace();
			result = Result.failure(e);
			if (String.valueOf(e.getMessage()).toLowerCase().indexOf("no space") != -1) {
				throw new RuntimeException(e.getMessage(), e);
			}
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.abor();
				} catch(Exception e) {}
				try {
					ftp.logout();
				} catch(Exception e) {}
				try {
					ftp.disconnect();
				} catch(Exception e) {}
				ftp = null;
			}
		}
		
		return result;
	}

	public static Result<ArrayList<FtpFileInfo>> listFtp(URI uri, Filter<FtpFileInfo> filter) {
		FTPClient ftp = createFtpConnection(uri);
		if (null == ftp) return Result.failure("can not open ftp: "+uri);
		
		Result<ArrayList<FtpFileInfo>> result;
		try {
			ArrayList<FtpFileInfo> list= new ArrayList<FtpFileInfo>();
			FTPFile[] files = ftp.listFiles( uri.getPath() );
			ftp.getReplyCode();
			int len = files.length;
			for (int i = 0; i < len; ++i) {
				FTPFile file = files[i];
				FtpFileInfo info = FtpFileInfo.of(uri, file);
				if (filter.accept(info)) {
					list.add(info);
				}
			}
			
			result = Result.wrap(list);
			
		} catch (IOException e) {
			e.printStackTrace();
			result = Result.failure(e);
			if (String.valueOf(e.getMessage()).toLowerCase().indexOf("no space") != -1) {
				throw new RuntimeException(e.getMessage(), e);
			}
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.abor();
				} catch(Exception e) {}
				try {
					ftp.logout();
				} catch(Exception e) {}
				try {
					ftp.disconnect();
				} catch(Exception e) {}
				ftp = null;
			}
		}
		
		return result;
	}

	public static FTPClient createFtpConnection(URI uri) {
		String host = uri.getHost();
		int port = uri.getPort();
		String userinfo = uri.getUserInfo();
		String user = "anonymous";
		String password = "anonymous@"+host;
		if (null != userinfo) {
			int idx = userinfo.indexOf(":");
			if (idx == -1) {
				user = userinfo;
			}
			else {
				user = userinfo.substring(0, idx);
				password = userinfo.substring(1+idx);
			}
		}
		
		FTPClient ftp = new FTPClient();
		boolean ok = false;
		try {
			ftp.setConnectTimeout(60000);
			ftp.setDataTimeout(60000);
			ftp.setDefaultTimeout(60000);
			
			if (port > 0) {
				ftp.connect(host, port);
			}
			else {
				ftp.connect(host);
			}
			
			if (ftp.login(user, password)) {
				ftp.setFileType( FTP.BINARY_FILE_TYPE );
				ftp.setKeepAlive( true );
				ftp.enterLocalPassiveMode();
				ftp.changeWorkingDirectory( "/" );
				
				ok = true;
			}
			else {
				throw new IllegalStateException("ftp log fail("+ftp.getReplyCode() + ") " + ftp.getReplyString());
			}
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			if (String.valueOf(e.getMessage()).toLowerCase().indexOf("no space") != -1) {
				throw new RuntimeException(e.getMessage(), e);
			}
		} catch (Exception e) {
			System.out.println("ftp error("+ftp.getReplyCode() + ") " + ftp.getReplyString());
			e.printStackTrace();
		} finally {
			if (!ok && ftp.isConnected()) {
				try {
					ftp.logout();
				} catch(Exception e) {}
				try {
					ftp.disconnect();
				} catch(Exception e) {}
				ftp = null;
			}
		}
		return ftp;
	}

//	public static void main(String[] args) throws Exception {
//		try {
//			Console.startup();
//			
//			boolean fileExists = true;
//			int fileSize = 2024;
//			
//			int newSize = 2024;
//			boolean isLatest = fileExists && (newSize == fileSize);
//			
//			DB db = Console.mongo.getDB("persistent");
//			DBCollection task = db.getCollection("weather.task");
//			
//			BasicDBObject query = new BasicDBObject("_id", "_download:ftp://ftp.ncdc.noaa.gov/pub/data/noaa/XXXXX-YYYY-2013.gz");
//			boolean needUpdate = true;
//			BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
//				.push("$setOnInsert")
//					// basic
//					.add("task", "_download")
//					.add("errorCount", 0)
////					.add("log", new BasicDBList())
//					// task
//					.add("year", 2013)
//					.add("station", "XXXXX-YYYY")
//					.add("file", "/raw/2013/XXXXX-YYYY-2013.gz")
//					.add("priority", 2)
//				.pop();
//			if (isLatest) {
//				DBCursor cursor = null;
//				try {
//					cursor = task.find(query);
//					BasicDBObject t = (BasicDBObject) cursor.one();
//					if (null == t || (null != t && "yet".equals(t.getString("status")))) {
//						update.push("$set")
//							.add("status", "done")
//							.add("size", newSize)
//						.pop()
//						.push("$push")
//							.add("log", BsonUtil.getBasicBson(ErrorCode.ok("file already downloaded")))
//						.pop();
//					}
//					else {
//						needUpdate = false;
//					}
//				} finally {
//					cursor.close();;
//				}
//			}
//			else {
//				// create new
//				update.push("$set")
//					.add("status", "yet")
//					.add("size", newSize)
//				.pop();
//			}
////			System.out.println((BasicDBObject)update.get());
//			if (needUpdate) {
//				BulkWriteOperation bulk = task.initializeUnorderedBulkOperation();
//				
////				System.out.println(task.update(query, update.get(), true, true));
//				bulk.find(query).upsert().update(update.get());
//				query.putAll(update.get());
//				System.out.println(query);
//				
//				BulkWriteResult result = bulk.execute();
//				System.out.println(result);
//			}
//			else {
//				System.out.println("no change");
//			}
//			
//		} finally {
//			Console.shutdown();
//		}
//	}
	
//	public static void main(String[] args) throws Exception {
//		File file = new File("system.setup");
//		System.out.println("renew system-setup> "+file.delete());
//		try {
//			Console.startup();
//			
//			System.out.println(Console.cache.getServerAddressList());
//			DB db = Console.cache.getDB("persistent");
//			DBCollection task = db.getCollection("weather.task");
//
//			long ts = System.currentTimeMillis();
//			BasicDBObjectBuilder query = BasicDBObjectBuilder.start()
//					.add("status", TaskStatus.YET.text)
////					.append("task", WeatherTask.INSERT.text);
//					.push("task")
//						.add("$in", Arrays.asList(WeatherTask.INSERT.text, WeatherTask.DOWNLOAD.text))
//					.pop();
//			BasicDBObjectBuilder sort = BasicDBObjectBuilder.start()
//					.add("priority", 1)
//					.add("year", -1);
//			BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
//					.push("$set")
//						.append("status", TaskStatus.EXECUTING.text)
//					.pop();
//			BasicDBObject data = (BasicDBObject) task.findAndModify(query.get(), sort.get(), update.get());
////			BasicDBObject data = (BasicDBObject) task.find(query.get()).sort(sort.get()).one();
//			System.out.println(data);
//			System.out.println((System.currentTimeMillis()-ts)+"ms");
//			
//			ts = System.currentTimeMillis();
//			System.out.println(task.count());
//			System.out.println((System.currentTimeMillis()-ts)+"ms");
//			
//		} finally {
//			Console.shutdown();
//		}
//	}
	
	public static final String getWeatherDbName() throws Exception {
		Lookup setup = Console.setup.cates("weather");
		return setup.lookup("database");
	}

	public static final DB getWeatherDb() throws Exception {
		return Console.mongo.getDB(getWeatherDbName());
	}

}
