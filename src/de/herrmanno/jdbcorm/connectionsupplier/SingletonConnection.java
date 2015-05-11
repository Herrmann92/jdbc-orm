package de.herrmanno.jdbcorm.connectionsupplier;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.function.Supplier;

import de.herrmanno.jdbcorm.JDBCORM;

public class SingletonConnection implements Supplier<Connection> {

	private final Connection conn;

	public SingletonConnection() throws Exception {
		Class.forName(JDBCORM.getConf().getDriverClass());
		Connection c = DriverManager.getConnection(JDBCORM.getConf().getConnectionString());

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
