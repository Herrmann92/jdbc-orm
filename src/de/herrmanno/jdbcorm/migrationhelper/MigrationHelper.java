package de.herrmanno.jdbcorm.migrationhelper;

import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import de.herrmanno.jdbcorm.ConnectorManager;
import de.herrmanno.jdbcorm.exceptions.NoConfigDefinedException;
import de.herrmanno.jdbcorm.tables.Entity;
import de.herrmanno.jdbcorm.tables.EntityHelper;
import de.herrmanno.jdbcorm.tables.StaticFieldProxy;
import de.herrmanno.jdbcorm.util.ClassFinder;

public abstract class MigrationHelper {

	public void migrate(String _package) throws Exception {
		
		Queue<Class<? extends Entity>> classes = new ArrayDeque<Class<? extends Entity>>();

		for(Class<?> c : getClasses(_package)) {
			classes.add((Class<? extends Entity>) c);
		}
		
		List<Class<?>> migratedClasses = new ArrayList<Class<?>>();
		
		try(Connection conn = ConnectorManager.getConnection()) {
			try {
				
				conn.setAutoCommit(false);
				
				while(classes.peek() != null) {
					
					Class<? extends Entity> c = classes.poll();
					
					//------- Check if there are ForeinKeyField, which Classes aren't migrated yet
					boolean doLater = false;
					for(StaticFieldProxy refFp : EntityHelper.getSingleReferencedFields(c)) {
						if(!migratedClasses.contains(refFp.getType()))
							doLater = true;
					}
					
					if(doLater) {
						classes.add(c);
						continue;
					}
					
					//TODO create stbl from references here and insert into Queue
					migrate(conn, c);
					migratedClasses.add(c);
				}
				
				conn.commit();
			} catch(Exception e) {
				conn.rollback();
			}
		}
	}
	
	public void migrate(Class<? extends Entity> c) throws ClassNotFoundException, SQLException, NoConfigDefinedException, Exception {
		Connection connection = ConnectorManager.getConnection();
		connection.setAutoCommit(false);
		
		migrate(connection, c);
		
		connection.commit();
	}
	
	protected List<Class<?>> getClasses(String _package) {
		return ClassFinder.find(_package)
			.stream()
			.filter(c -> Entity.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers()))
			.collect(Collectors.toList());
	}
	
	protected abstract void migrate(Connection conn, Class<? extends Entity> c) throws Exception;
	
}
