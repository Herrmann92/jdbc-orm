package de.herrmanno.jdbcorm.querybuilder.mysql;

import de.herrmanno.jdbcorm.querybuilder.SQL_QueryBuilder;

public class MySql_QueryBuilder extends SQL_QueryBuilder {

	@Override
	protected String getAutoIncrementSQL() {
		return "AUTO_INCREMENT";
	}

	@Override
	protected String getPrimaryKeyInlineSQL() {
		return "PRIMARY KEY";
	}

}
