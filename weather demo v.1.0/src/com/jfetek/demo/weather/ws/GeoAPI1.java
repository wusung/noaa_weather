package com.jfetek.demo.weather.ws;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.jfetek.common.util.JsonUtil;
import com.jfetek.common.util.TextUtil;
import com.jfetek.demo.weather.Console;
import com.jfetek.demo.weather.api.MongoAPI;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;

/**
 * 
 */
public class GeoAPI1 extends HttpServlet {
	
	public static final String	VERSION	= "1.0";

	/**
	 * 
	 */
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
	public GeoAPI1() {
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

		String city = params.getParam("city");
		String country = params.getParam("country");
		String state = params.getParam("state");

		MongoAPI api = new MongoAPI(db);
		Result<BasicDBObject> rsStation = api.queryGeoByCity(country, state, city);
		System.out.println("nearest station: " + rsStation);
		if (rsStation.data != null) {
			String station = rsStation.data.getString("_id");
			if (TextUtil.noValueOrBlank(station)) {
				JSONObject json = JsonUtil.getBasicJson(
						ErrorCode.error(String.format("%s can not cound neariest station", station)));
				json.write(out);
				return;
			}
		}

		String g_columns = params.getParam("fields", "");
		String[] columns = g_columns.split(",");

		HashMap<String,List<?>> map = new HashMap<String,List<?>>(1);
		//map.put("columns", Arrays.asList(columns));
		ArrayList<Long> idxList = new ArrayList<Long>();
		//map.put("index", idxList);
		
		Map<String, Object> resultMap = new HashMap<String, Object>();		
		if (rsStation.positive()) {
			resultMap.put("_id", rsStation.data.getString("_id"));
			resultMap.put("usaf", rsStation.data.getString("usaf"));
			resultMap.put("wban", rsStation.data.getString("wban"));
			resultMap.put("name", rsStation.data.getString("name"));
			resultMap.put("country", rsStation.data.getString("country"));
			resultMap.put("state", rsStation.data.getString("state"));
			resultMap.put("geo", rsStation.data.getString("geo"));
			resultMap.put("elv", rsStation.data.getString("elv"));
			resultMap.put("data_range", rsStation.data.getString("data_range"));
		}
		
		ArrayList<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		dataList.add(resultMap);
		map.put("data", dataList);

		JSONObject json = JsonUtil.getBasicJson(ErrorCode.ok());
		String resStr = JsonUtil.makeJsonize(map).toString();
		JsonUtil.addField(json, "data", resStr);
		JsonUtil.addField(json, "version", VERSION);
		json.write(out);		
	}
}
