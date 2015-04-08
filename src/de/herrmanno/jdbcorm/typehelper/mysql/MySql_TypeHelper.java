package de.herrmanno.jdbcorm.typehelper.mysql;

import java.text.SimpleDateFormat;

import de.herrmanno.jdbcorm.typehelper.SQL_TypeHelper;

public class MySql_TypeHelper extends SQL_TypeHelper {

	@Override
	protected String getIntType() {
		return "INT(11)";
	}

	@Override
	protected String getLongType() {
		return "INT(11)";
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

	
}
