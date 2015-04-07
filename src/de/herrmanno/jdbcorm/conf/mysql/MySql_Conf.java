package de.herrmanno.jdbcorm.conf.mysql;

import de.herrmanno.jdbcorm.conf.BaseConfig;
import de.herrmanno.jdbcorm.querybuilder.QueryBuilder;
import de.herrmanno.jdbcorm.querybuilder.mysql.MySql_QueryBuilder;

public abstract class MySql_Conf extends BaseConfig {

	@Override
	public String getDriverClass() {
		return "com.mysql.jdbc.Driver";
	}

	@Override
	public String getConnectionString() {
		return "jdbc:mysql://" 
				+ getHost()
				+ (getPort() != null ? (":" + getPort()) : (""))
				+ "/" + getSchema()
				+ "?user=" + getUsername()
				+ "&password=" + getPassword();
	}
	
	
	@Override
	public QueryBuilder getQueryBuilder() {
		return new MySql_QueryBuilder();
	}

	abstract protected String getHost();
	
	abstract protected String getPort();
	
	abstract protected String getSchema();
	
	abstract protected String getUsername();
	
	abstract protected String getPassword();

}
