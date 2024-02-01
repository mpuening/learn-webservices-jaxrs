package io.github.learnjaxrs.util.env;

public interface ExpressionEvaluator {
	/**
	 * @param description Human-readable description of expression
	 * @param expression  expression to evaluate
	 * @return
	 */
	String evaluateExpression(String description, String expression);
}
