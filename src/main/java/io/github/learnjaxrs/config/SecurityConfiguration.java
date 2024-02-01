package io.github.learnjaxrs.config;

import io.github.learnjaxrs.util.env.ConfigurableEnvironment;
import io.github.learnjaxrs.util.env.Environment;
import io.github.learnjaxrs.util.env.MPExpressionEvaluator;
import io.github.learnjaxrs.util.security.auth.BasicAuthProvider;
import io.github.learnjaxrs.util.security.auth.CheckAuthProvider;
import io.github.learnjaxrs.util.security.auth.HttpAuthenticationMechanismChain;
import io.github.learnjaxrs.util.security.auth.JWTVerifier;
import io.github.learnjaxrs.util.security.identity.CredentialValidator;
import io.github.learnjaxrs.util.security.identity.CredentialValidatorChain;
import io.github.learnjaxrs.util.security.identity.EnvConfiguredLDAPCredentialValidator;
import io.github.learnjaxrs.util.security.identity.TestCredentialValidator;
import jakarta.annotation.security.DeclareRoles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;

@DeclareRoles({ "admin", "user" })
@ApplicationScoped
public class SecurityConfiguration extends HttpAuthenticationMechanismChain implements HttpAuthenticationMechanism {

	/**
	 * Support Basic Auth and JWTs
	 */
	public SecurityConfiguration() {
		super(basicAuthProvider(), jwtVerifier(), checkAuthProvider());
	}

	/**
	 * Configures an Auth Provider to supports Basic Auth for credentials that either
	 * are in the TestCredentialValidator (when in Test Mode), or exists in an LDAP
	 * system.
	 */
	protected static BasicAuthProvider basicAuthProvider() {
		Environment environment = new ConfigurableEnvironment(
				SecurityConfiguration.class.getClassLoader(), new MPExpressionEvaluator());

		final CredentialValidator credentialValidator =

				new CredentialValidatorChain(
						new TestCredentialValidator(environment),
						new EnvConfiguredLDAPCredentialValidator(environment));

		return new BasicAuthProvider(new IdentityStore() {
			@Override
			public CredentialValidationResult validate(Credential credential) {
				return credentialValidator.validate(credential);
			}
		});
	}

	/**
	 * Configures an Auth Provider that supports JWT verification.
	 */
	protected static JWTVerifier jwtVerifier() {
		return new JWTVerifier();
	}

	/**
	 * Provides an Auth Provider that informs users of required credentials.
	 */
	protected static CheckAuthProvider checkAuthProvider() {
		return new CheckAuthProvider();
	}
}
