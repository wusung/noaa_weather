package com.jfetek.demo.test;

import static java.text.CharacterIterator.DONE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.Character.UnicodeBlock;

import com.jfetek.common.util.CharUtil;
import com.jfetek.common.util.ResourceUtil;

public class ReaderParser {
	
	protected Reader reader;
	protected char curr;
	protected int pos;
	protected boolean eof;
	protected int idxWord;
	public ReaderParser(Reader reader) {
		this.reader = reader;
		this.curr = DONE;
		this.pos = 0;
		this.eof = false;
		this.idxWord = 0;
	}

	public char current() throws IOException {
		if (DONE == this.curr && !this.eof) {
			int i = this.reader.read();
			if (-1 == i) {
				this.eof = true;
			}
			else {
				++this.pos;
				this.curr = (char) i;
			}
		}
		return this.curr;
	}
	
	public String nextWord() throws IOException {
		char c = current();
		if (DONE == c) return null;
		UnicodeBlock cub = UnicodeBlock.of(c);
		if (CharUtil.isChinese(cub)) {
			next();
			return String.valueOf(c);
		}
		else if (CharUtil.isPunctuation(cub)) {
			next();
			return String.valueOf(c);
		}
		else if (CharUtil.isWhitespace(c)) {
			StringBuilder tmp = new StringBuilder();
			tmp.append(c);
			for (c = next(); DONE != c; c = next()) {
				if (!CharUtil.isWhitespace(c)) break;
				tmp.append(c);
			}
			return tmp.toString();
		}
		else if (Character.isDigit(c)) {
			StringBuilder tmp = new StringBuilder();
			tmp.append(c);
			for (c = next(); DONE != c; c = next()) {
				if (!Character.isDigit(c)) break;
				tmp.append(c);
			}
			return tmp.toString();
		}
		else if (CharUtil.isLetter(c)) {
			StringBuilder tmp = new StringBuilder();
			tmp.append(c);
			for (c = next(); DONE != c; c = next()) {
				if (!CharUtil.isLetter(c)) break;
				tmp.append(c);
			}
			return tmp.toString();
		}
		else {
			next();
			return String.valueOf(c);
		}
	}
	
	public char next() throws IOException {
		int i = this.reader.read();
		if (-1 == i) {
			this.eof = true;
			this.curr = DONE;
		}
		else {
			++this.pos;
			this.curr = (char) i;
		}
		return this.curr;
	}

	public int getIndex() {
		return this.pos;
	}

	public static void main(String[] args) throws IOException {
		File file = new File("C:/Users/小補/Desktop/test.txt");
		System.out.println(ResourceUtil.detectCharset(file));
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "MS950"));
		
		ReaderParser sp = new ReaderParser(in);
		for (String w = sp.nextWord(); null != w; w = sp.nextWord()) {
			System.out.println("["+w+"]");
		}
	}
}
