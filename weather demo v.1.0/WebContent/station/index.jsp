<%@ page language="java" contentType="application/json; charset=UTF-8"
	import="com.mongodb.*"
	import="java.util.*"
	import="com.jfetek.demo.weather.*"
	import="com.jfetek.common.http.*"
	import="com.jfetek.common.util.*"
	pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/include/common-vars.jsp"%>
<%
out.clear();
Params params = Params.of(request);
//System.out.println(params);

String g_country = params.trimParam("country");

double g_lat = params.getDoubleParam("lat", -999);
double g_lng = params.getDoubleParam("lng", -999);

DB db = Console.mongo.getDB("weather1");
DBCollection station = db.getCollection("station");
BasicDBObject query = new BasicDBObject();
if (null == g_country && -999 != g_lat && -999 != g_lng) {
	BasicDBObject near = new BasicDBObject();
	near.append("$near", Arrays.asList(g_lng, g_lat));
	query.append("geo", near);
}
else {
	query.append("country", g_country);
}
DBCursor c = station.find(query, new BasicDBObject("_id", 1).append("usaf", 1).append("wban", 1).append("name", 1).append("state", 1).append("date_range", 1).append("geo", 1));
out.write("[");
int count = 0;
for (DBObject o : c) {
	BasicDBObject data = (BasicDBObject) o;
	if (count++ > 0) out.write(",");
	BasicDBList geo = (BasicDBList) data.get("geo");
	double lng = (Double) geo.get(0);
	double lat = (Double) geo.get(1);
	data.append("dist", distance(g_lat, g_lng, lat, lng));
	out.write(data.toString());
}
out.write("]");
%>
<%!
static final double R	= 6371;	// earth radius
static double distance(double lat1, double lng1, double lat2, double lng2) {
	lat1 = lat1 * Math.PI / 180;
	lat2 = lat2 * Math.PI / 180;
	lng1 = lng1 * Math.PI / 180;
	lng2 = lng2 * Math.PI / 180;
	double d = Math.acos(Math.sin(lat1)*Math.sin(lat2)+Math.cos(lat1)*Math.cos(lat2)*Math.cos(lng2-lng1))*R;
	return d;	// in km
}
%>