package de.herrmanno.jdbcorm.querybuilder;

import java.util.List;

import de.herrmanno.jdbcorm.ConnectorManager;
import de.herrmanno.jdbcorm.exceptions.MigrationTypeNotSupportedException;
import de.herrmanno.jdbcorm.exceptions.UnsupportedFieldTypeException;
import de.herrmanno.jdbcorm.tables.Entity;
import de.herrmanno.jdbcorm.tables.EntityHelper;
import de.herrmanno.jdbcorm.tables.StaticFieldProxy;

public abstract class SQL_QueryBuilder extends QueryBuilder {

	@Override
	public String getCreateScript(Class<? extends Entity> c) throws IllegalArgumentException, IllegalAccessException, InstantiationException, UnsupportedFieldTypeException, MigrationTypeNotSupportedException {
		switch (ConnectorManager.getConf().getMigrationType()) {
		case CREATE_IF_NOT_EXISTS:
			return create_if_not_exsist_script(c);
		default:
			throw new MigrationTypeNotSupportedException();
		}
	}

	protected String create_if_not_exsist_script(Class<? extends Entity> c) throws UnsupportedFieldTypeException, IllegalArgumentException, IllegalAccessException, InstantiationException {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append("`" + c.getSimpleName() + "`");
		sb.append(" (");
		
		List<StaticFieldProxy> fields = EntityHelper.getFields(c);
		for(int f = 0; f < fields.size(); f++) {
			sb.append(fields.get(f).getName() + " ");
			sb.append(fields.get(f).getSQLType() + " ");
			sb.append(fields.get(f).getIsAutoIncrement() ? getAutoIncrementSQL()+" " : "");
			sb.append(fields.get(f).getAnnotation() + " ");
			sb.append(fields.get(f).getIsPrimaryKey() ? getPrimaryKeyInlineSQL()+" " : "");
			if(f+1 < fields.size())
				sb.append(",");
		}
		
		sb.append(") ");
		
		return sb.toString();
	}
	
	@Override
	public String getSaveScript(Entity e) throws IllegalArgumentException, IllegalAccessException, InstantiationException, UnsupportedFieldTypeException {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append("`" + e.getClass().getSimpleName() + "`");
		sb.append(" (");
		
		List<de.herrmanno.jdbcorm.tables.ObjectFieldProxy> fields = EntityHelper.getFields(e);
		for(int f = 0; f < fields.size(); f++) {
			if(fields.get(f).getIsAutoIncrement()) {
				continue;
			}
			sb.append(fields.get(f).getName());
			if(f+1 < fields.size())
				sb.append(",");
		}
		
		sb.append(")");
		
		sb.append(" VALUES (");
		
		for(int f = 0; f < fields.size(); f++) {
			if(fields.get(f).getIsAutoIncrement()) {
				continue;
			}
			sb.append(fields.get(f).getSQLValue());
			if(f+1 < fields.size())
				sb.append(",");
		}
		
		sb.append(");");
		
		return sb.toString();
	}
	
	@Override
	public String getDeleteScript(Entity e) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(EntityHelper.getPKFieldProxy(e).getName());
		sb.append("=");
		sb.append(EntityHelper.getPKFieldProxy(e).getSQLValue());
		
		return getDeleteScript(e.getClass(), sb.toString());
	}
	
	@Override
	public String getDeleteScript(Class<? extends Entity> c) throws Exception {
		return getDeleteScript(c, null);
	}
	
	private String getDeleteScript(Class<? extends Entity> c, String where) {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ");
		sb.append(c.getSimpleName() + " ");
		if(where != null) {
			sb.append("WHERE ");
			sb.append(where);
		}
		
		return sb.toString();
	}
	
	@Override
	public String getLastIdSelectScript(Class<? extends Entity> c) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(EntityHelper.getPKFieldProxy(c).getName() + " ");
		sb.append("FROM ");
		sb.append(c.getSimpleName() + " ");
		sb.append("ORDER BY ");
		sb.append(EntityHelper.getPKFieldProxy(c).getName() + " ");
		sb.append("DESC ");
		sb.append("LIMIT 1 ");
		
		return sb.toString();
	}

	protected abstract String getAutoIncrementSQL();
	

	protected abstract String getPrimaryKeyInlineSQL();

	

}
