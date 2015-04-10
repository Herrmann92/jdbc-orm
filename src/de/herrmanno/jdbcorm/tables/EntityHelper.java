package de.herrmanno.jdbcorm.tables;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import de.herrmanno.jdbcorm.exceptions.MultiplePrimaryKeysFoundException;
import de.herrmanno.jdbcorm.exceptions.NoPrimaryKeyFoundException;

public class EntityHelper {

	/*
	 * Static-based Methods
	 */
	
	public static String getTableName(Class<? extends Entity> clazz) {
		return clazz.getSimpleName();
	}
	
	public static List<StaticFieldProxy> getFields(Class<? extends Entity> clazz) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		List<StaticFieldProxy> fields = new ArrayList<StaticFieldProxy>();
		
		for(Field field : clazz.getDeclaredFields()) {
			de.herrmanno.jdbcorm.annotations.Field annotation = field.getAnnotation(de.herrmanno.jdbcorm.annotations.Field.class);
			if(annotation == null)
				continue;
			
			fields.add(new StaticFieldProxy(field));
		}
		
		@SuppressWarnings("unchecked")
		Class<? extends Entity> superClass = (Class<? extends Entity>) clazz.getSuperclass();
		if(superClass != null)
			fields.addAll(getFields(superClass));
		
		return fields;
	}
	
	public static StaticFieldProxy getFieldByName(Class<? extends Entity> clazz, String name) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		List<StaticFieldProxy> fields = getFields(clazz);
		
		return fields.stream().filter((fp) -> fp.name.equals(name)).findFirst().get();
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
	
	/*
	static void populateEntity(Entity e, ResultSet rs) throws Exception {
		
		
		List<ObjectFieldProxy> fields = getFields(e);
		for(ObjectFieldProxy fp : fields) {
			//------- List Reference
			if(fp.getIsReference()) {
				fp.setValue(createEmptyCollection(fp.getReferenceClass()));
			//------- Single Reference or ForeignKey
			} else if(Entity.class.isAssignableFrom(fp.getType())) {
				if(rs.getObject(fp.name) != null) {
					Entity refE = (Entity) fp.getType().newInstance();
					refE.setId(rs.getLong(fp.name));
					fp.setValue(refE);
				}
			} else {
				fp.setValue(rs.getObject(fp.name));
			}
		}
		e.isNew = false;
	}
	*/
	
	/*
	static private <T> Collection<T> createEmptyCollection(Class<T> clazz) {
		return new ArrayList<T>();
	}
	*/
	
	/*
	static public <T extends Entity> List<T> createEntityList(Class<T> clazz, ResultSet rs) throws Exception {
		List<T> list = new ArrayList<T>();
		
		while(rs.next()) {
			T instance = clazz.newInstance();
			
			instance.beforeLoad();
			
			
			EntityHelper.populateEntity(instance, rs);
			
			list.add(instance);
			
			instance.afterLoad();
		}
		return list;
	}
	*/
	
	public static List<StaticFieldProxy> getForeignKeyFields(Class<? extends Entity> clazz) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		return getFields(clazz).stream().filter((fp) -> fp.getIsForeignKey()).collect(Collectors.toList());
	}
	
	/*
	 * Object-based Methods
	 */
	
	public static List<ObjectFieldProxy> getFields(Entity entity) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		return getFields(entity.getClass(), entity);
	}
	
	public static ObjectFieldProxy getFieldByName(Entity e, String name) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		List<ObjectFieldProxy> fields = getFields(e);
		
		return fields.stream().filter((fp) -> fp.name.equals(name)).findFirst().get();
	}
	
	public static ObjectFieldProxy getPKFieldProxy(Entity entity) throws NoPrimaryKeyFoundException, IllegalArgumentException, IllegalAccessException, InstantiationException {
		List<ObjectFieldProxy> fields = getFields(entity);
		for(ObjectFieldProxy fp : fields) {
			if(fp.isPrimaryKey)
				return fp;
		}
		throw new NoPrimaryKeyFoundException();
	}
	
	public static List<Entity> getSingleReferencedEntities(Entity e) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		List<Entity> fields = getFields(e)
				.stream()
				.filter((fp) -> Entity.class.isAssignableFrom(fp.getType()))
				.map((fp) -> (Entity)fp.getValue())
				.collect(Collectors.toList());
		
		return fields;
	}
	
	public static List<ObjectFieldProxy> getListReferencedFields(Entity e) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		List<ObjectFieldProxy> fields = getFields(e)
				.stream()
				.filter((fp) -> fp.getIsReference())
				.collect(Collectors.toList());
		
		return fields;
	}
	
	/*
	public static String toString(Entity e) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		List<ObjectFieldProxy> fields = getFields(e);
		StringBuilder sb = new StringBuilder();
		for(ObjectFieldProxy fp : fields) {
			sb.append(fp.name);
			sb.append(" - ");
			sb.append(fp.value != null ? fp.value : "NULL");
			sb.append("\t [");
			for(int c = 0; c < fp.annotation.length; c++) {
				sb.append(fp.annotation[c].toString());
				if(c + 1 < fp.annotation.length)
					sb.append(", ");
			}
			sb.append("]");
			sb.append("\n");
		}
		return sb.toString();
	}
	*/
	
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
		
		@SuppressWarnings("unchecked")
		Class<? extends Entity> superClass = (Class<? extends Entity>) clazz.getSuperclass();
		if(superClass != null)
			fields.addAll(getFields(superClass, entity));
		
		return fields;
	}
	
	
}
