<%@ page language="java"
	pageEncoding="UTF-8"%>
<%!
static final String verStyle = "0.91";
static final String verScript = "0.91";
static final String verPage = "0.91";

%><%
long			 tsNow = System.currentTimeMillis();
String			webapp = request.getContextPath();
String			subapp = request.getPathInfo();
out.clear();
%>