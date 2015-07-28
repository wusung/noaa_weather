package com.jfetek.demo.weather.ws;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.jfetek.common.ErrorCode;
import com.jfetek.common.data.Result;
import com.jfetek.common.http.Params;
import com.jfetek.common.http.PathRouter;
import com.jfetek.common.time.Date;
import com.jfetek.common.time.DateRange;
import com.jfetek.common.time.DateTime;
import com.jfetek.common.util.JsonUtil;
import com.jfetek.common.util.TextUtil;
import com.jfetek.demo.weather.ConcurrentLimit;
import com.jfetek.demo.weather.Console;
import com.jfetek.demo.weather.api.MongoAPI;
import com.mongodb.DB;

/**
 * Servlet implementation class Query1
 */
public class WeatherAPI1 extends HttpServlet {
	
	public static final String VERSION	= "0";

	/**
	 * 
	 */
	private static final long serialVersionUID = 6280300524715168048L;

	private static ConcurrentLimit	CONCURRENT_LIMIT = null;
	

	private PathRouter router;
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public WeatherAPI1() {
		super();
	}

	@Override
	public void init(ServletConfig config) {
		String route = config.getInitParameter("param.route");
		boolean merge = TextUtil.booleanValue(config.getInitParameter("merge.query"), false);
		this.router = PathRouter.compile(route, merge);
		
		CONCURRENT_LIMIT = new ConcurrentLimit(100, 200);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setHeader("Content-Type", "text/html; charset=UTF-8");
//		Params params = Params.resolve(router, request);
		Params params = Params.of(request);
//		System.out.println(params.toJson());
		
		long tsWaiting = 0L;
		long tsAccess = 0L;
		

		PrintWriter out = null;
		try {
			long ts = System.currentTimeMillis();
			CONCURRENT_LIMIT.enter();
			tsWaiting = System.currentTimeMillis() - ts;
			
			DB db = Console.mongo.getDB("weather1");
			out = response.getWriter();

			tsAccess = _handleRequest(params, out, db, tsWaiting);
			
			out.flush();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != out) {
				try {
					out.close();
				} catch (Exception e) {
				}
				out = null;
			}
			CONCURRENT_LIMIT.leave();
		}
	}

	private long _handleRequest(Params params, PrintWriter out, DB db, long waiting) throws Exception {

		DateRange drange = null;
		String station = params.getParam("station");

		Date g_begin = params.getDateParam("begin_time");
		Date g_end = params.getDateParam("end_time");
		if (null == g_begin && null == g_end) {

		}
		else if (null == g_begin) {
			drange = DateRange.is(g_end);
		}
		else if (null == g_end) {
			drange = DateRange.is(g_begin);
		}
		else {
			drange = DateRange.between(g_begin, g_end);
		}

		String g_sample = params.getParam("sample_rate");

//		String[] g_columns = params.getParams("fields");
		String g_columns = params.getParam("fields", "");
		String[] columns = g_columns.split(",");


		HashMap<String,List<?>> map = new HashMap<String,List<?>>(4);
		ArrayList<Long> idxList = new ArrayList<Long>();
		map.put("index", idxList);
		ArrayList<ArrayList<?>> dataList = new ArrayList<ArrayList<?>>();
		map.put("data", dataList);

		
		MongoAPI api = new MongoAPI(db);

		long ts = System.currentTimeMillis();
		if ("d".equals(g_sample)) {
			// daily base
			ArrayList<String> exp_cols = new ArrayList<String>(3*columns.length);
			for (int i = 0, lenCol = columns.length; i < lenCol; ++i) {
				String col = columns[i];
				if ("date".equals(col)) {
					exp_cols.add(col);
				}
				else if ("time".equals(col)) {
				}
				else {
					exp_cols.add("min_"+col);
					exp_cols.add("max_"+col);
					exp_cols.add("avg_"+col);
					//exp_cols.add("sum_"+col);
				}
			}
			map.put("columns", exp_cols);
			for (int year = drange.first.year.value, end = drange.last.year.value; year <= end; ++year) {
				Result<ArrayList<ArrayList<Object>>> result = api.queryDailyWeatherByStation(year, station, drange, columns);
//				map.put(year, result.positive()? result.data : new TupleList(0));
				if (result.positive()) {
					for (int i = 0, lenData =  result.data.size(); i < lenData; ++i) {
						ArrayList<Object> arr = result.data.get(i);
//						DateTime dt = DateTime.valueOf(arr.get(0)+" "+arr.get(1));
						String datetime = String.valueOf(arr.remove(arr.size()-1));
						DateTime dt = DateTime.valueOf(datetime);
						idxList.add(dt.timestamp);
						dataList.add(arr);	
					}
				}
			}
		}
		else if ("w".equals(g_sample)) {
			// daily base
			ArrayList<String> exp_cols = new ArrayList<String>(3*columns.length);
			for (int i = 0, lenCol = columns.length; i < lenCol; ++i) {
				String col = columns[i];
				if ("date".equals(col)) {
					exp_cols.add(col);
				}
				else if ("time".equals(col)) {
				}
				else {
					exp_cols.add("min_"+col);
					exp_cols.add("max_"+col);
					exp_cols.add("avg_"+col);
					//exp_cols.add("sum_"+col);
				}
			}
			map.put("columns", exp_cols);
			for (int year = drange.first.year.value, end = drange.last.year.value; year <= end; ++year) {
				Result<ArrayList<ArrayList<Object>>> result = api.queryWeeklyWeatherByStation(year, station, drange, columns);
//				map.put(year, result.positive()? result.data : new TupleList(0));
				if (result.positive()) {
					for (int i = 0, lenData =  result.data.size(); i < lenData; ++i) {
						ArrayList<Object> arr = result.data.get(i);
//						DateTime dt = DateTime.valueOf(arr.get(0)+" "+arr.get(1));
						String datetime = String.valueOf(arr.remove(arr.size()-1));
						DateTime dt = DateTime.valueOf(datetime);
						idxList.add(dt.timestamp);
						dataList.add(arr);	
					}
				}
			}
		}
		else if ("m".equals(g_sample)) {
			// daily base
			ArrayList<String> exp_cols = new ArrayList<String>(3*columns.length);
			for (int i = 0, lenCol = columns.length; i < lenCol; ++i) {
				String col = columns[i];
				if ("date".equals(col)) {
					exp_cols.add(col);
				}
				else if ("time".equals(col)) {
				}
				else {
					exp_cols.add("min_"+col);
					exp_cols.add("max_"+col);
					exp_cols.add("avg_"+col);
					//exp_cols.add("sum_"+col);
				}
			}
			map.put("columns", exp_cols);
			for (int year = drange.first.year.value, end = drange.last.year.value; year <= end; ++year) {
				Result<ArrayList<ArrayList<Object>>> result = api.queryMonthlyWeatherByStation(year, station, drange, columns);
//				map.put(year, result.positive()? result.data : new TupleList(0));
				if (result.positive()) {
					for (int i = 0, lenData =  result.data.size(); i < lenData; ++i) {
						ArrayList<Object> arr = result.data.get(i);
//						DateTime dt = DateTime.valueOf(arr.get(0)+" "+arr.get(1));
						String datetime = String.valueOf(arr.remove(arr.size()-1));
						DateTime dt = DateTime.valueOf(datetime);
						idxList.add(dt.timestamp);
						dataList.add(arr);	
					}
				}
			}
		}
		else {
			map.put("columns", Arrays.asList(columns));
			for (int year = drange.first.year.value, end = drange.last.year.value; year <= end; ++year) {
				Result<ArrayList<ArrayList<Object>>> result = api.queryWeatherByStation(year, station, drange, columns);
//				map.put(year, result.positive()? result.data : new TupleList(0));
				if (result.positive()) {
					for (int i = 0, lenData =  result.data.size(); i < lenData; ++i) {
						ArrayList<Object> arr = result.data.get(i);
//						DateTime dt = DateTime.valueOf(arr.get(0)+" "+arr.get(1));
						String datetime = String.valueOf(arr.remove(arr.size()-1));
						DateTime dt = DateTime.valueOf(datetime);
						idxList.add(dt.timestamp);
						dataList.add(arr);	
					}
				}
			}
		}
		ts = System.currentTimeMillis() - ts;

		JSONObject json = JsonUtil.getBasicJson(ErrorCode.ok());
		String resStr = "{}";
		if (!dataList.isEmpty()) {
			resStr = JsonUtil.makeJsonize(map).toString();
		}
		JsonUtil.addField(json, "data", resStr);
		JsonUtil.addField(json, "version", VERSION);
		json.write(out);
		
		return ts;
	}

}
