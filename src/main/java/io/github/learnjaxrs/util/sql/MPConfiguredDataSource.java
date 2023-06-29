package io.github.learnjaxrs.util.sql;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

/**
 * This is a configurable DataSource extension that is capable of looking up
 * database connection information from the Micro-Profile configuration.
 *
 * See the DataSourceConfiguration class for example usage.
 */
public class MPConfiguredDataSource extends AbstractConfiguredDataSource {

	@Override
	protected String evaluateExpression(String property, String expression) {
		// LOG.debug("Evaluate " + property + ": " + expression);
		checkConfig();
		Config config = ConfigProvider.getConfig();
		String value = config.getOptionalValue(expression, String.class).orElse("");
		return value;
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
