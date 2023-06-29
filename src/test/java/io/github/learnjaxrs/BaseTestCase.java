package io.github.learnjaxrs;

import java.net.URL;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import io.github.learnjaxrs.util.security.identity.TestCredentialValidator;

public abstract class BaseTestCase {

	/**
	 * Arquillian doesn't seem to support re-using deployments, so the
	 * actual @Deployment must exist in the leaf classes
	 */
	public static WebArchive commonTestDeployment() {

		System.setProperty(TestCredentialValidator.TEST_USERS_ENABLED_SYS_PROP, "true");

		return new RestApiWarBuilder("learn-webservices-jaxrs.war")
				.packages("io.github.learnjaxrs")
				.persistenceXml()
				.beansXml()
				.publicIndexHtml()
				.build();
	}

	@ArquillianResource
	protected URL baseURL;
}
