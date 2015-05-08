package com.jfetek.common.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.jfetek.common.SystemDefault;
import com.jfetek.common.util.TextUtil;

public class PathRouter {
	
	public static final String		ROUTER_PARAMS_SIGNATURE	= "__buds_path_route_param";
	
	private static final Pattern	OPTIONAL_PARAM		= Pattern.compile("\\((.*?)\\)");
	private static final String		OPTIONAL_REPLACE	= "(?:$1)?";
	private static final Pattern	NAMED_PARAM			= Pattern.compile("(\\(\\?)?:(\\w+)");
	private static final String		NAMED_REPLACE		= "([^/?]+)";
	private static final Pattern	SPLAT_PARAM			= Pattern.compile("\\*(\\w+)");
	private static final String		SPLAT_REPLACE		= "([^?]*?)";
	private static final Pattern	ESCAPE_REGEXP		= Pattern.compile("[\\-{}\\[\\]+?.,\\\\\\^$|#\\s]");
	private static final String		EXCEPE_REPLACE		= "\\\\$0";
	private static final String		QUERY_REGEXP		= "(?:\\?([\\s\\S]*))?$";

	private final Pattern pattern;
	private final String[] names;
	private final boolean merge;
//	public PathRouter(String route) {
//		this(route, false);
//	}
//	public PathRouter(String route, boolean merge) {
//		this.merge = merge;
//		ArrayList<String> list = new ArrayList<String>(5);
//		list.add(route);
//		String str = route;
//		str = ESCAPE_REGEXP.matcher(str).replaceAll(EXCEPE_REPLACE);
//		str = OPTIONAL_PARAM.matcher(str).replaceAll(OPTIONAL_REPLACE);
//		str = replaceNamedParam(str, list);
//		str = replaceSplatParam(str, list);
////		str = SPLAT_PARAM.matcher(str).replaceAll(SPLAT_REPLACE);
//		str = "^" + str + QUERY_REGEXP;
//		this.pattern = Pattern.compile(str);
//		list.add("query-string");
//		this.names = list.toArray(new String[0]);
//	}
	public PathRouter(Pattern pattern, String[] names) {
		this(pattern, names, false);
	}
	public PathRouter(Pattern pattern, String[] names, boolean merge) {
		this.pattern = pattern;
		this.names = names;
		this.merge = merge;
	}
	
	public static PathRouter compile(String route) {
		return compile(route, false);
	}
	public static PathRouter compile(String route, boolean merge) {
		if (TextUtil.noValueOrBlank(route)) return null;
		ArrayList<String> list = new ArrayList<String>(5);
		list.add(route);
		String str = route;
		str = ESCAPE_REGEXP.matcher(str).replaceAll(EXCEPE_REPLACE);
		str = OPTIONAL_PARAM.matcher(str).replaceAll(OPTIONAL_REPLACE);
		str = replaceNamedParam(str, list);
		str = replaceSplatParam(str, list);
//		str = SPLAT_PARAM.matcher(str).replaceAll(SPLAT_REPLACE);
		str = "^" + str + QUERY_REGEXP;
		Pattern pattern = Pattern.compile(str);
		list.add("query-string");
		String[] names = list.toArray(new String[0]);
		return new PathRouter(pattern, names, merge);
	}
	
	public Params matches(String path) {
		return matches(path, this.merge);
	}
	public Params matches(String path, boolean merge_query_string) {
		Matcher m = pattern.matcher(path);
		if (m.find()) {
			Params params = new Params();
			int count = m.groupCount();
			for (int i = 1; i < count; ++i) {
				String value = m.group(i);
				String name = names[i];
				if (null != value) {
					params.addParam(name, value);
				}
			}
			String query = m.group(count);
			if (merge_query_string) {
				params.merge(Params.parse(query, SystemDefault.CHARSET_VALUE));
			}
			else {
				params.addParam(names[count], query);
			}
			return params;
		}
		return null;
	}

	private static String getRequestString(HttpServletRequest request) {
		String path = request.getPathInfo();
		String query = request.getQueryString();
		return TextUtil.noValueOrBlank(query)? path : path + "?" + query;
	}
	public Params matches(HttpServletRequest request) {
		return matches(request, this.merge);
	}
	public Params matches(HttpServletRequest request, boolean merge_query_string) {
		String charset = request.getCharacterEncoding();
		if (null == charset) {
			charset = SystemDefault.CHARSET_VALUE;
		}
		String path = getRequestString(request);
		Matcher m = pattern.matcher(path);
		if (m.find()) {
			Params params = new Params();
			params.addParam(ROUTER_PARAMS_SIGNATURE, this.names[0]);
			int count = m.groupCount();
			for (int i = 1; i < count; ++i) {
				String value = m.group(i);
				String name = names[i];
				if (null != value) {
					if (value.matches("%\\d\\d")) {
						try {
							value = URLDecoder.decode( value , charset );
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}
//					params.addParam(name, value);
					
					String[] vals = value.split(",");
					params.addParams(name, vals);
				}
			}
			String query = m.group(count);
			if (merge_query_string) {
				params.merge(Params.parse(query, charset));
			}
			else {
				params.addParam(names[count], query);
			}
			return params;
		}
		return null;
	}
	

	private static String replaceNamedParam(String str, ArrayList<String> names) {
		Matcher m = NAMED_PARAM.matcher(str);
		int start = 0;
		StringBuilder s = new StringBuilder(str.length());
		for (int i = 1; m.find(start); ++i) {
			s.append(str.subSequence(start, m.start()));
			String group = m.group(1);
			String name = m.group(2);
			names.add(name);
			if (TextUtil.noValue(group)) {
				s.append(NAMED_REPLACE);
			}
			else {
				s.append(group);
			}
			start = m.end();
		}
		s.append(str.subSequence(start, str.length()));
		return s.toString();
	}
	
	private static String replaceSplatParam(String str, ArrayList<String> names) {
		Matcher m = SPLAT_PARAM.matcher(str);
		int start = 0;
		StringBuilder s = new StringBuilder(str.length());
		for (int i = 1; m.find(start); ++i) {
			s.append(str.subSequence(start, m.start()));
			String group = m.group(1);
			names.add(group);
			s.append(SPLAT_REPLACE);
			start = m.end();
		}
		s.append(str.subSequence(start, str.length()));
		return s.toString();
	}
	
	public String toString() {
		return this.names[0];
	}
	
	public static void main(String[] args) {
		PathRouter router = PathRouter.compile(":sn/:filename(/*test)");
		Params params = router.matches("photo123/image.jpg?a=a&b=b&filename=image.gif", false);
		System.out.println(params.toJson());
	}
}
