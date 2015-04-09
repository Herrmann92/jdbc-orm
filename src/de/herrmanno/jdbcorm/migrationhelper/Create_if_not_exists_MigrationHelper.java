package de.herrmanno.jdbcorm.migrationhelper;

import java.sql.Connection;
import java.sql.Statement;

import de.herrmanno.jdbcorm.ConnectorManager;
import de.herrmanno.jdbcorm.conf.Conf;
import de.herrmanno.jdbcorm.tables.Entity;
import de.herrmanno.jdbcorm.tables.JoinTable;

public class Create_if_not_exists_MigrationHelper extends MigrationHelper {

	@Override
	protected void migrate(Connection conn, Class<? extends Entity> c) throws Exception {
		Conf conf = ConnectorManager.getConf();
		String sql = conf.getQueryHelper().getCreateScript(c);
		Statement stmt = conn.createStatement();
		stmt.execute(sql);
	}

	@Override
	protected void migrate(Connection conn, JoinTable jt) throws Exception {
		Conf conf = ConnectorManager.getConf();
		String sql = conf.getQueryHelper().getCreateScript(jt);
		Statement stmt = conn.createStatement();
		stmt.execute(sql);
		
	}

}
