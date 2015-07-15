package com.jfetek.demo.weather.api.impl;

import java.util.ArrayList;
import java.util.Arrays;

import com.jfetek.demo.weather.Console;
import com.jfetek.demo.weather.api.StationService;
import com.jfetek.demo.weather.model.Station;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class StationServiceImpl implements StationService {
	
	static final double R	= 6371;	// earth radius
	static double distance(double lat1, double lng1, double lat2, double lng2) {
		lat1 = lat1 * Math.PI / 180;
		lat2 = lat2 * Math.PI / 180;
		lng1 = lng1 * Math.PI / 180;
		lng2 = lng2 * Math.PI / 180;
		double d = Math.acos(Math.sin(lat1)*Math.sin(lat2)+Math.cos(lat1)*Math.cos(lat2)*Math.cos(lng2-lng1))*R;
		return d;	// in km
	}

	@Override
	public ArrayList<Station> queryList(String country, double g_lat, double g_lng) {

		DB db = Console.mongo.getDB("weather1");
		DBCollection station = db.getCollection("station");
		BasicDBObject query = new BasicDBObject();
		if (null == country && -999 != g_lat && -999 != g_lng) {
			BasicDBObject near = new BasicDBObject();
			near.append("$near", Arrays.asList(g_lng, g_lat));
			query.append("geo", near);
		}
		else {
			query.append("country", country);
		}
		
		ArrayList<Integer> idxList = new ArrayList<Integer>();
		ArrayList<Station> dataList = new ArrayList<Station>();
		DBCursor c = station.find(query, 
				new BasicDBObject("_id", 1)
					.append("usaf", 1)
					.append("wban", 1)
					.append("name", 1)
					.append("state", 1)
					.append("date_range", 1)
					.append("geo", 1));
		int count = 0;
		for (DBObject o : c) {
			BasicDBObject data = (BasicDBObject) o;
			BasicDBList geo = (BasicDBList) data.get("geo");
			double lng = (Double) geo.get(0);
			double lat = (Double) geo.get(1);
			data.append("dist", distance(g_lat, g_lng, lat, lng));
			idxList.add(count++);
			
//			ArrayList<Object> arr = new ArrayList<Object>(4);
//			arr.add(data.getString("_id"));
//			arr.add(data.getString("name"));
//			arr.add(data.getString("date_range"));
//			arr.add(data.getString("geo"));
			
			Station station1 = new Station();
			station1.setId(data.getString("_id"));
			station1.setName(data.getString("name"));
			station1.setDateRange(data.getString("date_range"));
			station1.setGeo(data.getString("geo"));
			station1.setDist(distance(g_lat, g_lng, lat, lng));
			dataList.add(station1);
		}
		
		return dataList;
	}
}
