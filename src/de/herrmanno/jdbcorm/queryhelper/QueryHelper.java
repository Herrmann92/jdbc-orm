package de.herrmanno.jdbcorm.queryhelper;

import de.herrmanno.jdbcorm.ConnectorManager;
import de.herrmanno.jdbcorm.constants.CascadeType;
import de.herrmanno.jdbcorm.exceptions.MultiplePrimaryKeysFoundException;
import de.herrmanno.jdbcorm.exceptions.NoPrimaryKeyFoundException;
import de.herrmanno.jdbcorm.tables.Entity;
import de.herrmanno.jdbcorm.typehelper.TypeHelper;

public abstract class QueryHelper {

	protected static TypeHelper getTypeHelper() {
		return ConnectorManager.getConf().getTypeHelper();
	}
	
	public abstract String getCreateScript(Class<? extends Entity> c) throws Exception;
	
	public abstract String getSaveScript(Entity e) throws Exception;
	
	public abstract String getUpdateScript(Entity e) throws Exception;
	
	public abstract String getDeleteScript(Entity e) throws Exception;
	
	public abstract String getDropTableScriptScript(Class<? extends Entity> c) throws Exception;
	
	public abstract String getDeleteScript(Class<? extends Entity> c) throws Exception;
	
	public abstract String getLastIdSelectScript(Class<? extends Entity> c) throws Exception;
	
	protected abstract String getAutoIncrementSQL() throws Exception;
	
	protected abstract String getPrimaryKeyInlineSQL() throws Exception;
	
	protected abstract String getForeignKeyInlineSQL(String fieldName, Class<? extends Entity> c, CascadeType onDelete, CascadeType onUpdate) throws Exception;

}
