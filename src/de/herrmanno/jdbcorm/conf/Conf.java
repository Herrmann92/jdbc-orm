package de.herrmanno.jdbcorm.conf;

import de.herrmanno.jdbcorm.migrationhelper.MigrationHelper2;
import de.herrmanno.jdbcorm.queryhelper.QueryHelper;
import de.herrmanno.jdbcorm.typehelper.TypeHelper;

public /*abstract class*/ interface Conf {

	abstract public String getDriverClass();
	
	abstract public String getConnectionString();
	
	public QueryHelper getQueryHelper();
	
	public TypeHelper getTypeHelper();
	
	public MigrationHelper2 getMigrationHelper();
	
}
