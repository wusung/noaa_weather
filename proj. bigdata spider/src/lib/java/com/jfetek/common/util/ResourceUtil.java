package com.jfetek.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfetek.common.SystemDefault;

public final class ResourceUtil {
	
	private static final Logger	LOGGER	= LoggerFactory.getLogger(ResourceUtil.class);

	
	private ResourceUtil() {
	}

	public static boolean isFastOutputStream(OutputStream output) {
		if (null == output) return false;
		if (output instanceof ByteArrayOutputStream) return true;
		if (output instanceof BufferedOutputStream) return true;
		return false;
	}

	public static OutputStream ensureFastOutputStream(OutputStream output) {
		if (null == output) return null;
		if (output instanceof ByteArrayOutputStream) return output;
		if (output instanceof BufferedOutputStream) return output;
		return new BufferedOutputStream( output );
	}
	
	public static boolean isFastInputStream(InputStream input) {
		if (null == input) return false;
		if (input instanceof ByteArrayInputStream) return true;
//		if (input instanceof StringBufferInputStream) return true;
		if (input instanceof BufferedInputStream) return true;
		return false;
	}

	public static InputStream ensureFastInputStream(InputStream input) {
		if (null == input) return null;
		if (input instanceof ByteArrayInputStream) return input;
//		if (input instanceof StringBufferInputStream) return input;
		if (input instanceof BufferedInputStream) return input;
		return new BufferedInputStream( input );
	}
	
	public static boolean isFastWriter(Writer writer) {
		if (null == writer) return false;
		if (writer instanceof CharArrayWriter) return true;
		if (writer instanceof StringWriter) return true;
		if (writer instanceof BufferedWriter) return true;
		return false;
	}

	public static Writer ensureFastWriter(Writer writer) {
		if (null == writer) return null;
		if (writer instanceof CharArrayWriter) return writer;
		if (writer instanceof StringWriter) return writer;
		if (writer instanceof BufferedWriter) return writer;
		return new BufferedWriter( writer );
	}
	
	public static boolean isFastReader(Reader reader) {
		if (null == reader) return false;
		if (reader instanceof CharArrayReader) return true;
		if (reader instanceof StringReader) return true;
		if (reader instanceof BufferedReader) return true;
		return false;
	}

	public static Reader ensureFastReader(Reader reader) {
		if (null == reader) return null;
		if (reader instanceof CharArrayReader) return reader;
		if (reader instanceof StringReader) return reader;
		if (reader instanceof BufferedReader) return reader;
		return new BufferedReader( reader );
	}
	
	
//	public static String getString(String locate) {
//		return getString( getResourceURL(locate) );
//	}
	
	public static String getString(File file) {
		try {
			return getString( file.toURI().toURL() );
		} catch (MalformedURLException e) { }
		return null;
	}
	
	public static String getString(URL resource) {
		if (resource == null) return null;
		try {
			Charset cs = detectCharset(resource);
			if (null == cs) cs = SystemDefault.CHARSET;
			return getString(resource.openStream(), -1, cs);
		} catch (IOException e) {
		}
		return null;
	}
	
	public static String getString(InputStream from) {
		return getString(from, -1);
	}
	
	public static String getString(InputStream from, int length) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ResourceUtil.channel(from, bos);
		byte[] buff = bos.toByteArray();
		Charset cs = detectCharset(buff, CHARSET_TEST_ORDER);
		if (null == cs) cs = SystemDefault.CHARSET;
		return getString(new ByteArrayInputStream(buff), length, cs);
	}
	
	public static String getString(InputStream from, int length, Charset charset) {
		if (from == null) return null;
		
		String ret = null;
		BufferedReader in = null;
		try {
			in = new BufferedReader( new InputStreamReader( from , charset ) );
			char[] buff = new char[SystemDefault.BUFFER_SIZE];
			int len;
			StringBuffer tmp = new StringBuffer();
			
			if (length < 0) {
				while ((len = in.read(buff, 0, SystemDefault.BUFFER_SIZE)) != -1) {
					tmp.append(buff, 0, len);
				}
			}
			else {
//				int total = 0;
				int left = length;
				while ((len = in.read(buff, 0, left>SystemDefault.BUFFER_SIZE? SystemDefault.BUFFER_SIZE : left)) != -1) {
					tmp.append(buff, 0, len);
					left -= len;
					if (left <= 0) break;
				}
			}
			ret = tmp.toString();
			
		} catch(IOException e) {
			
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch(Exception e) { }
				in = null;
			}
		}
		
		return ret.toString();
	}
	
//	public static byte[] getByteArray(String locate) {
//		return getByteArray( getResourceURL(locate) );
//	}

	public static byte[] getByteArray(File file) {
		try {
			return getByteArray( file.toURI().toURL() );
		} catch (MalformedURLException e) { }
		return null;
	}
	
	public static byte[] getByteArray(URL resource) {
		if (resource == null) return null;

		ByteArrayOutputStream tmp = getByteArrayOutputStream(resource);
		
		return tmp==null? null : tmp.toByteArray();
	}

//	public static ByteArrayOutputStream getByteArrayOutputStream(String locate) {
//		return getByteArrayOutputStream( getResourceURL(locate) );
//	}

	public static ByteArrayOutputStream getByteArrayOutputStream(File file) {
		try {
			return getByteArrayOutputStream( file.toURI().toURL() );
		} catch (MalformedURLException e) { }
		return null;
	}
	
	public static ByteArrayOutputStream getByteArrayOutputStream(URL resource) {
		if (resource == null) return null;

		ByteArrayOutputStream tmp = new ByteArrayOutputStream();
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream( resource.openStream() );
//            channel(in, tmp, false);
			channel(in, tmp);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch(Exception e) { }
				in = null;
			}
		}
		
		return tmp;
	}
	
//	public static InputStream getInputStream(String locate) {
//		return getInputStream( getResourceURL(locate) );
//	}

	public static InputStream getInputStream(File file) {
		try {
			return getInputStream( file.toURI().toURL() );
		} catch (MalformedURLException e) { }
		return null;
	}
	
	public static InputStream getInputStream(URL resource) {
		if (resource == null) return null;
		
		URLConnection conn = null;
		try {
			conn = resource.openConnection();
			if (conn.getDoInput()) {
				return new BufferedInputStream( conn.getInputStream() );
			}
		} catch(IOException e) {
		} finally {
		}
		
		return null;
	}
	
//	public static OutputStream getOutputStream(String locate) {
//		return getOutputStream( getResourceURL(locate) );
//	}

	public static OutputStream getOutputStream(File file) {
		try {
			return getOutputStream( file.toURI().toURL() );
		} catch (MalformedURLException e) { }
		return null;
	}
	
	public static OutputStream getOutputStream(URL resource) {
		if (resource == null) return null;

		URLConnection conn = null;
		try {
			conn = resource.openConnection();
			if (conn.getDoOutput()) {
				return new BufferedOutputStream( conn.getOutputStream() );
			}
		} catch(IOException e) {
		} finally {
		}
		
		return null;
	}
	
	
//	public static File duplicate(String locate) {
//		return duplicate( getResourceURL(locate) );
//	}

	public static File duplicate(File file) {
		try {
			return duplicate( file.toURI().toURL() );
		} catch (MalformedURLException e) { }
		return null;
	}
	
	public static File duplicate(URL resource) {
		if (resource == null) return null;
		
		boolean ok = false;
		File f = null;
		BufferedOutputStream out = null;
		try {
			f = File.createTempFile("rsutil.", ".duplicate");
			out = new BufferedOutputStream(new FileOutputStream(f));
            ok = channel(resource.openStream(), out);
		} catch (IOException e) {
			f = null;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch(Exception e) { }
				out = null;
			}
			if(!ok && f != null) {
				if (!f.delete()) {
					f.deleteOnExit();
				}
				f = null;
			}
		}
		
		return f;
	}
	
//	public static boolean copy(String locate, File to) {
//		return copy( getResourceURL(locate) , to );
//	}
	
	public static boolean copy(File from, File to) {
		try {
			return copy( from.toURI().toURL() , to );
		} catch (MalformedURLException e) { }
		return false;
	}
	
	public static boolean copy(URL resource, File to) {
		File tmp = duplicate(resource);
		if (tmp != null) {
			boolean ok = true;
			if (to.exists()) {
				ok = to.delete();
			}
			if (ok) {
				ok = tmp.renameTo( to );
				return ok;
			}
			else {
				tmp.delete();
			}
		}
		return false;
	}

//	public static boolean output(String locate, OutputStream to) {
//		return output(getResourceURL(locate), to);
//	}
	
	public static boolean output(File[] srcs, OutputStream to) {
		boolean ok = true;
		BufferedInputStream in = null;
		to = ensureFastOutputStream( to );
		for (int i = 0, len = srcs.length; i < len; ++i) {
			if (!ok) break;
			try {
				in = new BufferedInputStream(new FileInputStream(srcs[i]));
				ok &= channel(in, to);
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					}
					catch(Exception e) { }
					in = null;
				}
			}
		}
		return ok;
	}
	public static boolean output(URL[] urls, OutputStream to) {
		boolean ok = true;
		BufferedInputStream in = null;
		to = ensureFastOutputStream( to );
		for (int i = 0, len = urls.length; i < len; ++i) {
			if (!ok) break;
			try {
				in = new BufferedInputStream(urls[i].openStream());
				ok &= channel(in, to);
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					}
					catch(Exception e) { }
					in = null;
				}
			}
		}
		return ok;
	}
	public static boolean output(InputStream[] inputs, OutputStream to) {
		boolean ok = true;
		InputStream in = null;
		to = ensureFastOutputStream( to );
		for (int i = 0, len = inputs.length; i < len; ++i) {
			if (!ok) break;
			try {
				in = ensureFastInputStream( inputs[i] );
				ok &= channel(in, to);
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					}
					catch(Exception e) { }
					in = null;
				}
			}
		}
		return ok;
	}
	
	public static boolean output(File src, OutputStream to) {
		try {
			return output(src.toURI().toURL(), to);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean output(URL resource, OutputStream to) {
		boolean ok = false;
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(resource.openStream());
			ok = channel(in, to);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				}
				catch(Exception e) { }
				in = null;
			}
		}
		return ok;
	}
	
	public static boolean output(byte[] src, OutputStream to) {
		boolean ok = false;
		OutputStream out = null;
		try {
//			boolean isMemOut = to instanceof ByteArrayOutputStream;
//			boolean isBufOut = to instanceof BufferedOutputStream;
//			out = isMemOut||isBufOut? to : new BufferedOutputStream(to);
			
			out = ensureFastOutputStream( to );
			out.write(src);
			out.flush();
			ok = true;
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch(Exception e) { }
				out = null;
			}
		}
		return ok;
	}
	
	public static boolean output(byte[] src, File to) {
		boolean ok = false;
		BufferedOutputStream out = null;
		try {
			out = new BufferedOutputStream( new FileOutputStream(to) );
			out.write(src);
			out.flush();
			ok = true;
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				}
				catch(Exception e) { }
				out = null;
			}
		}
		return ok;
	}
	
	public static boolean output(InputStream from, File to) {
		boolean ok = false;
		BufferedOutputStream out = null;
		try {
			out = new BufferedOutputStream( new FileOutputStream(to) );
			ok = channel(from, out);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				}
				catch(Exception e) { }
				out = null;
			}
		}
		return ok;
	}
	
//	public static boolean channel(BufferedInputStream from, BufferedOutputStream to) {
//		if (from == null || to == null) return false;
//
//		return channel(from, to, false);
//	}
//
//	public static boolean channel(ByteArrayInputStream from, ByteArrayOutputStream to) {
//		if (from == null || to == null) return false;
//
//		return channel(from, to, false);
//	}
//
//	public static boolean channel(ByteArrayInputStream from, BufferedOutputStream to) {
//		if (from == null || to == null) return false;
//
//		return channel(from, to, false);
//	}
//
//	public static boolean channel(BufferedInputStream from, ByteArrayOutputStream to) {
//		if (from == null || to == null) return false;
//
//		return channel(from, to, false);
//	}
//
//	public static boolean channel(InputStream from, OutputStream to, boolean check_buffed) {
//		if (from == null || to == null) return false;
//
//		if (check_buffed) {
//			InputStream bfIn = null;
//			OutputStream bfOut = null;
//			
//			boolean isMemIn = from instanceof ByteArrayInputStream;
//			boolean isMemOut = to instanceof ByteArrayOutputStream;
//			boolean isBufIn = from instanceof BufferedInputStream;
//			boolean isBufOut = to instanceof BufferedOutputStream;
//			
//			bfIn = isMemIn||isBufIn? from : new BufferedInputStream(from);
//			bfOut = isMemOut||isBufOut? to : new BufferedOutputStream(to);
//			
//			return channel(bfIn, bfOut);
//		}
//
//		return channel(from, to);
//	}
	
	public static boolean channel(InputStream from, OutputStream to) {
		if (from == null || to == null) return false;
		
		boolean ok = false;
		try {
			from = ensureFastInputStream( from );
			to = ensureFastOutputStream( to );
			byte[] buff = new byte[SystemDefault.BUFFER_SIZE];
			int len;
			
			while ((len = from.read(buff, 0, SystemDefault.BUFFER_SIZE)) != -1) {
				to.write(buff, 0, len);
			}
//			bfOut.flush();
			ok = true;
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (to != null) {
				try {
					to.flush();
				} catch(Exception e) { }
			}
		}
		return ok;
	}

	public static boolean channel(Reader from, Writer to) {
		if (from == null || to == null) return false;
		
		boolean ok = false;
		try {
			from = ensureFastReader( from );
			to = ensureFastWriter( to );
			char[] buff = new char[SystemDefault.BUFFER_SIZE];
			int len;
			
			while ((len = from.read(buff, 0, SystemDefault.BUFFER_SIZE)) != -1) {
				to.write(buff, 0, len);
			}
			ok = true;
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (to != null) {
				try {
					to.flush();
				} catch(Exception e) { }
			}
		}
		return ok;
	}

	

	public static ArrayList<String> getList(File file) {
		try {
			Charset cs = detectCharset(file);
			if (null == cs) cs = SystemDefault.CHARSET;
			return getList( file.toURI().toURL() , cs , false );
		} catch (MalformedURLException e) { e.printStackTrace(); }
		return null;
	}
	public static ArrayList<String> getList(File file, boolean raw_line) {
		try {
			Charset cs = detectCharset(file);
			if (null == cs) cs = SystemDefault.CHARSET;
			return getList( file.toURI().toURL() , cs , raw_line );
		} catch (MalformedURLException e) { e.printStackTrace(); }
		return null;
	}
	public static ArrayList<String> getList(URL resource) {
		Charset cs = detectCharset(resource);
		if (null == cs) cs = SystemDefault.CHARSET;
		return getList(resource, SystemDefault.CHARSET, false);
	}
	public static ArrayList<String> getList(File file, Charset charset) {
		try {
			return getList( file.toURI().toURL() , charset , false );
		} catch (MalformedURLException e) { e.printStackTrace(); }
		return null;
	}
	public static ArrayList<String> getList(File file, Charset charset, boolean raw_line) {
		try {
			return getList( file.toURI().toURL() , charset , raw_line );
		} catch (MalformedURLException e) { e.printStackTrace(); }
		return null;
	}
	public static ArrayList<String> getList(URL resource, Charset charset) {
		return getList(resource, charset, false);
	}
	public static ArrayList<String> getList(URL resource, Charset charset, boolean raw_line) {
		if (resource == null) return null;
		if (null == charset) charset = SystemDefault.CHARSET;

		ArrayList<String> arr = new ArrayList<String>();
		BufferedReader in = null;
		try {
			in = new BufferedReader( new InputStreamReader( resource.openStream() , charset ) );
			String line = null;
			if (raw_line) {
				while ((line = in.readLine()) != null) {
					arr.add(line);
				}
			}
			else {
				while ((line = in.readLine()) != null) {
					line = line.trim();
					if ("".equals( line ) || '#' == line.charAt(0)) {
						// ignore
						continue;
					}
					else if (TextUtil.isQuoted(line)) {
						// escaped string is quoted
						line = TextUtil.removeQuote(line);
						line = TextUtil.unescape(line);
					}
					arr.add(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch(Exception e) { }
				in = null;
			}
		}
		
		return arr;
	}
	


//	public static Map<String,String> getMap(File file) {
//		try {
//			return getMap( file.toURI().toURL() );
//		} catch (MalformedURLException e) { e.printStackTrace(); }
//		return null;
//	}
//	public static Map<String,String> getMap(URL resource) {
//		HashMap<String,String> m = new HashMap<String,String>();
//		BufferedReader in = null;
//		try {
//			
//			in = new BufferedReader( new InputStreamReader( resource.openStream() , SystemDefault.CHARSET ) );
//			String line = null;
//			while ((line = in.readLine()) != null) {
//				line = line.trim();
//				if ("".equals( line ) || line.startsWith( "#" )) {
//					// ignore
//				}
//				else {
//					String[] pair = line.split("\\s*=\\s*", 2);
//					if (pair == null || pair.length == 0) {
//						// just ignore
//					}
//					else if (pair.length == 1) {
//						m.put(pair[0].trim(), "");
//					}
//					else {
//						m.put(pair[0].trim(), pair[1].trim());
//					}
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (in != null) {
//				try {
//					in.close();
//				} catch(Exception e) { }
//				in = null;
//			}
//		}
//		
//		return p;
//	}
	
	private static final byte[]		BOM_UTF8		= {	(byte)0xEF, (byte)0xBB, (byte)0xBF	};
	private static final byte[]		BOM_UTF16BE		= {	(byte)0xFE, (byte)0xFF	};
	private static final byte[]		BOM_UTF16LE		= {	(byte)0xFF, (byte)0xFE	};
	private static final byte[]		BOM_UTF32BE		= {	(byte)0x00, (byte)0x00, (byte)0xFE, (byte)0xFF	};
	private static final byte[]		BOM_UTF32LE		= {	(byte)0xFF, (byte)0xFE, (byte)0x00, (byte)0x00	};
	
	private static boolean isBomEquals(byte[] buffer, byte[] bom) {
		if (null == buffer || buffer.length < bom.length) return false;
		for (int i = 0, len = bom.length; i < len; ++i) {
			if (bom[i] != buffer[i]) return false;
		}
		return true;
	}
	
	private static final Charset[]	CHARSET_TEST_ORDER	= {
		Charset.forName("UTF-8"),
		Charset.forName("MS950"),
		Charset.forName("Big5"),
		Charset.forName("UTF-16LE")
//		Charset.forName("ASCII")
	};
	private static final int	DEFAULT_TEST_BUFFER_SIZE	= 512;
	public static Charset detectCharset(File file) {
		return detectCharset(file, CHARSET_TEST_ORDER);
	}
	public static Charset detectCharset(URL url) {
		return detectCharset(url, CHARSET_TEST_ORDER);
	}
	public static Charset detectCharset(URL url, String[] test_order) {
		if (null == test_order || 0 == test_order.length) return detectCharset(url, CHARSET_TEST_ORDER);

		int len = test_order.length;
		Charset[] charset_order = new Charset[len];
		for (int i = 0; i < len; ++i) {
			charset_order[i] = Charset.forName( test_order[i] );
		}

		return detectCharset(url, charset_order);
	}
	public static Charset detectCharset(byte[] buff) {
		return detectCharset(buff, CHARSET_TEST_ORDER);
	}
	public static Charset detectCharset(File file, String[] test_order) {
		if (null == test_order || 0 == test_order.length) return detectCharset(file, CHARSET_TEST_ORDER);

		int len = test_order.length;
		Charset[] charset_order = new Charset[len];
		for (int i = 0; i < len; ++i) {
			charset_order[i] = Charset.forName( test_order[i] );
		}

		return detectCharset(file, charset_order);
	}
	public static Charset detectCharset(URL url, Charset[] test_order) {
		return detectCharset(ResourceUtil.getByteArray(url), test_order);
	}
	public static Charset detectCharset(File file, Charset[] test_order) {
		return detectCharset(ResourceUtil.getByteArray(file), test_order);
	}
	public static Charset detectCharset(byte[] buff, Charset[] test_order) {
		return detectCharset(buff, DEFAULT_TEST_BUFFER_SIZE, test_order);
	}
	public static Charset detectCharset(byte[] buff, int test_size, Charset[] test_order) {
		if (null == test_order || 0 == test_order.length) test_order = CHARSET_TEST_ORDER;
		
//		// retrieve charset from contents
//		String charset = HttpUtil.retrieveCharset(ResourceUtil.getString(file), null);
//		try {
//			if (null != charset) {
//				return Charset.forName(charset);
//			}
//		} catch(UnsupportedCharsetException e) {
//			e.printStackTrace();
//		}
		
		// test bom
//		if (isBomEquals(buff, BOM_UTF8)) {
//			return Charset.forName("UTF-8");
//		}
//		else if (isBomEquals(buff, BOM_UTF16BE)) {
//			return Charset.forName("UTF-16BE");
//		}
//		else if (isBomEquals(buff, BOM_UTF16LE)) {
//			return Charset.forName("UTF-16LE");
//		}
//		else if (isBomEquals(buff, BOM_UTF32BE)) {
//			return Charset.forName("UTF-32BE");
//		}
//		else if (isBomEquals(buff, BOM_UTF32LE)) {
//			return Charset.forName("UTF-32LE");
//		}

		
		// test charset-decode
		ByteBuffer buffer = ByteBuffer.wrap(buff);
		for (int i = 0, len = test_order.length; i < len; ++i) {
			try {
				buffer.rewind();
				CharsetDecoder decoder = test_order[i].newDecoder();
				decoder.decode(buffer);
				
				return test_order[i];
			} catch (CharacterCodingException e) {
//				System.out.println("test not "+test_order[i]);
				LOGGER.trace("test not {}", test_order[i]);
			}
		}

		return null;
	}
	public static void main(String[] args) {
		File file = new File("x:/博1.xls");
		System.out.println(detectCharset(file));
	}
//	public static Charset detectCharset(InputStream in) {
//		return detectCharset(in, SystemDefault.CHARSET);
//	}
//	public static Charset detectCharset(InputStream in, Charset default_charset) {
//	}
	
	
	public static void close(Closeable closeable) {
		if (null != closeable) {
			try {
				closeable.close();
			} catch(Exception e) { }
			closeable = null;
		}
	}
	
//	public static getMap(File file) {
//	}
//	
//	public static getMap(URL url) {
//	}
//	
//	public static getMap(
}
