package de.herrmanno.jdbcorm.conf;

import static de.herrmanno.jdbcorm.constants.MigrationType.CREATE_IF_NOT_EXISTS;
import de.herrmanno.jdbcorm.constants.MigrationType;
import de.herrmanno.jdbcorm.migrationhelper.Create_if_not_Exists_MigrationHelper2;
import de.herrmanno.jdbcorm.migrationhelper.Drop_and_Create_if_not_Exists_MigrationHelper2;
import de.herrmanno.jdbcorm.migrationhelper.MigrationHelper2;

public abstract class BaseConfig implements Conf {

	public abstract String getDriverClass();

	public abstract String getConnectionString();

	@Override
	public MigrationHelper2 getMigrationHelper() {
		switch (getMigrationType()) {
		case CREATE_IF_NOT_EXISTS:
			return new Create_if_not_Exists_MigrationHelper2();
		case DROP_AND_CREATE_IF_NOT_EXSITS:
			return new Drop_and_Create_if_not_Exists_MigrationHelper2();

		default:
			return new Create_if_not_Exists_MigrationHelper2();
		}
	}
	
	protected MigrationType getMigrationType() {
		return CREATE_IF_NOT_EXISTS;
	}

}
