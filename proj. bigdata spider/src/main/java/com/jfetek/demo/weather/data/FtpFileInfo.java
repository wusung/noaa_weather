package com.jfetek.demo.weather.data;

import java.io.File;
import java.net.URI;

import org.apache.commons.net.ftp.FTPFile;

public class FtpFileInfo {
	
	public enum FileType {
		File,
		Directory,
		Link
	}

	public final URI uri;
	public final FileType type;
	public final int size;
	public final long lastModify;
	public final String name;
	protected File tmp;
	protected FtpFileInfo(URI uri, FileType type, int size, long last_modify, String name) {
		this.uri = uri;
		this.type = type;
		this.size = size;
		this.lastModify = last_modify;
		this.name = name;
		this.tmp = null;
	}
	
	public static FtpFileInfo create(URI uri, FileType type, int size) {
		String path = uri.getPath();
		File file = new File(path);
		return new FtpFileInfo(uri, type, size, -1L, file.getName());
	}
	
	public static FtpFileInfo of(URI path, FTPFile file) {
		FileType type;
		String filename = file.getName();
		if (file.isDirectory()) {
			type = FileType.Directory;
		}
		else if (file.isFile()) {
			type = FileType.File;
		}
		else if (file.isSymbolicLink()) {
			type = FileType.Link;
		}
		else {
			throw new RuntimeException("unknown file type: "+file);
		}
		
		int size = (int) file.getSize();
		
		long ts = file.getTimestamp().getTimeInMillis();
		
		String p = path.getPath();
		if (!p.endsWith("/")) {
			path = path.resolve(p+"/");
		}
		URI uri = path.resolve(filename);
		return new FtpFileInfo(uri, type, size, ts, filename);
	}
	
//	public static FtpFileInfo parse(URI path, String s) {
//		String[] fields = s.split("\\s+");
//		
//		FileType type;
//		String filename = fields[8];
//		switch (fields[0].charAt(0)) {
//			case '-':
//				type = FileType.File;
//				break;
//			case 'd':
//				type = FileType.Directory;
//				filename += "/";
//				break;
//			case 'l':
//				type = FileType.Link;
//				break;
//			default:
//				throw new RuntimeException("unknown file type: "+fields[0]);
//		}
//		
//		int size = TextUtil.intValue(fields[4]);
//		
//		long ts;
//		if (fields[7].indexOf(":") == -1) {
//			SimpleDateFormat sdf = new SimpleDateFormat("MMMddyyyy", Locale.US);
//			try {
//				Date date = sdf.parse(ArrayUtil.join(fields, 5, 3, ""));
//				ts = date.getTime();
//			} catch (ParseException e) {
//				e.printStackTrace();
//				return null;
//			}
//		}
//		else {
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMMddHH:mm", Locale.US);
//			try {
//				Date date = sdf.parse(com.jfetek.common.time.Date.today().year+ArrayUtil.join(fields, 5, 3, ""));
//				ts = date.getTime();
//			} catch (ParseException e) {
//				e.printStackTrace();
//				return null;
//			}
//		}
//		
//		URI uri = path.resolve(filename);
//		return new FtpFileInfo(uri, type, size, ts, fields[8]);
//	}
	
	public boolean isDownloaded() {
		return null!=this.tmp;
	}
	public boolean setFile(File file) {
		if (file.length() != this.size) return false;
		this.tmp = file;
		return true;
	}
	public File getFile() {
		return this.tmp;
	}
	
	public static void main(String[] args) throws Exception {
		URI uri = new URI("ftp://ftp.ncdc.noaa.gov/pub/data/noaa/2014/");
		uri = uri.resolve(uri.getPath()+"/");
		System.out.println(uri);
		URI uri2 = uri.resolve("filename");
		System.out.println(uri2);
	}
	
//	public static void main(String[] args) throws Exception {
//		int year = 2004;
//		String s = "ftp://ftp.ncdc.noaa.gov/pub/data/noaa/"+year+"/";
//		URI uri = new URI(s);
//
//		File dir = new File("\\\\192.168.3.33/bu-disk-c/weather/ready/"+year+"/raw/");
//		BufferedReader in = null;
//		try {
//			in = new BufferedReader(new InputStreamReader(uri.toURL().openStream()));
//			String line;
//			while (null != (line = in.readLine())) {
////				System.out.println(line);
//				FtpFileInfo info = FtpFileInfo.parse(uri, line);
//				if (null == info) continue;
//				
//				File file = new File(dir, info.name);
//				if (!file.exists()) {
//					System.out.println("["+info.size+"] "+info.uri);
//					System.out.println("> yet download");
//				}
//				else if (file.length() != info.size) {
//					System.out.println("["+info.size+"] "+info.uri);
//					System.out.println("> file modified");
//				}
//				else {
//				}
//			}
//		} catch(FileNotFoundException e) {
//			e.printStackTrace();
//		} catch(IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (null != in) {
//				try {
//					in.close();
//				} catch(Exception e) {}
//				in = null;
//			}
//		}
//	}
}
