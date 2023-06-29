package io.github.learnjaxrs.util.security.identity;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

public class MPConfiguredLDAPCredentialValidator extends LDAPCredentialValidator {

	public MPConfiguredLDAPCredentialValidator(
			String ldapUrl,
			String userBaseDn,
			String userNameAttribute,
			String groupBaseDn,
			String groupMemberAttribute,
			String groupNameAttribute) {
		super(
				new MPConfig().evaluateExpression("ldapUrl", ldapUrl),
				new MPConfig().evaluateExpression("userBaseDn", userBaseDn),
				new MPConfig().evaluateExpression("userNameAttribute", userNameAttribute),
				new MPConfig().evaluateExpression("groupBaseDn", groupBaseDn),
				new MPConfig().evaluateExpression("groupMemberAttribute", groupMemberAttribute),
				new MPConfig().evaluateExpression("groupNameAttribute", groupNameAttribute));
	}

	public MPConfiguredLDAPCredentialValidator() {
		this(
				"ldap.url",

				"ldap.user.basedn",

				"ldap.user.nameattr",

				"ldap.group.basedn",

				"ldap.group.memberattr",

				"ldap.group.nameattr");
	}

	// ==========================================

	public static class MPConfig {

		public String evaluateExpression(String property, String expression) {
			// LOG.debug("Evaluate " + property + ": " + expression);
			checkConfig();
			Config config = ConfigProvider.getConfig();
			String value = config.getOptionalValue(expression, String.class).orElse("");
			return value;
		}
	}

	private static boolean checked = false;

	/**
	 * QUESTIONABLE
	 *
	 * This method shouldn't be necessary. But my experience shows that GlassFish
	 * does not register a ConfigProviderResolver in SPI. Thus, simple code such as
	 * ConfigProvider.getConfig(); fails even though it does include an
	 * implementation. Therefore this class sets one. Yes, this class is designed to
	 * work specifically for GlassFish.
	 *
	 * Note to self: Periodically check to see if this issue is resolved.
	 */
	public static void checkConfig() {
		try {
			if (!checked) {
				ConfigProvider.getConfig();
				checked = true;
			}
		} catch (Exception ex) {
			try {
				Class<?> clazz = Class.forName("io.helidon.config.mp.MpConfigProviderResolver");
				Object resolver = clazz.getDeclaredConstructor().newInstance();
				ConfigProviderResolver.setInstance((ConfigProviderResolver) resolver);
				checked = true;
			} catch (Exception e) {
				throw new IllegalStateException("Still no ConfigProviderResolver implementation found", ex);
			}
		}
	}
}
