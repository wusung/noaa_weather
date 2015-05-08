package com.jfetek.common.http;

import com.jfetek.common.ErrorCode;

public class HttpErrorCode extends ErrorCode {

	private static int	BASE_CODE						= 400;

	public static final int HTTP_ERROR				= BASE_CODE++;
	public static final int HTTP_REQUEST_ERROR		= BASE_CODE++;
	public static final int	HTTP_RESPONSE_ERROR		= BASE_CODE++;

	
	
	private HttpErrorCode(int code, String message) {
		super(code, message);
	}

	public static HttpErrorCode error() {
		return new HttpErrorCode( BASE_CODE , DEFAULT_ERROR_MESSAGE );
	}
	public static HttpErrorCode error(String message) {
		return new HttpErrorCode( BASE_CODE , message );
	}


	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("EC(http)#");
		s.append(code).append(": ").append(message);
		return s.toString();
	}
	
}
