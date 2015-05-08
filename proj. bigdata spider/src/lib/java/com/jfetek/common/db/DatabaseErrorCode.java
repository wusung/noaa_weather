package com.jfetek.common.db;

import com.jfetek.common.ErrorCode;

public class DatabaseErrorCode extends ErrorCode {

	private static int	BASE_CODE						= 100;

	public static final int DATABASE_ERROR			= BASE_CODE++;
	public static final int SQL_ERROR				= BASE_CODE++;
	public static final int	SQL_INSERT_EMPTY		= BASE_CODE++;
	public static final int	SQL_UPDATE_EMPTY		= BASE_CODE++;
	public static final int	SQL_QUERY_EMPTY			= BASE_CODE++;
	
	public static final int	SQL_DELETE_EMPTY		= BASE_CODE++;
	public static final int TABLES_NOT_LOCKED		= BASE_CODE++;
	public static final int TABLE_NOT_EXISTS		= BASE_CODE++;
	
	
	private DatabaseErrorCode(int code, String message) {
		super(code, message);
	}

	public static DatabaseErrorCode error() {
		return new DatabaseErrorCode( BASE_CODE , DEFAULT_ERROR_MESSAGE );
	}
	public static DatabaseErrorCode error(String message) {
		return new DatabaseErrorCode( BASE_CODE , message );
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("EC(db)#");
		s.append(code).append(": ").append(message);
		return s.toString();
	}
	
}
