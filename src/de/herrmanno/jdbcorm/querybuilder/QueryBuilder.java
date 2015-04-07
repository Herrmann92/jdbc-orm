package de.herrmanno.jdbcorm.querybuilder;

import de.herrmanno.jdbcorm.tables.Entity;

public abstract class QueryBuilder {

	public abstract String getCreateScript(Class<? extends Entity> c) throws Exception;
	
	public abstract String getSaveScript(Entity e) throws Exception;
	
	public abstract String getDeleteScript(Entity e) throws Exception;
	
	public abstract String getDeleteScript(Class<? extends Entity> c) throws Exception;
	
	public abstract String getLastIdSelectScript(Class<? extends Entity> c) throws Exception;
	
	protected abstract String getAutoIncrementSQL();
	
	protected abstract String getPrimaryKeyInlineSQL();
}
