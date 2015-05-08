<%@ page language="java" contentType="application/json; charset=UTF-8"
	import="com.mongodb.*"
	import="com.jfetek.demo.weather.*"
	pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/include/common-vars.jsp"%>
<%
out.clear();

DB db = Console.mongo.getDB("weather1");
DBCollection country = db.getCollection("country");
DBCursor c = country.find(new BasicDBObject());
out.write("[");
int count = 0;
for (DBObject o : c) {
	if (count++ > 0) out.write(",");
	out.write(o.toString());
}
out.write("]");
%>