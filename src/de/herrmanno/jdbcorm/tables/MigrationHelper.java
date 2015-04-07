package de.herrmanno.jdbcorm.tables;

import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import de.herrmanno.jdbcorm.ConnectorManager;
import de.herrmanno.jdbcorm.conf.Conf;
import de.herrmanno.jdbcorm.exceptions.NoConfigDefinedException;
import de.herrmanno.jdbcorm.util.ClassFinder;

public class MigrationHelper {

	public static void migrate(String _package) throws Exception {
		List<Class<?>> classesList = ClassFinder.find(_package)
			.stream()
			.filter(c -> Entity.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers()))
			.collect(Collectors.toList());

		Queue<Class<? extends Entity>> classes = new ArrayDeque<Class<? extends Entity>>();
		for(Class<?> c : classesList) {
			classes.add((Class<? extends Entity>) c);
		}
		
		try(Connection conn = ConnectorManager.getConnection()) {
			conn.setAutoCommit(false);
			
			while(classes.peek() != null) {
				
				Class<? extends Entity> c = classes.poll();
				//TODO create stbl from references here and insert into Queue
				migrate(conn, c);
			}
			
			conn.commit();
		}
	}
	
	public static void migrate(Class<? extends Entity> c) throws ClassNotFoundException, SQLException, NoConfigDefinedException, Exception {
		migrate(ConnectorManager.getConnection(), c);
	}
	
	
	private static void migrate(Connection conn, Class<? extends Entity> c) throws Exception {
		
		Conf conf = ConnectorManager.getConf();
		String sql = conf.getQueryBuilder().getCreateScript(c);
		Statement stmt = conn.createStatement();
		stmt.execute(sql);
		
	}
	
}
