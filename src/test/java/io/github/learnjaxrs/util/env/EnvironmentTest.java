package io.github.learnjaxrs.util.env;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class EnvironmentTest {

	@Test
	public void testEnvironment() {
		System.setProperty(Environment.PROFILES_PROPERTY_NAME, "mock,unittest,dev");
		ExpressionEvaluator evaluator = new MPExpressionEvaluator();
		ConfigurableEnvironment environment = new ConfigurableEnvironment(EnvironmentTest.class.getClassLoader(), evaluator);
		assertNotNull(environment);
		
		String username = environment.getProperty("username", "db.user");
		assertEquals("APP", username);

		String absent = environment.getProperty("absent", "unknown.prop", "default");
		assertEquals("default", absent);
		
		String appName = environment.getProperty("AppName", "app.name");
		assertEquals("shadowed-app-name", appName);

		String evaluated = environment.getProperty("Evaluated", "eval.prop");
		assertEquals(null, evaluated);

	}
}
