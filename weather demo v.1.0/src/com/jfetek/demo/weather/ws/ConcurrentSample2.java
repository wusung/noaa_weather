package com.jfetek.demo.weather.ws;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ConcurrentSample1
 */
public class ConcurrentSample2 extends HttpServlet {

	static final long			WAIT_LIMIT			= 20;
	static final long			CONCURRENT_LIMIT	= 10;
	static final AtomicInteger	CONCURRENT_COUNTER	= new AtomicInteger();
	static final void enterQueue() {
		int count = CONCURRENT_COUNTER.getAndIncrement();
		if (count >= WAIT_LIMIT) {
			throw new RuntimeException("exceed wait-limit: "+WAIT_LIMIT);
		}
		while (count > CONCURRENT_LIMIT) {
			try {
				synchronized (CONCURRENT_COUNTER) {
					CONCURRENT_COUNTER.wait();
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
				break;
			} finally {
				--count;
			}
		}
	}
	static final void leaveQueue() {
		CONCURRENT_COUNTER.decrementAndGet();
		synchronized (CONCURRENT_COUNTER) {
			CONCURRENT_COUNTER.notifyAll();
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		////////////////////////////////////////////////////////////////////////////////
		//enter queue
		try {
			enterQueue();
			////////////////////////////////////////////////////////////////////////////////
			
			//TODO ////////////////////////////////////////////////////////////////////////
		
		
		
		
		
		////////////////////////////////////////////////////////////////////////////////
		//leave queue
		} catch(RuntimeException e) {
			response.sendError(429, "Too Manay Requests");
		} finally {
			leaveQueue();
		}
		////////////////////////////////////////////////////////////////////////////////
	}

}
