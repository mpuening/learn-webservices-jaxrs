package io.github.learnjaxrs.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Startup;

import io.github.learnjaxrs.util.env.Environment;
import jakarta.enterprise.context.ApplicationScoped;

@Startup
@ApplicationScoped
public class StartupCheck implements HealthCheck {

	@Override
	public HealthCheckResponse call() {
		String name = "JAX-RS API Startup Check";
		// If you got here, you're up
		return HealthCheckResponse
				.named(name)
				.withData("activeProfiles", Environment.getActiveProfiles())
				.up()
				.build();
	}
}
