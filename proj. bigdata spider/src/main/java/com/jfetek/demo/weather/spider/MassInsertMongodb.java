package com.jfetek.demo.weather.spider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.jfetek.common.Lookup;
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
import com.mongodb.MongoClient;

public class MassInsertMongodb {	
	public static void main(String[] args) {
		MongoClient mongo = null;
		try {			
			Console.setup.cates("weather:spider", "weather");
			Lookup setup = Console.setup.cates("weather");
			DB db = Console.mongo.getDB(setup.lookup("database"));

			for (int i = 2001, end = 1993; i >= end; --i) {
				long ts = System.currentTimeMillis();
				DBCollection records = createRecordCollectionOfYear(db, i, false);
				System.out.println(i + " total " + records.count()
						+ " records in "
						+ timetext(System.currentTimeMillis() - ts) + "ms");
			}			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (null != mongo) {
				mongo.close();
				mongo = null;
			}
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
	
	public static DBCollection createCountryCollection(DB db, boolean drop_old) {
		DBCollection countries = db.getCollection("country");
		if (drop_old) countries.drop();
//		countries.createIndex(new BasicDBObject(""), new BasicDBObject());
		BulkWriteOperation bulk = countries.initializeUnorderedBulkOperation();
		bulk.insert(BasicDBObjectBuilder.start().append("_id", "").append("name", "UNKNOWN").append("station", new BasicDBList()).get());
		
		File file = new File("\\\\192.168.3.107/disk_d/資料備份/new Project/BigData/country-list.txt");
		HashSet<String> ids = new HashSet<String>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = in.readLine();	// skip first line: header
			while (null != (line = in.readLine())) {
				line = line.trim();
				if (0 == line.length()) continue;
				
				String id = line.substring(0, 12).trim();
				if (ids.contains(id)) continue;
				ids.add(id);
				
				String name = line.substring(12).trim();
				
				BasicDBObject country = new BasicDBObject("_id", id);
				country
					.append("name", name)
					.append("station", new BasicDBList());
				bulk.insert(country);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch(Exception e) {}
				in = null;
			}
		}
		
		BulkWriteResult result = bulk.execute();
		System.out.println(result);
		
		return countries;
	}
	
	public static void appendStationToCountry(DB db, BulkWriteOperation bulk, String country, String station) {
		DBCollection countries = db.getCollection("country");
		BasicDBObjectBuilder find = BasicDBObjectBuilder.start()
				.add("_id", country)
				.push("station")
					.add("$nin", Arrays.asList(station))
				.pop();
		BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
				.push("$push")
					.add("station", station)
				.pop();
//		countries.update(find.get(), update.get());
		bulk.find(find.get()).update(update.get());
	}

	public static final String[]	STATION_COLUMN	= {
//		 0       1       2       3          4        5       6      7      8      9        10
		"usaf", "wban", "name", "country", "state", "icao", "lat", "lng", "elv", "beign", "end"
	};
	public static DBCollection createStationCollection(DB db, BulkWriteOperation country_bulk, boolean drop_old) {
		DBCollection stations = db.getCollection("station");
		if (drop_old) stations.drop();
		BasicDBObject primary = new BasicDBObject();
		primary
			.append("usaf", 1)
			.append("wban", 1);
		stations.createIndex(primary, new BasicDBObject("unique", true));
		stations.createIndex(new BasicDBObject("country", 1));
		stations.createIndex(new BasicDBObject("geo", "2d"));
		BulkWriteOperation bulk = stations.initializeUnorderedBulkOperation();

		File file = new File("\\\\192.168.3.107/disk_d/資料備份/new Project/BigData/isd-history.csv");
		int cntLine = 1;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd");
			String line = in.readLine();	// skip first line: header
			while (null != (line = in.readLine())) {
				++cntLine;
				line = line.trim();
				if (0 == line.length()) continue;
				
//				String[] cols = line.split(",");
				String[] cols = parseCSVLine(line);
				String usaf = TextUtil.strip(cols[0], '"');
				String wban = TextUtil.strip(cols[1], '"');
				String id = usaf + "-" + wban;
				
				BasicDBObjectBuilder builder = BasicDBObjectBuilder.start()
						.append("_id", id)
						.append("usaf", usaf)
						.append("wban", wban);
				for (int i = 2, len = STATION_COLUMN.length; i < len; ++i) {
					String val = TextUtil.strip(cols[i], '"').trim();
					if (3 == i) {
						builder.append(STATION_COLUMN[i], val);
						
						appendStationToCountry(db, country_bulk, val, id);
					}
					else if (6 == i) {
						String val2 = TextUtil.strip(cols[++i], '"').trim();
						if (0 == val.length() || 0 == val2.length()) {
							// no geo info
							builder.append("geo", Arrays.asList(-180.0, -180.0));
						}
						else {
							double lng = TextUtil.doubleValue(val2);
							double lat = TextUtil.doubleValue(val);
							if (lng > 180 || lng < -180) lng /= 10;
							if (lat > 180 || lat < -180) lat /= 10;
							builder.append("geo", Arrays.asList(lng, lat));
						}
					}
					else {
						if (val.length() > 0) {
							if (i == 9) {
								String end = TextUtil.strip(cols[++i], '"').trim();
								builder.append("date_range", Arrays.asList(iso.format(sdf.parse(val)), iso.format(sdf.parse(end))));
							}
							else {
								builder.append(STATION_COLUMN[i], val);
							}
						}
					}
				}

				bulk.insert(builder.get());
			}
		} catch(Exception e) {
			System.out.println("line#"+cntLine);
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch(Exception e) {}
				in = null;
			}
		}
		
		BulkWriteResult result = bulk.execute();
		System.out.println(result);
		
		return stations;
	}

	public static final String[]	RECORD_COLUMN	= {
	//   0       1       2               3            4        5      6      7      8    9    10
		"usaf", "wban", "yyyyMMddHHmm", "direction", "speed", "gus", "clg", "skc", "l", "m", "h",
	//   11     12    13    14    15    16    17    18    19    20   21             22      23
		"vsb", "mw", "mw", "mw", "mw", "aw", "aw", "aw", "aw", "w", "temperature", "dewp", "slp",
	//   24     25     26     27     28       29       30       31       32
		"alt", "stp", "max", "min", "pcp01", "pcp06", "pcp24", "pcpxx", "sd"
	};
	public static BulkWriteResult processRecord(DBCollection records, File file, boolean drop_old) {
		String name = file.getName();
		String station = name.substring(0, name.lastIndexOf('-'));
		HashSet<String> ids = new HashSet<String>();
		
		BulkWriteOperation bulk = records.initializeUnorderedBulkOperation();
		BufferedReader in = null;
		String line = null;
		int cntLine = 1;
		try {
			in = new BufferedReader(new InputStreamReader(new com.jcraft.jzlib.GZIPInputStream(new FileInputStream(file))));
			line = in.readLine();	// skip first line: header
			while (null != (line = in.readLine())) {
				++cntLine;
				line = line.trim();
				if (0 == line.length()) continue;
				
				String[] cols = line.split(" +");
				String usaf = cols[0];
				String wban = '*'==cols[1].charAt(0)? "99999" : cols[1];
				String date = cols[2].substring(0, 4) + "-" + cols[2].substring(4, 6) + "-" + cols[2].substring(6, 8);
				String time = cols[2].substring(8, 10) + ":" + cols[2].substring(10, 12) + ":00";
				
				BasicDBObjectBuilder builder = BasicDBObjectBuilder.start()
						.append("station", station)
						.append("date", date)
						.append("time", time);
				String id = station+"+"+date+"+"+time;
				if (ids.contains(id)) continue;
				ids.add(id);
				
				for (int i = 3, len = RECORD_COLUMN.length; i < len; ++i) {
					String val = cols[i];
					if (0 == val.length() || '*' == val.charAt(0)) continue;
					if (12 == i) {
						boolean hasValue = false;
						ArrayList<Integer> mw = new ArrayList<Integer>(4);
						for (; i <= 15; ++i) {
							val = cols[i];
							if (0 == val.length() || '*' == val.charAt(0)) {
								mw.add(null);
							}
							else {
								hasValue = true;
								mw.add(TextUtil.intValue(val));
							}
						}
						if (hasValue) builder.append(RECORD_COLUMN[--i], mw);
					}
					else if (16 == i) {
						boolean hasValue = false;
						ArrayList<Integer> aw = new ArrayList<Integer>(4);
						for (; i <= 19; ++i) {
							val = cols[i];
							if (0 == val.length() || '*' == val.charAt(0)) {
								aw.add(null);
							}
							else {
								hasValue = true;
								aw.add(TextUtil.intValue(val));
							}
						}
						if (hasValue) builder.append(RECORD_COLUMN[--i], aw);
					}
					else {
						if (val.length() > 0) {
							builder.append(RECORD_COLUMN[i], val);
						}
					}
				}

				bulk.insert(builder.get());
			}
		} catch(Exception e) {
			System.out.println("line#"+cntLine+"> "+line);
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch(Exception e) {}
				in = null;
			}
		}

		BulkWriteResult result = null;
		BasicDBObject findStation = new BasicDBObject("station", station);
		long cntRec = records.count(findStation);
		if (drop_old) {
			result = bulk.execute();
		}
		else if (cntRec != ids.size()) {
			// remove records of this station, re-insert
			records.remove(findStation);
			result = bulk.execute();
		}
		return result;
	}
	public static DBCollection createRecordCollectionOfYear(DB db, int year, boolean drop_old) {
		DBCollection records = db.getCollection("record.y"+year);
		if (drop_old) {
			records.drop();

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
		
		File dir = new File("\\\\192.168.3.33/bu-disk-c/weather/ready/"+year+"/csv/");
//		File dir = new File("\\\\192.168.3.33/bu/weather/ready/"+year+"/csv/");
		File[] files = dir.listFiles();
		long tsLoop = System.currentTimeMillis();
		for (int i = 0; i < files.length; ++i) {
			File file = files[i];
			if (file.isDirectory()) continue;

//			System.out.println("["+(1+i)+"/"+files.length+"]process file> "+file.getName());
			long ts = System.currentTimeMillis();
			
			BulkWriteResult result = processRecord(records, file, drop_old);

			if (0 == i % 100) {
				System.out.println("["+(1+i)+"/"+files.length+"] "+DateTime.now()+"> ("+timetext(System.currentTimeMillis()-tsLoop)+") ");
			}
			if (null != result) {
				System.out.println("["+(1+i)+"/"+files.length+"] "+file.getName()+"> ("+(System.currentTimeMillis()-ts)+"ms) "+(null==result? "IGNORE" : result));
			}
		}

		return records;
	}
	
	
	public static String[] parseCSVLine(String line) {
		int idxBegin = 0;
		int idxEnd = 0;
		int len = line.length();
		boolean inQuote = false;
		ArrayList<String> fields = new ArrayList<String>();
		for (int i = 0; i < len; ++i) {
			char c = line.charAt(i);
			switch (c) {
				case '"':
					if (inQuote) {
						inQuote = false;
						idxEnd = i;
						String field = line.substring(idxBegin, idxEnd);
						fields.add(field);
					}
					else {
						inQuote = true;
						idxBegin = i;
					}
					break;
				case ',':
					break;
				default:
			}
		}
		return fields.toArray(new String[0]);
	}
}
