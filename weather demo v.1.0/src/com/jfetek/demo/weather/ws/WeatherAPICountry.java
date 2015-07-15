package com.jfetek.demo.weather.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.jfetek.common.ErrorCode;
import com.jfetek.common.data.Result;
import com.jfetek.common.http.Params;
import com.jfetek.common.time.Date;
import com.jfetek.common.time.DateRange;
import com.jfetek.common.time.DateTime;
import com.jfetek.common.util.JsonUtil;
import com.jfetek.demo.weather.Console;
import com.jfetek.demo.weather.api.MongoAPI;
import com.jfetek.demo.weather.api.ServiceFactory;
import com.jfetek.demo.weather.api.StationService;
import com.jfetek.demo.weather.model.Station;
import com.mysql.jdbc.StringUtils;

/**
 * Servlet implementation class WeatherAPICountry
 */
@SuppressWarnings("serial")
public class WeatherAPICountry extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(WeatherAPICountry.class);
	
	public static final String 	VERSION	= "0";

	private StationService stationService = ServiceFactory.getInstance().stationService();
	
	static final double R	= 6371;	// earth radius
	static double distance(double lat1, double lng1, double lat2, double lng2) {
		lat1 = lat1 * Math.PI / 180;
		lat2 = lat2 * Math.PI / 180;
		lng1 = lng1 * Math.PI / 180;
		lng2 = lng2 * Math.PI / 180;
		double d = Math.acos(Math.sin(lat1)*Math.sin(lat2)+Math.cos(lat1)*Math.cos(lat2)*Math.cos(lng2-lng1))*R;
		return d;	// in km
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("doGet(HttpServletRequest, HttpServletResponse) - start"); 
		}
		
		response.setHeader("Content-Type", "text/html; charset=UTF-8");
		Params params = Params.of(request);

		PrintWriter out = null;
		try {
			out = response.getWriter();
			_handleRequest(params, request, response, out);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("doGet() failed.", e);
		} catch (JSONException e) {
			e.printStackTrace();
			logger.error("doGet() failed.", e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("doGet() failed.", e);
		} finally {
			if (null != out) {
				try {
					out.close();
				} catch (Exception e) {
					logger.error("doGet() failed.", e);
				}
				out = null;
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("doGet(HttpServletRequest, HttpServletResponse) - end"); 
		}
	}
	
	private void _handleRequest(Params params, HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws Exception {
		
		logger.debug("params={}", params.toString());

		String g_country = params.trimParam("country");

		double g_lat = params.getDoubleParam("lat", -999);
		double g_lng = params.getDoubleParam("lng", -999);
		
		Date g_begin = params.getDateParam("begin_time");
		Date g_end = params.getDateParam("end_time");
		String sample_rate = params.getParam("sample_rate");
		
		DateRange drange = null;
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
			
		HashMap<String,List<?>> map = new HashMap<String,List<?>>(4);
		
		String g_columns = params.getParam("fields", "");
		String[] columns = g_columns.split(",");
		
		ArrayList<Long> idxList = new ArrayList<Long>();
		map.put("index", idxList);
		ArrayList<ArrayList<?>> dataList = new ArrayList<ArrayList<?>>();
		map.put("data", dataList);
		
		MongoAPI api = new MongoAPI(Console.mongo.getDB("weather1"));
		ArrayList<Station> stations = stationService.queryList(g_country, g_lat, g_lng);
		
		if ("d".equals(sample_rate)) {
			// daily base
			ArrayList<String> exp_cols = new ArrayList<String>(4*columns.length);
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
					exp_cols.add("sum_"+col);
				}
			}
			map.put("columns", exp_cols);
			
			for (Station s: stations) {
				String station = s.getId();
				for (int year = drange.first.year.value, end = drange.last.year.value; year <= end; ++year) {
					Result<ArrayList<ArrayList<Object>>> result = api.queryDailyWeatherByStation(year, station, drange, columns);
					if (result.positive()) {
						for (int i = 0, lenData =  result.data.size(); i < lenData; ++i) {
							ArrayList<Object> arr = result.data.get(i);
							String datetime = String.valueOf(arr.remove(arr.size()-1));
							DateTime dt = DateTime.valueOf(datetime);
							idxList.add(dt.timestamp);
							dataList.add(arr);	
						}
					}
				}
			}
		}
		else if ("w".equals(sample_rate)) {
			// weekly base
			ArrayList<String> exp_cols = new ArrayList<String>(4*columns.length);
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
					exp_cols.add("sum_"+col);
				}
			}
			map.put("columns", exp_cols);
			for (Station s: stations) {
				String station = s.getId();
				for (int year = drange.first.year.value, end = drange.last.year.value; year <= end; ++year) {
					Result<ArrayList<ArrayList<Object>>> result = api.queryWeeklyWeatherByStation(year, station, drange, columns);
					if (result.positive()) {
						for (int i = 0, lenData =  result.data.size(); i < lenData; ++i) {
							ArrayList<Object> arr = result.data.get(i);
							String datetime = String.valueOf(arr.remove(arr.size()-1));
							DateTime dt = DateTime.valueOf(datetime);
							idxList.add(dt.timestamp);
							dataList.add(arr);	
						}
					}
				}
			}
		}
		else if ("m".equals(sample_rate)) {
			// monthly base
			ArrayList<String> exp_cols = new ArrayList<String>(4*columns.length);
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
					exp_cols.add("sum_"+col);
				}
			}
			map.put("columns", exp_cols);
			
			for (Station s: stations) {
				String station = s.getId();
				for (int year = drange.first.year.value, end = drange.last.year.value; year <= end; ++year) {
					Result<ArrayList<ArrayList<Object>>> result = api.queryMonthlyWeatherByStation(year, station, drange, columns);
					if (result.positive()) {
						for (int i = 0, lenData =  result.data.size(); i < lenData; ++i) {
							ArrayList<Object> arr = result.data.get(i);
							String datetime = String.valueOf(arr.remove(arr.size()-1));
							DateTime dt = DateTime.valueOf(datetime);
							idxList.add(dt.timestamp);
							dataList.add(arr);	
						}
					}
				}
			}
		}
		else {
			// raw base
			map.put("columns", Arrays.asList(columns));
			
			for (Station s: stations) {
				String station = s.getId();
				for (int year = drange.first.year.value, end = drange.last.year.value; year <= end; ++year) {
					Result<ArrayList<ArrayList<Object>>> result = api.queryWeatherByStation(year, station, drange, columns);
					if (result.positive()) {
						for (int i = 0, lenData =  result.data.size(); i < lenData; ++i) {
							ArrayList<Object> arr = result.data.get(i);
							String datetime = String.valueOf(arr.remove(arr.size()-1));
							DateTime dt = DateTime.valueOf(datetime);
							idxList.add(dt.timestamp);
							dataList.add(arr);	
						}
					}
				}
			}
		}
		
		JSONObject json = JsonUtil.getBasicJson(ErrorCode.ok());
		String resStr = "{}";
		if (!dataList.isEmpty()) {
			resStr = JsonUtil.makeJsonize(map).toString();
		}
		JsonUtil.addField(json, "data", resStr);
		JsonUtil.addField(json, "version", VERSION);
		json.write(out);
	}

	private String[] splitColumns(Params params) {		
		if (StringUtils.isNullOrEmpty(params.getParam("fields")))
			return null;
		return params.getParam("fields", "").split(",");
	}
}
