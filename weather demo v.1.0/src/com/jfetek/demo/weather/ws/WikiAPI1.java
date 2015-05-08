package com.jfetek.demo.weather.ws;

import java.io.IOException;
import java.io.PrintWriter;
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
import com.jfetek.common.util.JsonUtil;
import com.jfetek.common.util.TextUtil;
import com.jfetek.demo.weather.Console;
import com.jfetek.demo.weather.api.WikiAPI;
import com.mongodb.DB;

/**
 * Servlet implementation class WikiAPI1
 */
public class WikiAPI1 extends HttpServlet {
	
	public static final String 	VERSION	= "1.0";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7244226315168220934L;
	private PathRouter router;

	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public WikiAPI1() {
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
//		Params params = Params.resolve(router, request);
		Params params = Params.of(request);

		PrintWriter out = null;
		try {
			DB db = Console.mongo.getDB("wiki1");
			out = response.getWriter();

			_handleRequest(params, out, db);

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

		DateRange drange = null;
		String title = params.getParam("title");
		if (TextUtil.noValueOrBlank(title)) {
			JSONObject json = JsonUtil.getBasicJson(ErrorCode.error("no title"));
			json.write(out);
			return;
		}
//		System.out.println(params.toJson());
		
		Date g_begin = params.getDateParam("begin_time");
		Date g_end = params.getDateParam("end_time");
		if (null == g_begin && null == g_end) {
			JSONObject json = JsonUtil.getBasicJson(ErrorCode.error("no date range"));
			json.write(out);
			return;
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

		WikiAPI api = new WikiAPI(db);
		
		long tsIn = System.currentTimeMillis();
		Result<HashMap<String,List<?>>> result;
//		if ("y".equals(g_sample)) {
//			result = api.queryYearlyWiki(title, drange);
//		}
		if ("h".equals(g_sample)) {
			result = api.queryHourlyWiki(title, drange);
		}
		else if ("w".equals(g_sample)) {
			result = api.queryWeeklyWiki(title, drange);
		}
		else if ("m".equals(g_sample)) {
			result = api.queryMonthlyWiki(title, drange);
		}
		else {
			// daily base
			result = api.queryDailyWiki(title, drange);
		}
//		result = api.queryHourlyWiki(title, drange);
		
		JSONObject json = JsonUtil.getBasicJson((ErrorCode)result);
		String resStr = JsonUtil.makeJsonize(result.data).toString();
		JsonUtil.addField(json, "data", resStr);
		JsonUtil.addField(json, "version", VERSION);
		JsonUtil.addExecTimeField(json, tsIn, System.currentTimeMillis()).write(out);
	}
	
//	public static void main(String[] args) throws Exception {
//		try {
//			Console.startup();
//
////			DB db = Console.mongo.getDB("wiki1");
////			WikiAPI api = new WikiAPI(db);
////			
////			System.out.println(api.queryHourlyWiki("usa", DateRange.is(Date.valueOf("2008-02-03"))));
//			
//			Date today = Date.today();
//			Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
//			cal.setTimeInMillis(today.timestamp);
//			
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			sdf.setTimeZone(cal.getTimeZone());
//			System.out.println(sdf.format(cal.getTime()));
//			
//			cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
//			System.out.println(sdf.format(cal.getTime()));
//			System.out.println(new Date(cal.getTime()));
//			
//		} finally {
//			Console.shutdown();
//		}
//	}

}
