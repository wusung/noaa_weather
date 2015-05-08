package com.jfetek.common.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import com.jfetek.common.SystemDefault;
import com.jfetek.common.http.HttpConstants.Request;
import com.jfetek.common.time.Date;
import com.jfetek.common.time.DateTime;
import com.jfetek.common.time.Time;
import com.jfetek.common.util.TextUtil;
import com.jfetek.common.util.TimeUtil;

public class Params {
	
//	public static final String DEFAULT_CHARSET	= "UTF-8";

	protected final Map<String,String[]> m;
	
	public Params() {
		this.m = new HashMap<String,String[]>();
	}
	public Params(boolean ordered) {
		this.m = ordered? new LinkedHashMap<String,String[]>() : new HashMap<String,String[]>();
	}
	
	@SuppressWarnings("unchecked")
	protected Params(HttpServletRequest request) {
		this.m = new HashMap<String,String[]>( request.getParameterMap() );
	}
	@SuppressWarnings("unchecked")
	public Params(HttpServletRequest request, boolean ordered) {
		this.m = ordered? new LinkedHashMap<String,String[]>() : new HashMap<String,String[]>();
		this.m.putAll( request.getParameterMap() );
	}
	
	protected Params(Params params, boolean ordered) {
		this.m = new HashMap<String,String[]>();
		this.m.putAll( params.m );	// copy all key-values pair
	}
	
	public Params duplicate() {
		return new Params( this , this.m instanceof LinkedHashMap );
	}
	
	public boolean isEmpty() {
		return m.isEmpty();
	}
	
	public String getParam(String key) {
		return getParam(key, null, false);
	}
	public String trimParam(String key) {
		return getParam(key, null, true);
	}
	public String getParam(String key, boolean trim) {
		return getParam(key, null, trim);
	}
	public String getParam(String key, String default_value) {
		return getParam(key, default_value, false);
	}
	public String trimParam(String key, String default_value) {
		return getParam(key, default_value, true);
	}
	public String getParam(String key, String default_value, boolean trim) {
		String[] vs = (String[]) m.get( key );
		if (vs == null || vs.length == 0) return default_value;
		String v = vs[0];
		if (null == v) return default_value;
		return trim? v.trim() : v;
	}

	public boolean getBooleanParam(String key) {
		return getBooleanParam(key, false);
	}
	public boolean getBooleanParam(String key, boolean default_value) {
		String[] vs = (String[]) m.get( key );
		if (vs == null || vs.length == 0) {
			return default_value;
		}
		else {
			return TextUtil.booleanValue(vs[0], default_value);
		}
	}

	public int getIntParam(String key) {
		return getIntParam(key, -1);
	}
	public int getIntParam(String key, int default_value) {
		String[] vs = (String[]) m.get( key );
		if (vs == null || vs.length == 0) {
			return default_value;
		}
		else {
			return TextUtil.intValue(vs[0], default_value);
		}
	}

	public long getLongParam(String key) {
		return getLongParam(key, -1L);
	}
	public long getLongParam(String key, long default_value) {
		String[] vs = (String[]) m.get( key );
		if (vs == null || vs.length == 0) {
			return default_value;
		}
		else {
			return TextUtil.longValue(vs[0], default_value);
		}
	}

	public double getDoubleParam(String key) {
		return getDoubleParam(key, -1.0);
	}
	public double getDoubleParam(String key, double default_value) {
		String[] vs = (String[]) m.get( key );
		if (null == vs || 0 == vs.length) {
			return default_value;
		}
		else {
			return TextUtil.doubleValue(vs[0], default_value);
		}
	}
	
	public Date getDateParam(String key) {
		return getDateParam(key, null);
	}
	public Date getDateParam(String key, Date default_value) {
		String[] vs = (String[]) m.get( key );
		if (vs == null || vs.length == 0) {
			return default_value;
		}
		else {
			Date d = TimeUtil.parseDate(vs[0]);
			return null==d? default_value : d;
		}
	}

	public Time getTimeParam(String key) {
		return getTimeParam(key, null);
	}
	public Time getTimeParam(String key, Time default_value) {
		String[] vs = (String[]) m.get( key );
		if (vs == null || vs.length == 0) {
			return default_value;
		}
		else {
			Time dt = TimeUtil.parseTime(vs[0]);
			return null==dt? default_value : dt;
		}
	}
	
	public DateTime getDateTimeParam(String key) {
		return getDateTimeParam(key, null);
	}
	public DateTime getDateTimeParam(String key, DateTime default_value) {
		String[] vs = (String[]) m.get( key );
		if (vs == null || vs.length == 0) {
			return default_value;
		}
		else {
			DateTime dt = TimeUtil.parse(vs[0]);
			return null==dt? default_value : dt;
		}
	}
	
	public void setParam(String key, String value) {
		if (value == null) {
			m.remove( key );
		}
		else {
			String[] tmp = { value };
			m.put( key , tmp );
		}
	}
	public void setParams(String key, String[] values) {
		if (values == null || values.length == 0) {
			m.remove( key );
		}
		else {
			m.put( key , values );
		}
	}
	
	public void addParam(String key, String value) {
		String[] vs = (String[]) m.get( key );
		String[] tmp;
		if (vs == null || vs.length == 0) {
			tmp = new String[] { value };
		}
		else {
			int lenOld = vs.length;
			tmp = new String[lenOld+1];
			System.arraycopy(vs, 0, tmp, 0, lenOld);
			tmp[lenOld] = value;
		}
		m.put( key , tmp );
	}
	public void addParams(String key, String[] values) {
		String[] vs = (String[]) m.get( key );
		String[] tmp;
		if (vs == null || vs.length == 0) {
			tmp = values;
		}
		else {
			int lenOld = vs.length;
			int lenAdd = values.length;
			tmp = new String[lenOld+lenAdd];
			System.arraycopy(vs, 0, tmp, 0, lenOld);
			System.arraycopy(values, 0, tmp, lenOld, lenAdd);
		}
		m.put( key , tmp );
	}
	
	public String[] getParams(String key) {
		String[] vs = (String[]) m.get( key );
		if (vs == null || vs.length == 0) return null;
		return vs;
	}
	
	public int[] getIntParams(String key) {
		String[] vs = (String[]) m.get( key );
		if (vs == null || vs.length == 0) return null;
		int[] ret = new int[vs.length];
		for (int i = 0, len = vs.length; i < len; ++i) {
			ret[i] = TextUtil.intValue( vs[i] , -1 );
		}
		return ret;
	}
	
	public long[] getLongParams(String key) {
		String[] vs = (String[]) m.get( key );
		if (vs == null || vs.length == 0) return null;
		long[] ret = new long[vs.length];
		for (int i = 0, len = vs.length; i < len; ++i) {
			ret[i] = TextUtil.longValue( vs[i] , -1L );
		}
		return ret;
	}

	public double[] getDoubleParams(String key) {
		String[] vs = (String[]) m.get( key );
		if (vs == null || vs.length == 0) return null;
		double[] ret = new double[vs.length];
		for (int i = 0, len = vs.length; i < len; ++i) {
			ret[i] = TextUtil.doubleValue( vs[i] , -1 );
		}
		return ret;
	}
	
	public Set<String> getParamNames() {
		return m.keySet();
	}
	
	public boolean hasParam(String key) {
//		if (null == key) return false;
		return this.m.containsKey(key);
	}

	public boolean isArray(String key) {
		String[] vs = (String[]) m.get( key );
		return (vs != null && vs.length > 1);
	}
	
	public boolean hasValue(String key) {
		return !noValue(key, false);
	}

	public boolean hasValue(String key, boolean trim) {
		return !noValue(key, trim);
	}
	
	public boolean noValue(String key) {
		return noValue(key, false);
	}

	public boolean noValue(String key, boolean trim) {
		String[] vs = (String[]) m.get( key );
		if (vs == null || vs.length == 0) return true;
		String v = vs[0];
		if (null == v) return true;
		if (trim) v = v.trim();
		return (0 == v.length());
	}
	
	public boolean isBlank(String key) {
		String[] vs = (String[]) m.get( key );
		if (vs == null || vs.length == 0) return false;
		String v = vs[0];
		if (null == v) return false;
		return 0 == v.trim().length();
	}
	

//	public boolean isEmail(String key) {
//		String[] vs = (String[]) m.get( key );
//		if (vs == null || vs.length == 0) return false;
//		return TextUtil.isEmail(vs[0]);
//	}
	
//	public boolean isNumber(String key) {
//		String[] vs = (String[]) m.get( key );
//		if (vs == null || vs.length == 0) return false;
//		return TextUtil.noValue(vs[0]);
//	}
	
	public int size() {
		return this.m.size();
	}
	
	public int sizeOf(String key) {
		String[] vs = (String[]) m.get( key );
		return null==vs? 0 : vs.length;
	}
	
	public Params extend(Params default_params) {
		if (null == default_params) return this.duplicate();
		Params params = default_params.duplicate();
		Iterator<Map.Entry<String,String[]>> it = this.m.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String,String[]> e = it.next();
			String key = (String) e.getKey();
			String[] value = (String[]) e.getValue();
			params.setParams(key, value);
		}
		return params;
	}
	
	public Params merge(Params another) {
		if (null == another) return this;
		Iterator<Map.Entry<String,String[]>> it = another.m.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String,String[]> e = it.next();
			String key = (String) e.getKey();
			String[] value = (String[]) e.getValue();
			this.addParams(key, value);
		}
		return this;
	}
	
	public String appendTo(String path) {
//		return appendTo(path, DEFAULT_CHARSET);
		return appendTo(path, SystemDefault.CHARSET_VALUE);
	}
	
	public String appendTo(String path, String charset) {
		if (null == path) return null;
		if (this.isEmpty()) return path;

		StringBuilder b = new StringBuilder(path.length() + 32 * this.m.size());
		
		int i1 = path.indexOf('?');
		int i2 = path.indexOf('#');
		if (i1 > i2) i1 = -1;
		boolean no1 = -1 == i1;
		boolean no2 = -1 == i2;
		if (no2) {
			b.append(path).append(no1? '?' : '&').append( toQueryString(charset) );
		}
		else {
			String p1 = path.substring(0, i2);
			String p2 = path.substring(i2);
			b.append(p1).append(no1? '?' : '&').append( toQueryString(charset) ).append(p2);
		}
		
//		int idx = path.indexOf('#');
//		if (path.indexOf('?') == -1) {
//			if (idx == -1) {
//				b.append('?').append( toQueryString(charset) );
//			}
//			else {
//				b.insert(idx, toQueryString(charset)).insert(idx, '?');
//			}
//		}
//		else {
//			if (-1 == idx) {
//				b.append('&').append( toQueryString(charset) );
//			}
//			else {
//				b.insert(idx, toQueryString(charset)).insert(idx, '&');
//			}
//		}
		
		return b.toString();
	}
	
	public String toQueryString() {
//		return toQueryString(DEFAULT_CHARSET);
		return toQueryString(SystemDefault.CHARSET_VALUE);
	}
	public String toQueryString(String charset) {
		StringBuilder s = new StringBuilder();
		Iterator<String> it = m.keySet().iterator();
		while (it.hasNext()) {
			String k = (String) it.next();
			String[] vs = (String[]) m.get( k );
			try {
				String encK = URLEncoder.encode(k , charset );
				for (int i = 0, len = vs.length; i < len; ++i) {
					s.append('&').append( encK )
						.append('=').append( URLEncoder.encode( vs[i] , charset ) );
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		if (s.length() > 0) s.deleteCharAt(0);
		return s.toString();
	}
	
	public String serialize() {
//		return toQueryString(DEFAULT_CHARSET);
		return toQueryString(SystemDefault.CHARSET_VALUE);
	}
	public String serialize(String charset) {
		return toQueryString(charset);
	}

	public String toString() {
//		return toQueryString(DEFAULT_CHARSET);
		return toQueryString(SystemDefault.CHARSET_VALUE);
	}
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		Iterator<String> it = m.keySet().iterator();
		while (it.hasNext()) {
			String k = (String) it.next();
			String[] vs = (String[]) m.get( k );
			if (vs.length == 1) {
				try {
					json.put(k, vs[0]);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			else if (vs.length > 1) {
				for (int i = 0, len = vs.length; i < len; ++i) {
					try {
						json.append(k, vs[i]);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			else {
				try {
					json.append(k, JSONObject.NULL);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return json;
	}
	
	public static Params parse(String query) {
//		return parse(query, DEFAULT_CHARSET);
		return parse(query, SystemDefault.CHARSET_VALUE);
	}
	public static Params parse(String query, String charset) {
		// TODO
		//	reference: https://github.com/sstephenson/prototype/blob/1fb9728/src/lang/string.js#L428
		return valueOf(query, charset);
	}
	
	public static Params valueOf(String query) {
//		return valueOf(query, DEFAULT_CHARSET);
		return valueOf(query, SystemDefault.CHARSET_VALUE);
	}
	public static Params valueOf(String query, String charset) {
		if (null == query) return null;
		int idx = query.indexOf('?');
		if (idx != -1) {
			query = query.substring(idx+1);
		}
		idx = query.indexOf('#');
		if (idx != -1) {
			query = query.substring(0, idx);
		}
		Params params = new Params();
		String[] pairs = query.split("&");
		for (int i = 0, len = pairs.length; i < len; ++i) {
			String tmp = pairs[i];
			idx = tmp.indexOf("=");
			if (-1 == idx) {
				tmp = tmp.trim();
				if (TextUtil.hasValue(tmp)) {
					try {
						tmp = URLDecoder.decode(tmp, charset);
						params.addParam(tmp, "");
					} catch(UnsupportedEncodingException e) {}
				}
			}
			else {
				try {
					String key = URLDecoder.decode(tmp.substring(0, idx), charset);
					String value = URLDecoder.decode(tmp.substring(idx+1), charset);
					params.addParam(key, value);
				} catch(UnsupportedEncodingException e) {}
			}
		}
		return params;
	}

	public static String append(String path, Params params) {
//		return append(path, params, DEFAULT_CHARSET);
		return append(path, params, SystemDefault.CHARSET_VALUE);
	}
	public static String append(String path, Params params, String charset) {
		// append params to an url string
		if (null == params) return path;
		return params.appendTo(path, charset);
	}
	
	public static String append(String path, String name, String value) {
//		return append(path, name, value, DEFAULT_CHARSET);
		return append(path, name, value, SystemDefault.CHARSET_VALUE);
	}
	public static String append(String path, String name, String value, String charset) {
		if (null == path) return null;
		if (TextUtil.noValue(name) || null == value) return path;

		StringBuilder b = new StringBuilder(path.length() + name.length() + value.length() + 32);

		int i1 = path.indexOf('?');
		int i2 = path.indexOf('#');
		if (i1 > i2) i1 = -1;
		boolean no1 = -1 == i1;
		boolean no2 = -1 == i2;
		if (no2) {
			b.append(path).append(no1? '?' : '&');
			try {
				b.append( URLEncoder.encode(name , charset ) ).append('=').append( URLEncoder.encode( value , charset ) );
			} catch (UnsupportedEncodingException e) { }
		}
		else {
			String p1 = path.substring(0, i2);
			String p2 = path.substring(i2);
			b.append(p1).append(no1? '?' : '&');
			try {
				b.append( URLEncoder.encode(name , charset ) ).append('=').append( URLEncoder.encode( value , charset ) );
			} catch (UnsupportedEncodingException e) { }
			b.append(p2);
		}
		
//		int idx = path.indexOf('#');
//		if (path.indexOf('?') == -1) {
//			if (idx == -1) {
//				b.append('?').append( URLEncoder.encode( name , charset ) ).append('=').append( URLEncoder.encode( value , charset ) );
//			}
//			else {
//				b.insert(idx, toQueryString()).insert(idx, '?');
//			}
//		}
//		else {
//			if (-1 == idx) {
//				b.append('&').append( URLEncoder.encode( name , charset ) ).append('=').append( URLEncoder.encode( value , charset ) ;
//			}
//			else {
//				b.insert(idx, toQueryString()).insert(idx, '&');
//			}
//		}
		return b.toString();
	}

	public static Params of(HttpServletRequest request) {
		return of(request, Request.PARAMETERS);
	}
	public static Params of(HttpServletRequest request, String attr) {
		Params params = (Params) request.getAttribute(attr);
		if (null == params) {
			params = new Params(request);
			request.setAttribute(Request.PARAMETERS, params);
		}
		return params;
	}

	public static Params resolve(PathRouter router, HttpServletRequest request) {
		return resolve(router, request, Request.PARAMETERS);
	}
	public static Params resolve(PathRouter router, HttpServletRequest request, String attr) {
		if (null == router) return Params.of(request, attr);
		Params params = (Params) request.getAttribute(attr);
		if (null != params) {
			String value = params.getParam(PathRouter.ROUTER_PARAMS_SIGNATURE);
			if (router.toString().equals(value)) return params;
		}
		
		params = router.matches(request);
		request.setAttribute(Request.PARAMETERS, params);
		return params;
	}
}
