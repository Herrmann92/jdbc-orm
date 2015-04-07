package de.herrmanno.jdbcorm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import de.herrmanno.jdbcorm.conf.Conf;
import de.herrmanno.jdbcorm.exceptions.NoConfigDefinedException;

public class ConnectorManager {

	static private Conf conf;
	
	static public void setConf(Conf conf) {
		ConnectorManager.conf = conf;
	} 
	
	static public Connection getConnection() throws SQLException, ClassNotFoundException, NoConfigDefinedException {
		if(conf == null) {
			throw new NoConfigDefinedException();
		}
		
		Class.forName(conf.getDriverClass());
		Connection conn = DriverManager.getConnection(conf.getConnectionString());
		
		return conn;
		
		
	}

	public static Conf getConf() {
		return conf;
	}
}
