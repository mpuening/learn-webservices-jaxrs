package io.github.learnjaxrs.config;

import javax.sql.DataSource;

import jakarta.annotation.Resource;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;

import io.github.learnjaxrs.util.flyway.FlywayMigration;
import io.github.learnjaxrs.util.sql.AppDataSource;

/**
 * Use micro-profile config properties to connect to database.
 */
@DataSourceDefinition(
		name = "java:app/env/jdbc/appDataSource",
		className = "io.github.learnjaxrs.util.sql.MPConfiguredDataSource",
		url = "db.url",
		user = "db.user/db.password",
		password = "db.password",
		properties = {
				"driverClassName=db.driver"
		})
@ApplicationScoped
public class DataSourceConfiguration {

	@Resource(lookup = "java:app/env/jdbc/appDataSource")
	DataSource dataSource;

	@Produces
	@AppDataSource
	public DataSource getDatasource() {
		return dataSource;
	}

	/**
	 * QUESTIONABLE Should use only for local development
	 */
	public void onStart(@Observes @Initialized(ApplicationScoped.class) Object unused) {
		// It's not good practice to have an app be responsible to run migrations.
		// But it is quite convenient.
		FlywayMigration.run(dataSource, DataSourceConfiguration.class.getClassLoader());
	}
}