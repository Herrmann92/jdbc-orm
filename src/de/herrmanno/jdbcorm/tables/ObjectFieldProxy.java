package de.herrmanno.jdbcorm.tables;

import de.herrmanno.jdbcorm.exceptions.UnsupportedFieldTypeException;


public class ObjectFieldProxy extends StaticFieldProxy {
	
	protected Object object;
	
	Object value = null;

	public ObjectFieldProxy(Object object, java.lang.reflect.Field field) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		super(field);
		
		this.object = object;
		this.value = field.get(object);
	}
	
	public Object getSQLValue() throws UnsupportedFieldTypeException  {
		if(type == int.class || type == Integer.class)
			return value;
		if(type == long.class || type == Long.class)
			return value;
		else if(type == String.class)
			return "'" + value + "'";
		
		else
			throw new UnsupportedFieldTypeException();
	}
	
	void setValue(Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.field.set(this.object, value);
	}
	
}
