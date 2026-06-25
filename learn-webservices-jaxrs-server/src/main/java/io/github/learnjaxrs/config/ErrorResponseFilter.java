package io.github.learnjaxrs.config;

import java.io.IOException;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;

import io.github.learnjaxrs.util.api.ProblemDetail;
import io.github.learnjaxrs.util.security.auth.AuthProvider;

/**
 * App servers are inconsistent with responses from HttpAuthenticationMechanisms
 * (Looking at you TomEE). What should be a WebApplicationExceptionMapper is
 * this filter instead.
 *
 * QUESTIONABLE Is this the best way to handle this?
 */
@Provider
public class ErrorResponseFilter implements ContainerResponseFilter {

	private static final String FORBIDDEN_MESSAGE = "You are not authorized to access this resource.";
	private static final String UNAUTHORIZED_MESSAGE = "You must provide valid credentials to access this resource.";
	private static final String NOT_FOUND_ERROR_MESSAGE = "Uh oh, 404 Not Found.";
	private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Ugh. The server encountered an error from which it could not recover.";
	private static final String NO_MESSAGE = "No message.";

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		int status = responseContext.getStatus();
		if ("401".equals(requestContext.getProperty(AuthProvider.AUTH_VALIDATION_RESULT))) {
			respondWith(requestContext, responseContext, Response.Status.UNAUTHORIZED, UNAUTHORIZED_MESSAGE);
		} else if (status == 401) {
			respondWith(requestContext, responseContext, Response.Status.UNAUTHORIZED, UNAUTHORIZED_MESSAGE);
		} else if (status == 403 && requestContext.getHeaderString(HttpHeaders.AUTHORIZATION) == null) {
			respondWith(requestContext, responseContext, Response.Status.UNAUTHORIZED, UNAUTHORIZED_MESSAGE);
		} else if (status == 403) {
			respondWith(requestContext, responseContext, Response.Status.FORBIDDEN, FORBIDDEN_MESSAGE);
		} else if (status == 404) {
			respondWith(requestContext, responseContext, Response.Status.NOT_FOUND, NOT_FOUND_ERROR_MESSAGE);
		} else if (status == 500) {
			respondWith(requestContext, responseContext, Response.Status.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
		} else {
			Object currentEntity = responseContext.getEntity();
			if (currentEntity != null) {
				// Try to preserve message. Not sure if this is correct.
				responseContext.setEntity(
						currentEntity,
						responseContext.getEntityAnnotations(),
						MediaType.APPLICATION_JSON_TYPE);
			} else {
				respondWith(requestContext, responseContext, Response.Status.fromStatusCode(status), NO_MESSAGE);
			}
		}
	}

	private void respondWith(ContainerRequestContext requestContext, ContainerResponseContext responseContext, Status status, String detail) {
		ProblemDetail pd = ProblemDetail.fromStatusAndDetail(status, detail);
		responseContext.setStatus(status.getStatusCode());
		responseContext.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, "application/json");
		if (Boolean.TRUE.equals(requestContext.getProperty(AuthProvider.REQUEST_USER_AUTHORIZATION))) {
			responseContext.getHeaders().add("WWW-Authenticate", "Basic");
		}
		responseContext.setEntity(pd.toJson());
	}
}
