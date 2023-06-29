package io.github.learnjaxrs.health;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import io.github.learnjaxrs.util.sql.AppDataSource;

@Liveness
@ApplicationScoped
public class DatabaseCheck implements HealthCheck {

	@Inject
	@AppDataSource
	DataSource dataSource;

	@PersistenceContext
	EntityManager entityManager;

	@Override
	public HealthCheckResponse call() {
		String name = "JAX-RS API Database Check";
		
		boolean dataSourceFound = dataSource != null;
		boolean entityManagerFound = entityManager != null;
		boolean isUp = false;

		if (dataSource != null) {
			String validationSql = "SELECT 1 FROM SYSIBM.SYSDUMMY1";
			try (Connection connection = dataSource.getConnection();
					PreparedStatement statement = connection.prepareStatement(validationSql);
					ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					int value = resultSet.getInt(1);
					isUp = value == 1;
				}
			} catch (SQLException ex) {
				// status will be DOWN
				ex.printStackTrace();
			}
		}

		return HealthCheckResponse
				.named(name)
				.withData("dataSource", dataSourceFound)
				.withData("entityManager", entityManagerFound)
				.status(isUp)
				.build();
	}
}
