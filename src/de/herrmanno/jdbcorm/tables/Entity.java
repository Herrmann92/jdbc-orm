package de.herrmanno.jdbcorm.tables;


public abstract class Entity {
	
	boolean isNew = true;
	
	@de.herrmanno.jdbcorm.annotations.Field("NOT NULL")
	@de.herrmanno.jdbcorm.annotations.Autoincrement
	@de.herrmanno.jdbcorm.annotations.PrimaryKey
	private long id;
	
	@de.herrmanno.jdbcorm.annotations.Field
	public long createdAt;
	
	@de.herrmanno.jdbcorm.annotations.Field
	public long updatedAt;
	
	public long getId() {
		return this.id;
	}
	/*
	public T load(long id) throws Exception {
		return super.load("id="+id);
	}
	
	
	public void save() throws Exception {
		
		try(Connection conn = ConnectorManager.getConnection()) {
			conn.setAutoCommit(false);
			QueryBuilder queryBuilder = ConnectorManager.getConf().getQueryBuilder();
			String saveSQL = queryBuilder.getSaveScript(this);
			String getIdSQL = queryBuilder.getLastIdSelectScript(this.getClass());
			
			Statement stmt = conn.createStatement();
			stmt.execute(saveSQL);
			ResultSet rs = stmt.executeQuery(getIdSQL);
			if(!rs.next()) {
				throw new EmptyResultSetException();
			} else {
				ObjectFieldProxy pkFieldProxy = EntityHelper.getPKFieldProxy(this);
				Object pkValue = rs.getObject(1);
				pkFieldProxy.setValue(pkValue);
			}
			
			conn.commit();
		}
	}
	
	@SuppressWarnings("unchecked")
	public T load(String where) throws Exception {
		String sql = "SELECT * FROM " + this.getClass().getSimpleName() + " WHERE " + where;
		try(Connection conn = ConnectorManager.getConnection()) {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			
			EntityHelper.populateEntity(this, rs);
			return (T) this;
		}
	}
	*/
	
	@Override
	public String toString() {
		try {
			return EntityHelper.toString(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "An error occured while invoking 'toString'";
	}
	

	/*
	private static Entity makeOneFromResultSet(Class<? extends Entity> clazz, ResultSet rs) throws EmptyResultSetException, InstantiationException, IllegalAccessException, IllegalArgumentException, NoSuchFieldException, SecurityException, SQLException {
		if(!rs.next())
			throw new EmptyResultSetException();
		
		Entity instance = clazz.newInstance();
		
		List<ObjectFieldProxy> fields = getFields(instance);
		for(ObjectFieldProxy fp : fields) {
			fp.setValue(rs.getObject(fp.name));
		}
		
		return instance;
	}
	*/

}
