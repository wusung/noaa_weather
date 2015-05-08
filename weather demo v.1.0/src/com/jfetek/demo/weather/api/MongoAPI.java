package com.jfetek.demo.weather.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.jfetek.common.data.Result;
import com.jfetek.common.time.Date;
import com.jfetek.common.time.DateRange;
import com.jfetek.common.time.MonthOfYear;
import com.jfetek.common.util.CompareUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoAPI {
	
	
	public static final List<String>	CountryField	= Collections.unmodifiableList(
		Arrays.asList("_id",	"name")
	);
	
	public static final List<String>	StationField	= Collections.unmodifiableList(
		Arrays.asList(
				"_id", "usaf", "wban", "name", "country", "icao", "geo",
				"date_range"
			)
	);
	
	public static final List<String>	RecordField		= Collections.unmodifiableList(
		Arrays.asList(
//				"_id",
//				 0       1       2            3        4      5
				"date", "time", "direction", "speed", "gus", "clg",
//				 6      7    8    9    10     11    12    13   14
				"skc", "l", "m", "h", "vsb", "mw", "aw", "w", "temperature",
//				 15      16     17     18     19     20     21       22
				"dewp", "slp", "alt", "stp", "max", "min", "pcp01", "pcp06",
//				 23       24       25
				"pcp24", "pcpxx", "sd"
			)
	);	
	
	protected DB db;
	public MongoAPI(DB db) {
		this.db = db;
	}
	
	
//	public Result<HashMap<String,ArrayList<JSONObject>>> listLocation() {
//	}
//	
//	public Result<PairList<String,String>> listContries() {
//		
//	}
	
	public Result<BasicDBObject> queryNearestStationByGeo(double lat, double lng) {
		DBCollection station = db.getCollection("station");
		BasicDBObject near = new BasicDBObject("$near", Arrays.asList(lng, lat));
		BasicDBObject query = new BasicDBObject("geo", near);
		BasicDBObject data = (BasicDBObject) station.findOne(query, new BasicDBObject("_id", 1).append("usaf", 1).append("wban", 1).append("name", 1).append("state", 1).append("date_range", 1).append("geo", 1));
		if (null == data) return Result.failure();
		return Result.wrap(data);
	}


	public Result<ArrayList<ArrayList<Object>>> queryWeatherByStation(int year, String station, DateRange drange, String[] columns) {
		List<String> cols = null==columns||0==columns.length? RecordField : Arrays.asList(columns);
		return queryWeatherByStation(year, station, drange, cols);
	}
	public Result<ArrayList<ArrayList<Object>>> queryWeatherByStation(int year, String station, DateRange drange, List<String> columns) {
		boolean hasRange = (null != drange);
		boolean oneday = (hasRange && 1 == drange.days());
		
		if (!RecordField.containsAll(columns)) {
			return Result.failure("some query column not exists.");
		}
		
//		Result<TupleList> result;
		
		DBCollection record = db.getCollection("record.y"+year);
		if (0 == record.count()) return Result.failure("collection not exists");
		
		BasicDBObjectBuilder query = BasicDBObjectBuilder.start()
				.add("station", station);
		if (hasRange) {
			query.push("date");
			if (oneday) {
				query.add("$eq", drange.first.toString());
			}
			else {
				query
				.add("$gte", drange.first.toString())
				.add("$lte", drange.last.toString());
			}
			query.pop();
		}
		BasicDBObjectBuilder cols = BasicDBObjectBuilder.start();
		cols.add("date", 1).add("time", 1);
		for (int i = 0, len = columns.size(); i < len; ++i) {
			String c = columns.get(i);
			if (!"date".equals(c) && !"time".equals(c)) {
				cols.add(c, 1);
			}
		}

		int len = columns.size();
		ArrayList<ArrayList<Object>> list = new ArrayList<ArrayList<Object>>(len);
		DBCursor c = record.find(query.get(), cols.get());
		for (DBObject o : c) {
			BasicDBObject data = (BasicDBObject) o;
			ArrayList<Object> row = new ArrayList<Object>(len);
			for (int i = 0; i < len; ++i) {
				//row.add(o.get(columns.get(i)));
				String s = data.getString( columns.get(i) );
				if (null == s) {
					row.add( -999 );
				}
				else {
					try {
						Double d = Double.valueOf(s);
						row.add( d );
					} catch(Exception e) {
						row.add( s );
					}
				}
			}
			
			String datetime = data.getString("date") + " " + data.getString("time");
			row.add(datetime);

			list.add(row);
		}
		
		return Result.wrap(list);
	}

	public Result<ArrayList<ArrayList<Object>>> queryDailyWeatherByStation(int year, String station, DateRange drange, String[] columns) {
		List<String> cols = null==columns||0==columns.length? RecordField : Arrays.asList(columns);
		return queryDailyWeatherByStation(year, station, drange, cols);
	}
	public Result<ArrayList<ArrayList<Object>>> queryDailyWeatherByStation(int year, String station, DateRange drange, List<String> columns) {
		boolean hasRange = (null != drange);
		boolean oneday = (hasRange && 1 == drange.days());
		
		if (!RecordField.containsAll(columns)) {
			return Result.failure("some query column not exists.");
		}
		
//		Result<TupleList> result;
		
		DBCollection record = db.getCollection("record.y"+year);
		if (0 == record.count()) return Result.failure("collection not exists");
		
		BasicDBObjectBuilder query = BasicDBObjectBuilder.start()
				.add("station", station);
		if (hasRange) {
			query.push("date");
			if (oneday) {
				query.add("$eq", drange.first.toString());
			}
			else {
				query
				.add("$gte", drange.first.toString())
				.add("$lte", drange.last.toString());
			}
			query.pop();
		}
		BasicDBObjectBuilder cols = BasicDBObjectBuilder.start();
		cols.add("date", 1).add("time", 1);
		for (int i = 0, len = columns.size(); i < len; ++i) {
			String c = columns.get(i);
			if (!"date".equals(c) && !"time".equals(c)) {
				cols.add(c, 1);
			}
		}

		int len = columns.size();
		TreeMap<DailyGroupKey, HashMap<String,Object>> cache = new TreeMap<DailyGroupKey, HashMap<String,Object>>();
		DBCursor c = record.find(query.get(), cols.get());
		for (DBObject o : c) {
			BasicDBObject data = (BasicDBObject) o;
			
			String date = data.getString("date");
			DailyGroupKey key = new DailyGroupKey(date, station);

			HashMap<String,Object> m = cache.get(key);
			if (null == m) {
				m = new HashMap<String,Object>();
				cache.put(key, m);
			}

			for (int i = 0; i < len; ++i) {
				//row.add(o.get(columns.get(i)));
				String col = columns.get(i);
				String s = data.getString( col );
				if (null == s) {
				}
				else {
					try {
						Double d = Double.valueOf(s);
						
						// if numeric...
						DataSummary dsum = (DataSummary) m.get(col);
						if (null == dsum) {
							dsum = new DataSummary();
							m.put(col, dsum);
						}
						dsum.add(d);
						
					} catch(Exception e) {
						m.put(col, s);
					}
				}
			}
		}
		
		
		
		ArrayList<ArrayList<Object>> list = new ArrayList<ArrayList<Object>>(len);
		for (Entry<DailyGroupKey, HashMap<String,Object>> e : cache.entrySet()) {
			DailyGroupKey key = e.getKey();
			HashMap<String,Object> data = e.getValue();
			
			String date = data.get("date").toString();
			
			ArrayList<Object> row = new ArrayList<Object>(len);
			for (int i = 0; i < len; ++i) {
				String col = columns.get(i);
				if ("time".equals(col)) continue;
				Object obj = data.get( col );
				if (null == obj) {
					row.add( -999.0 );
					row.add( -999.0 );
					row.add( -999.0 );
					row.add( -999.0 );
				}
				else if (obj instanceof DataSummary) {
					DataSummary dsum = (DataSummary) obj;
					row.add( dsum.getMin() );
					row.add( dsum.getMax() );
					row.add( dsum.getAvg() );
					row.add( dsum.getSum() );
				}
				else {
					row.add( obj.toString() );
				}
			}
			
			String datetime = date + " 00:00:00";
			row.add(datetime);

			list.add(row);
		}
		
		return Result.wrap(list);
	}

	static class DailyGroupKey implements Comparable<DailyGroupKey> {
		final String date;
		final String station;
		public DailyGroupKey(String date, String station) {
			this.date = date;
			this.station = station;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (null == obj) return false;
			if (!(obj instanceof DailyGroupKey)) return false;
			DailyGroupKey ano = (DailyGroupKey) obj;
			
			if (!CompareUtil.isEqual(this.date, ano.date)) return false;
			if (!CompareUtil.isEqual(this.station, ano.station)) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			return (int) (1003 * this.date.hashCode() + 107 * this.station.hashCode() + 200397);
		}

		@Override
		public int compareTo(DailyGroupKey ano) {
			int off = this.date.compareTo( ano.date );
			if (0 == off) {
				off = this.station.compareTo( ano.station );
			}
			return off;
		}
	}

	public Result<ArrayList<ArrayList<Object>>> queryWeeklyWeatherByStation(int year, String station, DateRange drange, String[] columns) {
		List<String> cols = null==columns||0==columns.length? RecordField : Arrays.asList(columns);
		return queryWeeklyWeatherByStation(year, station, drange, cols);
	}
	public Result<ArrayList<ArrayList<Object>>> queryWeeklyWeatherByStation(int year, String station, DateRange drange, List<String> columns) {
		boolean hasRange = (null != drange);
		boolean oneday = (hasRange && 1 == drange.days());
		
		if (!RecordField.containsAll(columns)) {
			return Result.failure("some query column not exists.");
		}
		
//		Result<TupleList> result;
		
		DBCollection record = db.getCollection("record.y"+year);
		if (0 == record.count()) return Result.failure("collection not exists");
		
		BasicDBObjectBuilder query = BasicDBObjectBuilder.start()
				.add("station", station);
		if (hasRange) {
			query.push("date");
			if (oneday) {
				query.add("$eq", drange.first.toString());
			}
			else {
				query
				.add("$gte", drange.first.toString())
				.add("$lte", drange.last.toString());
			}
			query.pop();
		}
		BasicDBObjectBuilder cols = BasicDBObjectBuilder.start();
		cols.add("date", 1).add("time", 1);
		for (int i = 0, len = columns.size(); i < len; ++i) {
			String c = columns.get(i);
			if (!"date".equals(c) && !"time".equals(c)) {
				cols.add(c, 1);
			}
		}

		int len = columns.size();
		TreeMap<WeeklyGroupKey, HashMap<String,Object>> cache = new TreeMap<WeeklyGroupKey, HashMap<String,Object>>();
		DBCursor c = record.find(query.get(), cols.get());
		for (DBObject o : c) {
			BasicDBObject data = (BasicDBObject) o;
			
			String date = data.getString("date");
			WeeklyGroupKey key = new WeeklyGroupKey(date, station);

			HashMap<String,Object> m = cache.get(key);
			if (null == m) {
				m = new HashMap<String,Object>();
				cache.put(key, m);
			}

			for (int i = 0; i < len; ++i) {
				//row.add(o.get(columns.get(i)));
				String col = columns.get(i);
				String s = data.getString( col );
				if (null == s) {
				}
				else {
					try {
						Double d = Double.valueOf(s);
						
						// if numeric...
						DataSummary dsum = (DataSummary) m.get(col);
						if (null == dsum) {
							dsum = new DataSummary();
							m.put(col, dsum);
						}
						dsum.add(d);
						
					} catch(Exception e) {
						m.put(col, s);
					}
				}
			}
		}
		
		
		
		ArrayList<ArrayList<Object>> list = new ArrayList<ArrayList<Object>>(len);
		for (Entry<WeeklyGroupKey, HashMap<String,Object>> e : cache.entrySet()) {
			WeeklyGroupKey key = e.getKey();
			HashMap<String,Object> data = e.getValue();
			
			String date = key.sunday.toString();
			
			ArrayList<Object> row = new ArrayList<Object>(len);
			for (int i = 0; i < len; ++i) {
				String col = columns.get(i);
				if ("time".equals(col)) continue;
				if ("date".equals(col)) {
					row.add( date );
				}
				else {
					Object obj = data.get( col );
					if (null == obj) {
						row.add( -999.0 );
						row.add( -999.0 );
						row.add( -999.0 );
						row.add( -999.0 );
					}
					else if (obj instanceof DataSummary) {
						DataSummary dsum = (DataSummary) obj;
						row.add( dsum.getMin() );
						row.add( dsum.getMax() );
						row.add( dsum.getAvg() );
						row.add( dsum.getSum() );
					}
					else {
						row.add( obj.toString() );
					}
				}
			}
			
			String datetime = date + " 00:00:00";
			row.add(datetime);

			list.add(row);
		}
		
		return Result.wrap(list);
	}

	static class WeeklyGroupKey implements Comparable<WeeklyGroupKey> {
		final Date sunday;
		final String station;
		protected WeeklyGroupKey(Date date, String station) {
			Calendar cal = new GregorianCalendar();
			cal.setTimeInMillis(date.timestamp);
			cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			this.sunday = new Date(cal.getTime());
			this.station = station;
		}
		public WeeklyGroupKey(String date, String station) {
			this(Date.valueOf(date), station);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (null == obj) return false;
			if (!(obj instanceof WeeklyGroupKey)) return false;
			WeeklyGroupKey ano = (WeeklyGroupKey) obj;
			
			if (!CompareUtil.isEqual(this.sunday, ano.sunday)) return false;
			if (!CompareUtil.isEqual(this.station, ano.station)) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			return (int) (1003 * this.sunday.timestamp + 107 * this.station.hashCode() + 200397);
		}

		@Override
		public int compareTo(WeeklyGroupKey ano) {
			int off = this.sunday.compareTo( ano.sunday );
			if (0 == off) {
				off = this.station.compareTo( ano.station );
			}
			return off;
		}
	}

	public Result<ArrayList<ArrayList<Object>>> queryMonthlyWeatherByStation(int year, String station, DateRange drange, String[] columns) {
		List<String> cols = null==columns||0==columns.length? RecordField : Arrays.asList(columns);
		return queryMonthlyWeatherByStation(year, station, drange, cols);
	}
	public Result<ArrayList<ArrayList<Object>>> queryMonthlyWeatherByStation(int year, String station, DateRange drange, List<String> columns) {
		boolean hasRange = (null != drange);
		boolean oneday = (hasRange && 1 == drange.days());
		
		if (!RecordField.containsAll(columns)) {
			return Result.failure("some query column not exists.");
		}
		
//		Result<TupleList> result;
		
		DBCollection record = db.getCollection("record.y"+year);
		if (0 == record.count()) return Result.failure("collection not exists");
		
		BasicDBObjectBuilder query = BasicDBObjectBuilder.start()
				.add("station", station);
		if (hasRange) {
			query.push("date");
			if (oneday) {
				query.add("$eq", drange.first.toString());
			}
			else {
				query
				.add("$gte", drange.first.toString())
				.add("$lte", drange.last.toString());
			}
			query.pop();
		}
		BasicDBObjectBuilder cols = BasicDBObjectBuilder.start();
		cols.add("date", 1).add("time", 1);
		for (int i = 0, len = columns.size(); i < len; ++i) {
			String c = columns.get(i);
			if (!"date".equals(c) && !"time".equals(c)) {
				cols.add(c, 1);
			}
		}

		int len = columns.size();
		TreeMap<MonthlyGroupKey, HashMap<String,Object>> cache = new TreeMap<MonthlyGroupKey, HashMap<String,Object>>();
		DBCursor c = record.find(query.get(), cols.get());
		for (DBObject o : c) {
			BasicDBObject data = (BasicDBObject) o;
			
			String date = data.getString("date");
			MonthlyGroupKey key = new MonthlyGroupKey(date, station);

			HashMap<String,Object> m = cache.get(key);
			if (null == m) {
				m = new HashMap<String,Object>();
				cache.put(key, m);
			}

			for (int i = 0; i < len; ++i) {
				//row.add(o.get(columns.get(i)));
				String col = columns.get(i);
				String s = data.getString( col );
				if (null == s) {
				}
				else {
					try {
						Double d = Double.valueOf(s);
						
						// if numeric...
						DataSummary dsum = (DataSummary) m.get(col);
						if (null == dsum) {
							dsum = new DataSummary();
							m.put(col, dsum);
						}
						dsum.add(d);
						
					} catch(Exception e) {
						m.put(col, s);
					}
				}
			}
		}
		
		
		
		ArrayList<ArrayList<Object>> list = new ArrayList<ArrayList<Object>>(len);
		for (Entry<MonthlyGroupKey, HashMap<String,Object>> e : cache.entrySet()) {
			MonthlyGroupKey key = e.getKey();
			HashMap<String,Object> data = e.getValue();
			
			String date = key.moy.firstDay().toString();
			
			ArrayList<Object> row = new ArrayList<Object>(len);
			for (int i = 0; i < len; ++i) {
				String col = columns.get(i);
				if ("time".equals(col)) continue;
				if ("date".equals(col)) {
					row.add( date );
				}
				else {
					Object obj = data.get( col );
					if (null == obj) {
						row.add( -999.0 );
						row.add( -999.0 );
						row.add( -999.0 );
						row.add( -999.0 );
					}
					else if (obj instanceof DataSummary) {
						DataSummary dsum = (DataSummary) obj;
						row.add( dsum.getMin() );
						row.add( dsum.getMax() );
						row.add( dsum.getAvg() );
						row.add( dsum.getSum() );
					}
					else {
						row.add( obj.toString() );
					}
				}
			}
			
			String datetime = date + " 00:00:00";
			row.add(datetime);

			list.add(row);
		}
		
		return Result.wrap(list);
	}

	static class MonthlyGroupKey implements Comparable<MonthlyGroupKey> {
		final MonthOfYear moy;
		final String station;
		public MonthlyGroupKey(MonthOfYear moy, String station) {
			this.moy = moy;
			this.station = station;
		}
		public MonthlyGroupKey(Date date, String station) {
			this.moy = date.getMonthOfYear();
			this.station = station;
		}
		public MonthlyGroupKey(String date, String station) {
			this(Date.valueOf(date), station);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (null == obj) return false;
			if (!(obj instanceof MonthlyGroupKey)) return false;
			MonthlyGroupKey ano = (MonthlyGroupKey) obj;
			
			if (!CompareUtil.isEqual(this.moy, ano.moy)) return false;
			if (!CompareUtil.isEqual(this.station, ano.station)) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			return (int) (1003 * this.moy.hashCode() + 107 * this.station.hashCode() + 200397);
		}

		@Override
		public int compareTo(MonthlyGroupKey ano) {
			int off = this.moy.compareTo( ano.moy );
			if (0 == off) {
				off = this.station.compareTo( ano.station );
			}
			return off;
		}
	}

	static class DataSummary {
		private final ArrayList<Double> list;
		private double min;
		private double max;
		public DataSummary() {
			this.list = new ArrayList<Double>();
			this.min = Double.MAX_VALUE;
			this.max = Double.MIN_VALUE;
		}
		
		public DataSummary add(double data) {
			list.add(data);
			if (data > max) max = data;
			if (data < min) min = data;
			return this;
		}
		
		public double getSum() {
			double sum = 0.0;
			for (int i = 0, len = list.size(); i < len; ++i) {
				sum += list.get(i);
			}
			return sum;
		}
		
		public double getAvg() {
			int len = this.list.size();
			return 0==len? 0.0 : getSum() / this.list.size();
		}
		
		public double getCount() {
			return this.list.size();
		}
		
		public double getMax() {
			int len = this.list.size();
			return 0==len? -999.0 : this.max;
		}
		
		public double getMin() {
			int len = this.list.size();
			return 0==len? -999.0 : this.min;
		}
	}
}
