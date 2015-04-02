package de.herrmanno.jdbcorm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectorManager {

	static private Conf conf;
	
	static public void setConf(Conf conf) {
		ConnectorManager.conf = conf;
	} 
	
	static public Connection getConnection() throws SQLException, ClassNotFoundException {
		if(conf == null) {
			//TODO custom Exception
			throw new NullPointerException();
		}
		
		Class.forName(conf.getDriverClass());
		Connection conn = DriverManager.getConnection(conf.getConnectionString());
		
		return conn;
		
		
	}
}
