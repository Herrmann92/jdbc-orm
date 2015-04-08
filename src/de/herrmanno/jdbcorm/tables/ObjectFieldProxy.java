package de.herrmanno.jdbcorm.tables;



public class ObjectFieldProxy extends StaticFieldProxy {
	
	protected Object object;
	
	Object value = null;

	public ObjectFieldProxy(Object object, java.lang.reflect.Field field) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		super(field);
		
		this.object = object;
		this.value = field.get(object);
	}
	
	public Object getValue() {
		return value;
	}
	
	void setValue(Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		this.field.set(this.object, value);
		this.value = value;
	}
	
}
