package de.herrmanno.jdbcorm.tables;

import de.herrmanno.jdbcorm.annotations.Field;
import de.herrmanno.jdbcorm.annotations.ForeignKey;
import de.herrmanno.jdbcorm.annotations.References;
import de.herrmanno.jdbcorm.constants.CascadeType;
import de.herrmanno.jdbcorm.constants.Constraint;

public class StaticFieldProxy {

	protected java.lang.reflect.Field field;
	
	String name = null;
	Class<?> type;
	Constraint[] annotation = null;
	Boolean isPrimaryKey = false;
	Boolean isAutoIncrement = false;
	Boolean isNotNull = false;
	
	References rfAnn;

	ForeignKey fkAnn;
	

	
	
	public StaticFieldProxy(java.lang.reflect.Field field) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		this.field = field;
		
		if(!field.isAccessible())
			field.setAccessible(true);
		
		this.name = field.getName();
		
		this.type = field.getType();
		
		
		Field fieldAnn = field.getAnnotation(Field.class);
		this.annotation = fieldAnn != null ? fieldAnn.value() : null;
		
		
		for(Constraint c : annotation) {
			if(c == Constraint.PRIMARY_KEY) isPrimaryKey = true;
			if(c == Constraint.AUTO_INCREMENT) isAutoIncrement = true;
			if(c == Constraint.NOT_NULL) isNotNull = true;
		}
		
		this.fkAnn = field.getAnnotation(ForeignKey.class); 
		
		this.rfAnn = field.getAnnotation(References.class);
		
		
	}
	
	@SuppressWarnings("unchecked")
	public Class<? extends Entity> getDeclaringClass() {
		return (Class<? extends Entity>) field.getDeclaringClass();
	}


	@SuppressWarnings("rawtypes")
	public Class getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public Boolean getIsPrimaryKey() {
		return isPrimaryKey;
	}

	public Boolean getIsAutoIncrement() {
		return isAutoIncrement;
	}

	public boolean getIsNotNull() {
		return isNotNull;
	}


	public Boolean getIsForeignKey() {
		return fkAnn != null;
	}
	
	public CascadeType getOnDeleteType() {
		return fkAnn != null ? fkAnn.OnDelete() : null;
	}
	
	public CascadeType getOnUpdateType() {
		return fkAnn != null ? fkAnn.OnUpdate() : null;
	}
	
	public Boolean getIsReference() {
		return rfAnn != null;
	}
	
	public Class<? extends Entity> getReferenceClass() {
		return rfAnn != null ? rfAnn.Entity() : null;
	}
	
	public String getReferenceFieldName() {
		return rfAnn != null ? rfAnn.Field() : null;
	}
	
	public Boolean getIsJoinReference() throws Exception {
		if(rfAnn == null)
			return false;
		StaticFieldProxy field = EntityHelper.getFieldByName(getReferenceClass(), getReferenceFieldName());
		return field.getIsReference();
		
	}
	
	



}
