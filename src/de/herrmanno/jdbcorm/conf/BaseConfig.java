package de.herrmanno.jdbcorm.conf;

import de.herrmanno.jdbcorm.constants.MigrationType;
import static de.herrmanno.jdbcorm.constants.MigrationType.*;
import de.herrmanno.jdbcorm.migrationhelper.Create_if_not_exists_MigrationHelper;
import de.herrmanno.jdbcorm.migrationhelper.Drop_and_Create_if_not_Exists_MigrationHelper;
import de.herrmanno.jdbcorm.migrationhelper.MigrationHelper;

public abstract class BaseConfig implements Conf {

	public abstract String getDriverClass();

	public abstract String getConnectionString();

	@Override
	public MigrationHelper getMigrationHelper() {
		switch (getMigrationType()) {
		case CREATE_IF_NOT_EXISTS:
			return new Create_if_not_exists_MigrationHelper();
		case DROP_AND_CREATE_IF_NOT_EXSITS:
			return new Drop_and_Create_if_not_Exists_MigrationHelper();

		default:
			return new Create_if_not_exists_MigrationHelper();
		}
	}
	
	protected MigrationType getMigrationType() {
		return CREATE_IF_NOT_EXISTS;
	}

}
