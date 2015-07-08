package com.jfetek.demo.weather.ws;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jfetek.demo.weather.ConcurrentLimit;

/**
 * Servlet implementation class ConcurrentSample1
 */
public class ConcurrentSample1 extends HttpServlet {

	private static ConcurrentLimit	CONCURRENT_LIMIT = null;
	
	int intValue(String s, int default_value) {
		if (null == s || 0 == s.length()) return default_value;
		try {
			Double d = Double.valueOf(s);
			return d.intValue();
		} catch(Exception e2) { }
		return default_value;
	}
	
	@Override
	public void init(ServletConfig config) {
		int wait_limit = intValue(config.getInitParameter("wait-limit"), 10);
		int concurrent_limit = intValue(config.getInitParameter("concurrent-limit"), 15);
		CONCURRENT_LIMIT = new ConcurrentLimit(concurrent_limit, wait_limit);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		////////////////////////////////////////////////////////////////////////////////
		//enter queue
		try {
			CONCURRENT_LIMIT.enter();
			////////////////////////////////////////////////////////////////////////////////
			
			//TODO ////////////////////////////////////////////////////////////////////////

		
		
		
		
		////////////////////////////////////////////////////////////////////////////////
		//leave queue
		} catch(RuntimeException e) {
			response.sendError(429, "Too Manay Requests");
		} finally {
			CONCURRENT_LIMIT.leave();
		}
		////////////////////////////////////////////////////////////////////////////////
	}

}
