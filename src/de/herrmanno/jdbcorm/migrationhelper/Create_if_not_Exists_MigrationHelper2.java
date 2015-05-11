package de.herrmanno.jdbcorm.migrationhelper;

import java.sql.Connection;
import java.sql.Statement;

import de.herrmanno.jdbcorm.JDBCORM;
import de.herrmanno.jdbcorm.conf.Conf;
import de.herrmanno.jdbcorm.constants.CascadeType;
import de.herrmanno.jdbcorm.tables.Entity;
import de.herrmanno.jdbcorm.tables.JoinTable;

public class Create_if_not_Exists_MigrationHelper2 extends MigrationHelper2 {

	@Override
	protected void beforeMigration(Connection conn) {
	}

	@Override
	protected void afterMigration(Connection conn) {
	}

	@Override
	protected void createDatabase(Connection conn) {
	}

	@Override
	protected void dropDatabase(Connection conn) {
	}

	@Override
	protected void validateTable(Connection conn, Class<? extends Entity> c) {
	}

	@Override
	protected void createTable(Connection conn, Class<? extends Entity> c) throws Exception {
		Conf conf = JDBCORM.getConf();
		Statement stmt = conn.createStatement();
		
		String createsql = conf.getQueryHelper().getCreateScript(c);
		stmt.execute(createsql);
	}

	@Override
	protected void createTable(Connection conn, JoinTable jt) throws Exception {
		Conf conf = JDBCORM.getConf();
		Statement stmt = conn.createStatement();
		
		String createsql = conf.getQueryHelper().getCreateScript(jt);
		stmt.execute(createsql);
	}

	@Override
	protected void dropTable(Connection conn, Class<? extends Entity> c) throws Exception {
	}

	@Override
	protected void dropTable(Connection conn, JoinTable jt) throws Exception {
	}

	@Override
	protected void createForeignKey(Connection conn, Class<? extends Entity> c, String fieldName, Class<? extends Entity> refC, CascadeType onDeleteType, CascadeType onUpdateType) throws Exception {
		Conf conf = JDBCORM.getConf();
		String sql = conf.getQueryHelper().getCreateForeignKeySQL(c, fieldName, refC, onDeleteType, onUpdateType);
		Statement stmt = conn.createStatement();
		stmt.execute(sql);
	}

	
	@Override
	protected void dropForeignKey(Connection conn, Class<? extends Entity> c, String fieldName, Class<? extends Entity> refC, CascadeType onDeleteType, CascadeType onUpdateType) throws Exception {
		
	}
	

}
