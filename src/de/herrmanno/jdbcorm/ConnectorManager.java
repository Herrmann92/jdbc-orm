package de.herrmanno.jdbcorm;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.function.Supplier;

import de.herrmanno.jdbcorm.conf.Conf;
import de.herrmanno.jdbcorm.exceptions.NoConfigDefinedException;

public class ConnectorManager {

	static private Conf conf;
	static private Supplier<Connection> connectionSupplier = null;
	
	static public void setConf(Conf conf) {
		ConnectorManager.conf = conf;
	} 
	
	public static Conf getConf() {
		return conf;
	}
	
	static public Connection getConnection() throws SQLException, ClassNotFoundException, NoConfigDefinedException {
		if(conf == null) {
			throw new NoConfigDefinedException();
		}
		
		Connection conn = null;
		if(connectionSupplier == null) {
			Class.forName(conf.getDriverClass());
			conn = DriverManager.getConnection(conf.getConnectionString());
		} else {
			conn = connectionSupplier.get();
		}
		
		//return conn;
		ConnectionProxy p = new ConnectionProxy(conn);
		return (Connection) Proxy.newProxyInstance(
				Connection.class.getClassLoader(),
				new Class[] {Connection.class},
				p);
		
	}
	
	public static void setConnectionSupplier(Supplier<Connection> supplier) {
		ConnectorManager.connectionSupplier = supplier;
	}

	static class ConnectionProxy implements InvocationHandler {

		private final Connection conn;
		
		private ConnectionProxy(Connection c) {
			this.conn = c;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if(method.getName().equals("setSavepoint")) {
				if(args == null || args.length == 0) {
					return setSavepoint();
				} 
			}
			else if(method.getName().equals("rollback") && args != null && args.length == 1) {
				rollback((Savepoint) args[0]); 
				return null;
			}
			
			else {
				return method.invoke(conn, args);
			}
			
			throw new Exception("Unsupported Method");
		}
		
		

		private void rollback(Savepoint sp) throws SQLException {
			if(sp != null)
				conn.rollback(sp);
			else
				conn.rollback();
		}

		private Savepoint setSavepoint() {
			Savepoint sp = null;
			try {
				sp = conn.setSavepoint();
			} catch(Exception e) {}
			
			return sp;
		}

		
	} 
}
