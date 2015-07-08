package com.jfetek.demo.weather;


import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application Lifecycle Listener implementation class EventListeners
 *
 */
public class Bootstrap implements
							ServletContextListener,
							HttpSessionListener,
							ServletRequestListener {

	private static final Logger LOGGER		= LoggerFactory.getLogger(Bootstrap.class);

	
	private static Bootstrap	currentInstance = null;
	public static Bootstrap getCurrentInstance() {
		return currentInstance;
	}
	
	private ServletContext context;
	
    /**
     * Default constructor. 
     */
    public Bootstrap() {
    	if (null != Bootstrap.currentInstance) throw new IllegalStateException();
    	
    	this.context = null;
    	Bootstrap.currentInstance = this;
    }
    
    public ServletContext getServletContext() {
    	return this.context;
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent ev) {
    	this.context = ev.getServletContext();
    	try {
    		LOGGER.info("Bootstrap startup system...");
			Console.startup();
		} catch (Exception e) {
//			e.printStackTrace();
			LOGGER.error("fetal error occurr on system startup.", e);
		}
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent ev) {
		LOGGER.info("Bootstrap shutdown system...");
    	try {
			Console.shutdown();
		} catch (Exception e) {
//			e.printStackTrace();
			LOGGER.error("fetal error occurr on system shutdown.", e);
		}
    	this.context = null;
    }

	/**
     * @see HttpSessionListener#sessionCreated(HttpSessionEvent)
     */
    public void sessionCreated(HttpSessionEvent ev) {
    	LOGGER.trace("{}> session-created", COUNTER.incrementAndGet());
    	HttpSession session = ev.getSession();
//    	Authentication auth = (Authentication) session.getAttribute(Session.AUTNETICATION);
//    	session.setAttribute(Session.AUTNETICATION, Authentication.GUEST_AUTHENTICATION);
    }
	
	/**
     * @see HttpSessionListener#sessionDestroyed(HttpSessionEvent)
     */
    public void sessionDestroyed(HttpSessionEvent ev) {
    	LOGGER.trace("{}> session-destroyed", COUNTER.incrementAndGet());
    	HttpSession session = ev.getSession();
//    	session.removeAttribute(Session.AUTNETICATION);
//    	log.remove(session);
    }

    
//    LinkedList<TimestampData<HttpSession>> log = new LinkedList<TimestampData<HttpSession>>();

	/**
     * @see ServletRequestListener#requestInitialized(ServletRequestEvent)
     */
    public void requestInitialized(ServletRequestEvent ev) {
//    	LOGGER.trace("{}> request initialized", COUNTER.incrementAndGet());
    	HttpServletRequest request = (HttpServletRequest) ev.getServletRequest();
//    	HttpSession session = request.getSession();
//    	if (session.isNew()) {
////    		String ip = request.getRemoteHost();
//    		LOGGER.trace("session's auth: ", session.getAttribute(Session.AUTNETICATION));
//    	}
    }

	/**
     * @see ServletRequestListener#requestDestroyed(ServletRequestEvent)
     */
    public void requestDestroyed(ServletRequestEvent ev) {
//    	LOGGER.trace("{}> request-destroyed", COUNTER.incrementAndGet());
//    	HttpServletRequest request = (HttpServletRequest) ev.getServletRequest();
//    	HttpSession session = request.getSession();
    }

    public static final AtomicLong COUNTER = new AtomicLong();
}
