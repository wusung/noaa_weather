package com.jfetek.demo.weather.ws;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfetek.common.ErrorCode;
import com.jfetek.common.http.Params;
import com.jfetek.common.time.Date;
import com.jfetek.common.time.DateRange;
import com.jfetek.common.time.DateTime;
import com.jfetek.common.util.JsonUtil;
import com.jfetek.demo.weather.api.ServiceFactory;
import com.jfetek.demo.weather.api.StationService;
import com.jfetek.demo.weather.api.WeatherService;
import com.mysql.jdbc.StringUtils;

/**
 * Servlet implementation class querying Weather data by stations
 */
@SuppressWarnings("serial")
public class WeatherByStation extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(WeatherByStation.class);
	
	public static final String 	VERSION	= "0";

	private StationService stationService = ServiceFactory.getInstance().stationService();
	private WeatherService weatherService = ServiceFactory.getInstance().weatherService();
	
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
		} catch (Exception e) {
			logger.error("doGet() failed.", e);
			JSONObject json = JsonUtil.getBasicJson(ErrorCode.error(e));
			JsonUtil.addField(json, "data", "{}");
			JsonUtil.addField(json, "version", VERSION);
			try {
				json.write(out);
			} catch (JSONException e1) {
				logger.error("json.write() failed.", e1);
			}
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

		String g_stations = params.trimParam("stations");
		Date g_begin = params.getDateParam("begin_time");
		Date g_end = params.getDateParam("end_time");
		String sample_rate = params.getParam("sample_rate");
		
		if (StringUtils.isEmptyOrWhitespaceOnly(g_stations)) {
			JSONObject json = JsonUtil.getBasicJson(ErrorCode.error(ErrorCode.INVALID_PARAMETER, "Invalid argument: stations"));
			JsonUtil.addField(json, "data", "{}");
			JsonUtil.addField(json, "version", VERSION);
			json.write(out);			
			return;
		}
		
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
		
		ArrayList<String> exp_cols = new ArrayList<String>(3*columns.length);
		exp_cols.add("station");
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
			}
		}
		map.put("columns", exp_cols);
		
		if ("d".equals(sample_rate) || "r".equals(sample_rate)) {
			// daily base
			dataList.addAll(weatherService.queryDailyList(g_stations, drange, columns));
		}
		else if ("w".equals(sample_rate)) {
			// weekly base
			dataList.addAll(weatherService.queryWeeklyList(g_stations, drange, columns));
		}
		else if ("m".equals(sample_rate)) {
			// monthly base
			dataList.addAll(weatherService.queryMonthlyList(g_stations, drange, columns));
		}
		else {
			dataList.addAll(weatherService.queryDailyList(g_stations, drange, columns));
//			// raw base
//			map.put("columns", Arrays.asList(columns));
//			dataList.addAll(weatherService.queryRawList(g_stations, drange, columns));
		}

		// Build indexes with timestamp for pandas
		for (ArrayList<?> arr: dataList) {
			String datetime = String.valueOf(arr.remove(arr.size() - 1));
			DateTime dt = DateTime.valueOf(datetime);
			idxList.add(dt.timestamp);
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
}
