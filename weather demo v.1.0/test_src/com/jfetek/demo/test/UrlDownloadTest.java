package com.jfetek.demo.test;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import com.jfetek.demo.weather.Console;

public class UrlDownloadTest {

	public static void main(String[] args) throws Exception {
		Console.startup();
		
		//String s = "ftps://root@192.168.3.11/opt/apache-tomcat-6.0.35/jfetek/weather-demo.war";
		String s = "ftp://ftp.ncdc.noaa.gov/pub/data/noaa/1902";
//		String s = "ftp://myfresh:JFUYL5#@HCTRT.HCT.COM.TW/IN/04561010016買新鮮低20141218.txt";
		File tmp = Console.createTempFile(".url.download");
		FileOutputStream out = null;
		ReadableByteChannel ch = null;
		HttpURLConnection conn = null;
		try {
			URL url = new URL(s);
			ch = Channels.newChannel(url.openStream());
			out = new FileOutputStream(tmp);
			out.getChannel().transferFrom(ch, 0, Long.MAX_VALUE);
			out.flush();
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (null != ch) {
				try {
					ch.close();
				} catch(Exception e) {}
				ch = null;
			}
			if (null != out) {
				try {
					out.close();
				} catch(Exception e) {}
				out = null;
			}
			if (null != conn) {
				try {
					conn.disconnect();
				} catch(Exception e) {}
				conn = null;
			}
		}
		
		File file = new File("C:/Users/小補/Desktop/tmp.ftp");
		System.out.println( file.delete() );
		System.out.println( tmp.renameTo(file) );
		
		Console.shutdown();
	}
}
