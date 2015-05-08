package com.jfetek.common;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.json.JSONException;
import org.json.JSONObject;

public class ErrorCode {

	public static final String	DEFAULT_OK_MESSAGE		= "ok";
	public static final String	DEFAULT_ERROR_MESSAGE	= "error";
	
	
	public static final int OK							= 0;
	public static final int ERROR						= 1;
	public static final int UNHANDLED_EXCEPTION			= 2;
	public static final int	INVALID_PARAMETER			= 3;
	public static final int ILLEGAL_ARGUMENT			= 4;
	
	//	Database Error-Code	Base:	100
	//	Authorize Error-Code Base:	200
	//	Validate Error-Code Base:	300
	//	Http Error-Code Base:		400
	
	
	public final int code;
	public final String message;
	
	protected ErrorCode(int code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public static final ErrorCode ok() {
		return new ErrorCode( ErrorCode.OK , DEFAULT_OK_MESSAGE );
	}
	public static final ErrorCode ok(String message) {
		return new ErrorCode( ErrorCode.OK , message );
	}
	
	public static ErrorCode error() {
		return new ErrorCode( ErrorCode.ERROR , DEFAULT_ERROR_MESSAGE );
	}
	public static ErrorCode error(String message) {
		return new ErrorCode( ErrorCode.ERROR , message );
	}
	public static final ErrorCode error(int code, String message) {
		return new ErrorCode( code , message );
	}
	public static final ErrorCode error(ErrorCode ec) {
		return new ErrorCode( ec.code , ec.message );
	}
	public static final ErrorCode error(ErrorCode ec, String message) {
		return new ErrorCode( ec.code , message );
	}
	public static final ErrorCode error(Throwable t) {
		StringWriter s = new StringWriter();
		PrintWriter p = new PrintWriter(s);
		t.printStackTrace(p);
		p.flush();
		return new ErrorCode( UNHANDLED_EXCEPTION , s.toString() );
	}
	public static final ErrorCode error(int code, Throwable t) {
		StringWriter s = new StringWriter();
		PrintWriter p = new PrintWriter(s);
		t.printStackTrace(p);
		p.flush();
		return new ErrorCode( code , s.toString() );
	}
	
	public final boolean positive() {
		return (this.code == OK);
	}
	public final boolean negative() {
		return (this.code != OK);
	}
	public final boolean negativeButNot(int code) {
		return (this.code != OK && this.code != code);
	}
	public final boolean positiveOr(int code) {
		return (this.code == OK || this.code == code);
	}

	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("EC#");
		s.append(code).append(": ").append(message);
		return s.toString();
	}
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		
		try {
			json.put("code", code)
				.put("message", message);
		} catch (JSONException e) {
		}

		return json;
	}

//	public String describe() {
//		return toJson().toString();
//	}
//
//	public JSONArray toJsonArray() {
//		return new JSONArray().put(code).put(message);
//	}
//
//
//	public ErrorCode realize(String describe) {
//		char c = describe.charAt(0);
//		if ('[' == c) {
//			try {
//				return realize(new JSONArray(describe));
//			} catch (JSONException e) { }
//			try {
//				return realize(new JSONObject(describe));
//			} catch (JSONException e) { }
//		}
//		else if ('{' == c) {
//			try {
//				return realize(new JSONObject(describe));
//			} catch (JSONException e) { }
//			try {
//				return realize(new JSONArray(describe));
//			} catch (JSONException e) { }
//		}
//		return null;
//	}
//
//	public ErrorCode realize(JSONObject json) {
//		return ErrorCode.error( json.optInt("code") , json.optString("message") );
//	}
//
//	public ErrorCode realize(JSONArray json) {
//		return ErrorCode.error( json.optInt(0) , json.optString(1) );
//	}

}
