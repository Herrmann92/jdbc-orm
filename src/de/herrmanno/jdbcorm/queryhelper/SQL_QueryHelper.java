package de.herrmanno.jdbcorm.queryhelper;

import java.util.ArrayList;
import java.util.List;

import de.herrmanno.jdbcorm.constants.CascadeType;
import de.herrmanno.jdbcorm.tables.Entity;
import de.herrmanno.jdbcorm.tables.EntityHelper;
import de.herrmanno.jdbcorm.tables.JoinTable;
import de.herrmanno.jdbcorm.tables.ObjectFieldProxy;
import de.herrmanno.jdbcorm.tables.StaticFieldProxy;

public abstract class SQL_QueryHelper extends QueryHelper {

	
	@Override
	public String getCreateScript(Class<? extends Entity> c) throws Exception {
		return create_if_not_exsist_script(c);
	}
	
	@Override
	public String getCreateScript(JoinTable jt) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append("`" + jt.getTableName() + "`");
		sb.append(" (");
		
		StaticFieldProxy[] fieldProxies = jt.getFieldProxies();
		for(int f = 0; f < fieldProxies.length; f++) {
			StaticFieldProxy fp = fieldProxies[f];
			sb.append(fp.getDeclaringClass().getSimpleName() + "_" + fp.getName() + " ");
			sb.append(getTypeHelper().getSQLType(fp.getDeclaringClass()) + " ");
			sb.append(", ");
		}
		for(int f = 0; f < fieldProxies.length; f++) {
			StaticFieldProxy fp = fieldProxies[f];
			sb.append(getForeignKeyInlineSQL(fp.getName(), fp.getReferenceClass(), CascadeType.CASCADE, CascadeType.CASCADE));
			if(f+1 < fieldProxies.length) {
				sb.append(", ");
			}
		}
		sb.append(" )");
		
		return sb.toString();
	}

	protected String create_if_not_exsist_script(Class<? extends Entity> c) throws Exception {
		List<String> fkScripts = new ArrayList<String>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append("`" + EntityHelper.getTableName(c) + "`");
		sb.append(" (");
		
		List<StaticFieldProxy> fields = EntityHelper.getFields(c);
		for(int f = 0; f < fields.size(); f++) {
			
			StaticFieldProxy fp = fields.get(f);
			
			//-------- leave out References
			if(fp.getIsReference()) {
				continue;
			}
			
			sb.append(fp.getName() + " ");
			sb.append(getTypeHelper().getSQLType(fp.getType()) + " ");
			sb.append(fp.getIsAutoIncrement() ? getAutoIncrementSQL()+" " : "");
			sb.append(fp.getIsNotNull() ? getNotNullSQL()+ " " : "");
			sb.append(fp.getIsPrimaryKey() ? getPrimaryKeyInlineSQL()+" " : "");
			if(f+1 < fields.size())
				sb.append(",");
			
			if(fp.getIsForeignKey()) {
				fkScripts.add(getForeignKeyInlineSQL(fp.getName(), fp.getType(), fp.getOnDeleteType(), fp.getOnUpdateType()));
			}
		}
		
		for(String fkScript : fkScripts) {
			sb.append("," + fkScript);
		}
		
		sb.append(") ");
		
		return sb.toString();
	}

	@Override
	public String getSaveScript(Entity e) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append("`" + EntityHelper.getTableName(e.getClass()) + "`");
		sb.append(" (");
		
		List<de.herrmanno.jdbcorm.tables.ObjectFieldProxy> fields = EntityHelper.getFields(e);
		for(int f = 0; f < fields.size(); f++) {
			ObjectFieldProxy fp = fields.get(f);
			if(fp.getIsAutoIncrement() || fp.getIsReference()) {
				continue;
			}
			sb.append(fp.getName());
			if(f+1 < fields.size())
				sb.append(",");
		}
		
		sb.append(")");
		
		sb.append(" VALUES (");
		
		for(int f = 0; f < fields.size(); f++) {
			ObjectFieldProxy fp = fields.get(f);
			if(fp.getIsAutoIncrement() || fp.getIsReference()) {
				continue;
			}
			sb.append(getTypeHelper().getSQLValue(fp.getType(), fp.getValue())); //fields.get(f).getSQLValue());
			if(f+1 < fields.size())
				sb.append(",");
		}
		
		sb.append(");");
		
		return sb.toString();
	}
	
	@Override
	public String getUpdateScript(Entity e) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append("`" + EntityHelper.getTableName(e.getClass()) + "` ");
		sb.append("SET ");
		
		List<de.herrmanno.jdbcorm.tables.ObjectFieldProxy> fields = EntityHelper.getFields(e);
		for(int f = 0; f < fields.size(); f++) {
			ObjectFieldProxy fp = fields.get(f);
			
			if(fp.getIsAutoIncrement() || fp.getIsReference()) {
				continue;
			}
			sb.append(fp.getName());
			sb.append("=");
			sb.append(getTypeHelper().getSQLValue(fp.getType(), fp.getValue()));
			if(f+1 < fields.size())
				sb.append(",");
		}
		
		
		ObjectFieldProxy pkFp = EntityHelper.getPKFieldProxy(e);
		sb.append(" WHERE ");
		sb.append(pkFp.getName());
		sb.append("=");
		sb.append(getTypeHelper().getSQLValue(pkFp.getType(), pkFp.getValue()));
		
		return sb.toString();
	}
	
	@Override
	public String getDeleteScript(Entity e) throws Exception {
		StringBuilder sb = new StringBuilder();
		ObjectFieldProxy pkFp = EntityHelper.getPKFieldProxy(e);
		sb.append(pkFp.getName());
		sb.append("=");
		sb.append(getTypeHelper().getSQLValue(pkFp.getType(), pkFp.getValue()));
		
		return getDeleteScript(e.getClass(), sb.toString());
	}
	
	@Override
	public String getDeleteScript(Class<? extends Entity> c) throws Exception {
		return getDeleteScript(c, null);
	}
	
	private String getDeleteScript(Class<? extends Entity> c, String where) {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ");
		sb.append(EntityHelper.getTableName(c) + " ");
		if(where != null) {
			sb.append("WHERE ");
			sb.append(where);
		}
		
		return sb.toString();
	}
	
	@Override
	public String getDropTableScriptScript(Class<? extends Entity> c) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("DROP TABLE IF EXISTS ");
		sb.append(EntityHelper.getTableName(c) + " ");
		
		return sb.toString();
	}
	
	@Override
	public String getDropTableScriptScript(JoinTable jt) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("DROP TABLE IF EXISTS ");
		sb.append(jt.getTableName() + " ");
		
		return sb.toString();
	}
	
	@Override
	public String getLastIdSelectScript(Class<? extends Entity> c) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(EntityHelper.getPKFieldProxy(c).getName() + " ");
		sb.append("FROM ");
		sb.append(EntityHelper.getTableName(c) + " ");
		sb.append("ORDER BY ");
		sb.append(EntityHelper.getPKFieldProxy(c).getName() + " ");
		sb.append("DESC ");
		sb.append("LIMIT 1 ");
		
		return sb.toString();
	}


	protected String getNotNullSQL() {
		return "NOT NULL";
	}

	

}
