package de.herrmanno.jdbcorm.typehelper;

import java.util.Date;

import de.herrmanno.jdbcorm.exceptions.UnsupportedFieldTypeException;
import de.herrmanno.jdbcorm.tables.Entity;
import de.herrmanno.jdbcorm.tables.EntityHelper;

public abstract class SQL_TypeHelper extends TypeHelper {

	@Override
	public String getSQLType(Class<?> type) throws UnsupportedFieldTypeException {
		if(type == int.class || type == Integer.class)
			return getIntType();
		if(type == long.class || type == Long.class)
			return getLongType();
		if(type == Date.class)
			return getDateType();
		else if(type == String.class)
			return getStringType();
		else if(Entity.class.isAssignableFrom(type))
			return getForeignKeyType();
		
		
		else
			throw new UnsupportedFieldTypeException();
	}

	protected abstract String getIntType();

	protected abstract String getLongType();

	protected abstract String getStringType();

	protected abstract String getDateType();
	
	protected String getForeignKeyType() {
		return getLongType();
	};

	@Override
	public String getSQLValue(Class<?> type, Object value) throws UnsupportedFieldTypeException {
		if(type == int.class || type == Integer.class)
			return getIntValue(value);
		if(type == long.class || type == Long.class)
			return getLongValue(value);
		if(type == Date.class)
			return getDateValue(value);
		else if(type == String.class)
			return getStringValue(value);
		else if(Entity.class.isAssignableFrom(type))
			return getForeignKeyValue(value);
		
		else
			throw new UnsupportedFieldTypeException();
	}

	protected abstract String getIntValue(Object value);

	protected abstract String getLongValue(Object value);

	protected abstract String getStringValue(Object value);

	protected abstract String getDateValue(Object value);
	
	protected String getForeignKeyValue(Object value) {
		if(value == null)
			return null;
		try {
			return EntityHelper.getPKFieldProxy((Entity) value).getValue().toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getJavaValue(Class<?> target, Object value) throws Exception {
		if(target.equals(Date.class)) {
			return (T) parseDate((String) value);
		} else {
			return (T) value;
		}
	}

	protected abstract Date parseDate(String value);

}
