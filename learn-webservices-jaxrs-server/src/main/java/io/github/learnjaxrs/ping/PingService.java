package io.github.learnjaxrs.ping;

import java.util.Map;

import javax.sql.DataSource;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.security.enterprise.SecurityContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import io.github.learnjaxrs.util.api.Data;
import io.github.learnjaxrs.util.sql.AppDataSource;

@Path("api")
@Named
@ApplicationScoped
public class PingService {

	@Inject
	SecurityContext securityContext;

	@Inject
	@AppDataSource
	DataSource dataSource;

	@PersistenceContext
	EntityManager entityManager;

	@Inject
	@ConfigProperty(name = "app.greeting", defaultValue = "UNKNOWN")
	String greeting;

	@GET
	@Path("ping")
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponseSchema(responseCode = "200", responseDescription = "Ping Response", value = PingResponse.class)
	public Response ping() {
		Data<Map<String, Object>> data = Data.from(Map.of(
				"greeting", greeting,
				"dataSource", dataSource != null,
				"entityManager", entityManager != null
		));
		return Response.status(Status.OK).entity(data).build();
	}

	@Schema
	public static class PingResponse {
		@Schema(properties = {
				@SchemaProperty(name = "greeting", type = SchemaType.STRING, description = "Hello message"),
				@SchemaProperty(name = "dataSource", type = SchemaType.BOOLEAN, description = "Is data source available?"),
				@SchemaProperty(name = "entityManager", type = SchemaType.BOOLEAN, description = "Is entity manager available?")
		})
		Object data;
	}

	@RolesAllowed({ "user", "admin" })
	@GET
	@Path("me")
	@Produces(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "basicAuth")
	@SecurityRequirement(name = "oauth2")
	@APIResponseSchema(responseCode = "200", responseDescription = "Me Response", value = MeResponse.class)
	public Response me() {
		Data<Map<String, Object>> data = Data.from(Map.of(
				"username", securityContext.getCallerPrincipal().getName(),
				"admin", securityContext.isCallerInRole("admin")
		));
		return Response.status(Status.OK).entity(data).build();
	}

	@Schema
	public static class MeResponse {
		@Schema(properties = {
				@SchemaProperty(name = "username", type = SchemaType.STRING, description = "Name of user"),
				@SchemaProperty(name = "admin", type = SchemaType.BOOLEAN, description = "Is user admin?")
		})
		Object data;
	}
}