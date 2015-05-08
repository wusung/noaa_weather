package com.jfetek.demo.weather.data;

import com.jfetek.common.util.TextUtil;

public interface Filter<T> {
	
	public static final Filter<FtpFileInfo>	YEAR_DIR_FILTER	= new Filter<FtpFileInfo>() {
		public boolean accept(FtpFileInfo info) {
			if (null == info) return false;
			if (FtpFileInfo.FileType.Directory != info.type) return false;
			int year = TextUtil.intValue(info.name);
			return (year > 0);
		}
	};

//	public static final Filter<FTPFile>	YEAR_DIR_FILTER	= new Filter<FTPFile>() {
//		public boolean accept(FTPFile file) {
//			if (null == file) return false;
//			if (!file.isDirectory()) return false;
////			String name = file.getName().replace("\\/+", "");
//			int year = TextUtil.intValue(file.getName());
//			return (year > 0);
//		}
//	};

	public boolean accept(T data);
}
