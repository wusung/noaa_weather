package com.jfetek.demo.weather;

import java.io.File;
import java.lang.Character.UnicodeBlock;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import com.jfetek.common.util.CharUtil;
import com.jfetek.common.util.ResourceUtil;

public class StringParser implements CharacterIterator {
	
	protected String s;
	protected int begin;
	protected int end;
	protected int pos;
	protected int idxWord;
	public StringParser(String s) {
		this.s = s;
		this.begin = 0;
		this.end = s.length();
		this.pos = this.begin;
		this.idxWord = 0;
	}

	@Override
	public char first() {
		this.pos = this.begin;
		return current();
	}

	@Override
	public char last() {
		if (this.begin == this.end) {
			this.pos = this.end;
		}
		else {
			this.pos = this.end - 1;
		}
		return current();
	}

	@Override
	public char current() {
		if (this.pos < this.begin || this.pos >= this.end) return DONE;
		return this.s.charAt(this.pos);
	}
	
	public String nextWord() {
		char c = current();
		if (DONE == c) return null;
		UnicodeBlock cub = UnicodeBlock.of(c);
		if (CharUtil.isChinese(cub)) {
			++this.pos;
			return String.valueOf(c);
		}
		else if (CharUtil.isPunctuation(cub)) {
			++this.pos;
			return String.valueOf(c);
		}
		else if (CharUtil.isWhitespace(c)) {
			int idx = this.pos;
			for (c = next(); DONE != c; c = next()) {
				if (!CharUtil.isWhitespace(c)) return this.s.substring(idx, this.pos);
			}
			return this.s.substring(idx, this.end);
		}
		else if (Character.isDigit(c)) {
			int idx = this.pos;
			for (c = next(); DONE != c; c = next()) {
				if (!Character.isDigit(c)) return this.s.substring(idx, this.pos);
			}
			return this.s.substring(idx, this.end);
		}
		else if (CharUtil.isLetter(c)) {
			int idx = this.pos;
			for (c = next(); DONE != c; c = next()) {
//				if (CharUtil.isWhitespace(c)) return this.s.substring(idx, this.pos);
//				if (CharUtil.isPunctuation(c)) return this.s.substring(idx, this.pos);
//				if (CharUtil.isChinese(c)) return this.s.substring(idx, this.pos);
				if (!CharUtil.isLetter(c)) return this.s.substring(idx, this.pos);
			}
			return this.s.substring(idx, this.end);
		}
		else {
			++this.pos;
			return String.valueOf(c);
		}
	}
	
//	public String peekCurrentWord() {
//		int mark = this.pos;	// push
//		String curr = word();
//		this.pos = mark;	// pop
//		return curr;
//	}
//	public String peekNextWord() {
//		int mark = this.pos;	// push
//		String curr = word();
//		String next = word();
//		this.pos = mark;	// pop
//		return next;
//	}
//	public String peekPreviousWord() {
//	}

	@Override
	public char next() {
		if (this.pos < this.end -1) return this.s.charAt(++this.pos);
		this.pos = this.end;
		return DONE;
	}

	public char peekNext() {
		if (this.pos < this.end -1) return this.s.charAt(1+this.pos);
		return DONE;
	}
	
	@Override
	public char previous() {
		if (this.pos > this.begin) return this.s.charAt(--this.pos);
		return DONE;
	}
	
	public char peekPrevious() {
		if (this.pos > this.begin) return this.s.charAt(this.pos-1);
		return DONE;
	}

	@Override
	public char setIndex(int position) {
	    if (position < this.begin || position > this.end) throw new IllegalArgumentException("Invalid index");
		this.pos = position;
		return current();
	}

	@Override
	public int getBeginIndex() {
		return this.begin;
	}

	@Override
	public int getEndIndex() {
		return this.end;
	}

	@Override
	public int getIndex() {
		return this.pos;
	}

	@Override
    /**
     * Create a copy of this iterator
     * @return A copy of this
     */
    public Object clone() {
        try {
            StringCharacterIterator other
            = (StringCharacterIterator) super.clone();
            return other;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
	}

	public static void main(String[] args) {
//		String s = "123abc└ this is a book     %aa%bb%3a%c4   	.d3jkls.this-「我你好」　Ｔｈｉｓ　ｉｓ　ｄｏｇ．";
		File file = new File("C:/Users/小補/Desktop/test.txt");
		String s = ResourceUtil.getString(file);
		StringParser sp = new StringParser(s);
		for (String w = sp.nextWord(); null != w; w = sp.nextWord()) {
			System.out.println("["+w+"]");
		}
	}
}
