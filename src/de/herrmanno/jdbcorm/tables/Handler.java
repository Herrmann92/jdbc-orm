package de.herrmanno.jdbcorm.tables;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.herrmanno.jdbcorm.ConnectorManager;
import de.herrmanno.jdbcorm.exceptions.EmptyResultSetException;
import de.herrmanno.jdbcorm.queryhelper.QueryHelper;


public class Handler {
	
	//public static boolean saveCascade = true;
	private static int defaultDepth = 1;
	
	public static void setDefaultLoadingDepth(int d) {
		defaultDepth = d;
	}
	
	public static int getDefaultLoadingDepth() {
		return defaultDepth;
	}

	public static <T extends Entity> T load(Class<T> clazz, long id) throws Exception {
		return Loader.load(clazz, "id="+id);
	}
	
	public static <T extends Entity> T load(Class<T> clazz, String where) throws Exception {
		return Loader.load(clazz, where, getDefaultLoadingDepth());
	}
	
	public static <T extends Entity> T load(Class<T> clazz, String where, int depth) throws Exception {
		return Loader.load(clazz, where, depth, new ArrayList<Entity>());
	}
	
	public static <T extends Entity> List<T> loadAll(Class<T> clazz) throws Exception {
		return Loader.loadAll(clazz, null);
	}
	
	public static <T extends Entity> List<T> loadAll(Class<T> clazz, String where) throws Exception {
		return Loader.loadAll(clazz, where, getDefaultLoadingDepth());
	}
	
	public static <T extends Entity> List<T> loadAll(Class<T> clazz, String where, int depth) throws Exception {
		return Loader.loadAll(clazz, where, depth, new ArrayList<Entity>());
	}

	public static void save(Entity e) throws Exception {
		Saver.save(e);
	} 
	
	
	public static void delete(Entity e) throws Exception {
		Deleter.delete(e);
	}
	
	public static void deleteAll(Class<? extends Entity> c) throws Exception {
		Deleter.deleteAll(c);
	}
	
	
	private static class Loader {
		static <T extends Entity> T load(Class<T> clazz, long id) throws Exception {
			return load(clazz, "id="+id);
		}
		
		static <T extends Entity> T load(Class<T> clazz, String where) throws Exception {
			return load(clazz, where, 2);
		}
		
		static <T extends Entity> T load(Class<T> clazz, String where, int depth) throws Exception {
			return load(clazz, where, depth, new ArrayList<Entity>());
		}
		
		static <T extends Entity> T load(Class<T> clazz, String where, int depth, List<Entity> loadedEntities) throws Exception {
			T instance = clazz.newInstance();
			String sql = "SELECT * FROM " + EntityHelper.getTableName(clazz) + " WHERE " + where;
			try(Connection conn = ConnectorManager.getConnection()) {
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				
				if(!rs.next())
					return instance;
				
				instance.beforeLoad();
				populateEntity(instance, rs);
				loadedEntities.add(instance);
				
				populateChildren(instance, depth, loadedEntities);
				
				/*
				if(depth > 0) {
					for(ObjectFieldProxy fp : EntityHelper.getFields(instance)) {
						//------- Single Reference
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
						
						//------- List Reference
						if(fp.getIsReference()) {
							fp.setValue(loadAll((Class<T>) fp.getReferenceClass(), fp.getReferenceFieldName() + "=" + instance.getId(), depth-1, loadedEntities));
						}
					}
				}
				*/
				
				
				instance.afterLoad();
			} 
			
			return instance;
		}
		
		static <T extends Entity> List<T> loadAll(Class<T> clazz) throws Exception {
			return loadAll(clazz, null);
		}
		
		static <T extends Entity> List<T> loadAll(Class<T> clazz, String where) throws Exception {
			return loadAll(clazz, where, 1);
		}
		
		static <T extends Entity> List<T> loadAll(Class<T> clazz, String where, int depth) throws Exception {
			return loadAll(clazz, where, depth, new ArrayList<Entity>());
		}
		
		static <T extends Entity> List<T> loadAll(Class<T> clazz, String where, int depth, List<Entity> loadedEntities) throws Exception {
			String sql = "SELECT * FROM " + EntityHelper.getTableName(clazz);
			if(where != null) {
				sql += " WHERE " + where + " ";
			}
			
			List<T> list = new ArrayList<T>();
			
			try(Connection conn = ConnectorManager.getConnection()) {
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				
				while(rs.next()) {
					T instance = clazz.newInstance();
					
					instance.beforeLoad();
					populateEntity(instance, rs);
					
					for(Entity e : loadedEntities) {
						if(e.getClass().equals(instance.getClass()) && e.getId() == instance.getId()) {
							instance = (T) e;
						}
					}
					
					instance.afterLoad();
					
					populateChildren(instance, depth, loadedEntities);
					
					list.add(instance);
					
					loadedEntities.add(instance);
					
				}
			}
				
			return list;
		}
		
		static void populateEntity(Entity e, ResultSet rs) throws Exception {
			
			
			List<ObjectFieldProxy> fields = EntityHelper.getFields(e);
			for(ObjectFieldProxy fp : fields) {
				//------- List Reference
				if(fp.getIsReference()) {
					fp.setValue(createEmptyCollection(fp.getReferenceClass()));
				//------- Single Reference or ForeignKey
				} else if(Entity.class.isAssignableFrom(fp.getType())) {
					if(rs.getObject(fp.name) != null) {
						Entity refE = (Entity) fp.getType().newInstance();
						refE.setId(rs.getLong(fp.name));
						fp.setValue(refE);
					}
				} else {
					fp.setValue(rs.getObject(fp.name));
				}
			}
			e.isNew = false;
		}

		static void populateChildren(Entity e, int depth, List<Entity> loadedEntities) throws Exception {
			if(depth > 0) {
				for(ObjectFieldProxy fp : EntityHelper.getFields(e)) {
					//------- Single Reference
					if(fp.getValue() != null && fp.getValue() instanceof Entity && ((Entity) fp.getValue()).getId() != -1) {
						
						//Look-up loadedEntities for seeked entity
						Entity loaded = null;
						for(Entity le : loadedEntities) {
							if(le.getClass().equals(fp.getType()) && le.getId() == ((Entity) fp.getValue()).getId()) {
								loaded = le;
								break;
							}
						}
						if(loaded != null) {
							fp.setValue(loaded);
						//------- Load seeked Entity, if not present in loadedEntities
						} else {
							fp.setValue(load(fp.getType(), "id="+((Entity) fp.getValue()).getId() , depth-1, loadedEntities));
						}
						
					}
					
					//------- List Reference
					if(fp.getIsReference()) {
						//------- Many-to-One Reference - e.g. Field User.Groups, where Group have one(!) User, e.g. owner
						if(!fp.getIsJoinReference()) {
							fp.setValue(loadAll( fp.getReferenceClass(), fp.getReferenceFieldName() + "=" + e.getId(), depth-1, loadedEntities));
						}
						//------- Many-to-Many Reference
						else {
							//TODO Join Table Loading !!
						}
					}
				}
			}
		}
		
		static private <T> Collection<T> createEmptyCollection(Class<T> clazz) {
			return new ArrayList<T>();
		}
	}
	
	
	private static class Saver {
		static void save(Entity e) throws Exception {
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
		
		static void _save(Entity e) throws Exception {
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
		
		static void _update(Entity e) throws Exception {
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
	}
	
	
	private static class Deleter {
		static void delete(Entity e) throws Exception {
			try(Connection conn = ConnectorManager.getConnection()) {
				QueryHelper qh = ConnectorManager.getConf().getQueryHelper();
				String deleteSQL = qh.getDeleteScript(e);
				
				Statement stmt = conn.createStatement();
				stmt.execute(deleteSQL);
				
			}
		}
		
		static void deleteAll(Class<? extends Entity> c) throws Exception {
			try(Connection conn = ConnectorManager.getConnection()) {
				QueryHelper qh = ConnectorManager.getConf().getQueryHelper();
				String deleteSQL = qh.getDeleteScript(c);
				
				Statement stmt = conn.createStatement();
				stmt.execute(deleteSQL);
				
			}
		}
	}
}
