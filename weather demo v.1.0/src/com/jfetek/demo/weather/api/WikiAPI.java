package com.jfetek.demo.weather.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.TreeMap;

import com.jfetek.common.data.Result;
import com.jfetek.common.time.Date;
import com.jfetek.common.time.DateRange;
import com.jfetek.common.time.DateTime;
import com.jfetek.common.time.MonthOfYear;
import com.jfetek.common.util.CompareUtil;
import com.jfetek.common.util.TextUtil;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class WikiAPI {

	protected DB db;
	public WikiAPI(DB db) {
		this.db = db;
	}
	
//	private static final DBObject	PROJECT	= BasicDBObjectBuilder.start()
//												.push("$project")
//													.add("_id", 0)
//													.add("date", "$_id.date")
//													.add("hour", "$_id.hour")
//													.add("key", "$_id.key")
//													.add("count", 1)
//													.add("language", 1)
//													.add("titles", 1)
//												.pop().get();
	
	private static final DBObject	SORT	= BasicDBObjectBuilder.start()
												.push("$sort").add("dayhour", 1).pop()
												.get();
	
	private DBCollection getRecordCollection(MonthOfYear moy) {
		DBCollection record = db.getCollection("record.of"+moy.year.toText()+moy.month.toText());
		return 0==record.count()? null : record;
	}
	
	private DBCollection getRecordCollection(Date date) {
		return getRecordCollection(date.getMonthOfYear());
	}
	
//	public Result<HashMap<String,Integer>> querySpiderProgress() {
//		DBCollection taskrec = Console.
//	}
	
	private void packResult(MonthOfYear moy, AggregationOutput output, ArrayList<Long> lstTime, ArrayList<ArrayList<Object>> lstData) {
		for (DBObject d : output.results()) {
			BasicDBObject data = (BasicDBObject) d;
			DateTime dt = DateTime.valueOf(moy.toString()+"-"+data.getString("dayhour")+":00:00");
			lstTime.add(dt.timestamp);
			
			ArrayList<Object> arr = new ArrayList<Object>(4);
			lstData.add(arr);
			arr.add(moy.toString()+"-"+data.getString("dayhour")+":00:00");
			arr.add(data.getInt("count"));
			arr.add(data.get("language"));
			arr.add(data.get("title"));
		}
	}

	
	static class WeekGroupKey implements Comparable<WeekGroupKey> {
		final Date date;
		final String lang;
		final String title;
		public WeekGroupKey(Date date, String lang, String title) {
			this.date = date;
			this.lang = lang;
			this.title = title;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (null == obj) return false;
			if (!(obj instanceof WeekGroupKey)) return false;
			WeekGroupKey ano = (WeekGroupKey) obj;
			
			if (!CompareUtil.isEqual(this.date, ano.date)) return false;
			if (!CompareUtil.isEqual(this.lang, ano.lang)) return false;
			if (!CompareUtil.isEqual(this.title, ano.title)) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			return (int) (1003 * this.date.timestamp + 107 * this.lang.hashCode() + 200397 * this.title.hashCode());
		}

		@Override
		public int compareTo(WeekGroupKey ano) {
			int off = (int) (this.date.timestamp - ano.date.timestamp);
			if (0 == off) {
				off = this.lang.compareTo( ano.lang );
				if (0 == off) {
					off = this.title.compareTo( ano.title );
				}
			}
			return off;
		}
	}
	
	private void collectWeekResult(MonthOfYear moy, AggregationOutput output, TreeMap<WeekGroupKey, BasicDBObject> cache) {
		for (DBObject d : output.results()) {
			BasicDBObject data = (BasicDBObject) d;
			DateTime dt = DateTime.valueOf(moy.toString()+"-"+data.getString("dayhour")+":00:00");
			Calendar cal = new GregorianCalendar();
			cal.setTimeInMillis(dt.timestamp);
			cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			Date sunday = new Date(cal.getTime());
			
			String lang = data.getString("language");
			String title = data.getString("title");
			WeekGroupKey key = new WeekGroupKey(sunday, lang, title);
			
			BasicDBObject wdata = cache.get(key);
			if (null == wdata) {
				cache.put(key, data);
			}
			else {
				int cntOld = wdata.getInt("count");
				int cntNew = data.getInt("count");
				wdata.put("count", cntOld+cntNew);
			}
		}
	}
	

	private void packWeekResult(TreeMap<WeekGroupKey, BasicDBObject> cache, ArrayList<Long> lstTime, ArrayList<ArrayList<Object>> lstData) {
		for (Entry<WeekGroupKey, BasicDBObject> e : cache.entrySet()) {
			WeekGroupKey key = e.getKey();
			BasicDBObject data = e.getValue();

			lstTime.add(key.date.timestamp);
			ArrayList<Object> arr = new ArrayList<Object>(4);
			lstData.add(arr);
			arr.add(key.date.toString()+ " 00:00:00");
			arr.add(data.getInt("count"));
			arr.add(data.get("language"));
			arr.add(data.get("title"));
		}
	}

	
//	public Result<ArrayList<DBObject>> queryHourlyWiki(String title, DateRange drange) {
	public Result<HashMap<String,List<?>>> queryHourlyWiki(String title, DateRange drange) {
//		if (null == drange) return queryWiki(title);
		if (TextUtil.noValueOrBlank(title)) return Result.failure("null or empty title");
		if (null == drange) return Result.failure("date-range require");

		String key = title.trim().toLowerCase();
		boolean hasRange = (null != drange);
		boolean oneday = (hasRange && 1 == drange.days());

		DBObject group = BasicDBObjectBuilder.start()
				.push("$group")
					.push("_id")
						.add("key", "$key")
						.add("dayhour", "$dayhour")
						.push("language")
							.add("$concat", Arrays.asList("$lang", "$proj"))
						.pop()
						.add("title", "$title")
					.pop()
					.push("count").add("$sum", "$count").pop()
//					.push("language")
//						.push("$addToSet")
//							.add("$concat", new String[]{"$lang", "$proj"})
//						.pop()
//					.pop()
//					.push("titles").add("$addToSet", "$title").pop()
//					.push("language")
//						.add("$concat", Arrays.asList("$lang", "$proj"))
//					.pop()
//					.add("title", "$title")
				.pop()
			.get();
		
		DBObject project = BasicDBObjectBuilder.start()
				.push("$project")
					.add("_id", 0)
					.add("key", "$_id.key")
					.add("dayhour", "$_id.dayhour")
					.add("count", 1)
					.add("language", "$_id.language")
					.add("title", "$_id.title")
				.pop()
			.get();


//		ArrayList<DBObject> list = new ArrayList<DBObject>();
		HashMap<String,List<?>> map = new HashMap<String,List<?>>(4);
		map.put("columns", Arrays.asList("datetime", "count", "language", "title"));
		ArrayList<Long> lstTime = new ArrayList<Long>();
		map.put("index", lstTime);
		ArrayList<ArrayList<Object>> lstData = new ArrayList<ArrayList<Object>>();
		map.put("data", lstData);
		MonthOfYear[] moys = drange.getMonthOfYearArray();
		int len = moys.length;
		
		if (1 == len) {
			DBCollection record = getRecordCollection(moys[0]);
			if (null != record) {
				String begin = drange.first.toString().substring(8, 10);
				String end = drange.last.toString().substring(8, 10);
				DBObject match = BasicDBObjectBuilder.start()
						.push("$match")
							.add("key", key)
							.push("dayhour")
								.add("$gte", begin+" 00")
								.add("$lte", end+" 23")
							.pop()
						.pop()
					.get();
				
				AggregationOutput output = record.aggregate(Arrays.asList(
						match,
						group,
						project,
						SORT
					));
				
				packResult(moys[0], output, lstTime, lstData);
			}
		}
		else if (len >= 2) {
			// 1st month
			MonthOfYear moy = moys[0];
			DBCollection record = getRecordCollection(moy);
			if (null != record) {
				String day = drange.first.toString().substring(8, 10);
				DBObject match = BasicDBObjectBuilder.start()
						.push("$match")
							.add("key", key)
							.push("dayhour")
								.add("$gte", day+" 00")
							.pop()
						.pop()
					.get();

				AggregationOutput output = record.aggregate(Arrays.asList(
						match,
						group,
						project,
						SORT
					));

				packResult(moy, output, lstTime, lstData);
			}
			
			for (int i = 1; i < len-1; ++i) {
				moy = moys[i];
				record = getRecordCollection(moy);
				if (null != record) {
					String day = drange.last.toString().substring(8, 10);
					DBObject match = BasicDBObjectBuilder.start()
							.push("$match")
								.add("key", key)
							.pop()
						.get();

					AggregationOutput output = record.aggregate(Arrays.asList(
							match,
							group,
							project,
							SORT
						));

					packResult(moy, output, lstTime, lstData);
				}
			}
			
			// last month
			moy = moys[len-1];
			record = getRecordCollection(moy);
			if (null != record) {
				String day = drange.last.toString().substring(8, 10);
				DBObject match = BasicDBObjectBuilder.start()
						.push("$match")
							.add("key", key)
							.push("dayhour")
								.add("$lte", day+" 23")
							.pop()
						.pop()
					.get();

				AggregationOutput output = record.aggregate(Arrays.asList(
						match,
						group,
						project,
						SORT
					));
				
				packResult(moy, output, lstTime, lstData);
			}
		}
		
		return Result.wrap(map);
	}

	public Result<HashMap<String,List<?>>> queryDailyWiki(String title, DateRange drange) {
//		if (null == drange) return queryWiki(title);
		if (TextUtil.noValueOrBlank(title)) return Result.failure("null or empty title");
		if (null == drange) return Result.failure("date-range require");

		String key = title.trim().toLowerCase();
		boolean hasRange = (null != drange);
		boolean oneday = (hasRange && 1 == drange.days());

		DBObject group = BasicDBObjectBuilder.start()
				.push("$group")
					.push("_id")
						.add("key", "$key")
						.push("day")
							.add("$substr", Arrays.asList("$dayhour", 0, 2))
						.pop()
						.push("language")
							.add("$concat", Arrays.asList("$lang", "$proj"))
						.pop()
						.add("title", "$title")
					.pop()
					.push("count").add("$sum", "$count").pop()
//					.push("language")
//						.push("$addToSet")
//							.add("$concat", new String[]{"$lang", "$proj"})
//						.pop()
//					.pop()
//					.push("titles").add("$addToSet", "$title").pop()
//					.push("language")
//						.add("$concat", Arrays.asList("$lang", "$proj"))
//					.pop()
//					.add("title", "$title")
				.pop()
			.get();
		
		DBObject project = BasicDBObjectBuilder.start()
				.push("$project")
					.add("_id", 0)
					.add("key", "$_id.key")
					.add("day", "$_id.day")
					.push("dayhour")
						.add("$concat", Arrays.asList("$_id.day", " 00"))
					.pop()
					.add("count", 1)
					.add("language", "$_id.language")
					.add("title", "$_id.title")
				.pop()
			.get();


//		ArrayList<DBObject> list = new ArrayList<DBObject>();
		HashMap<String,List<?>> map = new HashMap<String,List<?>>(4);
		map.put("columns", Arrays.asList("datetime", "count", "language", "title"));
		ArrayList<Long> lstTime = new ArrayList<Long>();
		map.put("index", lstTime);
		ArrayList<ArrayList<Object>> lstData = new ArrayList<ArrayList<Object>>();
		map.put("data", lstData);
		MonthOfYear[] moys = drange.getMonthOfYearArray();
		int len = moys.length;
		
		if (1 == len) {
			DBCollection record = getRecordCollection(moys[0]);
			if (null != record) {
				String begin = drange.first.toString().substring(8, 10);
				String end = drange.last.toString().substring(8, 10);
				DBObject match = BasicDBObjectBuilder.start()
						.push("$match")
							.add("key", key)
							.push("dayhour")
								.add("$gte", begin+" 00")
								.add("$lte", end+" 23")
							.pop()
						.pop()
					.get();
				
				AggregationOutput output = record.aggregate(Arrays.asList(
						match,
						group,
						project,
						SORT
					));
				
				packResult(moys[0], output, lstTime, lstData);
			}
		}
		else if (len >= 2) {
			// 1st month
			MonthOfYear moy = moys[0];
			DBCollection record = getRecordCollection(moy);
			if (null != record) {
				String day = drange.first.toString().substring(8, 10);
				DBObject match = BasicDBObjectBuilder.start()
						.push("$match")
							.add("key", key)
							.push("dayhour")
								.add("$gte", day+" 00")
							.pop()
						.pop()
					.get();

				AggregationOutput output = record.aggregate(Arrays.asList(
						match,
						group,
						project,
						SORT
					));

				packResult(moy, output, lstTime, lstData);
			}
			
			for (int i = 1; i < len-1; ++i) {
				moy = moys[i];
				record = getRecordCollection(moy);
				if (null != record) {
					String day = drange.last.toString().substring(8, 10);
					DBObject match = BasicDBObjectBuilder.start()
							.push("$match")
								.add("key", key)
							.pop()
						.get();

					AggregationOutput output = record.aggregate(Arrays.asList(
							match,
							group,
							project,
							SORT
						));

					packResult(moy, output, lstTime, lstData);
				}
			}
			
			// last month
			moy = moys[len-1];
			record = getRecordCollection(moy);
			if (null != record) {
				String day = drange.last.toString().substring(8, 10);
				DBObject match = BasicDBObjectBuilder.start()
						.push("$match")
							.add("key", key)
							.push("dayhour")
								.add("$lte", day+" 23")
							.pop()
						.pop()
					.get();

				AggregationOutput output = record.aggregate(Arrays.asList(
						match,
						group,
						project,
						SORT
					));
				
				packResult(moy, output, lstTime, lstData);
			}
		}
		
		return Result.wrap(map);
	}

	public Result<HashMap<String,List<?>>> queryWeeklyWiki(String title, DateRange drange) {
//		if (null == drange) return queryWiki(title);
		if (TextUtil.noValueOrBlank(title)) return Result.failure("null or empty title");
		if (null == drange) return Result.failure("date-range require");

		String key = title.trim().toLowerCase();
		boolean hasRange = (null != drange);
		boolean oneday = (hasRange && 1 == drange.days());

		DBObject group = BasicDBObjectBuilder.start()
				.push("$group")
					.push("_id")
						.add("key", "$key")
						.push("day")
							.add("$substr", Arrays.asList("$dayhour", 0, 2))
						.pop()
						.push("language")
							.add("$concat", Arrays.asList("$lang", "$proj"))
						.pop()
						.add("title", "$title")
					.pop()
					.push("count").add("$sum", "$count").pop()
//					.push("language")
//						.push("$addToSet")
//							.add("$concat", new String[]{"$lang", "$proj"})
//						.pop()
//					.pop()
//					.push("titles").add("$addToSet", "$title").pop()
//					.push("language")
//						.add("$concat", Arrays.asList("$lang", "$proj"))
//					.pop()
//					.add("title", "$title")
				.pop()
			.get();
		
		DBObject project = BasicDBObjectBuilder.start()
				.push("$project")
					.add("_id", 0)
					.add("key", "$_id.key")
					.add("day", "$_id.day")
					.push("dayhour")
						.add("$concat", Arrays.asList("$_id.day", " 00"))
					.pop()
					.add("count", 1)
					.add("language", "$_id.language")
					.add("title", "$_id.title")
				.pop()
			.get();


//		ArrayList<DBObject> list = new ArrayList<DBObject>();
		TreeMap<WeekGroupKey, BasicDBObject> cache = new TreeMap<WeekGroupKey, BasicDBObject>();
		HashMap<String,List<?>> map = new HashMap<String,List<?>>(4);
		map.put("columns", Arrays.asList("datetime", "count", "language", "title"));
		ArrayList<Long> lstTime = new ArrayList<Long>();
		map.put("index", lstTime);
		ArrayList<ArrayList<Object>> lstData = new ArrayList<ArrayList<Object>>();
		map.put("data", lstData);
		MonthOfYear[] moys = drange.getMonthOfYearArray();
		int len = moys.length;
		
		if (1 == len) {
			DBCollection record = getRecordCollection(moys[0]);
			if (null != record) {
				String begin = drange.first.toString().substring(8, 10);
				String end = drange.last.toString().substring(8, 10);
				DBObject match = BasicDBObjectBuilder.start()
						.push("$match")
							.add("key", key)
							.push("dayhour")
								.add("$gte", begin+" 00")
								.add("$lte", end+" 23")
							.pop()
						.pop()
					.get();
				
				AggregationOutput output = record.aggregate(Arrays.asList(
						match,
						group,
						project,
						SORT
					));

				collectWeekResult(moys[0], output, cache);
			}
		}
		else if (len >= 2) {
			// 1st month
			MonthOfYear moy = moys[0];
			DBCollection record = getRecordCollection(moy);
			if (null != record) {
				String day = drange.first.toString().substring(8, 10);
				DBObject match = BasicDBObjectBuilder.start()
						.push("$match")
							.add("key", key)
							.push("dayhour")
								.add("$gte", day+" 00")
							.pop()
						.pop()
					.get();

				AggregationOutput output = record.aggregate(Arrays.asList(
						match,
						group,
						project,
						SORT
					));

				collectWeekResult(moy, output, cache);
			}
			
			for (int i = 1; i < len-1; ++i) {
				moy = moys[i];
				record = getRecordCollection(moy);
				if (null != record) {
					String day = drange.last.toString().substring(8, 10);
					DBObject match = BasicDBObjectBuilder.start()
							.push("$match")
								.add("key", key)
							.pop()
						.get();

					AggregationOutput output = record.aggregate(Arrays.asList(
							match,
							group,
							project,
							SORT
						));

					collectWeekResult(moy, output, cache);
				}
			}
			
			// last month
			moy = moys[len-1];
			record = getRecordCollection(moy);
			if (null != record) {
				String day = drange.last.toString().substring(8, 10);
				DBObject match = BasicDBObjectBuilder.start()
						.push("$match")
							.add("key", key)
							.push("dayhour")
								.add("$lte", day+" 23")
							.pop()
						.pop()
					.get();

				AggregationOutput output = record.aggregate(Arrays.asList(
						match,
						group,
						project,
						SORT
					));
				
				collectWeekResult(moy, output, cache);
			}
		}
		
		packWeekResult(cache, lstTime, lstData);
		
		return Result.wrap(map);
	}

	public Result<HashMap<String,List<?>>> queryMonthlyWiki(String title, DateRange drange) {
//		if (null == drange) return queryWiki(title);
		if (TextUtil.noValueOrBlank(title)) return Result.failure("null or empty title");
		if (null == drange) return Result.failure("date-range require");

		String key = title.trim().toLowerCase();
		boolean hasRange = (null != drange);
		boolean oneday = (hasRange && 1 == drange.days());

		DBObject group = BasicDBObjectBuilder.start()
				.push("$group")
					.push("_id")
						.add("key", "$key")
						.push("language")
							.add("$concat", Arrays.asList("$lang", "$proj"))
						.pop()
						.add("title", "$title")
					.pop()
					.push("count").add("$sum", "$count").pop()
//					.push("language")
//						.push("$addToSet")
//							.add("$concat", new String[]{"$lang", "$proj"})
//						.pop()
//					.pop()
//					.push("titles").add("$addToSet", "$title").pop()
//					.push("language")
//						.add("$concat", Arrays.asList("$lang", "$proj"))
//					.pop()
//					.add("title", "$title")
				.pop()
			.get();
		
		DBObject project = BasicDBObjectBuilder.start()
				.push("$project")
					.add("_id", 0)
					.add("key", "$_id.key")
					.push("dayhour")
						.add("$concat", Arrays.asList("01", " 00"))
					.pop()
					.add("count", 1)
					.add("language", "$_id.language")
					.add("title", "$_id.title")
				.pop()
			.get();


//		ArrayList<DBObject> list = new ArrayList<DBObject>();
		HashMap<String,List<?>> map = new HashMap<String,List<?>>(4);
		map.put("columns", Arrays.asList("datetime", "count", "language", "title"));
		ArrayList<Long> lstTime = new ArrayList<Long>();
		map.put("index", lstTime);
		ArrayList<ArrayList<Object>> lstData = new ArrayList<ArrayList<Object>>();
		map.put("data", lstData);
		MonthOfYear[] moys = drange.getMonthOfYearArray();
		int len = moys.length;
		
		if (1 == len) {
			DBCollection record = getRecordCollection(moys[0]);
			if (null != record) {
				String begin = drange.first.toString().substring(8, 10);
				String end = drange.last.toString().substring(8, 10);
				DBObject match = BasicDBObjectBuilder.start()
						.push("$match")
							.add("key", key)
							.push("dayhour")
								.add("$gte", begin+" 00")
								.add("$lte", end+" 23")
							.pop()
						.pop()
					.get();
				
				AggregationOutput output = record.aggregate(Arrays.asList(
						match,
						group,
						project,
						SORT
					));
				
				packResult(moys[0], output, lstTime, lstData);
			}
		}
		else if (len >= 2) {
			// 1st month
			MonthOfYear moy = moys[0];
			DBCollection record = getRecordCollection(moy);
			if (null != record) {
				String day = drange.first.toString().substring(8, 10);
				DBObject match = BasicDBObjectBuilder.start()
						.push("$match")
							.add("key", key)
							.push("dayhour")
								.add("$gte", day+" 00")
							.pop()
						.pop()
					.get();

				AggregationOutput output = record.aggregate(Arrays.asList(
						match,
						group,
						project,
						SORT
					));

				packResult(moy, output, lstTime, lstData);
			}
			
			for (int i = 1; i < len-1; ++i) {
				moy = moys[i];
				record = getRecordCollection(moy);
				if (null != record) {
					DBObject match = BasicDBObjectBuilder.start()
							.push("$match")
								.add("key", key)
							.pop()
						.get();

					AggregationOutput output = record.aggregate(Arrays.asList(
							match,
							group,
							project,
							SORT
						));

					packResult(moy, output, lstTime, lstData);
				}
			}
			
			// last month
			moy = moys[len-1];
			record = getRecordCollection(moy);
			if (null != record) {
				String day = drange.last.toString().substring(8, 10);
				DBObject match = BasicDBObjectBuilder.start()
						.push("$match")
							.add("key", key)
							.push("dayhour")
								.add("$lte", day+" 23")
							.pop()
						.pop()
					.get();

				AggregationOutput output = record.aggregate(Arrays.asList(
						match,
						group,
						project,
						SORT
					));
				
				packResult(moy, output, lstTime, lstData);
			}
		}
		
		return Result.wrap(map);
	}

//	public Result<ArrayList<DBObject>> queryWeeklyWiki(String title, DateRange drange) {
////		if (null == drange) return queryWiki(title);
//		if (TextUtil.noValueOrBlank(title)) return Result.failure("null or empty title");
//
//		DBObject query = makeQuery(title, drange);
//		
//		BasicDBObjectBuilder group = BasicDBObjectBuilder.start()
//				.push("$group")
//					.push("_id")
//						.push("week").add("$week", "$date").pop()
//						.add("key", "$key")
//					.pop()
//					.push("begin").add("$min", "$date").pop()
//					.push("end").add("$max", "$date").pop()
//					.push("count").add("$sum", "$count").pop()
//					.push("language")
//						.push("$addToSet")
//							.add("$concat", new String[]{"$lang", "$proj"})
//						.pop()
//					.pop()
//					.push("titles").add("$addToSet", "$title").pop()
//				.pop();
//
//		ArrayList<DBObject> list = new ArrayList<DBObject>();
//		for (int year = drange.first.year.value, end = drange.last.year.value; year <= end; ++year) {
//			DBCollection record = getRecordCollection(year);
//			if (null != record) {
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//				AggregationOutput output = record.aggregate(Arrays.asList(query, group.get(), PROJECT, SORT));
//				for (DBObject d : output.results()) {
//					BasicDBObject data = (BasicDBObject) d;
//					java.util.Date date = data.getDate("date");
//					d.put("timestamp", sdf.format(date));
//					list.add(d);
//				}
//			}
//		}
//		
//		return Result.wrap(list);
//	}
	
	
//	public static void main(String[] args) throws Exception {
//		try {
//			Console.startup();
//
//			WikiAPI api = new WikiAPI(Console.mongo.getDB("wiki4"));
//			
////			MonthOfYear moy = MonthOfYear.valueOf("2008-03");
//			Date date = Date.valueOf("2008-03-01");
//			MonthOfYear moy = date.getMonthOfYear();
//			DBCollection record = api.getRecordCollection(moy);
//			long ts = System.currentTimeMillis();
//			AggregationOutput output = record.aggregate(Arrays.asList(
////					BasicDBObjectBuilder.start()
////						.push("$match")
////							.add("key", "usa")
////						.pop()
////					.get(),
//
//					BasicDBObjectBuilder.start()
//						.push("$match")
//							.add("key", "usa")
//							.push("dayhour")
//								.add("$gte", date.toString().replaceAll("-", "").substring(6, 8))
//								.add("$lt", date.getMonthOfYear().lastDay().toString().replaceAll("-", "").substring(6, 8))
//							.pop()
//						.pop()
//					.get(),
//					
//					BasicDBObjectBuilder.start()
//						.push("$group")
//							.push("_id")
//								.add("dayhour", "$dayhour")
//								.add("key", "$key")
//							.pop()
//							.push("count").add("$sum", 1).pop()
//							.push("total").add("$sum", "$count").pop()
////							.push("language")
////								.push("$addToSet")
////									.add("$concat", new String[]{"$lang", "$proj"})
////								.pop()
////							.pop()
////							.push("titles").add("$addToSet", "$title").pop()
//						.pop()
//					.get(),
//					
//					BasicDBObjectBuilder.start()
//						.push("$project")
//							.add("_id", 0)
//							.add("day", new BasicDBObject("$substr", Arrays.asList(
//									"$_id.dayhour", 0, 2
//								)))
//							.add("hour", new BasicDBObject("$substr", Arrays.asList(
//									"$_id.dayhour", 2, 4
//								)))
//							.add("key", "$key")
////							.push("date")
////								.add("$concat", Arrays.asList(
////										date.year.toString(),
////										"-",
////										date.month.toText(),
////										"-",
////										new BasicDBObject("$substr", Arrays.asList(
////												"$_id.day", 0, 2
////											)),
////										" ",
////										new BasicDBObject("$substr", Arrays.asList(
////												"$_id.hour", 0, 2
////											)),
////										":00:00"
////									))
////							.pop()
//							.add("key", "$key")
//							.add("count", 1)
//						.pop()
//					.get(),
//
//					BasicDBObjectBuilder.start()
//						.push("$sort")
//							.add("day", 1)
//							.add("hour", 1)
////							.add("date", 1)
//							.add("count", 1)
//						.pop()
//					.get()
//					
//				));
//			
////			DBCursor c = record.find(BasicDBObjectBuilder.start().add("key", "africa").get(), new BasicDBObject("count", 1));
//			int step = -1;
////			int total = output.count();
//			int count = 0;
//			int offset = 0;
//			int sum = 0;
////			for (DBObject d : c.iterator()) {
////			c.limit(step);
////			while (offset <= total) {
////				c.skip(offset);
////				System.out.println("====== offset ["+offset+"] ========");
//				Iterator<DBObject> it = output.results().iterator();
//				while (it.hasNext()) {
//					BasicDBObject d = (BasicDBObject) it.next();
//					System.out.println((++count)+"> "+d);
//					sum += d.getInt("count");
//				}
////				offset += step;
////			}
////			System.out.println("total: "+total);
//			System.out.println("sum: "+sum);
//			System.out.println(System.currentTimeMillis()-ts);
//			
//		} finally {
//			Console.shutdown();
//		}
//	}

//	public static void main(String[] args) throws Exception {
//		try {
//			Console.startup();
//
//			WikiAPI api = new WikiAPI(Console.mongo.getDB("wiki3"));
//			
////			MonthOfYear moy = MonthOfYear.valueOf("2008-03");
//			Date date = Date.valueOf("2008-03-24");
//			MonthOfYear moy = date.getMonthOfYear();
//			DBCollection record = api.getRecordCollection(moy);
//			long ts = System.currentTimeMillis();
//			DBCursor c = record.find(
//					BasicDBObjectBuilder.start()
//						.add("key", "usa")
////						.add("date", date.toSqlDate())
////						.add("date", date.time(Time.valueOf("00:00:00")).toSqlTimestamp())
////						.push("date")
////							.add("$gte", moy.firstDay().toSqlDate())
////							.add("$lt", moy.lastDay().next().toSqlDate())
//////							.add("$gte", date.toSqlDate())
//////							.add("$lt", date.next().toSqlDate())
//////							.add("$gte", date.time(Time.valueOf("00:00:00")).toSqlTimestamp())
//////							.add("$lte", date.time(Time.valueOf("23:00:00")).toSqlTimestamp())
////						.pop()
//					.get()
//					
////					new BasicDBObject("count", 1)
//				);
////			c.hint(
////					BasicDBObjectBuilder.start()
//////						.add("key", "hashed")
////						.add("key", 1)
////						.add("date", 1)
////					.get()
////				);
//			int total = c.count();
//			System.out.println("total: "+total+" ... "+(System.currentTimeMillis()-ts)+"ms");
//			ts = System.currentTimeMillis();
//			int step = total;
//			int count = 0;
//			int offset = 0;
//			int sum = 0;
////			for (DBObject data : c) {
////				BasicDBObject d = (BasicDBObject) data;
//			c.limit(1000);
////			while (offset <= total) {
////				c.skip(offset);
////				System.out.println("====== offset ["+offset+"] ========");
//				Iterator<DBObject> it = c.iterator();
//				while (it.hasNext()) {
//					BasicDBObject d = (BasicDBObject) it.next();
//					System.out.println((++count)+"> "+d);
//					sum += d.getInt("count");
//				}
////				offset += step;old
////				System.out.println("step["+step+"] "+(System.currentTimeMillis()-ts)+"ms");
////			}
////			System.out.println("total: "+total);
//			System.out.println("sum: "+sum);
//			System.out.println(System.currentTimeMillis()-ts);
//			
//		} finally {
//			Console.shutdown();
//		}
//	}
}
