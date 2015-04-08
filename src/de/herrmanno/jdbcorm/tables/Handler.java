package de.herrmanno.jdbcorm.tables;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.herrmanno.jdbcorm.ConnectorManager;
import de.herrmanno.jdbcorm.exceptions.EmptyResultSetException;
import de.herrmanno.jdbcorm.queryhelper.QueryHelper;


public class Handler {
	
	public static boolean saveCascade = true;

	public static <T extends Entity> T load(Class<T> clazz, long id) throws Exception {
		return load(clazz, "id="+id);
	}
	
	public static <T extends Entity> T load(Class<T> clazz, String where) throws Exception {
		return load(clazz, where, 2);
	}
	
	public static <T extends Entity> T load(Class<T> clazz, String where, int depth) throws Exception {
		return load(clazz, where, depth, new ArrayList<Entity>());
	}
	
	private static <T extends Entity> T load(Class<T> clazz, String where, int depth, List<Entity> loadedEntities) throws Exception {
		T instance = clazz.newInstance();
		String sql = "SELECT * FROM " + EntityHelper.getTableName(clazz) + " WHERE " + where;
		try(Connection conn = ConnectorManager.getConnection()) {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			
			if(!rs.next())
				return instance;
			
			instance.beforeLoad();
			EntityHelper.populateEntity(instance, rs);
			loadedEntities.add(instance);
			
			if(depth > 0) {
				for(ObjectFieldProxy fp : EntityHelper.getFields(instance)) {
					if(fp.getValue() != null && fp.getValue() instanceof Entity && ((Entity) fp.getValue()).getId() != -1) {
						
						
						Entity loaded = null;
						for(Entity le : loadedEntities) {
							if(le.getClass().equals(fp.getType()) && le.getId() == ((Entity) fp.getValue()).getId()) {
								loaded = le;
								break;
							}
						}
						if(loaded != null) {
							fp.setValue(loaded);
						} else {
							fp.setValue(load(fp.getType(), "id="+((Entity) fp.getValue()).getId() , depth-1, loadedEntities));
						}
						 
						
						
					}
					
					if(fp.getIsReference()) {
						fp.setValue(loadAll((Class<T>) fp.getReferenceClass(), fp.getReferenceFieldName() + "=" + instance.getId(), depth-1, loadedEntities));
					}
				}
			}
			
			
			instance.afterLoad();
		} 
		
		return instance;
	}
	
	public static <T extends Entity> List<T> loadAll(Class<T> clazz) throws Exception {
		return loadAll(clazz, null);
	}
	
	public static <T extends Entity> List<T> loadAll(Class<T> clazz, String where) throws Exception {
		return loadAll(clazz, where, 1);
	}
	
	public static <T extends Entity> List<T> loadAll(Class<T> clazz, String where, int depth) throws Exception {
		return loadAll(clazz, where, depth, new ArrayList<Entity>());
	}
	
	private static <T extends Entity> List<T> loadAll(Class<T> clazz, String where, int depth, List<Entity> loadedEntites) throws Exception {
		String sql = "SELECT * FROM " + EntityHelper.getTableName(clazz);
		if(where != null)
			sql += " WHERE " + where + " ";
		
		try(Connection conn = ConnectorManager.getConnection()) {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			
				
			List<T> list = EntityHelper.createEntityList(clazz, rs);
			for(T e : list) {
				if(depth > 0) {
					for(ObjectFieldProxy fp : EntityHelper.getFields(e)) {
						if(fp.getValue() != null && fp.getValue() instanceof Entity && ((Entity) fp.getValue()).getId() != -1) {
							fp.setValue(load(fp.getType(), "id="+((Entity) fp.getValue()).getId() , depth-1));
						}
						
						if(fp.getIsReference()) {
							fp.setValue(loadAll((Class<T>) fp.getReferenceClass(), fp.getReferenceFieldName() + "=" + e.getId()));
						}
					}
				}
			}
			return list;
		}
	}

	public static void save(Entity e) throws Exception {
		e.beforeSave();
		e.isSaving = true;
		if(e.isNew) {
			_save(e);
			e.isNew = false;
		} else {
			_update(e);
		}
		e.isSaving = false;
		e.afterSave();
			
	} 
	
	private static void _save(Entity e) throws Exception {
		try(Connection conn = ConnectorManager.getConnection()) {
			
			conn.setAutoCommit(false);
			
			//------- Save references first to retrieve their Ids - qh.getSaveScript may need them!
			Statement stmt = conn.createStatement();
			for(Entity refE : EntityHelper.getSingleReferencedEntities(e)) {
				if(refE == null || refE.isSaving)
					continue;
				save(refE);
			}
			
			QueryHelper qh = ConnectorManager.getConf().getQueryHelper();
			String saveSQL = qh.getSaveScript(e);
			String getIdSQL = qh.getLastIdSelectScript(e.getClass());
			
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
	
	private static void _update(Entity e) throws Exception {
		try(Connection conn = ConnectorManager.getConnection()) {
			
			conn.setAutoCommit(false);
			
			for(Entity refE : EntityHelper.getSingleReferencedEntities(e)) {
				if(refE == null || refE.isSaving)
					continue;
				save(refE);
			}
			
			QueryHelper qh = ConnectorManager.getConf().getQueryHelper();
			String updateSQL = qh.getUpdateScript(e);
			
			
			Statement stmt = conn.createStatement();
			stmt.execute(updateSQL);
			
			conn.commit();
		}
	}
	
	public static void delete(Entity e) throws Exception {
		try(Connection conn = ConnectorManager.getConnection()) {
			QueryHelper qh = ConnectorManager.getConf().getQueryHelper();
			String deleteSQL = qh.getDeleteScript(e);
			
			Statement stmt = conn.createStatement();
			stmt.execute(deleteSQL);
			
		}
	}
	
	public static void deleteAll(Class<? extends Entity> c) throws Exception {
		try(Connection conn = ConnectorManager.getConnection()) {
			QueryHelper qh = ConnectorManager.getConf().getQueryHelper();
			String deleteSQL = qh.getDeleteScript(c);
			
			Statement stmt = conn.createStatement();
			stmt.execute(deleteSQL);
			
		}
	}
	
	
}
