package de.herrmanno.jdbcorm.conf.mysql;

import de.herrmanno.jdbcorm.conf.BaseConfig;
import de.herrmanno.jdbcorm.queryhelper.QueryHelper;
import de.herrmanno.jdbcorm.queryhelper.mysql.MySql_QueryHelper;
import de.herrmanno.jdbcorm.typehelper.TypeHelper;
import de.herrmanno.jdbcorm.typehelper.mysql.MySql_TypeHelper;

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
	public QueryHelper getQueryHelper() {
		return new MySql_QueryHelper();
	}
	
	@Override
	public TypeHelper getTypeHelper() {
		return new MySql_TypeHelper();
	}

	abstract protected String getHost();
	
	abstract protected String getPort();
	
	abstract protected String getSchema();
	
	abstract protected String getUsername();
	
	abstract protected String getPassword();

}
