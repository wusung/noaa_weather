package com.jfetek.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfetek.common.data.VariableExpression;
import com.jfetek.common.json.JsonWrapper;
import com.jfetek.common.util.TextUtil;

public class VariableSetup {
//public class VariableSetup implements JsonDescribable<VariableSetup> {
	
	private static final Logger LOGGER	= LoggerFactory.getLogger(VariableSetup.class);
	
	private static final String	DEFAULT_ADHERE	= ".";


	private final Map<String,Set<String>> c;	// category-alias(es) map
	private final Map<VariableExpression,String> p;	// var-value map
	
	public VariableSetup() {
		this.c = new HashMap<String,Set<String>>();
		this.p = new HashMap<VariableExpression,String>();
	}
	
//	public static VariableSetup load(File file) {
//		VariableSetup setup = new VariableSetup();
//		setup.from(file);
//		return setup;
//	}
//	
//	public static VariableSetup load(InputStream is) {
//		VariableSetup setup = new VariableSetup();
//		setup.from(is);
//		return setup;
//	}
	
	
	public String reg(String value, VariableExpression var) {
		if (null == var || !var.validate()) return value;
		synchronized (this.p) {
			Set<String> n = this.c.get(var.category);
			if (null == n) {
				n = new HashSet<String>();
				this.c.put(var.category, n);
			}
			n.add(var.name);
			this.p.put(var.removeValue(), value);
		}
		return value;
	}
	public String reg(String value, String category, String alias) {
		// usage:	final String c = reg("value",	"category", "alias");
		if (null == category || null == alias) return value;
		return reg(value, new VariableExpression(category, alias));
	}
	public String reg(VariableExpression var) {
		// usage:	final String c = reg("$cate{alias}=value");
		if (null == var) return null;
		if (var.isClean()) return null;	// no value for register
		return reg(var.value, var);
	}

	public boolean reg(boolean value, VariableExpression var) {
		if (null == var || !var.validate()) return value;
		synchronized (this.p) {
			Set<String> n = this.c.get(var.category);
			if (null == n) {
				n = new HashSet<String>();
				this.c.put(var.category, n);
			}
			n.add(var.name);
			this.p.put(var.removeValue(), String.valueOf(value));
		}
		return value;
	}
	public boolean reg(boolean value, String category, String alias) {
		// usage:	final String c = reg("value",	"category", "alias");
		if (null == category || null == alias) return value;
		return reg(value, new VariableExpression(category, alias));
	}

	public char reg(char value, VariableExpression var) {
		if (null == var || !var.validate()) return value;
		synchronized (this.p) {
			Set<String> n = this.c.get(var.category);
			if (null == n) {
				n = new HashSet<String>();
				this.c.put(var.category, n);
			}
			n.add(var.name);
			this.p.put(var.removeValue(), String.valueOf(value));
		}
		return value;
	}
	public char reg(char value, String category, String alias) {
		// usage:	final String c = reg("value",	"category", "alias");
		if (null == category || null == alias) return value;
		return reg(value, new VariableExpression(category, alias));
	}
	
	public int reg(int value, VariableExpression var) {
		if (null == var || !var.validate()) return value;
		synchronized (this.p) {
			Set<String> n = this.c.get(var.category);
			if (null == n) {
				n = new HashSet<String>();
				this.c.put(var.category, n);
			}
			n.add(var.name);
			this.p.put(var.removeValue(), String.valueOf(value));
		}
		return value;
	}
	public int reg(int value, String category, String alias) {
		// usage:	final String c = reg("value",	"category", "alias");
		if (null == category || null == alias) return value;
		return reg(value, new VariableExpression(category, alias));
	}

	public long reg(long value, VariableExpression var) {
		if (null == var || !var.validate()) return value;
		synchronized (this.p) {
			Set<String> n = this.c.get(var.category);
			if (null == n) {
				n = new HashSet<String>();
				this.c.put(var.category, n);
			}
			n.add(var.name);
			this.p.put(var.removeValue(), String.valueOf(value));
		}
		return value;
	}
	public long reg(long value, String category, String alias) {
		// usage:	final String c = reg("value",	"category", "alias");
		if (null == category || null == alias) return value;
		return reg(value, new VariableExpression(category, alias));
	}

	public double reg(double value, VariableExpression var) {
		if (null == var || !var.validate()) return value;
		synchronized (this.p) {
			Set<String> n = this.c.get(var.category);
			if (null == n) {
				n = new HashSet<String>();
				this.c.put(var.category, n);
			}
			n.add(var.name);
			this.p.put(var.removeValue(), String.valueOf(value));
		}
		return value;
	}
	public double reg(double value, String category, String alias) {
		// usage:	final String c = reg("value",	"category", "alias");
		if (null == category || null == alias) return value;
		return reg(value, new VariableExpression(category, alias));
	}

	public String del(String category, String alias) {
		if (null == category || null == alias) return null;
		VariableExpression var = new VariableExpression(category, alias);
		return del(var);
	}
	public String del(VariableExpression var) {
		if (null == var || !var.validate()) return null;
		String v = null;
		synchronized (this.p) {
			Set<String> n = this.c.get(var.category);
			if (null != n) {
				n.remove(var.name);
				v = this.p.remove(var);
				
				if (n.isEmpty()) this.c.remove(var.category);
			}
		}
		return v;
	}

	public String val(String category, String alias) {
		if (null == category || null == alias) return null;
		VariableExpression var = new VariableExpression(category, alias);
		return val(var, null);
	}
	public String val(String category, String alias, String default_value) {
		if (null == category || null == alias) return null;
		VariableExpression var = new VariableExpression(category, alias);
		return val(var, default_value);
	}
	public String val(VariableExpression var) {
		// value of variable instead of default-value
		if (null == var || !var.validate()) return null;
		synchronized (this.p) {
			return this.p.get( var.removeValue() );
		}
	}
	public String val(VariableExpression var, String default_value) {
		if (null == var || !var.validate()) return default_value;
		String v = default_value;
		synchronized (this.p) {
			v = this.p.get( var.removeValue() );
		}
		return v;
	}

	
	public boolean hasCategory(String category) {
		if (null == category) return false;
		synchronized (this.p) {
			return this.c.containsKey(category);
		}
	}
	public boolean hasAlias(String alias) {
		if (null == alias) return false;
		synchronized (this.p) {
			if (this.c.isEmpty()) return false;
			Iterator<Set<String>> it = this.c.values().iterator();
			while (it.hasNext()) {
				Set<String> n = it.next();
				if (n.contains(alias)) return true;
			}
		}
		return false;
	}
	public boolean has(String category, String alias) {
		if (null == category || null == alias) return false;
		VariableExpression var = new VariableExpression(category, alias);
		return has(var);
	}
	public boolean has(VariableExpression var) {
		if (null == var || !var.validate()) return false;
		synchronized (this.p) {
			return this.p.containsKey(var.removeValue());
		}
	}
	
	
//	@Override
//	public int hashCode() {
//		return this.p.hashCode();
//	}
//	
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj) return false;
//		if (!(obj instanceof VariableSetup)) return false;
//		
//		VariableSetup c = (VariableSetup) obj;
//		return this.p.equals( c.p );
//	}
	
	public Set<String> categories() {
		synchronized (this.p) {
			return new TreeSet<String>(this.c.keySet());
		}
	}
	
	public Set<String> aliases(String category) {
		synchronized (this.p) {
			Set<String> n = this.c.get(category);
			return null==n? null : new TreeSet<String>(n);
		}
	}

	public Lookup cate(String category) {
		if (null == category) return null;
		return new CategoryFilteredLookup(this, category);
	}
	
	public Lookup cate(String category, String alias) {
		if (null == category) return null;
		String value = val(category, alias);
		if (null == value) return null;
		return new CategoryFilteredLookup(this, value);
	}
	
	public Lookup cates(String... categories) {
		return new PriorityFilteredLookup(this, categories);
	}
	
//	public String pointerAlias(String category, String alias) {
//		String value = val(category, alias);
//		if (null == value) return null;
//		return val(category, value);
//	}
	
//	public <T extends Lookup> T filter(T filter) {
//		return null;
//	}
	
	public Lookup toLookup() {
		return new CateAsPrefixLookup(this, DEFAULT_ADHERE);
	}
	
	public VariableSetup combine(VariableSetup another) {
		if (this != another) {
			this.c.putAll(another.c);
			this.p.putAll(another.p);
		}
		return this;
	}
	
	public void mass(String category, Map<String,String> map) {
		if (null == category || null == map || map.isEmpty()) return;
		Iterator<Map.Entry<String,String>> it = map.entrySet().iterator();
		synchronized (this.p) {
			Set<String> n = this.c.get(category);
			if (null == n) {
				n = new HashSet<String>();
				this.c.put(category, n);
			}
			while (it.hasNext()) {
				Map.Entry<String, String> e = it.next();
				String name = e.getKey();
				VariableExpression var = new VariableExpression(category, name);
				if (var.validate()) {
					n.add(name);
					this.p.put(var, e.getValue());
				}
			}
		}
	}

	public void mass(String category, Properties properties) {
		if (null == category || null == properties || properties.isEmpty()) return;
		Iterator<Entry<Object, Object>> it = properties.entrySet().iterator();
		synchronized (this.p) {
			Set<String> n = this.c.get(category);
			if (null == n) {
				n = new HashSet<String>();
				this.c.put(category, n);
			}
			while (it.hasNext()) {
				Map.Entry<Object, Object> e = it.next();
				String name = String.valueOf(e.getKey());
				VariableExpression var = new VariableExpression(category, name);
				if (var.validate()) {
					n.add(name);
					this.p.put(var, String.valueOf(e.getValue()));
				}
			}
		}
	}

	
	public boolean loadFrom(File file) {
		boolean ok = true;
		BufferedReader in = null;
		try {
			in = new BufferedReader( new InputStreamReader( new FileInputStream(file) , SystemDefault.CHARSET ) );
			ok = loadFrom(in);
		} catch(Exception e) {
			e.printStackTrace();
			ok = false;
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch(Exception e) {};
				in = null;
			}
		}
		return ok;
	}
	
	public boolean loadFrom(URL url) throws IOException {
		InputStream in = url.openStream();
		boolean ret = loadFrom(in);
		try {
			in.close();
		} catch(Exception e) {}
		return ret;
	}
	
	public boolean loadFrom(InputStream is) throws IOException {
		boolean ok = true;
		BufferedReader in = new BufferedReader( new InputStreamReader( is , SystemDefault.CHARSET ) );
		ok = loadFrom(in);
		in = null;
		return ok;
	}

	public boolean loadFrom(Reader in) throws IOException {
		BufferedReader buff = in instanceof BufferedReader? (BufferedReader) in : new BufferedReader(in) ;
		boolean ok = loadFrom(buff);
		buff = null;
		return ok;
	}
	
	public boolean loadFrom(BufferedReader in) throws IOException {
		// line format:
		//	[$category]
		//	$alias=$value
		boolean ok = true;
		String line;
		String category = "";
		while (null != (line = in.readLine())) {
//			VariableExpress var = VariableExpress.valueOf(line);
//			if (null != var && var.validate()) reg(var);
			line = line.trim();
			if ("".equals(line)) continue;
			if ('#' == line.charAt(0)) continue;
			if ('[' == line.charAt(0) && ']' == line.charAt(line.length()-1)) {
				category = line.substring(1, line.length()-1).trim();
				// always escaped
				category = TextUtil.unescape( category );
			}
			else {
				int idx = line.indexOf('=');
				if (-1 == idx) continue;
				String alias = line.substring(0, idx).trim();
				String value = line.substring(idx+1);
				if (TextUtil.isQuoted(value)) {
					// quoted string is escaped string
					value = TextUtil.removeQuote(value);
					value = TextUtil.unescape(value);
				}
				reg(value, category, alias);
			}
		}
		return ok;
	}
	
	

	public boolean loadCategory(String category, File file) {
		boolean ok = true;
		BufferedReader in = null;
		try {
			in = new BufferedReader( new InputStreamReader( new FileInputStream(file) , SystemDefault.CHARSET ) );
			ok = loadCategory(category, in);
		} catch(Exception e) {
			e.printStackTrace();
			ok = false;
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch(Exception e) {};
				in = null;
			}
		}
		return ok;
	}
	
	public boolean loadCategory(String category, URL url) throws IOException {
		return loadCategory(category, url.openStream());
	}
	
	public boolean loadCategory(String category, InputStream is) throws IOException {
		boolean ok = true;
		BufferedReader in = new BufferedReader( new InputStreamReader( is , SystemDefault.CHARSET ) );
		ok = loadCategory(category, in);
		in = null;
		return ok;
	}

	public boolean loadCategory(String category, Reader in) throws IOException {
		BufferedReader buff = in instanceof BufferedReader? (BufferedReader) in : new BufferedReader(in) ;
		boolean ok = loadCategory(category, buff);
		buff = null;
		return ok;
	}
	
	public boolean loadCategory(String category, BufferedReader in) throws IOException {
		// line format:
		//	$alias=$value
		boolean ok = true;
		String line;
		while (null != (line = in.readLine())) {
//			VariableExpress var = VariableExpress.valueOf(line);
//			if (null != var && var.validate()) reg(var);
			line = line.trim();
			if ("".equals(line)) continue;
			if ('#' == line.charAt(0)) continue;
			if ('[' == line.charAt(0) && ']' == line.charAt(line.length()-1)) {
				// until change category... stop load
				break;
			}
			else {
				int idx = line.indexOf('=');
				if (-1 == idx) continue;
				String alias = line.substring(0, idx).trim();
				String value = line.substring(idx+1);
				if (TextUtil.isQuoted(value)) {
					// quoted string is escaped string
					value = TextUtil.removeQuote(value);
					value = TextUtil.unescape(value);
				}
				reg(value, category, alias);
			}
		}
		return ok;
	}
	
	

	
	
	public boolean saveTo(File file) {
		// line format:
		//	[$category]
		//	$alias=$value
		boolean ok = true;
		BufferedWriter out = null;
		try {
			out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream(file) , SystemDefault.CHARSET ) );
			ok = saveTo(out);
		} catch(Exception e) {
			e.printStackTrace();
			ok = false;
		} finally {
			if (null != out) {
				try {
					out.close();
				} catch(Exception e) {}
				out = null;
			}
		}
		return ok;
	}

	public boolean saveTo(OutputStream os) throws IOException {
		BufferedWriter buff = new BufferedWriter( new OutputStreamWriter( os , SystemDefault.CHARSET ) );
		boolean ok = saveTo(buff);
		buff = null;
		return ok;
	}

	public boolean saveTo(Writer out) throws IOException {
		if (out instanceof StringWriter) return saveTo((StringWriter) out);
		BufferedWriter buff = out instanceof BufferedWriter? (BufferedWriter) out : new BufferedWriter(out) ;
		boolean ok = saveTo(buff);
		buff = null;
		return ok;
	}
	public boolean saveTo(BufferedWriter out) throws IOException {
		// line format:
		//	[$category]
		//	$alias=$value
		boolean ok = true;
		TreeMap<VariableExpression,String> m = new TreeMap<VariableExpression,String>();
		synchronized (this.p) {
			m.putAll(this.p);
		}
		Iterator<Map.Entry<VariableExpression,String>> it = m.entrySet().iterator();
		String category = "";
		while (it.hasNext()) {
			Map.Entry<VariableExpression,String> e = it.next();
			VariableExpression var = e.getKey();
			String val = e.getValue();
			if (!var.category.equals(category)) {
				category = var.category;
				out.write("\n[");
				out.write( TextUtil.escape(category) );
				out.write("]\n");
			}
			out.write(var.name);
			out.write("=\"");	// quote for escaped string
			out.write( TextUtil.escape(val) );
			out.write("\"\n");
		}
		out.flush();
		return ok;
	}
	public boolean saveTo(StringWriter out) {
		// line format:
		//	[$category]
		//	$alias=$value
		boolean ok = true;
		TreeMap<VariableExpression,String> m = new TreeMap<VariableExpression,String>();
		synchronized (this.p) {
			m.putAll(this.p);
		}
		Iterator<Map.Entry<VariableExpression,String>> it = m.entrySet().iterator();
		String category = "";
		while (it.hasNext()) {
			Map.Entry<VariableExpression,String> e = it.next();
			VariableExpression var = e.getKey();
			String val = e.getValue();
			if (!var.category.equals(category)) {
				category = var.category;
				out.write("\n[");
				out.write(category);
				out.write("]\n");
			}
			out.write(var.name);
			out.write("=\"");	// quote for escaped string
			out.write( TextUtil.escape(val) );
			out.write("\"\n");
		}
		return ok;
	}

//	public String format(String pattern) {
//		if (null == pattern) return null;
//		Matcher m = VariableExpression.PATTERN.matcher(pattern);
//		if (!m.find()) return pattern;
//		m.reset();
//		
//		int start = 0;
//		StringBuilder s = new StringBuilder(pattern.length());
//		while (m.find(start)) {
//			s.append(pattern.subSequence(start, m.start()));
//			String g = m.group();
//			VariableExpression var = VariableExpression.parse(g);
//			String value = this.p.get(var);
//			if (null == value) {
//				s.append(g);
//			}
//			else {
//				s.append(value);
//			}
//			start = m.end();
//		}
//		s.append(pattern.subSequence(start, pattern.length()));
//		
//		return s.toString();	
//	}
	
	@Override
	public String toString() {
		StringWriter s = new StringWriter();
		this.saveTo(s);
		return s.toString();
	}
	

	public JSONObject toJson() {
		JsonWrapper json = new JsonWrapper();
		
		Iterator<String> it = this.categories().iterator();
		while (it.hasNext()) {
			String category = it.next();
			JSONObject tmp = new JSONObject();

			Set<String> keys = this.aliases(category);
			Iterator<String> it2 = keys.iterator();
			while (it2.hasNext()) {
				String key = it2.next();
				String value = this.val(category, key);
				
				try {
					tmp.put(key, value);
				} catch (JSONException e) { }
			}
			
			json.put(category, tmp);
		}
		
		return json.toJson();
	}
	
//	public String describe() {
//		return toString();
//	}
//
//	public VariableSetup realize(String describe) {
//		try {
//			this.loadFrom(new StringReader(describe));
//		} catch (IOException e) {
//			LOGGER.debug("exception when try to load from describe string", e);
//		}
//		return this;
//	}
//
//	public VariableSetup realize(JSONObject json) {
//		
//	}
	
	public static void main(String[] args) throws Throwable {
		File file = new File("x:/consts.txt");
		VariableSetup c = new VariableSetup();
		
		c.mass("jvm", System.getProperties());
		
//		System.out.println(c.reg("/auction/"						, "path", "list.auction"	));
//		System.out.println(c.reg("/auction/closed/"					, "path", "closed.auction"	));
//    	System.out.println(c.reg("/auction/detail/"					, "path", "auction.detail"	));
//    	System.out.println(c.reg("/auction/q/info.jsp"				, "path", "query.auction.info"	));
//    	System.out.println(c.reg("/auction/detail/q/bid.jsp"		, "path", "setup.bid.auction"	));
//    	System.out.println(c.reg("/auction/detail/q/auto.jsp"		, "path", "setup.autobid.auction"	));
//    	System.out.println(c.reg("/log/auction/"					, "path", "auction.log"	));
//    	System.out.println(c.reg("/member/register/step1/"			, "path", "register.step1"	));
//    	System.out.println(c.reg("/member/register/step2/"			, "path", "register.step2"	));
//    	System.out.println(c.reg("/member/register/step3/"			, "path", "register.step3"	));
//    	System.out.println(c.reg("/member/register/step4/"			, "path", "register.step4"	));
//    	System.out.println(c.reg("/member/forget/"					, "path", "forget.password"	));
//    	System.out.println(c.reg("/member/info/"					, "path", "member.info"	));
//    	System.out.println(c.reg("/member/info/change-password/"	, "path", "change.password"	));
//    	System.out.println(c.reg("/member/info/phone/"				, "path", "member.info.phone"	));
//    	System.out.println(c.reg("/member/info/email/"				, "path", "member.info.email"	));
//    	System.out.println(c.reg("中文字"							, "data", "text"	));
    	System.out.println(c.saveTo(file));
//		
//		System.out.println(c.from(file));
		System.out.println(c.p.size());
		System.out.println(c.saveTo(System.out));
		System.out.println(c.val("path", "list.auction"));
		System.out.println(c.val("path1", "list.auction2", "d"));
		System.out.println(c);
		
	}

}
