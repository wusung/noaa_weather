package com.jfetek.demo.weather.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.jfetek.common.time.Date;
import com.jfetek.common.util.TextUtil;
import com.jfetek.demo.weather.Console;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;

public class GeoTask {

	public static void main(String[] args) throws Exception {
		try {
			Console.startup();
			File dir = new File("\\\\192.168.3.208//Ramdisk");
			File file = new File(dir, "allCountries.zip");
			ZipFile zip = null;
			try {
				zip = new ZipFile(file, ZipFile.OPEN_READ);
				Enumeration<? extends ZipEntry> enu = zip.entries();
				while (enu.hasMoreElements()) {
					ZipEntry ze = enu.nextElement();
					System.out.println(ze);
					InputStream in = zip.getInputStream(ze);
					processCountry(in);
				}
			} catch(IOException e) {
				e.printStackTrace();
			} finally {
				if (null != zip) {
					try {
						zip.close();
					} catch(Exception e) {}
					zip = null;
				}
			}
		} finally {
			Console.shutdown();
		}
	}
//	public static void _main(String[] args) throws Exception {
//		try {
//			Console.startup();
//			
//			File dir = new File("\\\\192.168.3.208//Ramdisk");
//			File file = new File(dir, "sugihara.zip");
//			
//			ZipInputStream zip = null;
//			try {
//				zip = new ZipInputStream(new FileInputStream(file));
//				long ts = 0L;
//				int counter = 0;
//				for (ZipEntry entry = zip.getNextEntry(); null != entry; entry = zip.getNextEntry()) {
////					System.out.println(entry.getName());
//					ExecuteTimeMeasure etm = ExecuteTimeMeasure.start();
//					File tmp = uncompress(zip);
//					etm.end();
//					ts += etm.getExecTime();
//					++counter;
//					File download = Console.getUploadFile("sugihara/"+entry.getName());
//					FileUtils.moveFile(tmp, download);
//				}
//				System.out.println(1.0*ts/counter);
//			} catch(IOException e) {
//				e.printStackTrace();
//			} finally {
//				if (null != zip) {
//					try {
//						zip.close();
//					} catch(Exception e) {}
//					zip = null;
//				}
//			}
//			
//		} finally {
//			Console.shutdown();
//		}
//	}
	
	public static final String[]	COLUMNS	= {
	//  0     1       2             3            4      5      6
		"id", "name", "ascii_name", "alt_names", "lat", "lng", "feature_class",
	//  7               8               9      10             11
		"feature_code", "country_code", "cc2", "admin_code1", "admin_code2",
	//  12             13             14      15     16     17          18
		"admin_code3", "admin_code4", "popu", "elv", "dem", "timezone", "mod_date"
	};
	static void processCountry(InputStream is) throws IOException {
		BufferedReader in = null;
		String line;
		int cntLine = 0;
		int lenCol = COLUMNS.length;
		int lmtBulk = 10000;
		try {
			DB db = Console.cache.getDB("geo");
			DBCollection country = db.getCollection("country");
			BulkWriteOperation bulk = country.initializeUnorderedBulkOperation();
			in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			while (null != (line = in.readLine())) {
				++cntLine;
				String[] cols = line.split("\t");
//				emptyToNull(cols);
				BasicDBObject query = new BasicDBObject("_id", TextUtil.intValue(cols[0]));
				BasicDBObjectBuilder update = BasicDBObjectBuilder.start()
						.push("$set")
							.add("name", cols[1])
							.add("ascii_name", cols[2])
							.add("alt_names", cols[3].split(","))
							.add("loc", new double[] {
									TextUtil.doubleValue(cols[5]),
									TextUtil.doubleValue(cols[4])
								})
							.add("feature_class", cols[6])
							.add("feature_code", cols[7])
							.add("country_code", cols[8])
							.add("cc2", cols[9].split(","))
							.add("admin_code1", cols[10])
							.add("admin_code2", cols[11])
							.add("admin_code3", cols[12])
							.add("admin_code4", cols[13])
							.add("population", TextUtil.longValue(cols[14]))
							.add("elv", TextUtil.intValue(cols[15]))
							.add("dem", TextUtil.intValue(cols[16]))
							.add("timezone", cols[17])
							.add("modify_date", Date.valueOf(cols[18]).toSqlDate())
						.pop();
//				bulk.find(query).upsert().updateOne(update.get());
				BasicDBObject tmp =(BasicDBObject)update.get().get("$set"); 
				query.putAll(tmp.toMap());
				bulk.insert(query);
				if (0 == cntLine % lmtBulk) {
					BulkWriteResult result = bulk.execute();
					System.out.println("bulk#"+(cntLine/lmtBulk)+"> "+result);
					
					bulk = country.initializeUnorderedBulkOperation();
				}
			}
			if (0 != cntLine % lmtBulk) {
				BulkWriteResult result = bulk.execute();
				System.out.println(result);
			}
		} catch(IOException e) {
			System.err.println("line "+cntLine);
			e.printStackTrace();
		} catch(Exception e) {
			System.err.println("line "+cntLine);
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch(Exception e) {}
				in = null;
			}
		}
	}
	
	public static void emptyToNull(String[] sarr) {
		for (int i = 0, len = sarr.length; i < len; ++i) {
			if (TextUtil.noValueOrBlank(sarr[i])) {
				sarr[i] = null;
			}
		}
	}
	
	static File uncompress(ZipInputStream in) throws IOException {
		File tmp = Console.getTempFile(".unzip");
		FileOutputStream out = null;
		ReadableByteChannel ch = null;
		try {
			ch = Channels.newChannel(in);
			out = new FileOutputStream(tmp);
			out.getChannel().transferFrom(ch, 0, Long.MAX_VALUE);
			out.flush();
//			ResourceUtil.output(in, tmp);
		} finally {
//			if (null != ch) {
//				try {
//					ch.close();
//				} catch(Exception e) {}
//				ch = null;
//			}
			if (null != out) {
				try {
					out.close();
				} catch(Exception e) {}
				out = null;
			}
			in.closeEntry();
		}
		return tmp;
	}
}
