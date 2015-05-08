package com.jfetek.common.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jfetek.common.SystemDefault;
import com.jfetek.common.VariableSetup;
import com.jfetek.common.data.VariableExpression;

public class TextUtil {

	private TextUtil() {
	}
	
	public static final String	WHITESPACE	=
			
			// whitespace				// line terminators
			" \t\\x0B\\f\\xA0\ufeff"	+ "\n\r\u2028\u2029"
			
		    // unicode category "Zs" space separators
		    + "\u1680\u180e\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007"
		    + "\u2008\u2009\u200a\u200b\u202f\u205f\u3000";
	
	public static final Pattern	WHITESPACE_PATTERN	= Pattern.compile("["+WHITESPACE+"]+");
	public static final Pattern TRIM_PATTERN		= Pattern.compile("^["+WHITESPACE+"]+|["+WHITESPACE+"]+$");

	
	public static String trim(String s) {
		if (SystemDefault.NULL.equals(s) || 0 == s.length()) return s;
		return TRIM_PATTERN.matcher(s).replaceAll("");
	}
	
	public static String normalizeWhitespace(String s) {
		return normalizeWhitespace(s, " ");
	}
	public static String normalizeWhitespace(String s, String normal_space) {
		return WHITESPACE_PATTERN.matcher(s).replaceAll(normal_space);
	}

	public static boolean noValue(String s) {
//		return (s==null||"".equals(s));
//		return (s==null||0==s.length());
		return (SystemDefault.NULL.equals(s) || 0 == s.length());
	}
	
	public static boolean hasValue(String s) {
		return (!noValue(s));
	}
	
	public static boolean isEmpty(String s) {
//		return "".equals( s );
		return 0==s.length();
	}
	
	public static boolean isBlank(String s) {
//		if (s == null) return false;
		if (SystemDefault.NULL.equals(s)) return false;
//		return ("".equals( s.trim() ));
//		return 0==s.trim().length();
		return 0==trim(s).length();
	}
	
	public static boolean noValueOrBlank(String s) {
//		return (null == s || 0 == s.length() || 0 == s.trim().length());
//		return (SystemDefault.NULL.equals(s) || 0 == s.trim().length());
		return (SystemDefault.NULL.equals(s) || 0 == trim(s).length());
	}
	
	public static boolean isQuoted(String s) {
		return isQuoted(s, '"');
	}
	
	public static boolean isQuoted(String s, char q) {
//		if (null == s || s.length() < 2) return false;
		if (SystemDefault.NULL.equals(s) || s.length() < 2) return false;
		return (s.charAt(0) == q && s.charAt(s.length()-1) == q);
	}
	
	public static boolean isQuoted(String s, String q) {
//		if (null == s || s.length() < 2) return false;
		if (SystemDefault.NULL.equals(s) || s.length() < 2) return false;
		return (s.startsWith(q) && s.endsWith(q));
	}
	
	public static String nullToEmpty(String s) {
//		return null==s? "" : s;
		return SystemDefault.NULL.equals(s)? "" : s;
	}
	
	public static String nullToEmpty(Object obj) {
//		return null==obj? "" : obj.toString();
		return SystemDefault.NULL.equals(obj)? "" : obj.toString();
	}
	
	public static String nullToDefault(String s, String default_value) {
//		return null==s? default_value : s;
		return SystemDefault.NULL.equals(s)? default_value : s;
	}
	
	public static String nullToDefault(Object obj, String default_value) {
//		return null==obj? default_value : obj.toString();
		return SystemDefault.NULL.equals(obj)? default_value : obj.toString();
	}
	
	public static String quote(String s) {
		return quote(s, '"');
	}
	
	public static String quote(String s, char q) {
		StringBuilder tmp = new StringBuilder(s.length()+2);
		tmp.append(q).append(s).append(q);
		return tmp.toString();
	}
	
	public static String quote(String s, String q) {
		StringBuilder tmp = new StringBuilder(s.length()+2*q.length());
		tmp.append(q).append(s).append(q);
		return tmp.toString();
	}
	
	public static String removeQuote(String s) {
		return removeQuote(s, '"');
	}
	
	public static String removeQuote(String s, char q) {
//		if (null == s || s.length() < 2) return s;
		if (SystemDefault.NULL.equals(s) || s.length() < 2) return s;
		return (s.charAt(0) == q && s.charAt(s.length()-1) == q)?
			s.substring(1, s.length()-1) : s;
	}
	
	public static String removeQuote(String s, String q) {
//		if (null == s || s.length() < 2*q.length()) return s;
		if (SystemDefault.NULL.equals(s) || s.length() < 2*q.length()) return s;
		return (s.startsWith(q) && s.endsWith(q))?
			s.substring(1, s.length()-1) : s;
	}
	
	public static String strip(String s, char c) {
//		if (null == s || 0 == s.length()) return s;
		if (SystemDefault.NULL.equals(s) || 0 == s.length()) return s;
		int offset = 0;
		int len = s.length();
//		s.trim();
		while ((offset < len) && (s.charAt(offset) == c)) {
			offset++;
		}
		while ((offset < len) && (s.charAt(len - 1) == c)) {
		    len--;
		}
		return ((offset > 0) || (len < s.length())) ? s.substring(offset, len) : s;
	}

	public static boolean booleanValue(String s) {
		return booleanValue(s, SystemDefault.BOOLEAN_VALUE);
	}
	
	public static boolean booleanValue(String s, boolean default_value) {
//		if (s == null) return default_value;
		if (SystemDefault.NULL.equals(s)) return default_value;
		if ("true".equalsIgnoreCase(s) || "1".equals(s)) return true;
		if ("false".equalsIgnoreCase(s) || "0".equals(s)) return false;
		return default_value;
	}
	
	
	public static int intValue(String s) {
		return intValue(s, SystemDefault.INT_VALUE);
	}
	
	public static int intValue(String s, int default_value) {
//		if (s == null || 0 == s.length()) return default_value;
		if (SystemDefault.NULL.equals(s) || 0 == s.length()) return default_value;
		try {
			return Integer.parseInt( s );
		} catch(Exception e) {
			try {
				Double d = Double.valueOf(s);
				return d.intValue();
			} catch(Exception e2) { }
		}
		return default_value;
	}
	
	public static int intValue(String s, int radix, int default_value) {
//		if (s == null || "".equals(s)) return default_value;
//		if (s == null || 0 == s.length()) return default_value;
		if (SystemDefault.NULL.equals(s) || 0 == s.length()) return default_value;
		try {
			return Integer.parseInt( s , radix );
		} catch(Exception e) {
			return default_value;
		}
	}
	
	
	public static long longValue(String s) {
		return longValue(s, SystemDefault.RADIX, SystemDefault.LONG_VALUE);
	}
	
	public static long longValue(String s, long default_value) {
//		if (null == s || 0 == s.length()) return default_value;
		if (SystemDefault.NULL.equals(s) || 0 == s.length()) return default_value;
		try {
			return Long.parseLong( s );
		} catch(Exception e) {
			try {
				Double d = Double.valueOf(s);
				return d.longValue();
			} catch(Exception e2) {}
		}
		return default_value;
	}
	
	public static long longValue(String s, int radix, long default_value) {
//		if (s == null || "".equals(s)) return default_value;
//		if (s == null || 0 == s.length()) return default_value;
		if (SystemDefault.NULL.equals(s) || 0 == s.length()) return default_value;
		try {
			return Long.parseLong( s , radix );
		} catch(Exception e) {
			return default_value;
		}
	}
	
	public static double doubleValue(String s) {
		return doubleValue(s, SystemDefault.DOUBLE_VALUE);
	}
	public static double doubleValue(String s, double default_value) {
//		if (s == null || "".equals(s)) return default_value;
//		if (s == null || 0 == s.length()) return default_value;
		if (SystemDefault.NULL.equals(s) || 0 == s.length()) return default_value;
		try {
			return Double.parseDouble(s);
		} catch(Exception e) {
			return default_value;
		}
	}
		
	
	public static String repeat(String s, int times) {
//		if (times <= 0 || "".equals(s)) return "";
		if (times <= 0 || 0 == s.length()) return "";
		if (times == 1) return s;
//		if (s == null) s = SystemDefault.NULL_STRING;
		if (SystemDefault.NULL.equals(s)) s = SystemDefault.NULL_STRING;
		StringBuilder sb = new StringBuilder( s.length()*times );
		for (int i = 0; i < times; ++i) {
			sb.append( s );
		}
		return sb.toString();
	}
	
	public static String format(String source, Object[] args) {
		// replace all {index[,default-value]}
//		if (null == source) return null;
		if (SystemDefault.NULL.equals(source)) return null;
//		if (null == args || 0 == args.length) return source;
		if (SystemDefault.NULL.equals(args) || 0 == args.length) return source;
		Pattern pattern = Pattern.compile("\\{(\\d+)(\\,([^\\}]+))?\\}");
		Matcher m = pattern.matcher(source);
		if (!m.find()) return source;
		m.reset();
		
		int start = 0;
		StringBuilder s = new StringBuilder(source.length());
		while (m.find(start)) {
			s.append(source.subSequence(start, m.start()));
			int idx = TextUtil.intValue(m.group(1), -1);
			if (idx < 0 || idx >= args.length) {
				s.append(m.group(3));
			}
			else {
				s.append(args[idx]);
			}
			start = m.end();
		}
		s.append(source.subSequence(start, source.length()));
		
		return s.toString();
	}

	public static String format(String source, Map<String,String> args) {
		// replace all ${key[,default-value]}
//		if (null == source) return null;
		if (SystemDefault.NULL.equals(source)) return null;
//		if (null == args || 0 == args.size()) return source;
		if (SystemDefault.NULL.equals(args) || 0 == args.size()) return source;
		Pattern pattern = Pattern.compile("\\$\\{([\\w\\-\\.]+)(\\,([^\\}]+))?\\}");
		Matcher m = pattern.matcher(source);
		if (!m.find()) return source;
		m.reset();
		
		int start = 0;
		StringBuilder s = new StringBuilder(source.length());
		while (m.find(start)) {
			s.append(source.subSequence(start, m.start()));
			String key = m.group(1);
			String value = args.get(key);
			if (null == value) {
				s.append(m.group(3));
			}
			else {
				s.append(value);
			}
			start = m.end();
		}
		s.append(source.subSequence(start, source.length()));
		
		return s.toString();	
	}

	public String format(String pattern, VariableSetup setup) {
//		if (null == pattern) return null;
		if (SystemDefault.NULL.equals(pattern)) return null;
//		if (null == setup) return pattern;
		if (SystemDefault.NULL.equals(setup)) return pattern;
		Matcher m = VariableExpression.PATTERN.matcher(pattern);
		if (!m.find()) return pattern;
		m.reset();
		
		int start = 0;
		StringBuilder s = new StringBuilder(pattern.length());
		while (m.find(start)) {
			s.append(pattern.subSequence(start, m.start()));
			String g = m.group();
			VariableExpression var = VariableExpression.parse(g);
			String value = setup.val(var);
			if (null == value) {
				s.append(g);
			}
			else {
				s.append(value);
			}
			start = m.end();
		}
		s.append(pattern.subSequence(start, pattern.length()));
		
		return s.toString();	
	}
	
	public static String capitalize(String s) {
		StringBuilder buff = new StringBuilder(s.length());
		buff.append(Character.toUpperCase( s.charAt(0) ))
			.append(s.substring(1).toLowerCase());
		return buff.toString();
	}
	
	public static String camelize(String s) {
		// TODO
		//	http://api.prototypejs.org/language/String/prototype/camelize/
		return s;
	}
	
	public static String dasherize(String s) {
		// TODO
		//	http://api.prototypejs.org/language/String/prototype/dasherize/
		return s;
	}
	
	public static String underscore(String s) {
		// TODO
		//	http://api.prototypejs.org/language/String/prototype/underscore/
		return s;
	}
	
	
	public static String escape(String txt) {
		boolean change = false;
		StringBuilder tmp = new StringBuilder(txt.length()+16);
		char[] arr = txt.toCharArray();
		for (int i = 0, len = arr.length; i < len; ++i) {
			char c = arr[i];
			switch (c) {
				case '\\':
				case '\'':
				case '\"':
					change = true;
					tmp.append('\\').append(c);
					break;
				case '\n':
					change = true;
					tmp.append('\\').append('n');
					break;
				case '\r':
					change = true;
					tmp.append('\\').append('r');
					break;
				case '\t':
					change = true;
					tmp.append('\\').append('t');
					break;
				case '\f':
					change = true;
					tmp.append('\\').append('f');
					break;
				default:
					tmp.append(c);
			}
		}
		return change? tmp.toString() : txt;
	}
	
	public static String unescape(String txt) {
		boolean change = false;
		StringBuilder tmp = new StringBuilder(txt.length());
		char[] arr = txt.toCharArray();
		for (int i = 0, len = arr.length; i < len; ++i) {
			char c = arr[i];
			if ('\\' == c) {
				c = arr[++i];
				switch (c) {
					case 'n':
						change = true;
						c = '\n';
						break;
					case 'r':
						change = true;
						c = '\r';
						break;
					case 't':
						change = true;
						c = '\t';
						break;
					case 'f':
						change = true;
						c = '\f';
						break;
					case '\\':
						change = true;
						break;
					case '\'':
						change = true;
						break;
					case '"':
						change = true;
						break;
					default:
						c = arr[--i];
				}
			}
			tmp.append(c);
		}
		return change? tmp.toString() : txt;
	}

//	public static int occurrence(String str, String sym, boolean repeat) {
//		int count = 0;
//		for (int offset = 0; (offset = str.indexOf(sym, offset)) != -1; ++offset) {
//			++count;
//		}
//		return count;
//	}
	
	// Longest Common Subsequence
	// reference:
	//	http://introcs.cs.princeton.edu/java/96optimization/LCS.java.html
	public static String findLCSubsequence(String s1, String s2) {
//		if (null == s1 || null == s2) return null;
		if (SystemDefault.NULL.equals(s1) || SystemDefault.NULL.equals(s2)) return null;
		int len1 = s1.length();
		int len2 = s2.length();
		if (0 == len1 || 0 == len2) return "";
		char[] c1 = s1.toCharArray();
		char[] c2 = s2.toCharArray();
		int[][] opt = new int[1+len1][];
		for (int i = 0; i <= len1; ++i) opt[i] = new int[1+len2];

        // compute length of LCS and all subproblems via dynamic programming
		for (int i = len1-1; i >= 0; --i) {
			for (int j = len2-1; j >= 0; --j) {
				opt[i][j] = c1[i]==c2[j]? (opt[i+1][j+1] + 1) : (Math.max(opt[i+1][j], opt[i][j+1]));
			}
		}

        // recover LCS itself and output to tmp
		StringBuilder tmp = new StringBuilder( len1>len2? len2 : len1 );
		for (int i = 0, j = 0; i < len1 && j < len2;) {
			if (c1[i] == c2[j]) {
				tmp.append(c1[i]);
				++i;
				++j;
			}
			else if (opt[i+1][j] >= opt[i][j+1]) {
				++i;
			}
			else {
				++j;
			}
		}
		
		return tmp.toString();
	}

	// Longest Common Substring
	// reference:
	//	http://karussell.wordpress.com/2011/04/14/longest-common-substring-algorithm-in-java/
	public static String findLCSubstring(String s1, String s2) {
//		if (null == s1 || null == s2) return null;
		if (SystemDefault.NULL.equals(s1) || SystemDefault.NULL.equals(s2)) return null;
		int len1 = s1.length();
		int len2 = s2.length();
		if (0 == len1 || 0 == len2) return "";
		char[] c1 = s1.toCharArray();
		char[] c2 = s2.toCharArray();
		int[][] num = new int[len1][];
		for (int i = 0; i < len1; ++i) num[i] = new int[len2];
		
		int lenMax = 0;
		int idxBegin = 0;
		StringBuilder tmp = new StringBuilder( len1>len2? len2 : len1 );

		for (int i = 0; i < len1; ++i) {
			for (int j = 0; j < len2; ++j) {
				if (c1[i] == c2[j]) {
					int n = 0==i||0==j? 1 : 1 + num[i-1][j-1];
					num[i][j] = n;
					
					if (n > lenMax) {
						lenMax = n;
						
						int idx = i - n + 1;
						if (idxBegin == idx) {
							tmp.append(c1[i]);
						}
						else {
							idxBegin = idx;
							tmp.delete(0, tmp.length()).append(c1[idx]);
						}
					}
				}
			}
		}
		
		return tmp.toString();
	}
	
	public static void main(String[] args) {
		String s1 = "321台中縣";
		String s2 = "台 1 中縣";
		System.out.println(findLCSubsequence(s1, s2));
		System.out.println(findLCSubstring(s1, s2));
	}
	
//	public static void main(String[] args) {
////		String s = "1,2\\3\n	\t \\\'\"";
//		String s = ResourceUtil.getString(new File("x:/a.txt"));
//		System.out.println(s);
//		String escs = escape(s);
//		System.out.println(escs);
//		String unescs = unescape(escs);
//		System.out.println(unescs);
//		System.out.println(s.equals(unescs));
//		
//		String qs = quote(s);
//		System.out.println(isQuoted(qs));
//		System.out.println(qs);
//		String unqs = removeQuote(qs);
//		System.out.println(isQuoted(unqs));
//		System.out.println(unqs);
//	}
}
