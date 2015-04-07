package de.herrmanno.jdbcorm.tables;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.herrmanno.jdbcorm.exceptions.EmptyResultSetException;
import de.herrmanno.jdbcorm.exceptions.MultiplePrimaryKeysFoundException;
import de.herrmanno.jdbcorm.exceptions.NoPrimaryKeyFoundException;

public class EntityHelper {

	/*
	 * Static-based Methods
	 */
	
	public static List<StaticFieldProxy> getFields(Class<? extends Entity> clazz) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		List<StaticFieldProxy> fields = new ArrayList<StaticFieldProxy>();
		
		for(Field field : clazz.getDeclaredFields()) {
			de.herrmanno.jdbcorm.annotations.Field annotation = field.getAnnotation(de.herrmanno.jdbcorm.annotations.Field.class);
			if(annotation == null)
				continue;
			
			fields.add(new StaticFieldProxy(field));
		}
		
		Class<? extends Entity> superClass = (Class<? extends Entity>) clazz.getSuperclass();
		if(superClass != null)
			fields.addAll(getFields(superClass));
		
		return fields;
	}
	
	public static StaticFieldProxy getPKFieldProxy(Class<? extends Entity> clazz) throws NoPrimaryKeyFoundException, IllegalArgumentException, IllegalAccessException, InstantiationException, MultiplePrimaryKeysFoundException {
		StaticFieldProxy pkProxy = null;
		
		List<StaticFieldProxy> fields = getFields(clazz);
		for(StaticFieldProxy fp : fields) {
			if(fp.isPrimaryKey) {
				if(pkProxy == null)
					pkProxy = fp;
				else
					throw new MultiplePrimaryKeysFoundException();
					
			}
		}
		if(pkProxy != null)
			return pkProxy;
		else
			throw new NoPrimaryKeyFoundException();
	}
	
	static void populateEntity(Entity e, ResultSet rs) throws EmptyResultSetException, IllegalArgumentException, IllegalAccessException, InstantiationException, NoSuchFieldException, SecurityException, SQLException {
		if(!rs.next())
			throw new EmptyResultSetException();
		
		
		List<ObjectFieldProxy> fields = getFields(e);
		for(ObjectFieldProxy fp : fields) {
			fp.setValue(rs.getObject(fp.name));
		}
	}
	
	static public <T extends Entity> List<T> createEntityList(Class<T> clazz, ResultSet rs) throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException, NoSuchFieldException, SecurityException {
		List<T> list = new ArrayList<T>();
		while(rs.next()) {
			T instance = clazz.newInstance();
			List<ObjectFieldProxy> fields = getFields(instance);
			for(ObjectFieldProxy fp : fields) {
				fp.setValue(rs.getObject(fp.name));
			}
			list.add(instance);
		}
		return list;
	}
	
	/*
	 * Object-based Methods
	 */
	
	public static List<ObjectFieldProxy> getFields(Entity entity) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		return getFields(entity.getClass(), entity);
	}
	
	public static ObjectFieldProxy getPKFieldProxy(Entity entity) throws NoPrimaryKeyFoundException, IllegalArgumentException, IllegalAccessException, InstantiationException {
		List<ObjectFieldProxy> fields = getFields(entity);
		for(ObjectFieldProxy fp : fields) {
			if(fp.isPrimaryKey)
				return fp;
		}
		throw new NoPrimaryKeyFoundException();
	}
	
	public static String toString(Entity e) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		List<ObjectFieldProxy> fields = getFields(e);
		StringBuilder sb = new StringBuilder();
		for(ObjectFieldProxy fp : fields) {
			sb.append(fp.name);
			sb.append(" - ");
			sb.append(fp.value != null ? fp.value : "NULL");
			sb.append("\t [");
			sb.append(fp.isPrimaryKey ? "PK " : "");
			sb.append(fp.isAutoIncrement ? "AI " : "");
			sb.append(fp.annotation != null ? fp.annotation : "");
			sb.append("]");
			sb.append("\n");
		}
		return sb.toString();
	}
	
	/*
	 * Object-based Helper-Methods
	 */
	
	private static List<ObjectFieldProxy> getFields(Class<? extends Entity> clazz, Entity entity) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		List<ObjectFieldProxy> fields = new ArrayList<ObjectFieldProxy>();
		
		for(Field field : clazz.getDeclaredFields()) {
			de.herrmanno.jdbcorm.annotations.Field annotation = field.getAnnotation(de.herrmanno.jdbcorm.annotations.Field.class);
			if(annotation == null)
				continue;
			
			fields.add(new ObjectFieldProxy(entity, field));
		}
		
		Class<? extends Entity> superClass = (Class<? extends Entity>) clazz.getSuperclass();
		if(superClass != null)
			fields.addAll(getFields(superClass, entity));
		
		return fields;
	}
	
	
}
