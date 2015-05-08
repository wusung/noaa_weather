package com.jfetek.common.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.CRC32;

import com.jfetek.common.SystemDefault;

public final class EasyCodecUtil {

	private static final String		BASE64_DEFAULT_ENCODE_TABLE	= "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	private static final HashMap<String,byte[]>	BASE64_CODETABLE	= new HashMap<String,byte[]>();
	private static byte[] calculate_base64_codetable(String s) {
//		if (null == s) throw new IllegalArgumentException("null encode string");
		byte[] decode_table = (byte[]) BASE64_CODETABLE.get(s);
		if (null == decode_table) {
			int len = s.length();
			if (len < 64) throw new IllegalArgumentException("encode string less then 64");
			decode_table = new byte[256];
			Arrays.fill(decode_table, (byte)-9);
			char[] encode_table = s.toCharArray();
			for (int i = 0; i < len; ++i) {
				char c = encode_table[i];
				if (c >= 128) throw new ArrayIndexOutOfBoundsException("illegal character at encode string char["+i+"]="+c);
				decode_table[ c ] = (byte)(i & 0xff);
			}
		}
		return decode_table;
	}
	static {
		byte[] default_decode_table = calculate_base64_codetable(BASE64_DEFAULT_ENCODE_TABLE);
		BASE64_CODETABLE.put(null, default_decode_table);
		BASE64_CODETABLE.put(BASE64_DEFAULT_ENCODE_TABLE, default_decode_table);
	};
//	public static void main(String[] args) throws Throwable {
//		String s = "你是豬123bae";
//		String s1 = base64Encoder(s.getBytes("UTF-8"));
//		System.out.println(s1);
//		System.out.println(new String(base64Decoder(s1).toByteArray(), "UTF-8"));
//	}
   
	
	private EasyCodecUtil() {
	}
	

	public static String md5(String s) {
		if (s == null) return null;
		try {
			return md5(s.getBytes(SystemDefault.CHARSET_VALUE));
		} catch(Exception e) { }
		return null;
	}
	
	public static String md5(byte[] b) {
		if (b == null) return null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update( b );
			return hexEncode( md.digest() , false );
		} catch(Exception e) { }
		return null;
	}
	
	public static String md5(InputStream is) {
		if (is == null) return null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			
			byte[] buff = new byte[SystemDefault.BUFFER_SIZE];
			int len = 0;
			while ((len = is.read(buff)) != -1) {
				md.update(buff, 0, len);
			}
			return hexEncode( md.digest() , false );
		} catch(Exception e) { }
		return null;
	}
	
	public static String md5(File file) {
		if (file == null || !file.exists()) return null;
		
		InputStream in = null;
		String retVal = null;
		try {
			in = new BufferedInputStream( new FileInputStream(file) );
			retVal = md5( in );
		} catch(Exception e) {
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) { }
				in = null;
			}
		}
		return retVal;
	}
	
/*
	public static void main(String[] args) throws IOException {
		File file = new File("C:/Documents and Settings/小補/Desktop/未命名.JPG");
		
		long ts = System.currentTimeMillis();
		String str1 = base64Encoder( ResourceUtil.getByteArray(file) );
		System.out.println("ts= "+(System.currentTimeMillis()-ts));
		
		ts = System.currentTimeMillis();
		String str2 = base64Encoder( ResourceUtil.getInputStream(file) );
		System.out.println("ts= "+(System.currentTimeMillis()-ts));
		ts = System.currentTimeMillis();
		
		System.out.println(str1.equals(str2));

		ts = System.currentTimeMillis();
		ByteArrayOutputStream buf = base64Decoder(str2);
		ResourceUtil.output(buf.toByteArray(), new File(file.getParent(), "afs.jpg"));
		System.out.println("ts= "+(System.currentTimeMillis()-ts));
	}
*/

	public static long crc32(String s) {
		if (s == null) return -1L;
		try {
			return crc32(s.getBytes(SystemDefault.CHARSET_VALUE));
		} catch(Exception e) { }
		return -1L;
	}
	
	public static long crc32(byte[] b) {
		if (b == null) return -1L;
		CRC32 crc = new CRC32();
		crc.update( b );
		return crc.getValue();
	}
	
	public static long crc32(InputStream is) {
		if (is == null) return -1L;
		try {
			CRC32 crc = new CRC32();
			
			byte[] buff = new byte[SystemDefault.BUFFER_SIZE];
			int len = 0;
			while ((len = is.read(buff)) != -1) {
				crc.update(buff, 0, len);
			}
			return crc.getValue();
		} catch(Exception e) { }
		return -1L;
	}
	
	public static long crc32(File file) {
		if (file == null || !file.exists()) return -1L;
		
		InputStream in = null;
		long retVal = -1L;
		try {
			in = new BufferedInputStream( new FileInputStream(file) );
			retVal = crc32( in );
		} catch(Exception e) {
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) { }
				in = null;
			}
		}
		return retVal;
	}
	
	
	public static String base64Encoder(String s) {
		try {
			byte[] b = s.getBytes(SystemDefault.CHARSET_VALUE);
			return base64Encoder(b, null);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static String base64Encoder(String s, String encode_table) {
		try {
			byte[] b = s.getBytes(SystemDefault.CHARSET_VALUE);
			return base64Encoder(b, encode_table);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static String base64Encoder(byte[] data) {
		return base64Encoder(data, null);
	}
	public static String base64Encoder(byte[] data, String encode_table) {
		if (null == data) return null;
		return _base64Encoder(data, encode_table);
	}
	private static String _base64Encoder(byte[] data, String encode_table) {
		char[] encode_char = (null==encode_table? BASE64_DEFAULT_ENCODE_TABLE.toCharArray() : encode_table.toCharArray());
		StringBuffer sb = new StringBuffer();
		int len = data.length;
		int i = 0;
		int b1, b2, b3;

		while (i < len) {
			b1 = data[i++] & 0xff;
			if (i == len) {
				sb.append(encode_char[b1 >>> 2]);
				sb.append(encode_char[(b1 & 0x3) << 4]);
				sb.append("==");
				break;
			}
			b2 = data[i++] & 0xff;
			if (i == len) {
				sb.append(encode_char[b1 >>> 2]);
				sb.append(encode_char[((b1 & 0x03) << 4)
						| ((b2 & 0xf0) >>> 4)]);
				sb.append(encode_char[(b2 & 0x0f) << 2]);
				sb.append("=");
				break;
			}
			b3 = data[i++] & 0xff;
			sb.append(encode_char[b1 >>> 2]);
			sb.append(encode_char[((b1 & 0x03) << 4)
					| ((b2 & 0xf0) >>> 4)]);
			sb.append(encode_char[((b2 & 0x0f) << 2)
					| ((b3 & 0xc0) >>> 6)]);
			sb.append(encode_char[b3 & 0x3f]);
		}
		return sb.toString();
	}


	public static String base64Encoder(InputStream in) throws IOException {
		return base64Encoder(in, null);
	}
	public static String base64Encoder(InputStream in, String encode_table) throws IOException {
		if (in == null) {
			return null;
		}
		else if (!(in instanceof BufferedInputStream)) {
			in = new BufferedInputStream( in );
		}
		char[] encode_char = (null==encode_table? BASE64_DEFAULT_ENCODE_TABLE.toCharArray() : encode_table.toCharArray());
		StringBuffer sb = new StringBuffer();
		int b1, b2, b3;

		while (true) {
			b1 = in.read();
			if (b1 == -1) {
				break;
			}
			b1 &= 0xff;
			b2 = in.read();
			if (b2 == -1) {
				sb.append(encode_char[b1 >>> 2]);
				sb.append(encode_char[(b1 & 0x3) << 4]);
				sb.append("==");
				break;
			}
			b2 &= 0xff;
			b3 = in.read();
			if (b3 == -1) {
				sb.append(encode_char[b1 >>> 2]);
				sb.append(encode_char[((b1 & 0x03) << 4)
						| ((b2 & 0xf0) >>> 4)]);
				sb.append(encode_char[(b2 & 0x0f) << 2]);
				sb.append("=");
				break;
			}
			b3 &= 0xff;
			sb.append(encode_char[b1 >>> 2]);
			sb.append(encode_char[((b1 & 0x03) << 4)
					| ((b2 & 0xf0) >>> 4)]);
			sb.append(encode_char[((b2 & 0x0f) << 2)
					| ((b3 & 0xc0) >>> 6)]);
			sb.append(encode_char[b3 & 0x3f]);
		}
		return sb.toString();
	}

	
	public static ByteArrayOutputStream base64Decoder(String str) {
		return base64Decoder(str.toCharArray(), null);
	}
	public static ByteArrayOutputStream base64Decoder(String str, String encode_table) {
		if (str == null) return null;
		return base64Decoder(str.toCharArray(), encode_table);
	}
	
	public static ByteArrayOutputStream base64Decoder(char[] data) {
		return base64Decoder(data, null);
	}
	public static ByteArrayOutputStream base64Decoder(char[] data, String encode_table) {
		byte[] decode_char = calculate_base64_codetable(encode_table);
		int len = data.length;
		ByteArrayOutputStream buf = new ByteArrayOutputStream(len);
		int i = 0;
		int b1, b2, b3, b4;

		while (i < len) {

			/* b1 */
			do {
				b1 = decode_char[data[i++]];
			} while (i < len && b1 == -1);
			if (b1 == -1) {
				break;
			}

			/* b2 */
			do {
				b2 = decode_char[data[i++]];
			} while (i < len && b2 == -1);
			if (b2 == -1) {
				break;
			}
			buf.write((int) ((b1 << 2) | ((b2 & 0x30) >>> 4)));

			/* b3 */
			do {
				b3 = data[i++];
				if (b3 == 61) {
					return buf;
				}
				b3 = decode_char[b3];
			} while (i < len && b3 == -1);
			if (b3 == -1) {
				break;
			}
			buf.write((int) (((b2 & 0x0f) << 4) | ((b3 & 0x3c) >>> 2)));

			/* b4 */
			do {
				b4 = data[i++];
				if (b4 == 61) {
					return buf;
				}
				b4 = decode_char[b4];
			} while (i < len && b4 == -1);
			if (b4 == -1) {
				break;
			}
			buf.write((int) (((b3 & 0x03) << 6) | b4));
		}

		return buf;
	}
	
	public static String hexEncode(byte[] b) {
		return hexEncode(b, false);
	}
	
	public static String hexEncode(byte[] b, boolean upper_case) {
		if (b == null) return "";
		StringBuffer ret = new StringBuffer(2*b.length);
		for (int j = 0; j < b.length; j++) {
		    char ch = Character.forDigit((b[j] >> 4) & 0xF, 16);
		    if (upper_case && Character.isLetter(ch)) {
				ch -= 32;	// 'a' - 'A'
		    }
		    ret.append(ch);
		    ch = Character.forDigit(b[j] & 0xF, 16);
		    if (upper_case && Character.isLetter(ch)) {
				ch -= 32;	// 'a' - 'A'
		    }
		    ret.append(ch);
		}
		return ret.toString();
	}

    public static byte[] hexDecode(String hex) {
		if (hex == null) {
			System.out.println("hexDecode() got null data...");
			return new byte[0];
		}
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		int numChars = hex.length();
		int i = 0;
	
		try {
			while (i < numChars) {
			    buff.write( (byte)Integer.parseInt(hex.substring(i,i+2),16) );
				i+= 2;
	        }
		} catch (NumberFormatException e) {
		    throw new IllegalArgumentException(
                    "hexDecode(string): Illegal hex characters - " 
		    + e.getMessage());
		}

        return buff.toByteArray();
    }
    
    public static String htmlCharEntifyEncode(String s) {
    	return htmlCharEntifyEncode(s, false);
    }
    
    public static String htmlCharEntifyEncode(String s, boolean hex) {
		StringBuffer tmp = new StringBuffer();
		if (hex) {
			for (int i = 0, len = s.length(); i < len; ++i) {
				int c = (int) s.charAt(i);
				tmp.append("&#x").append(Integer.toHexString( c )).append(";");
			}
		}
		else {
			for (int i = 0, len = s.length(); i < len; ++i) {
				int c = (int) s.charAt(i);
				tmp.append("&#").append(c).append(";");
			}
		}
		return tmp.toString();
    }
    
    private static char parseHTMLCharEntity(StringCharacterIterator scit) {
    	StringBuffer sb = new StringBuffer();
    	char c = scit.next();
    	if (CharacterIterator.DONE == c) {
    		// error
    		return CharacterIterator.DONE;
    	}
    	else if ('#' == c) {
//    		int index = scit.getIndex();
    		c = scit.next();
    		if ('x' == c || 'X' == c) {
    			// hex mode
        		for (c = scit.next(); c != ';'; c = scit.next()) {
        			if (CharacterIterator.DONE == c) {
        				return CharacterIterator.DONE;
        			}
        			sb.append( c );
        		}
        		
        		try {
        			c = (char) Integer.parseInt(sb.toString(), 16);
        		} catch(Exception e) {
        			new Exception("error hex: "+sb, e).printStackTrace();
        			return CharacterIterator.DONE;
        		}
        		
        		return c;
    		}
    		else {
    			// normal
    			sb.append( c );
        		for (c = scit.next(); c != ';'; c = scit.next()) {
        			if (CharacterIterator.DONE == c) {
        				return CharacterIterator.DONE;
        			}
        			sb.append( c );
        		}
        		
        		try {
        			c = (char) Integer.parseInt(sb.toString());
        		} catch(Exception e) {
        			return CharacterIterator.DONE;
        		}
        		
        		return c;
    		}
    	}
    	else {
    		sb.append( c );
    		for (c = scit.next(); c != ';'; c = scit.next()) {
    			if (CharacterIterator.DONE == c) {
    				return CharacterIterator.DONE;
    			}
    			sb.append( c );
    		}
    		
    		if ("gt".contentEquals( sb )) {
    			return '>';
    		}
    		else if ("lt".contentEquals( sb )) {
    			return '<';
    		}
    		else if ("apos".contentEquals( sb )) {
    			return '\'';
    		}
    		else if ("quot".contentEquals( sb )) {
    			return '\"';
    		}
    		else if ("nbsp".contentEquals( sb )) {
    			return ' ';
    		}
    		else if ("amp".contentEquals( sb )) {
    			return '&';
    		}
//    		else if ("".contentEquals( sb )) {
//    			sb.append( '' );
//    		}
    		else {
    			// other case...
    			return CharacterIterator.DONE;
    		}
    	}
    }
    
    public static String htmlCharEntityDecode(String s) {
    	StringCharacterIterator scit = new StringCharacterIterator(s);
    	StringBuffer sb = new StringBuffer();
    	
    	for (char c = scit.current(); c != CharacterIterator.DONE; c = scit.next()) {
			if ('&' == c) {
				int index = scit.getIndex();
				// encoded string start
				c = parseHTMLCharEntity(scit);
				if (CharacterIterator.DONE == c) {
					c = '&';
					scit.setIndex( index );
				}
			}

			sb.append(c);
		}
    	
    	return sb.toString();
    }
    
    public static void main(String[] args) {
    	String s = "&#12562;&#12585;&#12579;";
    	System.out.println( htmlCharEntityDecode(s) );
    	System.out.println("U+340C");
    }
    
//    public static void main(String[] args) {
//    	String s = "&#955;, &#x03BB; or &#X03bb;";
//    	System.out.println(s);
////    	System.out.println(htmlCharEntityEncode(s, false));
////    	System.out.println(htmlCharEntityEncode(s, true));
//    	String ss = TextUtil.xmlSafeString( s , false );
////    	String ss = TextUtil.xmlSafeString( s , true );
////    	String ss = htmlCharEntityEncode(s, true);
//    	System.out.println(ss);
//    	String ds = htmlCharEntityDecode(ss);
//    	System.out.println(ds);
//    	System.out.println(s.equals(ds));
//    	
//    	System.out.println(htmlCharEntityDecode(s));
//    }
}
