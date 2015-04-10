package de.herrmanno.jdbcorm.migrationhelper;

import java.sql.Connection;
import java.sql.Statement;

import de.herrmanno.jdbcorm.ConnectorManager;
import de.herrmanno.jdbcorm.conf.Conf;
import de.herrmanno.jdbcorm.constants.CascadeType;
import de.herrmanno.jdbcorm.tables.Entity;
import de.herrmanno.jdbcorm.tables.JoinTable;

public class Drop_and_Create_if_not_Exists_MigrationHelper2 extends Create_if_not_Exists_MigrationHelper2 {


	@Override
	protected void dropTable(Connection conn, Class<? extends Entity> c) throws Exception {
		Conf conf = ConnectorManager.getConf();
		Statement stmt = conn.createStatement();
		
		String dropsql = conf.getQueryHelper().getDropTableScriptScript(c);
		stmt.execute(dropsql);
	}

	@Override
	protected void dropTable(Connection conn, JoinTable jt) throws Exception {
		Conf conf = ConnectorManager.getConf();
		Statement stmt = conn.createStatement();
		
		String dropsql = conf.getQueryHelper().getDropTableScriptScript(jt);
		stmt.execute(dropsql);
	}

	@Override
	protected void dropForeignKey(Connection conn, Class<? extends Entity> c, String fieldName, Class<? extends Entity> refC, CascadeType onDeleteType, CascadeType onUpdateType) throws Exception {
		Conf conf = ConnectorManager.getConf();
		String sql = conf.getQueryHelper().getDropForeignKeySQL(c, fieldName, refC, onDeleteType, onUpdateType);
		Statement stmt = conn.createStatement();
		stmt.execute(sql);
	}


}
