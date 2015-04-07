package de.herrmanno.jdbcorm.conf;

import de.herrmanno.jdbcorm.constants.MigrationType;
import de.herrmanno.jdbcorm.querybuilder.QueryBuilder;

public /*abstract class*/ interface Conf {

	abstract public String getDriverClass();
	
	abstract public String getConnectionString();
	
	public QueryBuilder getQueryBuilder();
	
	public MigrationType getMigrationType();
	
}
