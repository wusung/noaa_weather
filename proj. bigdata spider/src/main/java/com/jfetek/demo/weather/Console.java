package com.jfetek.demo.weather;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfetek.common.Lookup;
import com.jfetek.common.SystemDefault;
import com.jfetek.common.VariableSetup;
import com.jfetek.common.util.ResourceUtil;
import com.mongodb.MongoClient;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class Console {
	
	private static final Logger LOGGER		= LoggerFactory.getLogger(Console.class);

	
	private static String			sysName				= null;
	private static File				rootDir				= null;
	private static File				tempDir				= null;
	private static File				uploadDir			= null;
	
	private static ServletContext	webapp				= null;
	private static File				webappDir			= null;
	private static File				webappResourceDir	= null;

	private static Configuration	templateConfig;

	public static VariableSetup		setup		= null;
	public static MongoClient		mongo		= null;
	public static MongoClient		cache		= null;

	private static boolean			onGoing		= false;

	private Console() {
	}
	
	public static synchronized void startup() throws IOException, RuntimeException {
		if (onGoing) throw new IllegalStateException("Server already startup");
		
		try {
			LOGGER.info("system starting...");

			initSetup();
			initEnv();
			initMongodb();
			initCache();
			
			LOGGER.info("system started");
		} catch(IOException e) {
			throw e;
		} catch(RuntimeException e) {
			throw e;
		} finally {
			onGoing = true;
		}
	}
	
	private static void initSetup() throws IOException, RuntimeException {
		Console.setup = new VariableSetup();

		File file = new File("system.setup");
		LOGGER.info("Load setup: " + file.getAbsolutePath());
		if (!file.exists()) {
			URL url = Console.class.getResource("system.setup");
			ResourceUtil.copy(url, file);
		}
//		URL url = Console.class.getResource("/com/jfetek/demo/weather/system.setup");
//		URL url = Console.class.getResource("/resources/com/jfetek/demo/weather/system.setup");
		try {
			Console.setup.loadFrom(file);
//		} catch (IOException e) {
//			LOGGER.error("try to load setup from {}", file, e);
//			throw e;
		} catch (RuntimeException e) {
			LOGGER.error("try to load setup from {}", file, e);
			throw e;
		} catch (Exception e) {
			LOGGER.error("try to load setup from {}", file, e);
			throw new RuntimeException(e);
		}
	}
	
	private static void initEnv() {
		sysName = Console.setup.val("system", "name");
		
		String strRoot = Console.setup.val("system", "root.dir");
		Console.rootDir = new File(strRoot);
		Console.rootDir.mkdirs();
		
//		String strTemp = Console.setup.val("system", "temp.dir");
//		Console.tempDir = new File(strTemp);
		String pathTemp = Console.setup.val("system", "temp.path");
		Console.tempDir = new File(Console.rootDir, pathTemp);
		Console.tempDir.mkdirs();

//		String strUpload = Console.setup.val("system", "upload.dir");
//		Console.uploadDir = new File(strUpload);
		String pathUpload = Console.setup.val("system", "upload.path");
		Console.uploadDir = new File(Console.rootDir, pathUpload);
		Console.uploadDir.mkdirs();
		
		Bootstrap bootstrap = Bootstrap.getCurrentInstance();
		if (null != bootstrap) {
			Console.webapp = bootstrap.getServletContext();
			Console.webappDir = new File(webapp.getRealPath("/"));
			Console.webappResourceDir = new File(Console.webappDir, "WEB-INF");
		}

		Console.templateConfig = new Configuration(Configuration.VERSION_2_3_21);
		try {
			File templateDir = Console.getWebappResource("template/");
			if (null != bootstrap) {
				Console.templateConfig.setDirectoryForTemplateLoading( templateDir );
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void initMongodb() throws UnknownHostException {
		Lookup setup = Console.setup.cate("mongodb:bigdata");
		String host = setup.lookup("host");
		int port = setup.lookupInt("port", -1);
		if (port > 0) {
			Console.mongo = new MongoClient(host, port);
		}
		else {
			Console.mongo = new MongoClient(host);
		}
	}
	
	private static void initCache() throws UnknownHostException {
		Lookup setup = Console.setup.cate("mongodb:cache");
		String host = setup.lookup("host");
		int port = setup.lookupInt("port", -1);
		if (port > 0) {
			Console.cache = new MongoClient(host, port);
		}
		else {
			Console.cache = new MongoClient(host);
		}
	}
	
	public static synchronized void shutdown() throws Exception {
		if (!onGoing) throw new IllegalStateException("Server already shutdown");

		try {
			LOGGER.info("system shuting down...");
			
			finMongodb();
			finEnv();
			finSetup();
			
			LOGGER.info("system shutdown");
		} catch(Exception e) {
			throw e;
		} finally {
			onGoing = false;			
		}
	}
	
	private static void finSetup() {
		Console.setup = null;
	}
	
	private static void finEnv() {

		Console.templateConfig.clearTemplateCache();
		Console.templateConfig.clearEncodingMap();
		Console.templateConfig.clearSharedVariables();
		Console.templateConfig = null;
		
		Console.sysName = null;
		Console.rootDir = null;
		Console.tempDir = null;
		Console.uploadDir = null;
		
		Console.webapp = null;
		Console.webappDir = null;
		Console.webappResourceDir = null;
	}
	
	private static void finMongodb() {
		Console.mongo.close();
		Console.mongo = null;
	}
	
	private static void finCache() {
		Console.cache.close();
		Console.cache = null;
	}
	

//	public static File getRootDir() {
//		if (!onGoing) throw new IllegalStateException("Server already shutdown");
//
//		return Console.rootDir;
//	}
	
	public static File createDir(String name) {
		if (!onGoing) throw new IllegalStateException("Server already shutdown");

		File dir = new File( Console.rootDir , name );
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
	
	public static File createTempFile() throws IOException {
		if (!onGoing) throw new IllegalStateException("Server already shutdown");

		return File.createTempFile( Console.sysName , "system" , Console.tempDir );
	}

	public static File createTempFile(String suffix) throws IOException {
		if (!onGoing) throw new IllegalStateException("Server already shutdown");

		return File.createTempFile( Console.sysName , suffix , Console.tempDir );
	}
	
	public static File getTempFile(String name) {
		return new File(Console.tempDir, name);
	}
	
	public static File getUploadFile(String name) {
		return new File(Console.uploadDir, name);
	}
	
	public static File getResource(String name) {
		File file = new File(Console.rootDir, name);
		return file;
	}
	

//	public static File getWebappDir() {
//		return this.webappDir;
//	}
	
//	public static File getWebappResourceDir() {
//		return this.webappResourceDir;
//	}

	public static File getWebappResource(String name) {
		File file = new File(Console.webappResourceDir, name);
		return file;
	}

	public static Template getTemplate(String name) throws IOException {
		Template template = Console.templateConfig.getTemplate(name, SystemDefault.CHARSET_VALUE);
		template.setEncoding(SystemDefault.CHARSET_VALUE);
		return template;
	}

//	public static URL getSystemResource(String name) {
//		if (!onGoing) throw new IllegalStateException("Server already shutdown");
//
//		URL url = ClassLoader.getSystemResource(name);
//		return url;
//	}
//
//	public static <T> URL getSystemResource(Class<T> base_class, String name) {
//		if (!onGoing) throw new IllegalStateException("Server already shutdown");
//
//		URL url = base_class.getResource(name);
//		return url;
//	}
	
	public static void debug(String msg) {
		LOGGER.debug(msg);
	}
	public static void debug(String msg, Throwable t) {
		LOGGER.debug(msg, t);
	}
	public static void debug(String format, Object... args) {
		LOGGER.debug(format, args);
	}
	public static void trace(String msg) {
		LOGGER.trace(msg);
	}
	public static void trace(String msg, Throwable t) {
		LOGGER.trace(msg, t);
	}
	public static void trace(String format, Object... args) {
		LOGGER.trace(format, args);
	}
	public static void error(String msg) {
		LOGGER.error(msg);
	}
	public static void error(String msg, Throwable t) {
		LOGGER.error(msg, t);
	}
	public static void error(String format, Object... args) {
		LOGGER.error(format, args);
	}
	public static void info(String msg) {
		LOGGER.info(msg);
	}
	public static void info(String msg, Throwable t) {
		LOGGER.info(msg, t);
	}
	public static void info(String format, Object... args) {
		LOGGER.info(format, args);
	}
	public static void warn(String msg) {
		LOGGER.warn(msg);
	}
	public static void warn(String msg, Throwable t) {
		LOGGER.warn(msg, t);
	}
	public static void warn(String format, Object... args) {
		LOGGER.warn(format, args);
	}
}
