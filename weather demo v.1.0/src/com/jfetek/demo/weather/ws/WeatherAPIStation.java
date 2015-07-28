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
import com.jfetek.common.http.Params;
import com.jfetek.common.util.JsonUtil;
import com.jfetek.demo.weather.Console;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * Servlet implementation class WeatherAPIStation, Search weather station by country or lat/lng
 */
@SuppressWarnings("serial")
public class WeatherAPIStation extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(WeatherAPIStation.class);	
	public static final String 	VERSION	= "0";	
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
				}
				out = null;
			}
		}
	}

	private void _handleRequest(Params params, HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws Exception {

		String g_country = params.trimParam("country");
		double g_lat = params.getDoubleParam("lat", -999);
		double g_lng = params.getDoubleParam("lng", -999);
		int limit = params.getIntParam("limit", 10);
		
		HashMap<String,List<?>> map = new HashMap<String,List<?>>(4);
		map.put("columns", Arrays.asList("id", "name", "date_range", "geo"));
		ArrayList<Integer> idxList = new ArrayList<Integer>();
		map.put("index", idxList);
		ArrayList<ArrayList<?>> dataList = new ArrayList<ArrayList<?>>();
		map.put("data", dataList);

		DB db = Console.mongo.getDB("weather1");
		DBCollection station = db.getCollection("station");
		BasicDBObject query = new BasicDBObject();
		DBCursor c = null;
		if (null == g_country && -999 != g_lat && -999 != g_lng) {
			BasicDBObject near = new BasicDBObject();
			near.append("$near", Arrays.asList(g_lng, g_lat));
			query.append("geo", near);
			c = station.find(query, new BasicDBObject("_id", 1)
					.append("usaf", 1)
					.append("wban", 1)
					.append("name", 1)
					.append("state", 1)
					.append("date_range", 1)
					.append("geo", 1))
					.limit(limit);			
		}
		else {
			query.append("country", g_country);
			c = station.find(query, new BasicDBObject("_id", 1)
					.append("usaf", 1)
					.append("wban", 1)
					.append("name", 1)
					.append("state", 1)
					.append("date_range", 1)
					.append("geo", 1));
		}
		
		int count = 0;
		for (DBObject o : c) {
			BasicDBObject data = (BasicDBObject) o;
			BasicDBList geo = (BasicDBList) data.get("geo");
			double lng = (Double) geo.get(0);
			double lat = (Double) geo.get(1);
			data.append("dist", distance(g_lat, g_lng, lat, lng));
			idxList.add(count++);
			
			ArrayList<Object> arr = new ArrayList<Object>(4);
			arr.add(data.getString("_id"));
			arr.add(data.getString("name"));
			arr.add(data.getString("date_range"));
			arr.add(data.getString("geo"));
			dataList.add(arr);
		}

		JSONObject json = JsonUtil.getBasicJson(ErrorCode.ok());
		String resStr = JsonUtil.makeJsonize(map).toString();
		JsonUtil.addField(json, "data", resStr);
		JsonUtil.addField(json, "version", VERSION);
		json.write(out);
	}
}
