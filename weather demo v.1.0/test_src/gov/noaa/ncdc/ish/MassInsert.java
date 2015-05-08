package gov.noaa.ncdc.ish;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

import com.jfetek.common.db.ConnectionWrapper;
import com.jfetek.demo.weather.Console;

public class MassInsert {
	
	static ConnectionWrapper wrapper;
	static PreparedStatement ins;
	static PreparedStatement count;
	static PreparedStatement del;

	public static void main(String[] args) throws Exception {
		Console.startup();
		
		wrapper = Console.database.leaseConnection("");
		ins = wrapper.prepare("insert ignore into `all_2014` values (?,?,?,?,?,?,?,?,?,?,"
															+"?,?,?,?,?,?,?,?,?,?,"
															+"?,?,?,?,?,?,?,?,?,?,"
															+"?,?,?,?)");
		count = wrapper.prepare("select count(*) from `all_2014` where usaf=? and wban=?");
		del = wrapper.prepare("delete from `all_2014` where usaf=? and wban=?");
		
        File dir = new File("\\\\192.168.3.33/bu/weather/2014.out");
        File[] files = dir.listFiles();
        for (int i = 0, len = files.length; i < len; ++i) {
        	File file = files[i];
        	String name = file.getName();
        	String usaf = name.substring(0, 6);
//        	usaf = "999999".equals(usaf)? "******" : usaf;
        	String wban = name.substring(7, 12);
        	wban = "99999".equals(wban)? "*****" : wban;
        	int cntLine = countRawData(file);
        	int cntRow = countRow(usaf, wban);
        	if (cntLine != cntRow) {
            	System.out.println("#"+(1+i)+"> "+name+" ... lines["+cntLine+"] rows["+cntRow+"]");
        		delete(usaf, wban);
            	processFile(file);
        	}
        	else {
//    			System.out.println("total "+cntLine+" lines already processed");
        	}
        }
        
        if (null != wrapper) {
        	wrapper.release();
        }
        
        Console.shutdown();
	}
	
	public static int countRawData(File file) throws IOException {
		HashSet<String> keys = new HashSet<String>(15000);
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
			String line;
			while (null != (line = in.readLine())) {
				line = line.trim();
				if (0 == line.length() || line.startsWith("USAF  WBAN")) continue;

				String key = line.substring(13, 25);
				keys.add(key);

			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch(Exception e) {}
				in = null;
			}
		}
		return keys.size();
	}
	
	public static void processFile(File file) throws SQLException {
		int cntLine = 0;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
			String line;
			while (null != (line = in.readLine())) {
				line = line.trim();
				if (0 == line.length() || line.startsWith("USAF  WBAN")) continue;
				++cntLine;
				String[] cols = line.split("\\s+");
				
				insert(cols);
			}
			System.out.println("total "+cntLine+" lines processed");
		} catch(IOException e) {
			System.out.println("line#"+cntLine+" error...");
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch(Exception e) {}
				in = null;
			}

			if (cntBatch > 0) {
				ins.executeBatch();
				cntBatch = 0;
			}
		}
	}
	
	static int cntBatch = 0;
	public static void insert(String[] cols) throws SQLException {
		ins.setString(1, cols[0]);
		ins.setString(2, cols[1]);
		String str = cols[2];
		String d_record = str.substring(0, 4) + "-" + str.substring(4, 6) + "-" + str.substring(6, 8);
		String t_record = str.substring(8, 10) + ":" + str.substring(10, 12) + ":00";
		ins.setString(3, d_record);
		ins.setString(4, t_record);
		for (int i = 3, len = cols.length; i < len; ++i) {
			ins.setString(2+i, cols[i]);
		}
//		System.out.println(ins);
//		ins.executeUpdate();
		ins.addBatch();
		if (++cntBatch >= 100) {
			ins.executeBatch();
			cntBatch = 0;
		}
	}

	public static int countRow(String usaf, String wban) throws SQLException {
		int lines = 0;
		ResultSet rs = null;
		try {
			count.setString(1, usaf);
			count.setString(2, wban);
			rs = count.executeQuery();
			if (rs.next()) {
				lines = rs.getInt(1);
			}
			
		} finally {
			if (null != rs) {
				try {
					rs.close();
				} catch(Exception e) {}
				rs = null;
			}
		}
		return lines;
	}
	
	public static int delete(String usaf, String wban) throws SQLException {
		del.setString(1, usaf);
		del.setString(2, wban);
		return del.executeUpdate();
	}
}
