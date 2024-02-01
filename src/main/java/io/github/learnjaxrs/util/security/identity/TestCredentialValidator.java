package io.github.learnjaxrs.util.security.identity;

import java.util.Map;
import java.util.Set;

import io.github.learnjaxrs.util.env.Environment;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;

/**
 * Simple credential validator used for testing only. A set of hard-coded users
 * and roles are provided as default, but can be set from the constructor.
 *
 * This validator is enabled only when TEST_USERS_ALLOWED env variable or
 * test.users.allowed system property is set to true.
 */
public class TestCredentialValidator implements CredentialValidator {

	public static final String TEST_USERS_ENABLED_SYS_PROP = "test.users.enabled";

	private static Map<String, String> USERS = Map.of(

			"admin", "password",

			"alice", "password",

			"bob", "password"

	);

	private static Map<String, Set<String>> ROLES = Map.of(

			"admin", Set.of("admin", "user"),

			"alice", Set.of("user"),

			"bob", Set.of("user")

	);

	private final Map<String, String> users;
	private final Map<String, Set<String>> roles;
	private final boolean isEnabled;

	public TestCredentialValidator(Environment environment, Map<String, String> users, Map<String, Set<String>> roles) {
		this.users = users;
		this.roles = roles;
		this.isEnabled = checkIfEnabled(environment);
	}

	public TestCredentialValidator(Environment environment) {
		this(environment, USERS, ROLES);
	}

	protected boolean checkIfEnabled(Environment environment) {
		String testUsersEnabled = environment
				.getProperty(TEST_USERS_ENABLED_SYS_PROP, TEST_USERS_ENABLED_SYS_PROP, "false");
		return Boolean.valueOf(testUsersEnabled);
	}

	@Override
	public boolean appliesTo(Credential credential) {
		return isEnabled && credential instanceof UsernamePasswordCredential;
	}

	@Override
	public CredentialValidationResult validate(Credential credential) {
		UsernamePasswordCredential login = (UsernamePasswordCredential) credential;

		String password = users.get(login.getCaller());
		if (password != null && password.equals(login.getPasswordAsString())) {
			return new CredentialValidationResult(login.getCaller(), roles.get(login.getCaller()));
		} else {
			return CredentialValidationResult.NOT_VALIDATED_RESULT;
		}
	}

}
