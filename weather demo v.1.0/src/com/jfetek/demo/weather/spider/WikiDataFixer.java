package com.jfetek.demo.weather.spider;

import java.io.File;

import com.jfetek.demo.weather.Console;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class WikiDataFixer {
	
	public static void main(String[] args) throws Throwable {
		Console.startup();
		
		DB db = Console.mongo.getDB("persistent");
		DBCollection tasks = db.getCollection("wiki.task");
		
		// fix raw-file path
//		DBCursor cs = tasks.find(new BasicDBObject("task", WikiSpider.Task.BULK_INSERT.text));
//		for (DBObject o : cs) {
//			BasicDBObject data = (BasicDBObject) o;
//			String s = data.getString("file");
////			if (s.startsWith("D:\\Runtime\\weather-demo\\wiki")) {
////			if (s.startsWith("\\\\192.168.3.33\\bu-disk-c\\Runtime\\weather-demo\\wiki")) {
//			if (s.startsWith("W:\\root\\weather-demo\\wiki")) {
//				File file = new File(s);
//				String name = file.getName();
//				DateTime datetime = DateTime.valueOf(name.substring(11, 26), "yyyyMMdd-HHmmss");
//				Date date = datetime.date;
//				String dirname = date.year.toString() + "/" + date.month.toText() + "/";
//				
//				BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
//						.push("$set")
//							.append("file", dirname+name)
//						.pop();
//				WriteResult result = tasks.update(o, update.get());
//				if (result.getN() > 0) {
//					System.out.println("OK> "+s+" --> "+dirname+name);
//				}
//				else {
//					System.out.println("FAILURE> "+s);
//				}
//			}
//			else {
//				System.out.println("PASS> "+s);
//			}
//		}
		
		File dir = Console.createDir("wiki");
		BasicDBObjectBuilder query = BasicDBObjectBuilder.start()
				.add("task", "bulk-insert");
		DBCursor cs = tasks.find(query.get());
		for (DBObject o : cs) {
			BasicDBObject data = (BasicDBObject) o;
			String name = data.getString("file");
			File file = new File(dir, name);
			if (!file.exists()) {
				System.out.println("NOT-EXISTS> "+file.getAbsolutePath());
			}
		}
		
		Console.shutdown();
	}

}
