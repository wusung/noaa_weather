package com.jfetek.demo.weather.spider;

import java.io.File;

import com.jfetek.common.util.ArrayUtil;
import com.jfetek.demo.weather.Utils;

public class Spiders {

	public static final String[]	SPIDER_NAME	= {
		"wiki",
		"weather",
		"test"
	};
	
	public static void main(String[] args) throws Throwable {
		if (null == args || 0 == args.length) {
			System.out.println("usage: java Spiders spidername [renew]");
			System.exit(1);
		}
		
		boolean renew = false;
		int spider = -1;
		for (int i = 0; i < args.length; ++i) {
			String arg = args[i];
			if ("renew".equals(arg)) {
				renew = true;
			}
			else {
				spider = ArrayUtil.indexOf(SPIDER_NAME, arg);
			}
		}
		if (-1 == spider) {
			System.out.println("available spider: wiki|weather");
			System.exit(1);
		}

		File file = new File("system.setup");
		if (args.length > 0 && "renew".equals(args[0])) {
			System.out.println("renew system-setup> "+file.delete());
		}
		switch (spider) {
			case 0:	// wiki
				WikiSpider.main(args);
				break;
			case 1:	// weather
				WeatherSpider.main(args);
				break;
			case 2:	// test
				Utils.main(args);
			default:
				System.out.println("unknown spider["+spider+"]: "+SPIDER_NAME[spider]);
		}
	}

}
