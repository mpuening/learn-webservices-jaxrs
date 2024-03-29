package io.github.learnjaxrs.util.sql;

import io.github.learnjaxrs.util.env.ConfigurableEnvironment;
import io.github.learnjaxrs.util.env.MPExpressionEvaluator;

/**
 * This is a configurable DataSource extension that is capable of looking up
 * database connection information from the environment / profiles /
 * micro-profile config properties.
 *
 * See the DataSourceConfiguration class for example usage.
 */
public class MPConfigurableDataSource extends ConfigurableDataSource {

	public MPConfigurableDataSource() {
		super(new ConfigurableEnvironment(new MPExpressionEvaluator()), new MPExpressionEvaluator());
	}
}
