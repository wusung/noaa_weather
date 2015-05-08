package com.jfetek.common.util;

import java.lang.Character.UnicodeBlock;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CharUtil {
	
	public static final Set<Character>	WHITESPACES	= makeSet(

			// whitespace				// line terminators
			(" \t\\x0B\\f\\xA0\ufeff"	+ "\n\r\u2028\u2029"
			
		    // unicode category "Zs" space separators
		    + "\u1680\u180e\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007"
		    + "\u2008\u2009\u200a\u200b\u202f\u205f\u3000").toCharArray()
			
		);
	public static final Set<Character>	HALF_PUNCTUATION	= makeSet(
			"`~!@#$%^&*()_+-=[]\\{}|;\':\",./<>?".toCharArray()
		);
	public static final Set<Character>	FULL_PUNCTUATION	= makeSet(
			"‘～！＠＃＄％＾＆＊（）＿＋－＝〔〕＼｛｝｜；’：”，．／＜＞？".toCharArray()
		);
	
	private CharUtil() {
	}

	public static Set<Character> makeSet(char[] chars) {
		HashSet<Character> set = new HashSet<Character>();
		for (int i = 0, len = chars.length; i < len; ++i) {
			set.add(chars[i]);
		}
		return Collections.unmodifiableSet(set);
	}
	
	public static boolean isLetter(char c) {
		return (
				(c >= 'a' && c <= 'z') ||
				(c >= 'A' && c <= 'Z') ||
				(c >= 'ａ' && c <= 'ｚ') ||
				(c >= 'Ａ' && c <= 'Ｚ')
			);
	}

	public static boolean isDigit(char c) {
		return (
				(c >= '0' && c <= '9') ||
				(c >= '０' && c <= '９')
			);
	}
	
	public static boolean isChinese(char c) {
		UnicodeBlock cub = UnicodeBlock.of(c);
		return isChinese(cub);
	}
	public static boolean isChinese(UnicodeBlock cub) {
		return (
				UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS == cub ||
				UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A == cub ||
				UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B == cub
			);
	}

	public static boolean isWhitespace(char c) {
		if (Character.isWhitespace(c)) return true;
		return WHITESPACES.contains(c);
	}
	
	public static boolean isPunctuation(char c) {
		UnicodeBlock cub = UnicodeBlock.of(c);
		return (
				isPunctuation(cub) ||
				HALF_PUNCTUATION.contains(c) ||
				FULL_PUNCTUATION.contains(c)
			);
	}
	public static boolean isPunctuation(UnicodeBlock cub) {
		return (
				UnicodeBlock.GENERAL_PUNCTUATION == cub ||
				UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION == cub
//				UnicodeBlock.SUPPLEMENTAL_PUNCTUATION == cub	// java6 not support?
//				UnicodeBlock.CUNEIFORM_NUMBERS_AND_PUNCTUATION == cub
			);
	}
	
	public static boolean isHalfPunctuation(char c) {
		UnicodeBlock cub = UnicodeBlock.of(c);
		return (
				isHalfPunctuation(cub) ||
				HALF_PUNCTUATION.contains(c)
			);
	}
	public static boolean isHalfPunctuation(UnicodeBlock cub) {
		return UnicodeBlock.GENERAL_PUNCTUATION == cub;
	}
	
	public static boolean isFullPunctuation(char c) {
		UnicodeBlock cub = UnicodeBlock.of(c);
		return (
				isFullPunctuation(cub) ||
				FULL_PUNCTUATION.contains(c)
			);
	}
	public static boolean isFullPunctuation(UnicodeBlock cub) {
		return (
				UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION == cub
//				UnicodeBlock.SUPPLEMENTAL_PUNCTUATION == cub	// java6 not support?
//				UnicodeBlock.CUNEIFORM_NUMBERS_AND_PUNCTUATION == cub
			);
	}
	
	public static void main(String[] args) {
		char c = '%';
		UnicodeBlock ub = UnicodeBlock.of(c);
		System.out.println(ub);
		System.out.println(Character.isLetter(c));
		System.out.println(isPunctuation(c));
		System.out.println(isWhitespace(c));
		System.out.println(Character.isDigit(c));
	}
}
