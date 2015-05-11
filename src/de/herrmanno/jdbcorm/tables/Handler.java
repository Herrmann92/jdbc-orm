package de.herrmanno.jdbcorm.tables;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.herrmanno.jdbcorm.JDBCORM;
import de.herrmanno.jdbcorm.exceptions.EmptyResultSetException;
import de.herrmanno.jdbcorm.exceptions.NoConfigDefinedException;
import de.herrmanno.jdbcorm.queryhelper.QueryHelper;


public class Handler {
	
	public static boolean saveCascade = true;
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
			try(Connection conn = JDBCORM.getConnection()) {
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
			
			try(Connection conn = JDBCORM.getConnection()) {
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
							
							fp.setValue(ChildList.create(loadAll( fp.getReferenceClass(), fp.getReferenceFieldName() + "=" + e.getId() , depth-1, loadedEntities)));
						}
						//------- Many-to-Many Reference
						else {
							//------- Retrieve Ids from Join Table
							List<Long> ids = loadIds(new JoinTable(fp), fp);
							String where = ids.size() > 0 ? "id IN (" + Arrays.toString(ids.toArray()).replace("[", "").replace("]",  "") + ")" : "1=2";
							//------- Load referenced Entities by retrieved IDs
							fp.setValue(ChildList.create(loadAll( fp.getReferenceClass(), where, depth-1, loadedEntities)));
						}
					}
				}
			}
		}
		
		static List<Long> loadIds(JoinTable jt, ObjectFieldProxy fp) throws ClassNotFoundException, SQLException, NoConfigDefinedException {
			String sql = "SELECT * FROM " + jt.getTableName() + " WHERE ";
			sql += jt.getColumnName(jt.getOtherFp(fp)) + "=" + fp.getEntityID();
			
			List<Long> ids = new ArrayList<Long>();
			
			try(Connection conn = JDBCORM.getConnection()) {
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				
				while(rs.next()) {
					ids.add(rs.getLong(jt.getColumnName(jt.fp2)));
				}
			}
			
			return ids;
		}
		
		static private <T> Collection<T> createEmptyCollection(Class<T> clazz) {
			return new ArrayList<T>();
		}
	}
	
	
	private static class Saver {
		
		static void save(Entity e) throws Exception {
			try(Connection conn = JDBCORM.getConnection()) {
				Savepoint savePoint = conn.setSavepoint();
				conn.setAutoCommit(false);
				try {
					save(conn, e);
					
					conn.commit();
				} catch(Exception e1) {
					e1.printStackTrace();
					conn.rollback(savePoint);
				}
			}
		}
		
		private static void save(Connection conn, Entity e) throws Exception {
				e.beforeSave();
				e.isSaving = true;
				if(e.isNew) {
					_save(conn, e);
					e.isNew = false;
				} else {
					_update(conn, e);
				}
				e.isSaving = false;
				e.afterSave();
		} 
		
		private static void _save(Connection conn, Entity e) throws Exception {
				
			Statement stmt = conn.createStatement();
			
			QueryHelper qh = JDBCORM.getConf().getQueryHelper();
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
			
			saveChildren(conn, e);
				
		}

		private static void _update(Connection conn, Entity e) throws Exception {
								
			QueryHelper qh = JDBCORM.getConf().getQueryHelper();
			String updateSQL = qh.getUpdateScript(e);
			
			Statement stmt = conn.createStatement();
			stmt.execute(updateSQL);
			
			saveChildren(conn, e);
			
		}

		private static void saveChildren(Connection conn, Entity e) throws Exception {
			
			//------- Save references first to retrieve their Ids - qh.getSaveScript may need them!
			for(Entity refE : EntityHelper.getSingleReferencedEntities(e)) {
				if(refE == null || refE.isSaving)
					continue;
				save(conn, refE);
			}
			
			for(ObjectFieldProxy refFp : EntityHelper.getListReferencedFields(e)) {
				
				ChildList<Entity> refEs = (ChildList<Entity>) refFp.getValue();
				
				String referenceFieldName = refFp.getReferenceFieldName();
				//------- One-to-Many Reference
				if(!refFp.getIsJoinReference()) {
					
					//------- Set Parent Entity as FieldValue for Referenced Entity 
					for(Entity addedE : refEs.getAdded()) {
						EntityHelper.getFieldByName(addedE, referenceFieldName).setValue(e);
						save(conn, addedE);
					}
					
					//------- Set Referenced Entity referenced FieldValue to null 
					for(Entity addedE : refEs.getRemoved()) {
						EntityHelper.getFieldByName(addedE, referenceFieldName).setValue(null);
						save(conn, addedE);
					}
				}
				
				//------- Many-to-Many Reference
				else {
					
					JoinTable jt = new JoinTable(refFp);
					//------- Set Parent Entity as FieldValue for Referenced Entity 
					for(Entity addedE : refEs.getAdded()) {
						save(conn, addedE);
						
						ObjectFieldProxy reffedFp = EntityHelper.getFieldByName(addedE, refFp.getReferenceFieldName());
						_insert(conn, jt, refFp, reffedFp);
						
					}
					
					//------- Set Referenced Entity referenced FieldValue to null 
					for(Entity addedE : refEs.getRemoved()) {
						save(conn, addedE);
						
						ObjectFieldProxy reffedFp = EntityHelper.getFieldByName(addedE, refFp.getReferenceFieldName());
						_remove(conn, jt, refFp, reffedFp);
					}
				}
				
				/*
				//------- Save all referenced Entities
				for(Entity refE : refEs) {
					if(!refE.isSaving)
						save(conn, refE);
				}
				*/
				
			}
		}

		private static void _insert(Connection conn, JoinTable jt, ObjectFieldProxy refFp, ObjectFieldProxy reffedFp) throws Exception {
			Statement stmt = conn.createStatement();
			QueryHelper qh = JDBCORM.getConf().getQueryHelper();
			String sql = qh.getSaveScript(jt, refFp, reffedFp);
			stmt.execute(sql );
			
		}

		private static void _remove(Connection conn, JoinTable jt, ObjectFieldProxy refFp, ObjectFieldProxy reffedFp) throws Exception {
			Statement stmt = conn.createStatement();
			QueryHelper qh = JDBCORM.getConf().getQueryHelper();
			String sql = qh.getDeleteScript(jt, refFp, reffedFp);
			stmt.execute(sql );
			
		}
	}
	
	
	private static class Deleter {
		static void delete(Entity e) throws Exception {
			try(Connection conn = JDBCORM.getConnection()) {
				QueryHelper qh = JDBCORM.getConf().getQueryHelper();
				String deleteSQL = qh.getDeleteScript(e);
				
				Statement stmt = conn.createStatement();
				stmt.execute(deleteSQL);
				
			}
		}
		
		static void deleteAll(Class<? extends Entity> c) throws Exception {
			try(Connection conn = JDBCORM.getConnection()) {
				QueryHelper qh = JDBCORM.getConf().getQueryHelper();
				String deleteSQL = qh.getDeleteScript(c);
				
				Statement stmt = conn.createStatement();
				stmt.execute(deleteSQL);
				
			}
		}
	}
}
