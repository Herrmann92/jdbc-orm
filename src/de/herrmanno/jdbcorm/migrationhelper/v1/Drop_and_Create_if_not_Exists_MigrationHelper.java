package de.herrmanno.jdbcorm.migrationhelper.v1;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import de.herrmanno.jdbcorm.ConnectorManager;
import de.herrmanno.jdbcorm.conf.Conf;
import de.herrmanno.jdbcorm.tables.Entity;
import de.herrmanno.jdbcorm.tables.EntityHelper;
import de.herrmanno.jdbcorm.tables.JoinTable;
import de.herrmanno.jdbcorm.tables.StaticFieldProxy;

@Deprecated
public class Drop_and_Create_if_not_Exists_MigrationHelper extends MigrationHelper {

	@Override
	public void migrate(String _package) throws Exception {
		Queue<Class<? extends Entity>> classesDrop = new ArrayDeque<Class<? extends Entity>>();
		Queue<Class<? extends Entity>> classesCreate = new ArrayDeque<Class<? extends Entity>>();

		for(Class<?> c : getClasses(_package)) {
			classesDrop.add((Class<? extends Entity>) c);
			classesCreate.add((Class<? extends Entity>) c);
		}
		
		List<Class<? extends Entity>> migratedClasses = new ArrayList<Class<? extends Entity>>();
		List<Class<? extends Entity>> droppedClasses = new ArrayList<Class<? extends Entity>>();
		Set<JoinTable> joinTables = new HashSet<JoinTable>();
		
		try(Connection conn = ConnectorManager.getConnection()) {
			try {
				
				conn.setAutoCommit(false);
				
				
				//------- Get all JoinTables
				for(Class<? extends Entity> clazz : classesDrop) {
					for(StaticFieldProxy refFp : EntityHelper.getFields(clazz)) {
						if(refFp.getIsJoinReference()) {
							JoinTable jt = new JoinTable(refFp);
							joinTables.add(jt);
						}
					}
				}
				
				//------- Drop all JoinTables
				for(JoinTable jt : joinTables) {
					drop(conn,  jt);
				}
				
				
				//------- Drop all ForeignKeys
				for(Class<? extends Entity> c : classesDrop) {
					for(StaticFieldProxy fkField : EntityHelper.getForeignKeyFields(c)) {
						dropForeignKey(conn, fkField);
					}
				}
				
				
				//------- Drop first in reversed order
				while(classesDrop.peek() != null) {
					
					Class<? extends Entity> c = classesDrop.poll();
					
					/*
					//------- Check if there are ForeinKeyField, which aren't dropped yet
					boolean doLater = false;
					for(Class<? extends Entity> otherClass : classesDrop) {
						for(StaticFieldProxy refFp : EntityHelper.getFields(otherClass)) {
							if(refFp.getIsForeignKey() && refFp.getType() == c) {
								doLater = true;
							}
							if(refFp.getIsJoinReference()) {
								JoinTable jt = new JoinTable(refFp);
								if(!joinTables.contains(jt))
									drop(conn, jt);
								joinTables.add(jt);
							}
						}
					}
					
					if(doLater) {
						classesDrop.add(c);
						continue;
					}*/
					
					
					drop(conn, c);
					droppedClasses.add(c);
				}
				
				
				//------- Then Migrate
				while(classesCreate.peek() != null) {
				
					Class<? extends Entity> c = classesCreate.poll();
					
					/*
					//------- Check if there are ForeinKeyField, which Classes aren't migrated yet
					boolean doLater = false;
					for(StaticFieldProxy refFp : EntityHelper.getFields(c)) {
						if(refFp.getIsForeignKey() && !migratedClasses.contains(refFp.getType())) {
							doLater = true;
						}
					}
					
					if(doLater) {
						classesCreate.add(c);
						continue;
					}
					*/
					
					create(conn, c);
					migratedClasses.add(c);
				}
				
				//------- Create all ForeignKeys
				for(Class<? extends Entity> c : migratedClasses) {
					for(StaticFieldProxy fkField : EntityHelper.getForeignKeyFields(c)) {
						migrateForeignKey(conn, fkField);
					}
				}
				
				
				//------- Create all JoinTables
				for (JoinTable jt : joinTables) {
					create(conn, jt);
				}
				
				conn.commit();
			} catch(Exception e) {
				e.printStackTrace();
				conn.rollback();
			}
		}
	}
	
	
	
	@Override
	protected void migrate(Connection conn, Class<? extends Entity> c)throws Exception {
		drop(conn, c);
		create(conn, c);
	}
	
	@Override
	protected void migrate(Connection conn, JoinTable jt) throws Exception {
		drop(conn, jt);
		create(conn, jt);
	}



	@Override
	protected void migrateForeignKey(Connection conn, StaticFieldProxy fkField) throws Exception {
	}



	@Override
	protected void dropForeignKey(Connection conn, StaticFieldProxy fkField)
			throws Exception {
	}



	private void create(Connection conn, Class<? extends Entity> c)throws Exception {
		Conf conf = ConnectorManager.getConf();
		Statement stmt = conn.createStatement();
		
		String createsql = conf.getQueryHelper().getCreateScript(c);
		stmt.execute(createsql);
	}
	
	private void create(Connection conn, JoinTable jt)throws Exception {
		Conf conf = ConnectorManager.getConf();
		Statement stmt = conn.createStatement();
		
		String createsql = conf.getQueryHelper().getCreateScript(jt);
		stmt.execute(createsql);
	}



	private void drop(Connection conn, Class<? extends Entity> c)throws Exception {
		Conf conf = ConnectorManager.getConf();
		Statement stmt = conn.createStatement();
		
		String dropsql = conf.getQueryHelper().getDropTableScriptScript(c);
		stmt.execute(dropsql);
	}
	
	private void drop(Connection conn, JoinTable jt)throws Exception {
		Conf conf = ConnectorManager.getConf();
		Statement stmt = conn.createStatement();
		
		String dropsql = conf.getQueryHelper().getDropTableScriptScript(jt);
		stmt.execute(dropsql);
	}

}
