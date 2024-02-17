package io.github.learnjaxrs.util.security.auth;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;

import io.github.learnjaxrs.config.SecurityConfiguration;
import io.github.learnjaxrs.util.env.ConfigurableEnvironment;
import io.github.learnjaxrs.util.env.Environment;
import io.github.learnjaxrs.util.env.MPExpressionEvaluator;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.CallerPrincipal;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;

public class JWTVerifier implements AuthProvider {

	private final static String AUTH_PREFIX = "Bearer ";

	private final JWTProcessor<SecurityContext> jwtProcessor;
	
	private final String expectedAudience;

	@Override
	public boolean appliesTo(HttpServletRequest request) {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		return authorization != null && authorization.startsWith(AUTH_PREFIX);
	}

	@Override
	public AuthenticationStatus validateRequest(
			HttpServletRequest request,
			HttpServletResponse response,
			HttpMessageContext httpMessageContext) throws AuthenticationException {

		String accessToken = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
				.filter(header -> !header.isEmpty())
				.filter(header -> header.startsWith(AUTH_PREFIX))
				.map(header -> header.substring(AUTH_PREFIX.length()))
				.orElseGet(() -> "");

		try {
			SecurityContext ctx = null;
			JWTClaimsSet claimsSet = jwtProcessor.process(accessToken, ctx);
			
			// Check audience too
			List<String> audiences = claimsSet.getAudience();
			if (!audiences.contains(expectedAudience)) {
				throw new IllegalArgumentException("Access Token has wrong audience");
			}

			// Get name and roles for principal
			CallerPrincipal principal = new CallerPrincipal(claimsSet.getSubject());
			Set<String> roles = Collections.emptySet();
			Object rolesClaim = claimsSet.getClaim("roles");
			if (rolesClaim instanceof Collection) {
				roles = ((Collection<?>) rolesClaim)
						.stream()
						.map(r -> r.toString())
						.collect(Collectors.toSet());
			}

			// Create result
			CredentialValidationResult result = new CredentialValidationResult(principal, roles);
			return httpMessageContext.notifyContainerAboutLogin(result);
		} catch (Exception e) {
			// e.printStackTrace();
			notifyContainerAboutFailedLogin(request);
		}

		return httpMessageContext.doNothing();
	}

	public JWTVerifier() {
		try {
			Environment environment = new ConfigurableEnvironment(new MPExpressionEvaluator());

			String issuerUrl = environment.getProperty("Issuer", "oauth2.issuer.url");
			String jwksUrl = environment.getProperty("JWKS Url", "oauth2.jwks.url");
			jwtProcessor = createJwtProcessor(
					issuerUrl, jwksUrl,
					Arrays.asList("sub", "iat", "exp", "scope", "aud"));
			expectedAudience = environment.getProperty("Audience", "oauth2.aud");
		} catch (Exception e) {
			throw new IllegalStateException("Unable to configure JWTResourceAuthProvider", e);
		}
	}

	protected JWTProcessor<SecurityContext> createJwtProcessor(
			String issuerUrl,
			String jwksUrl,
			List<String> expectedScopes) throws Exception {
		ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();

		// One can further specify the types of tokens to verify
		// JOSEObjectType joseObjectType = JOSEObjectType.JWT; //
		// JOSEObjectType("at+jwt");
		jwtProcessor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>());

		// Retrieve new keys periodically
		long ttl = 60 * 60 * 1000; // 1 hour
		long refreshTimeout = 60 * 1000; // 1 minute
		JWKSource<SecurityContext> jwkSource = JWKSourceBuilder
				.create(new URL(jwksUrl))
				.cache(ttl, refreshTimeout)
				.retrying(true)
				.build();

		// The expected JWS algorithm of the access tokens
		JWSAlgorithm expectedJWSAlgorithm = JWSAlgorithm.RS256;

		JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlgorithm, jwkSource);
		jwtProcessor.setJWSKeySelector(keySelector);

		// Verify the expected scopes from the issuer
		jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<SecurityContext>(
				new JWTClaimsSet.Builder().issuer(issuerUrl).build(), new HashSet<>(expectedScopes)));
		return jwtProcessor;
	}
}
