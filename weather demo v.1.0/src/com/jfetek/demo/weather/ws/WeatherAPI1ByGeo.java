package com.jfetek.demo.weather.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.jfetek.demo.weather.Console;
import com.jfetek.demo.weather.api.MongoAPI;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;

/**
 * Servlet implementation class Weather API
 */
public class WeatherAPI1ByGeo extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(WeatherAPI1ByGeo.class);
	
	public static final String	VERSION	= "0";

	private static final long serialVersionUID = 6280300524715168048L;
	
	static final double R	= 6371;	// earth radius
	static double distance(double lat1, double lng1, double lat2, double lng2) {
		lat1 = lat1 * Math.PI / 180;
		lat2 = lat2 * Math.PI / 180;
		lng1 = lng1 * Math.PI / 180;
		lng2 = lng2 * Math.PI / 180;
		double d = Math.acos(Math.sin(lat1)*Math.sin(lat2)+Math.cos(lat1)*Math.cos(lat2)*Math.cos(lng2-lng1))*R;
		return d;	// in km
	}	
	
	private PathRouter router;
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public WeatherAPI1ByGeo() {
		super();
	}

	@Override
	public void init(ServletConfig config) {
		String route = config.getInitParameter("param.route");
		boolean merge = TextUtil.booleanValue(config.getInitParameter("merge.query"), false);
		this.router = PathRouter.compile(route, merge);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setHeader("Content-Type", "text/html; charset=UTF-8");
		Params params = Params.resolve(router, request);
		System.out.println(params.toJson());

		PrintWriter out = null;
		try {
			DB db = Console.mongo.getDB("weather1");
			out = response.getWriter();

			_handleRequest(params, out, db);
			
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
		}
	}

	private void _handleRequest(Params params, PrintWriter out, DB db) throws Exception {

		double g_lat = params.getDoubleParam("lat");
		double g_lng = params.getDoubleParam("lng");

		MongoAPI api = new MongoAPI(db);
		
		Result<BasicDBObject> rsStation = api.queryNearestStationByGeo(g_lat, g_lng);
		logger.info("nearest station={}", rsStation);
		String station = rsStation.data.getString("_id");
		if (TextUtil.noValueOrBlank(station)) {
			JSONObject json = JsonUtil.getBasicJson(ErrorCode.error("lat:"+g_lat+" lng:"+g_lng+" can not cound neariest station"));
			json.write(out);
			return;
		}

		DateRange drange = null;

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

		String g_columns = params.getParam("fields", "");
		String[] columns = g_columns.split(",");

		HashMap<String,List<?>> map = new HashMap<String,List<?>>(4);
		map.put("columns", Arrays.asList(columns));
		ArrayList<Long> idxList = new ArrayList<Long>();
		map.put("index", idxList);
		ArrayList<ArrayList<?>> dataList = new ArrayList<ArrayList<?>>();
		map.put("data", dataList);
		
		logger.info("begin_time-end_time {}~{}", drange.first.year.value, drange.last.year.value);
		long ts = System.currentTimeMillis();
		
		for (int year = drange.first.year.value; year <= drange.last.year.value; year++) {
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
		ts = System.currentTimeMillis() - ts;

		JSONObject json = JsonUtil.getBasicJson(ErrorCode.ok());
		String resStr = "{}"; // Avoid crash if pandas.dataframe.emtpy
		if (!dataList.isEmpty())
			resStr = JsonUtil.makeJsonize(map).toString();
		JsonUtil.addField(json, "data", resStr);
		JsonUtil.addField(json, "version", VERSION);
		json.write(out);
		
		logger.info("_handleRequest() - end, data.size={}", dataList.size());
	}
}
