package de.herrmanno.jdbcorm.tables;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import de.herrmanno.jdbcorm.ConnectorManager;

public abstract class Table<T extends Table> {
	
	public void save() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append("`" + getClass().getSimpleName() + "`");
		sb.append(" (");
		
		List<de.herrmanno.jdbcorm.tables.ObjectFieldProxy> fields = getObjectFields();
		for(int f = 0; f < fields.size(); f++) {
			if(fields.get(f).isAutoIncrement) {
				continue;
			}
			sb.append(fields.get(f).name);
			if(f+1 < fields.size())
				sb.append(",");
		}
		
		sb.append(")");
		
		sb.append(" VALUES (");
		
		for(int f = 0; f < fields.size(); f++) {
			if(fields.get(f).isAutoIncrement) {
				continue;
			}
			sb.append(fields.get(f).getSQLValue());
			if(f+1 < fields.size())
				sb.append(",");
		}
		
		sb.append(");");
		
		
		String sql = sb.toString();
		try(Connection conn = ConnectorManager.getConnection()) {
			Statement stmt = conn.createStatement();
			stmt.execute(sql);
		}
	}
	
	//TODO make generic
	public static Table get(Class<? extends Table> clazz, Object key) throws Exception {
		StaticFieldProxy pkField = getStaticPKFieldProxy(clazz);
		String sql = "SELECT * FROM " + clazz.getSimpleName() + " WHERE " + pkField.name + " = " + key;
		try(Connection conn = ConnectorManager.getConnection()) {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			return makeOneFromResultSet(clazz, rs);
		}
	}
	
	private static StaticFieldProxy getStaticPKFieldProxy(Class<? extends Table> clazz) throws Exception {
		List<StaticFieldProxy> fields = getStaticFields(clazz);
		for(StaticFieldProxy fp : fields) {
			if(fp.isPrimaryKey)
				return fp;
		}
		throw new Exception("No PK field found"); //TODO Custom Exception
	}

	public static void migrate(Class<? extends Table> clazz) throws Exception {
		
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append("`" + clazz.getSimpleName() + "`");
		sb.append(" (");
		
		List<StaticFieldProxy> fields = getStaticFields(clazz);
		for(int f = 0; f < fields.size(); f++) {
			sb.append(fields.get(f).name + " ");
			sb.append(fields.get(f).getSQLType() + " ");
			sb.append(fields.get(f).isAutoIncrement ? "AUTO_INCREMENT " : "");
			sb.append(fields.get(f).annotation + " ");
			sb.append(fields.get(f).isPrimaryKey ? "primary key " : "");
			if(f+1 < fields.size())
				sb.append(",");
		}
		
		sb.append(") ");
		
		
		
		String sql = sb.toString();
		Statement stmt = ConnectorManager.getConnection().createStatement();
		stmt.execute(sql);
	}

	private List<ObjectFieldProxy> getObjectFields() throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		return getObjectFields(getClass());
	}

	private List<ObjectFieldProxy> getObjectFields(Class<? extends Table> clazz) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		List<ObjectFieldProxy> fields = new ArrayList<ObjectFieldProxy>();
		
		for(Field field : clazz.getDeclaredFields()) {
			de.herrmanno.jdbcorm.annotations.Field annotation = field.getAnnotation(de.herrmanno.jdbcorm.annotations.Field.class);
			if(annotation == null)
				continue;
			
			fields.add(new ObjectFieldProxy(this, field));
		}
		
		Class<? extends Table> superClass = (Class<? extends Table>) clazz.getSuperclass();
		if(superClass != null)
			fields.addAll(getObjectFields(superClass));
		
		return fields;
	}
	
	/*
	private static List<StaticFieldProxy> getStaticFields()  {
		return getStaticFields(getClass());
	}
	*/

	private static List<StaticFieldProxy> getStaticFields(Class<? extends Table> clazz) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
	List<StaticFieldProxy> fields = new ArrayList<StaticFieldProxy>();
	
	for(Field field : clazz.getDeclaredFields()) {
		de.herrmanno.jdbcorm.annotations.Field annotation = field.getAnnotation(de.herrmanno.jdbcorm.annotations.Field.class);
		if(annotation == null)
			continue;
		
		fields.add(new StaticFieldProxy(field));
	}
	
	Class<? extends Table> superClass = (Class<? extends Table>) clazz.getSuperclass();
	if(superClass != null)
		fields.addAll(getStaticFields(superClass));
	
	return fields;
	}
	
	private static Table makeOneFromResultSet(Class<? extends Table> clazz, ResultSet rs) throws Exception {
		if(!rs.next())
			throw new Exception("Query Error - ResultSet Empty"); //TODO Custom Exception
		
		Table instance = clazz.newInstance();
		
		List<ObjectFieldProxy> fields = instance.getObjectFields();
		for(ObjectFieldProxy fp : fields) {
			fp.setValue(rs.getObject(fp.name));
		}
		
		return instance;
	}

}
