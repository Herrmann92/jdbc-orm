package de.herrmanno.jdbcorm;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.function.Supplier;

public class SingleConnection implements Supplier<Connection> {

	private final Connection conn;
	
	public SingleConnection() throws Exception {
		Class.forName(ConnectorManager.getConf().getDriverClass());
		Connection c = DriverManager.getConnection(ConnectorManager.getConf().getConnectionString());
		
		ConnectionProxy p = new ConnectionProxy(c);
		this.conn = (Connection) Proxy.newProxyInstance(
				Connection.class.getClassLoader(),
				new Class[] {Connection.class},
				p);
	}
	
	@Override
	public Connection get() {
		return conn;
	}
	
	static class ConnectionProxy implements InvocationHandler {

		private final Connection conn;
		
		private ConnectionProxy(Connection c) {
			this.conn = c;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if(method.getName().equals("close")) {
				return null;
			}
			
			else {
				return method.invoke(conn, args);
			}
			
			
		}
	}

}
