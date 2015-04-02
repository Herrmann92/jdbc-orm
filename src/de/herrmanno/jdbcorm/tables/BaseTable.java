package de.herrmanno.jdbcorm.tables;


public abstract class BaseTable extends Table {

	@de.herrmanno.jdbcorm.annotations.Field("NOT NULL")
	@de.herrmanno.jdbcorm.annotations.Autoincrement
	@de.herrmanno.jdbcorm.annotations.PrimaryKey
	private long id;
	
	@de.herrmanno.jdbcorm.annotations.Field
	public long createdAt;
	
	@de.herrmanno.jdbcorm.annotations.Field
	public long updatedAt;
	
}
