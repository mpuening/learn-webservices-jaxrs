package io.github.learnjaxrs.ping;

import static com.jayway.jsonassert.JsonAssert.with;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.github.learnjaxrs.BaseTestCase;

/**
 * When running within Eclipse/IntelliJ, make sure to add VM args:
 *
 * --add-opens java.base/java.lang=ALL-UNNAMED
 * --add-opens=java.base/java.io=ALL-UNNAMED
 * --add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED
 *
 * In Eclipse, the JRE configuration had a field for default VM args, so you
 * need only to set it once and apply it for all test cases.
 */
@ExtendWith(ArquillianExtension.class)
public class PingServiceTest extends BaseTestCase {

	@Deployment
	public static WebArchive createTestDeployment() {
		return commonTestDeployment();
	}

	@Inject
	PingService pingService;

	@Inject
	@RestClient
	// See microprofile-config.properties for url
	protected PingTestClient pingTestClient;

	@Test
	public void testPingService() {
		assertNotNull(pingService);

		Response response = pingService.ping();
		assertPingResponse(response.getStatus(), response.readEntity(Object.class).toString());
	}

	@Test
	public void testPingServiceClient() {
		assertNotNull(pingTestClient);

		Response response = pingTestClient.ping();
		assertPingResponse(response.getStatus(), response.readEntity(String.class));
	}

	@Test
	public void testPingServiceUsingRestEasy() throws MalformedURLException {
		assertNotNull(baseURL);

		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(new URL(baseURL, "api/ping").toExternalForm());
		try (final Response response = target.request()
				.accept(MediaType.APPLICATION_JSON)
				.get()) {
			assertPingResponse(response.getStatus(), response.readEntity(String.class));
		}
	}

	protected void assertPingResponse(int status, String json) {
		assertEquals(200, status);
		with(json)
			.assertThat("$.data.greeting", is("HelloTest"))
			.assertThat("$.data.dataSource", is(true))
			.assertThat("$.data.entityManager", is(true));
	}

	@Test
	public void testMeService() {
		assertNotNull(pingService);

		Response response = pingService.me();
		assertMeResponse(response.getStatus(), response.readEntity(Object.class).toString(), "guest", false);
	}

	@Test
	public void testMeServiceClient() {
		assertNotNull(pingTestClient);

		Response response = pingTestClient.me();
		assertMeResponse(response.getStatus(), response.readEntity(String.class), "admin", true);
	}

	@Test
	public void testMeServiceUsingRestEasy() throws MalformedURLException {
		assertNotNull(baseURL);

		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(new URL(baseURL, "api/me").toExternalForm());
		String authorization = "Basic " + Base64.getEncoder().encodeToString("admin:password".getBytes());
		try (final Response response = target.request()
				.header("Authorization", authorization)
				.accept(MediaType.APPLICATION_JSON)
				.get()) {
			assertMeResponse(response.getStatus(), response.readEntity(String.class), "admin", true);
		}
	}

	protected void assertMeResponse(int status, String json, String username, boolean isAdmin) {
		assertEquals(200, status);
		with(json)
				.assertThat("$.data.username", is(username))
				.assertThat("$.data.admin", is(isAdmin));
	}
}
