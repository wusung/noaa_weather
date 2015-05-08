package com.jfetek.demo.weather;

import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentLimit {
	
	private final long			lmtWait;
	private final long			lmtConcurrent;
	private final AtomicInteger	counter;
	
	public ConcurrentLimit(int concurrent_limit, int wait_limit) {
		this.lmtConcurrent = concurrent_limit;
		this.lmtWait = wait_limit;
		this.counter = new AtomicInteger();
	}
	
	public void enter() {
		int count = counter.getAndIncrement();
		if (count >= lmtWait) {
			throw new RuntimeException("exceed wait-limit: "+lmtWait);
		}
		while (count > lmtConcurrent) {
			try {
				synchronized (counter) {
					counter.wait();
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
				break;
			} finally {
				--count;
			}
		}
	}
	public void leave() {
		counter.decrementAndGet();
		synchronized (counter) {
			counter.notifyAll();
		}
	}
	
	
	public static void usage() {
		ConcurrentLimit c = new ConcurrentLimit(10, 15);
		try {
			c.enter();
			
			// TODO below
			
			
		} catch(RuntimeException e) {
			e.printStackTrace();
		} finally {
			c.leave();
		}
	}
}
