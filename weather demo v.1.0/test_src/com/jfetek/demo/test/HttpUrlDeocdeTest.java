package com.jfetek.demo.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jfetek.common.SystemDefault;
import com.jfetek.common.util.EasyCodecUtil;
import com.jfetek.common.util.HttpUtil;

public class HttpUrlDeocdeTest {

	public static void main(String[] args) throws Exception {
		String s = "0xE60x880x91";
		System.out.println(decode(s));
	}
	
	public static String decode(String s) throws Exception {
		if (s.matches("(?i).*\\&\\#\\d+\\;.*")) {
			// html entity decimal
			System.out.println("html entity decimal");
			return EasyCodecUtil.htmlCharEntityDecode(s);
		}
		else if (s.matches("(?i).*\\&\\#x[\\da-f]+\\;.*")) {
			// html entity hex
			System.out.println("html entity hex");
			return EasyCodecUtil.htmlCharEntityDecode(s);
		}
		else if (s.matches("(?i).*\\%[\\da-f]{2}.*")) {
			// url escape
			System.out.println("url escape");
			return HttpUtil.decode(s);
		}
		else if (s.matches("(?i)(\\/x[\\da-f]{2})+$")) {
			// hex
			System.out.println("/x hex");
			return new String(EasyCodecUtil.hexDecode(s.replaceAll("(?i)\\/x", "")), SystemDefault.CHARSET_VALUE);
		}
		else if (s.matches("(?i).*0x[\\da-f]{2}.*")) {
			// hex: 0xHH
			System.out.println("0x hex");
			Pattern p = Pattern.compile("0x[\\da-f]{2}+", Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(s);
			StringBuilder tmp = new StringBuilder(s.length());
			boolean changed = false;
			int idxBegin = 0;
			while (m.find()) {
				changed = true;
				tmp.append(s.substring(idxBegin, m.start()));
				idxBegin = m.end();
				String s1 = new String(EasyCodecUtil.hexDecode(s.substring(2+m.start(), m.end())), SystemDefault.CHARSET_VALUE);
				tmp.append(s1);
			}
			tmp.append(s.substring(idxBegin));
			return changed? tmp.toString() : s;
		}
		else if (s.matches("(?i).*u\\+[\\da-f]{4}.*")) {
			// unicode code point
			System.out.println("unicode code point");
		}
		else if (s.matches("(?i).*\\\\u[\\da-f]+.*")) {
			// unicode
			System.out.println("unicode");
		}
		else if (s.matches("^([\\da-f]{2})+$")) {
			// hex
		}
		else {
			// unknown
			System.out.println("unknown");
		}
		return s;
	}
}
