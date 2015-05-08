package com.jfetek.common.util;

import java.util.Comparator;

import com.jfetek.common.SystemDefault;

public class CompareUtil {
	
	
	public static final Comparator<Object>	COMMON_STRING_COMPARATOR	= new Comparator<Object>() {
		public int compare(Object obj1, Object obj2) {
			boolean null1 = (null == obj1);
			boolean null2 = (null == obj2);
			if (null1 && null2) return 0;
			else if (null1) return -1;
			else if (null2) return 1;
			else {
				// compare object's string
				return obj1.toString().compareTo(obj2.toString());
			}
		}
	};
	
	public static final Comparator<Object>	COMMON_HASHCODE_COMPARAOTR	= new Comparator<Object>() {
		public int compare(Object obj1, Object obj2) {
			boolean null1 = (null == obj1);
			boolean null2 = (null == obj2);
			if (null1 && null2) return 0;
			else if (null1) return -1;
			else if (null2) return 1;
			else {
				// compare object's hashcode
				return obj1.hashCode() - obj2.hashCode();
			}
		}
	};
	
	public static final Comparator<Object>	COMMON_COMPARATOR	= COMMON_STRING_COMPARATOR;
	

	private CompareUtil() {
	}
	
	public static <T> int compare(T obj1, T obj2) {
		if (obj1 instanceof Comparable) {
			return ((Comparable<T>) obj1).compareTo(obj2);
		}
		return COMMON_COMPARATOR.compare(obj1, obj2);
	}
	
	public static boolean isNull(Object obj) {
		return SystemDefault.NULL.equals(obj);
	}
	
	public static boolean isBlank(Object obj) {
		return "".equals( obj.toString().trim() );
	}
	
	public static boolean isNullOrBlank(Object obj) {
		return SystemDefault.NULL.equals(obj) || "".equals( obj.toString().trim() );
	}

    public static boolean isEqual(Object obj1, Object obj2) {
        if (obj1 == obj2) return true;
//        if (obj1 == null || obj2 == null) return false;
        boolean eq = null==obj1? obj2.equals(obj1) : obj1.equals(obj2);
        return eq;
    }
    

	public static int levensthein(String s1, String s2, int limit) {
		if (null == s1 || null == s2) return SystemDefault.ERROR_VALUE;
		if (limit <= 0) return SystemDefault.ERROR_VALUE;
		int n = s1.length();
		int m = s2.length();
		if (Math.abs(n-m) > limit) return SystemDefault.ERROR_VALUE;
		if (s1.equals(s2)) return 0;
		int[][] dist = new int[2][];
		dist[0] = new int[m+2];
		dist[1] = new int[m+2];
		int cur = 0;
		
		for (int i = 0; i <= m + 1; i++) {
			dist[1][i] = i;
		}
		for (int i = 1; i <= n; i++) {
			boolean ok = false;
			dist[cur][0] = i;
			if (i - limit >= 1) dist[cur][i - limit - 1] = SystemDefault.ERROR_VALUE;
			for (int j = Math.max(i - limit, 1); j <= Math.min(i + limit, m); j++) {
//				System.out.println(i+" , "+j);
				if (s1.substring(i - 1, i).equals(s2.substring(j - 1, j))) {
					dist[cur][j] = dist[1 - cur][j - 1];
				}
				else {
					dist[cur][j] = CompareUtil.min(dist[1 - cur][j - 1], dist[1 - cur][j], dist[cur][j - 1]) + 1;
				}
				if (dist[cur][j] <= limit) ok = true;
			}
			if (i + limit <= m) dist[cur][i + limit + 1] = SystemDefault.ERROR_VALUE;
			if (!ok) return SystemDefault.ERROR_VALUE;
			cur = 1 - cur;
		}
		if (dist[1 - cur][m] > limit) return SystemDefault.ERROR_VALUE;
		
		return dist[1-cur][m];
	}
	
//	public static int min(int a, int b, int c) {
//		if (a > b) a = b;
//		return a < c? a : c;
//	}
	public static int min(int... a) {
		return min(a, 0, a.length);
	}
	public static int min(int[] a, int offset, int length) {
		if (1 == length) {
			return a[offset];
		}
		else if (2 == length) {
			int val0 = a[offset];
			int val1 = a[1+offset];
			return val0<val1 ? val0 : val1;
		}
		else if (3 == length) {
			int val0 = a[offset];
			int val1 = a[1+offset];
			int val2 = a[2+offset];
			return val0<val1 ? val0<val2? val0 : val2 : val1;
		}
		int half = length>>>1;
		int min1 = min(a, offset, half);
		int min2 = min(a, offset+half, length-half);
		return min1<min2? min1 : min2;
	}

	public static int max(int... a) {
		return max(a, 0, a.length);
	}
	public static int max(int[] a, int offset, int length) {
		if (1 == length) {
			return a[offset];
		}
		else if (2 == length) {
			int val0 = a[offset];
			int val1 = a[1+offset];
			return val0>val1 ? val0 : val1;
		}
		else if (3 == length) {
			int val0 = a[offset];
			int val1 = a[1+offset];
			int val2 = a[2+offset];
			return val0>val1 ? val0>val2? val0 : val2 : val1;
		}
		int half = length>>>1;
		System.out.println("DaC: ("+offset+","+half+") / ("+(offset+half)+","+(length-half)+")");
		int max1 = max(a, offset, half);
		int max2 = max(a, offset+half, length-half);
		return max1>max2? max1 : max2;
	}
	
//	public static void main(String[] args) {
//		class A<T> implements Comparable<T> {
//			@Override
//			public int compareTo(T t) {
//				return this.()-t.hashCode();
//			}
//		};
//		A<Integer> a1 = new A<Integer>();
//		A<Integer> a2 = new A<Integer>();
//		Object o = a1;
//		Comparable<Date> a3 = (A<Date>) o;
//		System.out.println(a3.compareTo(new Date()));
//	}
}
