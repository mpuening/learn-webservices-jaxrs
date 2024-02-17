package io.github.learnjaxrs.config;

import javax.sql.DataSource;

import io.github.learnjaxrs.util.env.ConfigurableEnvironment;
import io.github.learnjaxrs.util.env.Environment;
import io.github.learnjaxrs.util.env.ExpressionEvaluator;
import io.github.learnjaxrs.util.env.MPExpressionEvaluator;
import io.github.learnjaxrs.util.flyway.FlywayMigration;
import io.github.learnjaxrs.util.sql.AppDataSource;
import jakarta.annotation.Resource;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;

/**
 * Use environment profiles / micro-profile config properties to connect to database.
 */
@DataSourceDefinition(
		name = "java:app/env/jdbc/appDataSource",
		className = "io.github.learnjaxrs.util.sql.MPConfigurableDataSource",
		url = "ENV(db.url)",
		user = "ENV(db.user)/ENV(db.password)",
		password = "ENV(db.password)",
		properties = {
				"driverClassName=ENV(db.driver)"
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
		// But it is quite convenient for local development
		ExpressionEvaluator evaluator = new MPExpressionEvaluator();
		Environment environment = new ConfigurableEnvironment(evaluator);
		if ("true".equalsIgnoreCase(environment.getProperty("FlyWay", "flyway.migration.enabled", "false"))) {
			FlywayMigration.run(dataSource, DataSourceConfiguration.class.getClassLoader());			
		}
		else {
			FlywayMigration.setFlywayMigrationSkipped();
		}
	}
}