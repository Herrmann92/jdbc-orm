package de.herrmanno.jdbcorm.tables;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import de.herrmanno.jdbcorm.ConnectorManager;

public abstract class BaseTable {

	@de.herrmanno.jdbcorm.annotations.Field("NOT NULL")
	@de.herrmanno.jdbcorm.annotations.Autoincrement
	@de.herrmanno.jdbcorm.annotations.PrimaryKey
	private long id;
	
	@de.herrmanno.jdbcorm.annotations.Field
	public long createdAt;
	
	@de.herrmanno.jdbcorm.annotations.Field
	public long updatedAt;
	
	
	public void save() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append("`" + getClass().getSimpleName() + "`");
		sb.append(" (");
		
		List<de.herrmanno.jdbcorm.tables.FieldProxy> fields = getFields();
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
		Statement stmt = ConnectorManager.getConnection().createStatement();
		stmt.execute(sql);
	}
	
	public void migrate() throws Exception {
		
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append("`" + getClass().getSimpleName() + "`");
		sb.append(" (");
		
		List<FieldProxy> fields = getFields();
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
	
	private List<FieldProxy> getFields() throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		return getFields(getClass());
	}
	
	private List<FieldProxy> getFields(Class clazz) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		List<FieldProxy> fields = new ArrayList<FieldProxy>();
		
		for(Field field : clazz.getDeclaredFields()) {
			de.herrmanno.jdbcorm.annotations.Field annotation = field.getAnnotation(de.herrmanno.jdbcorm.annotations.Field.class);
			if(annotation == null)
				continue;
			
			fields.add(new FieldProxy(this, field));
		}
		
		Class superClass = clazz.getSuperclass();
		if(superClass != null)
			fields.addAll(getFields(superClass));
		
		return fields;
	}
	
}
