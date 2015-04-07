package de.herrmanno.jdbcorm.tables;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import de.herrmanno.jdbcorm.ConnectorManager;
import de.herrmanno.jdbcorm.exceptions.EmptyResultSetException;
import de.herrmanno.jdbcorm.querybuilder.QueryBuilder;


public class Handler {

	public static <T extends Entity> T load(Class<T> clazz, long id) throws Exception {
		return load(clazz, "id="+id);
	}
	
	public static <T extends Entity> T load(Class<T> clazz, String where) throws Exception {
		T instance = clazz.newInstance();
		String sql = "SELECT * FROM " + clazz.getSimpleName() + " WHERE " + where;
		try(Connection conn = ConnectorManager.getConnection()) {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			
			EntityHelper.populateEntity(instance, rs);
		}
		return instance;
	}
	
	public static <T extends Entity> List<T> loadAll(Class<T> clazz) throws Exception {
		return loadAll(clazz, null);
	}
	
	public static <T extends Entity> List<T> loadAll(Class<T> clazz, String where) throws Exception {
		String sql = "SELECT * FROM " + clazz.getSimpleName();
		if(where != null)
			sql += " WHERE " + where + " ";
		
		try(Connection conn = ConnectorManager.getConnection()) {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			
				
			return EntityHelper.createEntityList(clazz, rs);
		}
	}

	public static void save(Entity e) throws Exception {
		try(Connection conn = ConnectorManager.getConnection()) {
			conn.setAutoCommit(false);
			QueryBuilder queryBuilder = ConnectorManager.getConf().getQueryBuilder();
			String saveSQL = queryBuilder.getSaveScript(e);
			String getIdSQL = queryBuilder.getLastIdSelectScript(e.getClass());
			
			Statement stmt = conn.createStatement();
			stmt.execute(saveSQL);
			ResultSet rs = stmt.executeQuery(getIdSQL);
			if(!rs.next()) {
				throw new EmptyResultSetException();
			} else {
				ObjectFieldProxy pkFieldProxy = EntityHelper.getPKFieldProxy(e);
				Object pkValue = rs.getObject(1);
				pkFieldProxy.setValue(pkValue);
			}
			
			conn.commit();
			
		}
	}
	
	public static void delete(Entity e) throws Exception {
		try(Connection conn = ConnectorManager.getConnection()) {
			QueryBuilder queryBuilder = ConnectorManager.getConf().getQueryBuilder();
			String deleteSQL = queryBuilder.getDeleteScript(e);
			
			Statement stmt = conn.createStatement();
			stmt.execute(deleteSQL);
			
		}
	}
	
	public static void deleteAll(Class<? extends Entity> c) throws Exception {
		try(Connection conn = ConnectorManager.getConnection()) {
			QueryBuilder queryBuilder = ConnectorManager.getConf().getQueryBuilder();
			String deleteSQL = queryBuilder.getDeleteScript(c);
			
			Statement stmt = conn.createStatement();
			stmt.execute(deleteSQL);
			
		}
	}
	
	
}
