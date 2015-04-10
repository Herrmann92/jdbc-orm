package de.herrmanno.jdbcorm.migrationhelper;

import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.herrmanno.jdbcorm.ConnectorManager;
import de.herrmanno.jdbcorm.constants.CascadeType;
import de.herrmanno.jdbcorm.tables.Entity;
import de.herrmanno.jdbcorm.tables.EntityHelper;
import de.herrmanno.jdbcorm.tables.JoinTable;
import de.herrmanno.jdbcorm.tables.StaticFieldProxy;
import de.herrmanno.jdbcorm.util.ClassFinder;

public abstract class MigrationHelper2 {

	@SuppressWarnings("unchecked")
	public void migrate(String _package) throws Exception {
		Collection<Class<? extends Entity>> classes = new ArrayList<Class<? extends Entity>>();

		for(Class<?> c : getClasses(_package)) {
			classes.add((Class<? extends Entity>) c);
		}
		
		
		Set<JoinTable> joinTables = new HashSet<JoinTable>();
		
		try(Connection conn = ConnectorManager.getConnection()) {
			Savepoint savePoint = conn.setSavepoint();
			try {
				
				conn.setAutoCommit(false);
				
				
				dropDatabase(conn);
				createDatabase(conn);
				
				beforeMigration(conn);
				
				for(Class<? extends Entity> c : classes) {
					validateTable(conn, c);
				}
				
				
				//------- Get all JoinTables
				for(Class<? extends Entity> clazz : classes) {
					for(StaticFieldProxy refFp : EntityHelper.getFields(clazz)) {
						if(refFp.getIsJoinReference()) {
							JoinTable jt = new JoinTable(refFp);
							joinTables.add(jt);
						}
					}
				}
				
				//------- Drop all JoinTables
				for(JoinTable jt : joinTables) {
					dropTable(conn,  jt);
				}
				
				
				//------- Drop all ForeignKeys
				for(Class<? extends Entity> c : classes) {
					for(StaticFieldProxy fkField : EntityHelper.getForeignKeyFields(c)) {
						try {
							dropForeignKey(conn, fkField);
						} catch(Exception e) {
							System.err.println("Coul not drop ForeinKey for Field '" + fkField + "'");
						}
							
					}
				}
				
				
				//------- Drop all Classes
				for(Class<? extends Entity> c : classes) {
					dropTable(conn, c);
					//droppedClasses.add(c);
				}
				
				
				//------- Then create all Classes
				for(Class<? extends Entity> c : classes) {
					createTable(conn, c);
					//migratedClasses.add(c);
				}
				
				
				//------- Create all ForeignKeys
				for(Class<? extends Entity> c : classes) {
					for(StaticFieldProxy fkField : EntityHelper.getForeignKeyFields(c)) {
						createForeignKey(conn, fkField);
					}
				}
				
				
				//------- Create all JoinTables
				for (JoinTable jt : joinTables) {
					createTable(conn, jt);
				}
				
				
				afterMigration(conn);
				
				conn.commit();
			} catch(Exception e) {
				e.printStackTrace();
				conn.rollback(savePoint);
			}
		}
	}
	
	
	
	protected abstract void beforeMigration(Connection conn) throws Exception;

	
	protected abstract void afterMigration(Connection conn) throws Exception;
	
	
	protected abstract void createDatabase(Connection conn) throws Exception;


	protected abstract void dropDatabase(Connection conn) throws Exception;


	protected abstract void validateTable(Connection conn, Class<? extends Entity> c) throws Exception;


	protected abstract void createTable(Connection conn, Class<? extends Entity> c) throws Exception;


	protected abstract void createTable(Connection conn, JoinTable jt) throws Exception;


	protected abstract void dropTable(Connection conn, Class<? extends Entity> c) throws Exception;


	protected abstract void dropTable(Connection conn, JoinTable jt) throws Exception;


	protected abstract void createForeignKey(Connection conn, Class<? extends Entity> c, String fieldName, Class<? extends Entity> refC, CascadeType onDeleteType, CascadeType onUpdateType) throws Exception;


	protected abstract void dropForeignKey(Connection conn, Class<? extends Entity> c, String fieldName, Class<? extends Entity> refC, CascadeType onDeleteType, CascadeType onUpdateType) throws Exception;

	@SuppressWarnings("unchecked")
	private void createForeignKey(Connection conn, StaticFieldProxy fkField) throws Exception {
		createForeignKey(conn, fkField.getDeclaringClass(), fkField.getName(), fkField.getType(), fkField.getOnDeleteType(), fkField.getOnUpdateType());
	};
	
	@SuppressWarnings("unchecked")
	private void dropForeignKey(Connection conn, StaticFieldProxy fkField) throws Exception {
		dropForeignKey(conn, fkField.getDeclaringClass(), fkField.getName(), fkField.getType(), fkField.getOnDeleteType(), fkField.getOnUpdateType());
	};
	
	

	
	private List<Class<?>> getClasses(String _package) {
		return ClassFinder.find(_package)
			.stream()
			.filter(c -> Entity.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers()))
			.collect(Collectors.toList());
	}
}
