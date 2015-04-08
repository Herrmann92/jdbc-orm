package de.herrmanno.jdbcorm.migrationhelper;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import de.herrmanno.jdbcorm.ConnectorManager;
import de.herrmanno.jdbcorm.conf.Conf;
import de.herrmanno.jdbcorm.tables.Entity;
import de.herrmanno.jdbcorm.tables.EntityHelper;
import de.herrmanno.jdbcorm.tables.StaticFieldProxy;

public class Drop_and_Create_if_not_Exists_MigrationHelper extends MigrationHelper {

	@Override
	public void migrate(String _package) throws Exception {
		Queue<Class<? extends Entity>> classesDrop = new ArrayDeque<Class<? extends Entity>>();
		Queue<Class<? extends Entity>> classesCreate = new ArrayDeque<Class<? extends Entity>>();

		for(Class<?> c : getClasses(_package)) {
			classesDrop.add((Class<? extends Entity>) c);
			classesCreate.add((Class<? extends Entity>) c);
		}
		
		List<Class<?>> migratedClasses = new ArrayList<Class<?>>();
		List<Class<?>> droppedClasses = new ArrayList<Class<?>>();
		
		try(Connection conn = ConnectorManager.getConnection()) {
			try {
				
				conn.setAutoCommit(false);
				
				//------- Drop first in reversed order
				while(classesDrop.peek() != null) {
					
					Class<? extends Entity> c = classesDrop.poll();
					
					
					//------- Check if there are ForeinKeyField, which aren't dropped yet
					boolean doLater = false;
					for(Class<? extends Entity> otherClass : classesDrop) {
						for(StaticFieldProxy refFp : EntityHelper.getSingleReferencedFields(otherClass)) {
							if(refFp.getType() == c)
								doLater = true;
						}
					}
					
					if(doLater) {
						classesDrop.add(c);
						continue;
					}
					
					
					//TODO create stbl from references here and insert into Queue
					drop(conn, c);
					droppedClasses.add(c);
				}
				
				//------- Then Migrate
				while(classesCreate.peek() != null) {
				
					Class<? extends Entity> c = classesCreate.poll();
					
					//------- Check if there are ForeinKeyField, which Classes aren't migrated yet
					boolean doLater = false;
					for(StaticFieldProxy refFp : EntityHelper.getSingleReferencedFields(c)) {
						if(!migratedClasses.contains(refFp.getType()))
							doLater = true;
					}
					
					if(doLater) {
						classesCreate.add(c);
						continue;
					}
					
					//TODO create stbl from references here and insert into Queue
					create(conn, c);
					migratedClasses.add(c);
				}
				
				conn.commit();
			} catch(Exception e) {
				conn.rollback();
			}
		}
	}
	
	
	
	@Override
	protected void migrate(Connection conn, Class<? extends Entity> c)throws Exception {
		drop(conn, c);
		create(conn, c);
	}
	
	private void drop(Connection conn, Class<? extends Entity> c)throws Exception {
		Conf conf = ConnectorManager.getConf();
		Statement stmt = conn.createStatement();
		
		String dropsql = conf.getQueryHelper().getDropTableScriptScript(c);
		stmt.execute(dropsql);
	}
	
	private void create(Connection conn, Class<? extends Entity> c)throws Exception {
		Conf conf = ConnectorManager.getConf();
		Statement stmt = conn.createStatement();
		
		String createsql = conf.getQueryHelper().getCreateScript(c);
		stmt.execute(createsql);
	}

}
