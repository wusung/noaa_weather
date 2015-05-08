package gov.noaa.ncdc.ish;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bson.types.ObjectId;

import com.jfetek.common.util.TextUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

public class MassInsertMongodb {
	
	public static void main(String[] args) throws Exception {
		File countryFile = new File("C:/Disk/D/資料備份/new Project/BigData/country-list.txt");
		File stationFile = new File("C:/Disk/D/資料備份/new Project/BigData/isd-history.csv");
		MongoClient client = null;
		try {
			client = new MongoClient( "192.168.3.209" , 27017 );
			DB db = client.getDB("test");

			BasicDBObject indexOpt = new BasicDBObject();
			indexOpt.append("unique", true);
			
			DBCollection country = db.getCollection("country");
			country.drop();
			country.createIndex(new BasicDBObject().append("id", 1), indexOpt);
			List<DBObject> countryList = retrieveCountryList(countryFile);
			WriteResult rsIns1 = country.insert(countryList);
			System.out.println(rsIns1);
			
			DBCollection station = db.getCollection("station");
			station.drop();
			station.createIndex(new BasicDBObject().append("usaf", 1).append("wban", 1), indexOpt);
			List<DBObject> stationList = retrieveStationList(stationFile);
			WriteResult rsIns2 = station.insert(stationList);
			System.out.println(rsIns2);
//			BulkWriteOperation stationBulk = station.initializeUnorderedBulkOperation();
//			stationBulk.
			
			// add record: index:(year date time)
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (null != client) {
				client.close();
			}
		}
	}
	
	public static final HashMap<String,ObjectId> COUNTRY = new HashMap<String,ObjectId>();
	
	public static List<DBObject> retrieveCountryList(File file) {
		HashMap<String,DBObject> map = new HashMap<String,DBObject>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = in.readLine();	// skip first line: header
			while (null != (line = in.readLine())) {
				line = line.trim();
				if (0 == line.length()) continue;
				
				String id = line.substring(0, 12).trim();
				if (map.containsKey(id)) continue;
				String name = line.substring(12).trim();
				
//				BasicDBObject country = new BasicDBObject();
				BasicDBObject country = country(name);
				ObjectId _id = new ObjectId();
				country.append("_id", _id).append("id", id);
				map.put(id, country);

				COUNTRY.put(id, _id);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch(Exception e) {}
				in = null;
			}
		}
		return new ArrayList<DBObject>(map.values());
	}
	
	public static final String[]	STATION_COLUMN	= {
//		0       1       2       3          4        5       6      7      8      9        10
		"usaf", "wban", "name", "country", "state", "icao", "lat", "lng", "elv", "beign", "end"
	};
	public static List<DBObject> retrieveStationList(File file) {
		List<DBObject> list = new ArrayList<DBObject>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = in.readLine();	// skip first line: header
			while (null != (line = in.readLine())) {
				line = line.trim();
				if (0 == line.length()) continue;
				
				String[] cols = line.split(",");
				
				BasicDBObject station = station(TextUtil.strip(cols[0], '"'), TextUtil.strip(cols[1], '"'));
				for (int i = 2, len = STATION_COLUMN.length; i < len; ++i) {
					String val = TextUtil.strip(cols[i], '"').trim();
					if (val.length() > 0) {
						if (i >= 6 && i <= 8) {
							BasicDBObject geo = (BasicDBObject) station.get("geo");
//							geo.append("_"+STATION_COLUMN[i], val);
							geo.append(STATION_COLUMN[i], TextUtil.doubleValue(val));
						}
						else {
							station.append(STATION_COLUMN[i], val);
						}
					}
				}
				String country = station.getString(STATION_COLUMN[3]);
				ObjectId country_id = COUNTRY.get(country);
				if (null != country_id) {
					station.append(STATION_COLUMN[3], country_id);
				}
				else {
					station.remove(STATION_COLUMN[3]);
				}
				list.add(station);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch(Exception e) {}
				in = null;
			}
		}
		return list;
	}

	public static BasicDBObject country(String name) {
		BasicDBObject country = new BasicDBObject();
		country.append("name", name)
			.append("station", new BasicDBList());
		return country;
	}
	
	public static BasicDBObject station(String usaf, String wban) {
		BasicDBObject station = new BasicDBObject();
		station.append("usaf", usaf)
			.append("wban", wban)
//			.append("state", "")
//			.append("icao", "")
			.append("geo", new BasicDBObject())
			.append("history", new BasicDBObject());
		return station;
	}

	public static BasicDBObject geo(double lat, double lng, double elv) {
		BasicDBObject geo = new BasicDBObject();
		geo.append("lat", lat)
			.append("lng", lng)
			.append("elv", elv);
		return geo;
	}
	
	public static BasicDBObject record(java.sql.Date date, java.sql.Time time) {
		BasicDBObject record = new BasicDBObject();
		record.append("date", date)
			.append("time", time);
		return record;
	}
	
}
