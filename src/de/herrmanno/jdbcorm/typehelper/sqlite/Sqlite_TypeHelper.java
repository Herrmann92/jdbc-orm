package de.herrmanno.jdbcorm.typehelper.sqlite;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.herrmanno.jdbcorm.typehelper.SQL_TypeHelper;

public class Sqlite_TypeHelper extends SQL_TypeHelper {

	@Override
	protected String getIntType() {
		return "INTEGER";
	}

	@Override
	protected String getLongType() {
		return "INTEGER";
	}

	@Override
	protected String getStringType() {
		return "VARCHAR(256)";
	}

	@Override
	protected String getDateType() {
		return "DATE";
	}

	@Override
	protected String getIntValue(Object value) {
		return value.toString();
	}

	@Override
	protected String getLongValue(Object value) {
		return value.toString();
	}

	protected String getStringValue(Object value) {
		return "'" + value + "'";
	}

	protected String getDateValue(Object value){
		return "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value) + "'";
	}

	@Override
	protected Date parseDate(String value) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(value);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	
}
