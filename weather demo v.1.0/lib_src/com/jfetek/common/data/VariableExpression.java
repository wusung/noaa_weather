package com.jfetek.common.data;

import java.util.regex.Pattern;

import com.jfetek.common.util.CompareUtil;
import com.jfetek.common.util.TextUtil;

public class VariableExpression implements Comparable<VariableExpression> {


	// format: $category{name}=value
	public static final Pattern PATTERN		= Pattern.compile("\\$([\\.\\:\\w]*)\\{([\\.\\:\\w]+)\\}(\\=([\\.\\:\\-\\+\\w]*))?");
	

	public static void main(String[] args) {
		VariableExpression var = VariableExpression.parse("$A{bc}=3");
		System.out.println(var);
		System.out.println(var.id);
		System.out.println(var.category);
		System.out.println(var.name);
		System.out.println(var.value);
		System.out.println("---------------------------------");
		
		VariableExpression var2 = var.removeValue();
		System.out.println(var2);
		System.out.println(var2.id);
		System.out.println(var2.category);
		System.out.println(var2.name);
		System.out.println(var2.value);
		System.out.println(var.equals(var2));
		System.out.println(var2.equals(var));
		System.out.println(var2.id.equals(var.id));
		System.out.println(var2.id == var.id);
		System.out.println("---------------------------------");
		
		VariableExpression var3 = new VariableExpression(var.category, var.name, var.value);
		System.out.println(var3);
		System.out.println(var3.id);
		System.out.println(var3.category);
		System.out.println(var3.name);
		System.out.println(var3.value);
		System.out.println(var.equals(var3));
		System.out.println(var3.equals(var));
		System.out.println(var3.id.equals(var.id));
		System.out.println(var3.id == var.id);
	}
	
	
	public final String category;
	public final String name;
	
	private final String id;

	public final String value;
	
	private final int hashcode;
	
	public VariableExpression(String name) {
		this("", name, null);
	}
	public VariableExpression(String category, String name) {
		this(category, name, null);
	}
	public VariableExpression(String category, String name, String value) {
		StringBuilder s = new StringBuilder(category.length()+name.length()+3);
		s.append('$').append( category ).append('{').append(name).append('}');
		this.id = s.toString();
		
		int index = 0;
		if (null == category) {
			index = 4;
			this.category = null;
		}
		else {
			index = category.length() + 1;
			this.category = this.id.substring(1, index);
		}

		if (null == name) {
			this.name = null;
		}
		else {
			++index;
			this.name = id.substring(index, index+name.length());
		}

		this.value = value;
		
		this.hashcode = this.id.hashCode() * 37 + (null==this.value? 0 : 31*this.value.hashCode()+'=');
	}
	private VariableExpression(String id, int idx_cate, int len_cate, int idx_name, int len_name) {
		this(id, idx_cate, len_cate, idx_name, len_name, null);
	}
	private VariableExpression(String id, int idx_cate, int len_cate, int idx_name, int len_name, String value) {
		this.id = id;
		this.category = id.substring(idx_cate, idx_cate+len_cate);
		this.name = id.substring(idx_name, idx_name+len_name);
		this.value = value;
		this.hashcode = this.id.hashCode() * 37 + (null==this.value? 0 : 31*this.value.hashCode()+'=');
	}
	private VariableExpression(VariableExpression var, String value) {
		this.id = var.id;
		this.category = var.category;
		this.name = var.name;
		this.value = value;
		this.hashcode = this.id.hashCode() * 37 + (null==this.value? 0 : 31*this.value.hashCode()+'=');
	}
	
	public boolean validate() {
		if (null == this.category) return false;
		if (TextUtil.noValue(this.name)) return false;
		return true;
	}
	
	public boolean isClean() {
		return (null==this.value);
	}
	
	public VariableExpression removeValue() {
		return null==this.value? this : new VariableExpression(this, null);
	}
	
	public VariableExpression assignValue(String value) {
		return CompareUtil.isEqual(this.value, value)? this : new VariableExpression(this, value);
	}
	
	public boolean equals(VariableExpression v) {
		if (null == v) return false;
		if (this == v) return true;
		return CompareUtil.isEqual(this.id, v.id) && CompareUtil.isEqual(this.value, v.value);
	}

	@Override
	public boolean equals(Object obj) {
		// override Object.equals(Object)
		//	if not VariableExpress return false
		return (obj instanceof VariableExpression && equals((VariableExpression) obj));
	}

	public int compareTo(VariableExpression v) {
		int c = this.category.compareTo( v.category );
		if (0 == c) {
			c = this.name.compareTo( v.name );
			if (0 == c) {
				c = null==this.value? null==v.value? 0 : -v.value.length()-1 : this.value.compareTo( v.value );
			}
		}
		return c;
	}
	
	@Override
	public int hashCode() {
		// [EJ Item8] when override equals(...) must override hashCode(), too.
		// because two EQUAL objects must have the same hashcode
		return this.hashcode;
	}
	
	@Override
	public String toString() {
		// format: $category{name}=value
		if (null == this.value) return this.id;
		
		StringBuilder tmp = new StringBuilder(this.id.length()+16);
		tmp.append(this.id).append('=').append(TextUtil.escape(this.value));
		return tmp.toString();
	}
	
	public static VariableExpression parse(String s) {
		// format: $category{name}=value
		if (null == s) return null;
		if (s.length() < 3 || 0 != s.indexOf('$')) return null;
		
		int idxStart = s.indexOf('{');
		if (idxStart == -1) return null;
		
		int idxEnd = s.indexOf('}', idxStart);
		if (idxEnd == -1) return null;
		
		int idxValue = s.indexOf('=', idxEnd);
		if (idxValue == -1) {
			// no value
			if (idxEnd+1 != s.length()) return null;
			return new VariableExpression(s, 1, idxStart-1, idxStart+1, idxEnd-idxStart-1);
		}

		if (idxValue != idxEnd+1) return null;
		String value = TextUtil.unescape(s.substring(idxValue+1));
		return new VariableExpression(new String(s.substring(0, idxValue)), 1, idxStart-1, idxStart+1, idxEnd-idxStart-1, value);
	}

}
