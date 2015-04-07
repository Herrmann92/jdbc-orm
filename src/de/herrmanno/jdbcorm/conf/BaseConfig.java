package de.herrmanno.jdbcorm.conf;

import de.herrmanno.jdbcorm.constants.MigrationType;

public abstract class BaseConfig implements Conf {

	public abstract String getDriverClass();

	public abstract String getConnectionString();

	@Override
	public MigrationType getMigrationType() {
		return MigrationType.CREATE_IF_NOT_EXISTS;
	}

}
