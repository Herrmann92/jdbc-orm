package de.herrmanno.jdbcorm.tables;

import de.herrmanno.jdbcorm.ConnectorManager;



public class ObjectFieldProxy extends StaticFieldProxy {
	
	protected Entity object;
	
	Object value = null;

	public ObjectFieldProxy(Entity object, java.lang.reflect.Field field) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		super(field);
		
		this.object = object;
		this.value = field.get(object);
	}
	
	public Object getValue() {
		return value;
	}
	
	public Object getEntityID() {
		return object.getId();
	}
	
	void setValue(Object value) throws Exception {
		value = ConnectorManager.getConf().getTypeHelper().getJavaValue(type, value);
		this.field.set(this.object, value);
		this.value = value;
	}
	
	@Override
	public String toString() {
		return super.toString() + " Value: " + getValue();
	}
	
}
