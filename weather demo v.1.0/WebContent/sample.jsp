<%@ page language="java" contentType="application/json; charset=UTF-8"
	import="java.util.concurrent.atomic.*"
    pageEncoding="UTF-8"%>
<%!
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
%>
<%
out.clear();
////////////////////////////////////////////////////////////////////////////////
// enter queue
try {
	enterQueue();
////////////////////////////////////////////////////////////////////////////////

// TODO ////////////////////////////////////////////////////////////////////////





////////////////////////////////////////////////////////////////////////////////
// leave queue
} catch(RuntimeException e) {
	response.sendError(429, "Too Manay Requests");
} finally {
	leaveQueue();
}
////////////////////////////////////////////////////////////////////////////////
%>