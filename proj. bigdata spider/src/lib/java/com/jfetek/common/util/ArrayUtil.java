package com.jfetek.common.util;

import java.text.Format;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import com.jfetek.common.SystemDefault;




public final class ArrayUtil {

//	private static final Random _RANDOM	= new Random();
	private static final String DEFAULT_DELIMITER = ",";
	
	private ArrayUtil() {
	}
	
	
//	public static int min(byte[] array) {
//		if (null == array) throw new NullPointerException("null array");
//		int len = array.length;
//		if (0 == len) throw new IllegalArgumentException("empty array");
//		if (1 == len) return array[0];
//		if (2 == len) return (array[0]>array[1]? array[0] : array[1]);
//		return min(array, 0, len);
//	}
//	public static int min(byte[] array, int off, int len) {
//		if (2 == len) return (array[]>array[o]);
//		if (3 == len) return min(
//	}
//	
//	public static <T extends Comparable<T>> T max(T[] array) {
//		return null;
//	}
	
	
	// copy from Collections.fill(...);
    public static <T> void ensureSize(List<? super T> list, int size) {
		int szOld = list.size();
		if (szOld >= size) return;

		if (size < 25 || list instanceof RandomAccess) {
			for (int i = szOld; i < size; ++i) list.add(null);
		} else {
			ListIterator<? super T> itr = list.listIterator();
			for (int i = szOld; i < size; ++i) {
				itr.next();
				itr.set(null);
			}
		}
	}
	
	public static String join(boolean[] array) {
		return join(array, DEFAULT_DELIMITER);
	}
	public static String join(boolean[] array, int offset, int length) {
		return join(array, offset, length, DEFAULT_DELIMITER);
	}
	public static String join(boolean[] array, String delimiter) {
		if (null == array) return "";
		int len = array.length;
		return join(array, 0, len, delimiter);
	}
	public static String join(boolean[] array, int offset, int length, String delimiter) {
		if (null == array || 0 == array.length) return "";
		if (1 == length) return String.valueOf(array[0]);
		StringBuilder s = new StringBuilder( length * (5 + delimiter.length()) );
		s.append(array[offset]);
		for (int i = offset+1, end = offset+length; i < end; ++i) {
			s.append(delimiter).append(array[i]);
		}
		return s.toString();
	}

	public static String join(byte[] array) {
		return join(array, DEFAULT_DELIMITER);
	}
	public static String join(byte[] array, int offset, int length) {
		return join(array, offset, length, DEFAULT_DELIMITER);
	}
	public static String join(byte[] array, String delimiter) {
		if (null == array) return "";
		int len = array.length;
		return join(array, 0, len, delimiter);
	}
	public static String join(byte[] array, int offset, int length, String delimiter) {
		if (null == array || 0 == array.length) return "";
		if (1 == length) return String.valueOf(array[offset]);
		StringBuilder s = new StringBuilder( length * (3 + delimiter.length()) );
		s.append(array[offset]);
		for (int i = offset+1, end = offset+length; i < end; ++i) {
			s.append(delimiter).append(array[i]);
		}
		return s.toString();
	}
	public static String join(byte[] array, Format format) {
		return null==format? join(array, 0, array.length, DEFAULT_DELIMITER) : join(array, 0, array.length, DEFAULT_DELIMITER, format);
	}
	public static String join(byte[] array, int offset, int length, Format format) {
		return null==format? join(array, offset, length, DEFAULT_DELIMITER) : join(array, offset, length, DEFAULT_DELIMITER, format);
	}
	public static String join(byte[] array, String delimiter, Format format) {
		return null==format? join(array, 0, array.length, delimiter) : join(array, 0, array.length, delimiter, format);
	}
	public static String join(byte[] array, int offset, int length, String delimiter, Format format) {
		if (null == format) return join(array, offset, length, delimiter);
		if (null == array || 0 == array.length) return "";
		if (1 == length) return format.format(array[offset]);
		StringBuilder s = new StringBuilder( length * (3 + delimiter.length()) );
		s.append( format.format(array[offset]) );
		for (int i = offset+1, end = offset+length; i < end; ++i) {
			s.append(delimiter).append( format.format(array[i]) );
		}
		return s.toString();
	}
	
	
	public static String join(char[] array) {
		return join(array, DEFAULT_DELIMITER);
	}
	public static String join(char[] array, int offset, int length) {
		return join(array, offset, length, DEFAULT_DELIMITER);
	}
	public static String join(char[] array, String delimiter) {
		if (null == array) return "";
		int len = array.length;
		return join(array, 0, len, delimiter);
	}
	public static String join(char[] array, int offset, int length, String delimiter) {
		if (null == array || 0 == array.length) return "";
		if (1 == length) return String.valueOf(array[0]);
		StringBuilder s = new StringBuilder( length * (1 + delimiter.length()) );
		s.append(array[offset]);
		for (int i = offset+1, end = offset+length; i < end; ++i) {
			s.append(delimiter).append(array[i]);
		}
		return s.toString();
	}

	public static String join(int[] array) {
		return join(array, DEFAULT_DELIMITER);
	}
	public static String join(int[] array, int offset, int length) {
		return join(array, offset, length, DEFAULT_DELIMITER);
	}
	public static String join(int[] array, String delimiter) {
		if (null == array) return "";
		int len = array.length;
		return join(array, 0, len, delimiter);
	}
	public static String join(int[] array, int offset, int length, String delimiter) {
		if (null == array || 0 == array.length) return "";
		if (1 == length) return String.valueOf(array[offset]);
		StringBuilder s = new StringBuilder( length * (5 + delimiter.length()) );
		s.append(array[offset]);
		for (int i = offset+1, end = offset+length; i < end; ++i) {
			s.append(delimiter).append(array[i]);
		}
		return s.toString();
	}
	public static String join(int[] array, Format format) {
		return null==format? join(array, 0, array.length, DEFAULT_DELIMITER) : join(array, 0, array.length, DEFAULT_DELIMITER, format);
	}
	public static String join(int[] array, int offset, int length, Format format) {
		return null==format? join(array, offset, length, DEFAULT_DELIMITER) : join(array, offset, length, DEFAULT_DELIMITER, format);
	}
	public static String join(int[] array, String delimiter, Format format) {
		return null==format? join(array, 0, array.length, delimiter) : join(array, 0, array.length, delimiter, format);
	}
	public static String join(int[] array, int offset, int length, String delimiter, Format format) {
		if (null == format) return join(array, offset, length, delimiter);
		if (null == array || 0 == array.length) return "";
		if (1 == length) return format.format(array[offset]);
		StringBuilder s = new StringBuilder( length * (5 + delimiter.length()) );
		s.append( format.format(array[offset]) );
		for (int i = offset+1, end = offset+length; i < end; ++i) {
			s.append(delimiter).append( format.format(array[i]) );
		}
		return s.toString();
	}
	
	

	public static String join(long[] array) {
		return join(array, DEFAULT_DELIMITER);
	}
	public static String join(long[] array, int offset, int length) {
		return join(array, offset, length, DEFAULT_DELIMITER);
	}
	public static String join(long[] array, String delimiter) {
		if (null == array) return "";
		int len = array.length;
		return join(array, 0, len, delimiter);
	}
	public static String join(long[] array, int offset, int length, String delimiter) {
		if (null == array || 0 == array.length) return "";
		if (1 == length) return String.valueOf(array[offset]);
		StringBuilder s = new StringBuilder( length * (10 + delimiter.length()) );
		s.append(array[offset]);
		for (int i = offset+1, end = offset+length; i < end; ++i) {
			s.append(delimiter).append(array[i]);
		}
		return s.toString();
	}
	public static String join(long[] array, Format format) {
		return null==format? join(array, 0, array.length, DEFAULT_DELIMITER) : join(array, 0, array.length, DEFAULT_DELIMITER, format);
	}
	public static String join(long[] array, int offset, int length, Format format) {
		return null==format? join(array, offset, length, DEFAULT_DELIMITER) : join(array, offset, length, DEFAULT_DELIMITER, format);
	}
	public static String join(long[] array, String delimiter, Format format) {
		return null==format? join(array, 0, array.length, delimiter) : join(array, 0, array.length, delimiter, format);
	}
	public static String join(long[] array, int offset, int length, String delimiter, Format format) {
		if (null == format) return join(array, offset, length, delimiter);
		if (null == array || 0 == array.length) return "";
		if (1 == length) return format.format(array[offset]);
		StringBuilder s = new StringBuilder( length * (10 + delimiter.length()) );
		s.append( format.format(array[offset]) );
		for (int i = offset+1, end = offset+length; i < end; ++i) {
			s.append(delimiter).append( format.format(array[i]) );
		}
		return s.toString();
	}

	

	public static String join(double[] array) {
		return join(array, DEFAULT_DELIMITER);
	}
	public static String join(double[] array, int offset, int length) {
		return join(array, offset, length, DEFAULT_DELIMITER);
	}
	public static String join(double[] array, String delimiter) {
		if (null == array) return "";
		int len = array.length;
		return join(array, 0, len, delimiter);
	}
	public static String join(double[] array, int offset, int length, String delimiter) {
		if (null == array || 0 == array.length) return "";
		if (1 == length) return String.valueOf(array[offset]);
		StringBuilder s = new StringBuilder( length * (10 + delimiter.length()) );
		s.append(array[offset]);
		for (int i = offset+1, end = offset+length; i < end; ++i) {
			s.append(delimiter).append(array[i]);
		}
		return s.toString();
	}
	public static String join(double[] array, Format format) {
		return null==format? join(array, 0, array.length, DEFAULT_DELIMITER) : join(array, 0, array.length, DEFAULT_DELIMITER, format);
	}
	public static String join(double[] array, int offset, int length, Format format) {
		return null==format? join(array, offset, length, DEFAULT_DELIMITER) : join(array, offset, length, DEFAULT_DELIMITER, format);
	}
	public static String join(double[] array, String delimiter, Format format) {
		return null==format? join(array, 0, array.length, delimiter) : join(array, 0, array.length, delimiter, format);
	}
	public static String join(double[] array, int offset, int length, String delimiter, Format format) {
		if (null == format) return join(array, offset, length, delimiter);
		if (null == array || 0 == array.length) return "";
		if (1 == length) return format.format(array[offset]);
		StringBuilder s = new StringBuilder( length * (10 + delimiter.length()) );
		s.append( format.format(array[offset]) );
		for (int i = offset+1, end = offset+length; i < end; ++i) {
			s.append(delimiter).append( format.format(array[i]) );
		}
		return s.toString();
	}
	
	
	
	public static String join(Object[] array) {
		return join(array, DEFAULT_DELIMITER);
	}
	public static String join(Object[] array, int offset, int length) {
		return join(array, offset, length, DEFAULT_DELIMITER);
	}
	public static String join(Object[] array, String delimiter) {
		if (null == array) return "";
		int len = array.length;
		return join(array, 0, len, delimiter);
	}
	public static String join(Object[] array, int offset, int length, String delimiter) {
		if (null == array || 0 == array.length) return "";
		if (1 == length) return String.valueOf(array[offset]);
		StringBuilder s = new StringBuilder( length * (10 + delimiter.length()) );
		s.append(array[offset]);
		for (int i = offset+1, end = offset+length; i < end; ++i) {
			s.append(delimiter).append(array[i]);
		}
		return s.toString();
	}
	public static String join(Object[] array, Format format) {
		return null==format? join(array, 0, array.length, DEFAULT_DELIMITER) : join(array, 0, array.length, DEFAULT_DELIMITER, format);
	}
	public static String join(Object[] array, int offset, int length, Format format) {
		return null==format? join(array, offset, length, DEFAULT_DELIMITER) : join(array, offset, length, DEFAULT_DELIMITER, format);
	}
	public static String join(Object[] array, String delimiter, Format format) {
		return null==format? join(array, 0, array.length, delimiter) : join(array, 0, array.length, delimiter, format);
	}
	public static String join(Object[] array, int offset, int length, String delimiter, Format format) {
		if (null == format) return join(array, offset, length, delimiter);
		if (null == array || 0 == array.length) return "";
		if (1 == length) return format.format(array[offset]);
		StringBuilder s = new StringBuilder( length * (10 + delimiter.length()) );
		s.append( format.format(array[offset]) );
		for (int i = offset+1, end = offset+length; i < end; ++i) {
			s.append(delimiter).append( format.format(array[i]) );
		}
		return s.toString();
	}
	

	public static <T> String join(List<T> list) {
		return join(list, DEFAULT_DELIMITER);
	}
	public static <T> String join(List<T> list, int offset, int length) {
		return join(list, offset, length, DEFAULT_DELIMITER);
	}
	public static <T> String join(List<T> list, String delimiter) {
		if (null == list) return "";
		int len = list.size();
		return join(list, 0, len, delimiter);
	}
	public static <T> String join(List<T> list, int offset, int length, String delimiter) {
		if (null == list || 0 == list.size()) return "";
		if (1 == length) return String.valueOf(list.get(offset));
		StringBuilder s = new StringBuilder( length * (10 + delimiter.length()) );
		s.append(list.get(offset));
		if (list instanceof RandomAccess) {
			for (int i = offset+1, end = offset+length; i < end; ++i) {
				s.append(delimiter).append(list.get(i));
			}
		}
		else {
			Iterator<T> it = list.listIterator(offset+1);
			for (int i = 1; i < length && it.hasNext(); ++i) {
				s.append(delimiter).append(it.next());
			}
		}
		return s.toString();
	}
	public static <T> String join(List<T> list, Format format) {
		return null==format? join(list, 0, list.size(), DEFAULT_DELIMITER) : join(list, 0, list.size(), DEFAULT_DELIMITER, format);
	}
	public static <T> String join(List<T> list, int offset, int length, Format format) {
		return null==format? join(list, offset, length, DEFAULT_DELIMITER) : join(list, offset, length, DEFAULT_DELIMITER, format);
	}
	public static <T> String join(List<T> list, String delimiter, Format format) {
		return null==format? join(list, 0, list.size(), delimiter) : join(list, 0, list.size(), delimiter, format);
	}
	public static <T> String join(List<T> list, int offset, int length, String delimiter, Format format) {
		if (null == format) return join(list, offset, length, delimiter);
		if (null == list || 0 == list.size()) return "";
		if (1 == length) return format.format(list.get(offset));
		StringBuilder s = new StringBuilder( length * (10 + delimiter.length()) );
		s.append(format.format(list.get(offset)));
		if (list instanceof RandomAccess) {
			for (int i = offset+1, end = offset+length; i < end; ++i) {
				s.append(delimiter).append(format.format(list.get(i)));
			}
		}
		else {
			Iterator<T> it = list.listIterator(offset+1);
			for (int i = 1; i < length && it.hasNext(); ++i) {
				s.append(delimiter).append(format.format(it.next()));
			}
		}
		return s.toString();
	}
	

	public static <T> String join(Collection<T> list) {
		return join(list, DEFAULT_DELIMITER);
	}
	public static <T> String join(Collection<T> list, String delimiter) {
		if (null == list || 0 == list.size()) return "";
		int length = list.size();
		int lenDelimiter = delimiter.length();
		StringBuilder s = new StringBuilder( length * (10 + lenDelimiter) );
		Iterator<T> it = list.iterator();
		while (it.hasNext()) {
			s.append(delimiter).append(it.next());
		}
		return s.delete(0, lenDelimiter).toString();
	}
	public static <T> String join(Collection<T> list, Format format) {
		return null==format? join(list, DEFAULT_DELIMITER) : join(list, DEFAULT_DELIMITER, format);
	}
	public static <T> String join(Collection<T> list, String delimiter, Format format) {
		if (null == format) return join(list, delimiter);
		if (null == list || 0 == list.size()) return "";
		int length = list.size();
		int lenDelimiter = delimiter.length();
		StringBuilder s = new StringBuilder( length * (10 + lenDelimiter) );
		Iterator<T> it = list.iterator();
		while (it.hasNext()) {
			s.append(delimiter).append(format.format(it.next()));
		}
		return s.delete(0, lenDelimiter).toString();
	}
	
//	public static int[] reverse(int[] array) {
//		return reverse(array, 0, array.length);
//	}
//	public static int[] reverse(int[] array, int offset, int length) {
//		int[] newArray = new int[array.length];
//		System.arraycopy(array, 0, newArray, 0, newArray.length);
//		for (int i = offset, end = offset+length/2; i < end; ++i) {
//			int idx = end - i - 1;
//			int tmp = newArray[i];
//			newArray[i] = newArray[idx];
//			newArray[idx] = tmp;
//		}
//		return newArray;
//	}
	
//	public static void main(String[] args) {
//		Double[] arr = {
//			1.0,
//			2.2,
//			3.123,
//			4.0004,
//			5.11111,
//			6.666666
//		};
//		
////		List<Double> list = Arrays.asList(arr);
//		Set<Double> s = new LinkedHashSet<Double>();
//		s.addAll(Arrays.asList(arr));
//		
//		System.out.println(join(s, "\n", new DecimalFormat("000,000.000")));
//	}

	public static int count(boolean[] array, boolean value) {
		if (array == null || 0 == array.length) return 0;
		int count = 0;
		for (int i = 0, len = array.length; i < len; ++i) {
			if (array[i] == value) ++count;
		}
		return count;
	}

	public static int count(byte[] array, byte value) {
		if (array == null || 0 == array.length) return 0;
		int count = 0;
		for (int i = 0, len = array.length; i < len; ++i) {
			if (array[i] == value) ++count;
		}
		return count;
	}

	public static int count(char[] array, char value) {
		if (array == null || 0 == array.length) return 0;
		int count = 0;
		for (int i = 0, len = array.length; i < len; ++i) {
			if (array[i] == value) ++count;
		}
		return count;
	}

	public static int count(int[] array, int value) {
		if (array == null || 0 == array.length) return 0;
		int count = 0;
		for (int i = 0, len = array.length; i < len; ++i) {
			if (array[i] == value) ++count;
		}
		return count;
	}

	public static int count(long[] array, long value) {
		if (array == null || 0 == array.length) return 0;
		int count = 0;
		for (int i = 0, len = array.length; i < len; ++i) {
			if (array[i] == value) ++count;
		}
		return count;
	}

	public static int count(double[] array, double value) {
		if (array == null || 0 == array.length) return 0;
		int count = 0;
		for (int i = 0, len = array.length; i < len; ++i) {
			if (array[i] == value) ++count;
		}
		return count;
	}

	public static <T> int count(T[] array, T value) {
		if (array == null || 0 == array.length) return 0;
		int count = 0;
		if (value == null) {
			for (int i = 0, len = array.length; i < len; ++i) {
				if (null == array[i]) ++count;
			}
		}
		else {
			for (int i = 0, len = array.length; i < len; ++i) {
				T o = array[i];
				if (null == o) continue;
				if (o == value || o.equals( value )) ++count;
			}
		}
		return count;
	}

	public static int sum(byte[] array) {
		int sum = 0;
		for (int i = 0, len = array.length; i < len; ++i) {
			sum += array[i];
		}
		return sum;
	}

	public static int sum(char[] array) {
		int sum = 0;
		for (int i = 0, len = array.length; i < len; ++i) {
			sum += array[i];
		}
		return sum;
	}
	
	public static int sum(int[] array) {
		int sum = 0;
		for (int i = 0, len = array.length; i < len; ++i) {
			sum += array[i];
		}
		return sum;
	}
	
	public static long sum(long[] array) {
		long sum = 0;
		for (int i = 0, len = array.length; i < len; ++i) {
			sum += array[i];
		}
		return sum;
	}

	public static double sum(double[] array) {
		double sum = 0;
		for (int i = 0, len = array.length; i < len; ++i) {
			sum += array[i];
		}
		return sum;
	}
	
	public static int indexOf(boolean[] array, boolean value) {
		return indexOf(array, 0, array.length, value);
	}
	
	public static int indexOf(boolean[] array, int offset, int length, boolean value) {
		if (array == null || array.length == 0) return -1;
		for (int i = offset, end = offset+length; i < end; ++i) {
			if (value == array[i]) {
				return i;
			}
		}
		return -1;
	}

	public static int indexOf(byte[] array, byte value) {
		return indexOf(array, 0, array.length, value);
	}
	
	public static int indexOf(byte[] array, int offset, int length, byte value) {
		if (array == null || array.length == 0) return -1;
		for (int i = offset, end = offset+length; i < end; ++i) {
			if (value == array[i]) {
				return i;
			}
		}
		return -1;
	}

	public static int indexOf(char[] array, char value) {
		return indexOf(array, 0, array.length, value);
	}
	
	public static int indexOf(char[] array, int offset, int length, char value) {
		if (array == null || array.length == 0) return -1;
		for (int i = offset, end = offset+length; i < end; ++i) {
			if (value == array[i]) {
				return i;
			}
		}
		return -1;
	}

	public static int indexOf(int[] array, int value) {
		return indexOf(array, 0, array.length, value);
	}
	
	public static int indexOf(int[] array, int offset, int length, int value) {
		if (array == null || array.length == 0) return -1;
		for (int i = offset, end = offset+length; i < end; ++i) {
			if (value == array[i]) {
				return i;
			}
		}
		return -1;
	}

	public static int indexOf(long[] array, long value) {
		return indexOf(array, 0, array.length, value);
	}
	
	public static int indexOf(long[] array, int offset, int length, long value) {
		if (array == null || array.length == 0) return -1;
		for (int i = offset, end = offset+length; i < end; ++i) {
			if (value == array[i]) {
				return i;
			}
		}
		return -1;
	}

	public static int indexOf(double[] array, double value) {
		return indexOf(array, 0, array.length, value);
	}
	
	public static int indexOf(double[] array, int offset, int length, double value) {
		if (array == null || array.length == 0) return -1;
		for (int i = offset, end = offset+length; i < end; ++i) {
			if (value == array[i]) {
				return i;
			}
		}
		return -1;
	}

	public static <T> int indexOf(T[] array, T value) {
		return indexOf(array, 0, array.length, value);
	}
	
	public static <T> int indexOf(T[] array, int offset, int length, T value) {
		if (array == null || array.length == 0) return -1;
		if (value == null) {
			for (int i = offset, end = offset+length; i < end; ++i) {
				if (null == array[i]) {
					return i;
				}
			}
		}
		else {
			for (int i = offset, end = offset+length; i < end; ++i) {
				T obj = array[i];
				if (value == obj || value.equals(obj)) {
					return i;
				}
			}
		}
		return -1;
	}

	public static int lastIndexOf(boolean[] array, boolean value) {
		return lastIndexOf(array, 0, array.length, value);
	}
	
	public static int lastIndexOf(boolean[] array, int offset, int length, boolean value) {
		if (array == null || array.length == 0) return -1;
		for (int i = offset+length-1; i >= offset; --i) {
			if (value == array[i]) {
				return i;
			}
		}
		return -1;
	}
	
	public static int lastIndexOf(byte[] array, byte value) {
		return lastIndexOf(array, 0, array.length, value);
	}
	
	public static int lastIndexOf(byte[] array, int offset, int length, byte value) {
		if (array == null || array.length == 0) return -1;
		for (int i = offset+length-1; i >= offset; --i) {
			if (value == array[i]) {
				return i;
			}
		}
		return -1;
	}
	
	public static int lastIndexOf(char[] array, char value) {
		return lastIndexOf(array, 0, array.length, value);
	}
	
	public static int lastIndexOf(char[] array, int offset, int length, char value) {
		if (array == null || array.length == 0) return -1;
		for (int i = offset+length-1; i >= offset; --i) {
			if (value == array[i]) {
				return i;
			}
		}
		return -1;
	}
	
	public static int lastIndexOf(int[] array, int value) {
		return lastIndexOf(array, 0, array.length, value);
	}
	
	public static int lastIndexOf(int[] array, int offset, int length, int value) {
		if (array == null || array.length == 0) return -1;
		for (int i = offset+length-1; i >= offset; --i) {
			if (value == array[i]) {
				return i;
			}
		}
		return -1;
	}

	public static int lastIndexOf(long[] array, long value) {
		return lastIndexOf(array, 0, array.length, value);
	}
	
	public static int lastIndexOf(long[] array, int offset, int length, long value) {
		if (array == null || array.length == 0) return -1;
		for (int i = offset+length-1; i >= offset; --i) {
			if (value == array[i]) {
				return i;
			}
		}
		return -1;
	}

	public static int lastIndexOf(double[] array, double value) {
		return lastIndexOf(array, 0, array.length, value);
	}
	
	public static int lastIndexOf(double[] array, int offset, int length, double value) {
		if (array == null || array.length == 0) return -1;
		for (int i = offset+length-1; i >= offset; --i) {
			if (value == array[i]) {
				return i;
			}
		}
		return -1;
	}
	
	public static <T> int lastIndexOf(T[] array, T value) {
		return lastIndexOf(array, 0, array.length, value);
	}
	
	public static <T> int lastIndexOf(T[] array, int offset, int length, T value) {
		if (array == null || array.length == 0) return -1;
		if (value == null) {
			for (int i = offset+length-1; i >= offset; --i) {
				if (null == array[i]) {
					return i;
				}
			}
		}
		else {
			for (int i = offset+length-1; i >= offset; --i) {
				T obj = array[i];
				if (value == obj || value.equals(obj)) {
					return i;
				}
			}
		}
		return -1;
	}

	public static byte[] uniq(byte[] array) {
		return uniq(array, false);
	}
	public static byte[] uniq(byte[] array, boolean sorted) {
		if (array == null || array.length < 2) return array;
		int len = array.length;
		int idx = 0;
		byte[] tmp = new byte[len];
		if (sorted) {
			byte last = array[0];
			tmp[idx++] = last;
			for (int i = 1; i < len; ++i) {
				if (last != array[i]) {
					last = array[i];
					tmp[idx++] = last;
				}
			}
		}
		else {
			// version#1 ... sort and go
			System.arraycopy(array, 0, tmp, 0, len);
			Arrays.sort(tmp);
			byte last = tmp[idx++];
			for (int i = 1; i < len; ++i) {
				if (last != tmp[i]) {
					last = tmp[i];
					tmp[idx++] = last;
				}
			}
		}
		if (idx == len) return tmp;
		
		byte[] uniq_tmp = new byte[idx];
		System.arraycopy(tmp, 0, uniq_tmp, 0, idx);
		return uniq_tmp;
	}

	public static char[] uniq(char[] array) {
		return uniq(array, false);
	}
	public static char[] uniq(char[] array, boolean sorted) {
		if (array == null || array.length < 2) return array;
		int len = array.length;
		int idx = 0;
		char[] tmp = new char[len];
		if (sorted) {
			char last = array[0];
			tmp[idx++] = last;
			for (int i = 1; i < len; ++i) {
				if (last != array[i]) {
					last = array[i];
					tmp[idx++] = last;
				}
			}
		}
		else {
			// version#1 ... sort and go
			System.arraycopy(array, 0, tmp, 0, len);
			Arrays.sort(tmp);
			char last = tmp[idx++];
			for (int i = 1; i < len; ++i) {
				if (last != tmp[i]) {
					last = tmp[i];
					tmp[idx++] = last;
				}
			}
		}
		if (idx == len) return tmp;
		
		char[] uniq_tmp = new char[idx];
		System.arraycopy(tmp, 0, uniq_tmp, 0, idx);
		return uniq_tmp;
	}

	public static int[] uniq(int[] array) {
		return uniq(array, false);
	}
	public static int[] uniq(int[] array, boolean sorted) {
		if (array == null || array.length < 2) return array;
		int len = array.length;
		int idx = 0;
		int[] tmp = new int[len];
		if (sorted) {
			int last = array[0];
			tmp[idx++] = last;
			for (int i = 1; i < len; ++i) {
				if (last != array[i]) {
					last = array[i];
					tmp[idx++] = last;
				}
			}
		}
		else {
			// version#1 ... sort and go
			System.arraycopy(array, 0, tmp, 0, len);
			Arrays.sort(tmp);
			int last = tmp[idx++];
			for (int i = 1; i < len; ++i) {
				if (last != tmp[i]) {
					last = tmp[i];
					tmp[idx++] = last;
				}
			}

			// version#3 ... index of
//			tmp[idx++] = array[0];
//			for (int i = 1; i < len; ++i) {
//				if (indexOf(tmp, 0, idx, array[i]) == -1) {
//					tmp[idx++] = array[i];
//				}
//			}

			// version#2 ... use Set
//			HashSet s = new HashSet();
//			for (int i = 0; i < len; ++i) {
//				s.add(new Integer(array[i]));
//			}
//			idx = s.size();
//			int[] uniq_tmp = new int[idx];
//			Iterator it = s.iterator();
//			for (int i = 0; it.hasNext(); ++i) {
//				Integer integer = (Integer) it.next();
//				uniq_tmp[i] = integer.intValue();
//			}
//			return uniq_tmp;
		}
		if (idx == len) return tmp;
		
		int[] uniq_tmp = new int[idx];
		System.arraycopy(tmp, 0, uniq_tmp, 0, idx);
		return uniq_tmp;
	}

	public static long[] uniq(long[] array) {
		return uniq(array, false);
	}
	public static long[] uniq(long[] array, boolean sorted) {
		if (array == null || array.length < 2) return array;
		int len = array.length;
		int idx = 0;
		long[] tmp = new long[len];
		if (sorted) {
			long last = array[0];
			tmp[idx++] = last;
			for (int i = 1; i < len; ++i) {
				if (last != array[i]) {
					last = array[i];
					tmp[idx++] = last;
				}
			}
		}
		else {
			// version#1 ... sort and go
			System.arraycopy(array, 0, tmp, 0, len);
			Arrays.sort(tmp);
			long last = tmp[idx++];
			for (int i = 1; i < len; ++i) {
				if (last != tmp[i]) {
					last = tmp[i];
					tmp[idx++] = last;
				}
			}
		}
		if (idx == len) return tmp;
		
		long[] uniq_tmp = new long[idx];
		System.arraycopy(tmp, 0, uniq_tmp, 0, idx);
		return uniq_tmp;
	}

	public static double[] uniq(double[] array) {
		return uniq(array, false);
	}
	public static double[] uniq(double[] array, boolean sorted) {
		if (array == null || array.length < 2) return array;
		int len = array.length;
		int idx = 0;
		double[] tmp = new double[len];
		if (sorted) {
			double last = array[0];
			tmp[idx++] = last;
			for (int i = 1; i < len; ++i) {
				if (last != array[i]) {
					last = array[i];
					tmp[idx++] = last;
				}
			}
		}
		else {
			// version#1 ... sort and go
			System.arraycopy(array, 0, tmp, 0, len);
			Arrays.sort(tmp);
			double last = tmp[idx++];
			for (int i = 1; i < len; ++i) {
				if (last != tmp[i]) {
					last = tmp[i];
					tmp[idx++] = last;
				}
			}
		}
		if (idx == len) return tmp;
		
		double[] uniq_tmp = new double[idx];
		System.arraycopy(tmp, 0, uniq_tmp, 0, idx);
		return uniq_tmp;
	}

	public static <T> T[] uniq(T[] array) {
		return uniq(array, false, null);
	}
	public static <T> T[] uniq(T[] array, boolean sorted) {
		return uniq(array, sorted, null);
	}
	public static <T> T[] uniq(T[] array, Comparator<T> comparator) {
		return uniq(array, false, comparator);
	}
	@SuppressWarnings("unchecked")
	private static <T> T[] uniq(T[] array, boolean sorted, Comparator<T> comparator) {
		if (array == null || array.length < 2) return array;
		int len = array.length;
		int idx = 0;
		T[] tmp = (T[]) new Object[len];
		if (sorted) {
			T last = array[0];
			tmp[idx++] = last;
			for (int i = 1; i < len; ++i) {
				T obj = array[i];
				if (last == null) {
					if (obj != null) {
						last = obj;
						tmp[idx++] = last;
					}
				}
				else if (!last.equals( obj )) {
					last = obj;
					tmp[idx++] = last;
				}
			}
		}
		else {
			// version#1 ... sort and go
			System.arraycopy(array, 0, tmp, 0, len);
			if (comparator == null) {
				Arrays.sort(tmp);
			}
			else {
				Arrays.sort(tmp, comparator);
			}
			T last = tmp[idx++];
			for (int i = 1; i < len; ++i) {
				T obj = tmp[i];
				if (last == null) {
					if (obj != null) {
						last = obj;
						tmp[idx++] = last;
					}
				}
				else if (!last.equals( obj )) {
					last = obj;
					tmp[idx++] = last;
				}
			}
		}
		if (idx == len) return tmp;
		
		T[] uniq_tmp = (T[]) new Object[idx];
		System.arraycopy(tmp, 0, uniq_tmp, 0, idx);
		return uniq_tmp;
	}
	
	public static <T> boolean isNullArray(T[] array) {
		if (null == array || 0 == array.length) return true;
		return isNullArray(array, 0, array.length);
	}
	public static <T> boolean isNullArray(T[] array, int offset, int length) {
		if (null == array || 0 == length) return true;
		for (int i = offset, end = offset+length; i < end; ++i) {
			Object obj = array[i];
//			if (null != obj && SystemDefault.NULL != obj) return false;
			if (!CompareUtil.isNull(obj)) return false;
		}
		return true;
	}

	public static <T> boolean isBlankArray(T[] array) {
		if (null == array || 0 == array.length) return true;
		return isBlankArray(array, 0, array.length);
	}
	public static <T> boolean isBlankArray(T[] array, int offset, int length) {
		if (null == array || 0 == length) return true;
		for (int i = offset, end = offset+length; i < end; ++i) {
			Object obj = array[i];
//			if (null != obj && SystemDefault.NULL != obj && !"".equals( obj.toString().trim() )) return false;
			if (!CompareUtil.isNullOrBlank(obj)) return false;
		}
		return true;
	}
	public static void main(String[] args) {
		Object[] s = {};
		System.out.println(isNullArray(s));
		System.out.println(isBlankArray(s));
		System.out.println(isNullList(Arrays.asList(s)));
		System.out.println(isBlankList(Arrays.asList(s)));
		System.out.println(isNull((Collection<Object>)Arrays.asList(s)));
		System.out.println(isBlank((Collection<Object>)Arrays.asList(s)));
	}

	public static <T> boolean isNullList(List<T> list) {
		if (null == list || 0 == list.size()) return true;
		return isNullList(list, 0, list.size());
	}
	public static <T> boolean isNullList(List<T> list, int offset, int length) {
		if (null == list || 0 == length) return true;
		if (list instanceof RandomAccess) {
			for (int i = offset+1, end = offset+length; i < end; ++i) {
				Object obj = list.get(i);
//				if (null != obj && SystemDefault.NULL != obj) return false;
				if (!CompareUtil.isNull(obj)) return false;
			}
		}
		else {
			Iterator<T> it = list.listIterator(offset+1);
			for (int i = 1; i < length && it.hasNext(); ++i) {
				Object obj = it.next();
//				if (null != obj && SystemDefault.NULL != obj) return false;
				if (!CompareUtil.isNull(obj)) return false;
			}
		}
		return true;
	}

	public static <T> boolean isBlankList(List<T> list) {
		if (null == list || 0 == list.size()) return true;
		return isBlankList(list, 0, list.size());
	}
	public static <T> boolean isBlankList(List<T> list, int offset, int length) {
		if (null == list || 0 == length) return true;
		if (list instanceof RandomAccess) {
			for (int i = offset+1, end = offset+length; i < end; ++i) {
				Object obj = list.get(i);
//				if (null != obj && SystemDefault.NULL != obj && !"".equals( obj.toString().trim() )) return false;
				if (!CompareUtil.isNullOrBlank(obj)) return false;
			}
		}
		else {
			Iterator<T> it = list.listIterator(offset+1);
			for (int i = 1; i < length && it.hasNext(); ++i) {
				Object obj = it.next();
//				if (null != obj && SystemDefault.NULL != obj && !"".equals( obj.toString().trim() )) return false;
				if (!CompareUtil.isNullOrBlank(obj)) return false;
			}
		}
		return true;
	}

	public static <T> boolean isNull(Collection<T> list) {
		if (null == list || 0 == list.size()) return true;
		Iterator<T> it = list.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
//			if (null != obj && SystemDefault.NULL != obj) return false;
			if (!CompareUtil.isNull(obj)) return false;
		}
		return true;
	}
	
	public static <T> boolean isBlank(Collection<T> list) {
		if (null == list || 0 == list.size()) return true;
		Iterator<T> it = list.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
//			if (null != obj && SystemDefault.NULL != obj && !"".equals( obj.toString().trim() )) return false;
			if (!CompareUtil.isNullOrBlank(obj)) return false;
		}
		return true;
	}


	public static int[] fill(int[] array, int with) {
		Arrays.fill(array, with);
		return array;
	}
	
	public static <T> T[] fill(T[] array, T with) {
		Arrays.fill(array, with);
		return array;
	}
	
	public static <T> List<T> fill(List<T> list, T with) {
		Collections.fill(list, with);
		return list;
	}

    public static <T> List<? super T> fill(List<? super T> list, int size, T defalut_value) {
		int szOld = list.size();
		if (szOld >= size) return list;

		if (size < 25 || list instanceof RandomAccess) {
			for (int i = szOld; i < size; ++i) list.add(defalut_value);
		} else {
			ListIterator<? super T> itr = list.listIterator();
			for (int i = szOld; i < size; ++i) {
				itr.next();
				itr.set(defalut_value);
			}
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] repeat(T element, int times) {
		T[] array = (T[]) new Object[times];
		Arrays.fill(array, element);
		return array;
	}
	
	
	
	public static long[] toPrimitive(Long[] arr) {
		if (null == arr) return null;
		int len = arr.length;
		if (0 == len) return new long[0];
		final long[] result = new long[len];
		for (int i = 0; i < len; ++i) {
			result[i] = arr[i].longValue();
		}
		return result;
	}
	public static long[] toPrimitive(Long[] arr, long null_value) {
		if (null == arr) return null;
		int len = arr.length;
		if (0 == len) return new long[0];
		final long[] result = new long[len];
		for (int i = 0; i < len; ++i) {
			Long n = arr[i];
			result[i] = null==n? null_value : n.longValue();
		}
		return result;
	}
	public static long[] toPrimitive(Collection<Long> list) {
		if (null == list) return null;
		int len = list.size();
		if (0 == len) return new long[0];
		final long[] result = new long[len];
		Iterator<Long> it = list.iterator();
		for (int i = 0; it.hasNext(); ++i) {
			result[i] = it.next().longValue();
		}
		return result;
	}
	public static long[] toPrimitive(Collection<Long> list, long null_value) {
		if (null == list) return null;
		int len = list.size();
		if (0 == len) return new long[0];
        final long[] result = new long[len];
        Iterator<Long> it = list.iterator();
        for (int i = 0; it.hasNext(); ++i) {
        	Long n = it.next();
        	result[i] = null==n? null_value : n.longValue();
        }
        return result;
	}
	
	public static <T> String[] toStringArray(Collection<T> list) {
		return toStringArray(list, SystemDefault.NULL_STRING);
	}
	public static <T> String[] toStringArray(Collection<T> list, String null_value) {
		if (null == list) return null;
		int len = list.size();
		if (0 == len) return new String[0];
		final String[] result = new String[len];
		Iterator<T> it = list.iterator();
		for (int i = 0; it.hasNext(); ++i) {
			T obj = it.next();
			result[i] = null==obj? null_value : obj.toString();
		}
		return result;
	}
	
	
//	public static int[] fill(int[] fill_array, int[] with_array) {
//		return fill(fill_array, with_array, false);
//	}
//	public static int[] fill(int[] fill_array, int[] with_array, boolean repeat) {
//		return fill(fill_array, with_array, 0
//		if (null == fill_array || null == with_array) return fill_array;
//		int lenFill = fill_array.length;
//		int lenBy = with_array.length;
//		int lenShort = lenFill<lenBy? lenFill : lenBy;
//		if (lenFill > lenBy && repeat) {
//			for (int i = 0; i < lenFill; i += lenShort) {
//				if (i + lenShort > lenFill) lenShort = lenFill - i;
//				System.arraycopy(with_array, 0, fill_array, i, lenShort);
//			}
//		}
//		else {
//			System.arraycopy(with_array, 0, fill_array, 0, lenShort);
//		}
//		return fill_array;
//	}

//	public static int[] randomIntArray(int length) {
//		if (length < 0) return null;
//		if (length == 0) return new int[0];
//		int[] tmp = new int[length];
//		for (int i = 0; i < length; ++i) {
//			tmp[i] = RANDOM.nextInt(50);
//		}
//		return tmp;
//	}
//	
//	public static void main(String[] args) {
//		Date d = Date.valueOf("2012-02-01");
//		Object[] array = new Object[]{
//				Date.valueOf("2012-02-01") ,
//				Date.valueOf("2012-02-02") ,
//				d ,
//				Date.valueOf("2012-02-01") ,
//				d ,
//				Date.valueOf("2012-02-13") ,
//				Date.valueOf("2012-03-01") ,
//				new Date(System.currentTimeMillis()) ,
//				new Date(System.currentTimeMillis()-3000) ,
//				Date.valueOf("2011-12-01") ,
//				null ,
//				null ,
//				d,
//				d,
//				null
//		};
////		int[] array = randomIntArray(1000000);
////		int[] array = randomIntArray(1000);
////		Arrays.sort(array);
////		System.out.println(TextUtil.join(array));
////		System.out.println(TextUtil.join(uniq(array, false)));
////		System.out.println(TextUtil.join(uniq(array, true)));
//		
//		long ts = System.currentTimeMillis();
//		Object[] tmp = uniq(array, new Comparator() {
//
//			public int compare(Object arg0, Object arg1) {
//				if (arg0 == null) {
//					return arg1==null? 0 : -1;
//				}
//				else if (arg1 == null) {
//					return 1;
//				}
//				else {
//					return 
//				}
//				return 0;
//			}
//			
//		});
//		System.out.println("uniq algorithm#1 ... "+(System.currentTimeMillis()-ts)+"ms");
//		System.out.println("uniq size: "+tmp.length);
//		System.out.println(TextUtil.join(tmp));
//		
//	}
}
