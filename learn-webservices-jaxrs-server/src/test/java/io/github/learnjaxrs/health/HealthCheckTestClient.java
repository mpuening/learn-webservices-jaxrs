package io.github.learnjaxrs.health;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface HealthCheckTestClient {

	@GET
	@Path("/health/started")
	Response healthStarted();

	@GET
	@Path("/health/ready")
	Response healthReady();

	@GET
	@Path("/health/live")
	Response healthLive();
}
