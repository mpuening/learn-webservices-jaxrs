package io.github.learnjaxrs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.learnjaxrs.model.MeResponse;
import io.github.learnjaxrs.model.ProblemDetail;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
public class ApplicationTests {

	@Autowired
	protected WebClient.Builder webClientBuilder;

	@Test
	public void testRestApiModels() {
		WebClient webClient = webClientBuilder.build();
		Mono<String> result = webClient
			.get()
			.uri("http://localhost:8080/learn-webservices-jaxrs-server/api/me")
			.headers(headers -> {
				headers.setBasicAuth("admin", "password");
			})
			.accept(MediaType.APPLICATION_JSON)
			.exchangeToMono(response -> {
				if (response.statusCode().is2xxSuccessful()) {
					return response.bodyToMono(MeResponse.class).flatMap(me -> {
						return Mono.just(me.getData().getUsername());
					});
				}
				else {
					return response.bodyToMono(ProblemDetail.class).flatMap(problem -> {
						return Mono.just(problem.getDetail());
					});
				}
			});

		StepVerifier.create(result)
			// Behavior when no server is running
			.expectError();

			// If the server is running, you should get a 200 OK response
			//.expectNext("admin")

			// If you leave off the Authorization header, this is the 401 response
			//.expectNext("You must provide valid credentials to access this resource.")

			//.verifyComplete();
	}
}
