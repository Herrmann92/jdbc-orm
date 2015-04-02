package de.herrmanno.jdbcorm.tables;

import de.herrmanno.jdbcorm.annotations.Autoincrement;
import de.herrmanno.jdbcorm.annotations.Field;
import de.herrmanno.jdbcorm.annotations.PrimaryKey;

public class FieldProxy {
	
	String name = null;
	Class<?> type;
	Object value = null;
	String annotation = null;
	Boolean isPrimaryKey = false;
	Boolean isAutoIncrement = false;

	public FieldProxy(Object object, java.lang.reflect.Field field) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		if(!field.isAccessible())
			field.setAccessible(true);
		
		this.name = field.getName();
		
		this.type = field.getType();
		
		this.value = field.get(object);
		
		Field fieldAnn = field.getAnnotation(Field.class);
		this.annotation = fieldAnn != null ? fieldAnn.value() : null;
		
		Autoincrement aiAnn = field.getAnnotation(de.herrmanno.jdbcorm.annotations.Autoincrement.class);
		this.isAutoIncrement = aiAnn != null ? true : false;
		
		PrimaryKey pkAnn = field.getAnnotation(de.herrmanno.jdbcorm.annotations.PrimaryKey.class);
		this.isPrimaryKey = pkAnn != null ? true : false;
		
	}
	
	public String getSQLType() throws Exception {
		if(type == int.class || type == Integer.class)
			return "INT(11)";
		if(type == long.class || type == Long.class)
			return "INT(11)";
		else if(type == String.class)
			return "VARCHAR(256)";
		
		else
			throw new Exception("Invalid Type"); //TODO Custom exception
	}

	public Object getSQLValue() throws Exception {
		if(type == int.class || type == Integer.class)
			return value;
		if(type == long.class || type == Long.class)
			return value;
		else if(type == String.class)
			return "'" + value + "'";
		
		else
			throw new Exception("Invalid Type"); //TODO Custom exception
	}

}
