package com.jfetek.common.data;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.jfetek.common.ErrorCode;

public class Result<R> extends ErrorCode {
	
//	public static <R> Result<R> error() {
//		return new Result<R>();
//	}
//	public static <R> Result<R> error
//	public static <R> Result<R> wrap(R data) {
//		return new Result<R>(data);
//	}

	public final long timestamp;
	public final R data;

	public Result(R data, ErrorCode ec) {
		super(ec.code, ec.message);
		this.timestamp = System.currentTimeMillis();
		this.data = data;
	}
	public Result(R data, int code, String message) {
		super(code, message);
		this.timestamp = System.currentTimeMillis();
		this.data = data;
	}
	protected Result(long timestamp, R data, int code, String message) {
		super(code, message);
		this.timestamp = timestamp;
		this.data = data;
	}


	public static final <R> Result<R> wrap(R result) {
		return new Result<R>( result , ErrorCode.OK , DEFAULT_OK_MESSAGE );
	}
	public static final <R> Result<R> wrap(R result, String message) {
		return new Result<R>( result , ErrorCode.OK , message );
	}
	public static final <R> Result<R> wrap(R result, int code, String message) {
		return new Result<R>( result , code , message );
	}
	public static final <R> Result<R> wrap(R result, ErrorCode ec) {
		return new Result<R>( result , ec.code, ec.message );
	}

	public static final <R> Result<R> failure() {
		return new Result<R>( null , ErrorCode.ERROR , DEFAULT_ERROR_MESSAGE );
	}
	public static final <R> Result<R> failure(String message) {
		return new Result<R>( null , ErrorCode.ERROR , message );
	}
	public static final <R> Result<R> failure(int code, String message) {
		return new Result<R>( null , code , message );
	}
	public static final <R> Result<R> failure(ErrorCode ec) {
		return new Result<R>( null , ec.code , ec.message );
	}
	public static final <R> Result<R> failure(ErrorCode ec, String message) {
		return new Result<R>( null , ec.code , message );
	}
	public static final <R> Result<R> failure(Throwable t) {
		StringWriter s = new StringWriter();
		PrintWriter p = new PrintWriter(s);
		t.printStackTrace(p);
		p.flush();
		return new Result<R>( null , UNHANDLED_EXCEPTION , s.toString() );
	}
	public static final <R> Result<R> failure(int code, Throwable t) {
		StringWriter s = new StringWriter();
		PrintWriter p = new PrintWriter(s);
		t.printStackTrace(p);
		p.flush();
		return new Result<R>( null , code , s.toString() );
	}
	
	
	@Override
	public String toString() {
		if (positive()) {
			return String.valueOf(this.data);
		}
		else {
			return super.toString();
		}
	}
	
	
//	@Override
//	public String describe() {
//		return toString();
//	}
//	
//	@Override
//	public Result<R> realize(String describe) {
//		try {
//			return realize(new JSONObject(describe));
//		} catch (JSONException e) { }
//		return null;
//	}
//	
//	@Override
//	public Result<R> realize(JSONObject json) {
//		long timestamp = json.optLong("timestamp");
//		JSONObject error = json.optJSONObject("error");
//		int code = error.optInt("code");
//		String message = error.optString("message");
//		Object data = json.opt("result");
//		Result<?> result = new Result(timestamp, data, code, message);
//		return result;
//	}
//
//	@Override
//	public JSONObject toJson() {
//		return JsonUtil.getBasicJson(this);
//	}

//	@Override
//	public JSONObject toJson() {
//		JSONObject json = new JSONObject();
//		
//		try {
//			json.put("code", code)
//				.put("message", message)
//				.put("result", JsonUtil.makeJsonize(data));
//		} catch (JSONException e) {
//		}
//
//		return json;
//	}
}
