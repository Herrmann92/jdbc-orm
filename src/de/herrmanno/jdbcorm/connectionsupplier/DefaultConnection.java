package de.herrmanno.jdbcorm.connectionsupplier;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.function.Supplier;

import de.herrmanno.jdbcorm.JDBCORM;

public class DefaultConnection implements Supplier<Connection> {

	@Override
	public Connection get() {
		try {
			Class.forName(JDBCORM.getConf().getDriverClass());
			return DriverManager.getConnection(JDBCORM.getConf().getConnectionString());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
