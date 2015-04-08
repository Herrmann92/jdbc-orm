package de.herrmanno.jdbcorm.queryhelper.mysql;

import de.herrmanno.jdbcorm.constants.CascadeType;
import de.herrmanno.jdbcorm.exceptions.MultiplePrimaryKeysFoundException;
import de.herrmanno.jdbcorm.exceptions.NoPrimaryKeyFoundException;
import de.herrmanno.jdbcorm.queryhelper.SQL_QueryHelper;
import de.herrmanno.jdbcorm.tables.Entity;
import de.herrmanno.jdbcorm.tables.EntityHelper;

public class MySql_QueryHelper extends SQL_QueryHelper {

	@Override
	protected String getAutoIncrementSQL() {
		return "AUTO_INCREMENT";
	}

	@Override
	protected String getPrimaryKeyInlineSQL() {
		return "PRIMARY KEY";
	}

	@Override
	protected String getForeignKeyInlineSQL(String fieldName, Class<? extends Entity> c, CascadeType onDelete, CascadeType onUpdate) throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoPrimaryKeyFoundException, MultiplePrimaryKeysFoundException {
		StringBuilder sb = new StringBuilder();
		sb.append("FOREIGN KEY ");
		sb.append("(");
		sb.append(fieldName);
		sb.append(") ");
		sb.append("REFERENCES ");
		sb.append(EntityHelper.getTableName(c));
		sb.append("(");
		sb.append(EntityHelper.getPKFieldProxy(c).getName());
		sb.append(")");
		sb.append(" ON DELETE ");
		sb.append(getCascadeString(onDelete));
		sb.append(" ON UPDATE ");
		sb.append(getCascadeString(onUpdate));
		
		return sb.toString();
	}
	
	private String getCascadeString(CascadeType ct) {
		switch (ct) {
		case RESTRICT:
			return "RESTRICT";
		case CASCADE:
			return "CASCADE";
		case NO_ACTION:
			return "NO ACTION";
		case SET_NULL:
			return "SET NULL";
		default:
			return null;
		}
	}

}
