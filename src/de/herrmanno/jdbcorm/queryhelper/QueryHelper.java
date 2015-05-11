package de.herrmanno.jdbcorm.queryhelper;

import de.herrmanno.jdbcorm.JDBCORM;
import de.herrmanno.jdbcorm.constants.CascadeType;
import de.herrmanno.jdbcorm.tables.Entity;
import de.herrmanno.jdbcorm.tables.JoinTable;
import de.herrmanno.jdbcorm.tables.ObjectFieldProxy;
import de.herrmanno.jdbcorm.typehelper.TypeHelper;

public abstract class QueryHelper {

	protected static TypeHelper getTypeHelper() {
		return JDBCORM.getConf().getTypeHelper();
	}
	
	public abstract String getCreateScript(Class<? extends Entity> c) throws Exception;
	
	public abstract String getCreateScript(JoinTable jt) throws Exception;
	
	public abstract String getSaveScript(Entity e) throws Exception;
	
	public abstract String getSaveScript(JoinTable jt, ObjectFieldProxy fp1, ObjectFieldProxy fp2) throws Exception;
	
	public abstract String getUpdateScript(Entity e) throws Exception;
	
	public abstract String getDeleteScript(Entity e) throws Exception;
	
	public abstract String getDeleteScript(JoinTable jt, ObjectFieldProxy fp1, ObjectFieldProxy fp2) throws Exception;
	
	public abstract String getDropTableScriptScript(Class<? extends Entity> c) throws Exception;
	
	public abstract String getDropTableScriptScript(JoinTable jt) throws Exception;
	
	public abstract String getDeleteScript(Class<? extends Entity> c) throws Exception;
	
	public abstract String getLastIdSelectScript(Class<? extends Entity> c) throws Exception;
	
	public abstract String getCreateForeignKeySQL(Class<? extends Entity> c, String fieldName, Class<? extends Entity> refC, CascadeType onDelete, CascadeType onUpdate) throws Exception;

	public abstract String getDropForeignKeySQL(Class<? extends Entity> c, String fieldName, Class<? extends Entity> refC, CascadeType onDelete, CascadeType onUpdate) throws Exception;

	protected abstract String getAutoIncrementSQL() throws Exception;
	
	protected abstract String getPrimaryKeyInlineSQL() throws Exception;
	
	protected abstract String getForeignKeySQL(String fieldName, Class<? extends Entity> c, CascadeType onDelete, CascadeType onUpdate) throws Exception;

}
