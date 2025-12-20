package net.mcfarb.testing.mockapi.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuration properties for the Mock API application.
 *
 * Configure in application.yml or application.properties:
 * <pre>
 * mock.api:
 *   fallback:
 *     enabled: true
 *     base-url: http://localhost:9090
 *     timeout-ms: 30000
 *   controllers:
 *     user:
 *       fallback-url: http://localhost:9091
 *     product:
 *       fallback-url: http://localhost:9092
 * </pre>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mock.api")
public class MockApiConfiguration {

	/**
	 * Fallback configuration for proxying unmocked requests to real endpoints.
	 */
	private Fallback fallback = new Fallback();

	/**
	 * Per-controller configuration overrides.
	 * Key is the controller name (e.g., "user", "product").
	 */
	private Map<String, ControllerConfig> controllers = new HashMap<>();

	@Data
	public static class Fallback {
		/**
		 * Enable or disable fallback to real endpoints.
		 * When enabled, requests without matching mocks will be proxied to the configured URL.
		 */
		private boolean enabled = false;

		/**
		 * Base URL to proxy unmocked requests to.
		 * Example: "http://localhost:9090" or "https://api.example.com"
		 */
		private String baseUrl;

		/**
		 * Timeout in milliseconds for fallback requests.
		 */
		private int timeoutMs = 30000;

		/**
		 * Whether to forward request headers to the fallback endpoint.
		 */
		private boolean forwardHeaders = true;
	}

	@Data
	public static class ControllerConfig {
		/**
		 * Controller-specific fallback URL that overrides the global base-url.
		 * Example: "http://localhost:9091" or "https://user-api.example.com"
		 */
		private String fallbackUrl;
	}

	/**
	 * Gets the fallback URL for a specific controller.
	 * Returns the controller-specific URL if configured, otherwise the global base URL.
	 *
	 * @param controllerName The name of the controller (e.g., "user", "product")
	 * @return The fallback URL for this controller, or null if not configured
	 */
	public String getFallbackUrlForController(String controllerName) {
		ControllerConfig controllerConfig = controllers.get(controllerName);
		if (controllerConfig != null && controllerConfig.getFallbackUrl() != null) {
			return controllerConfig.getFallbackUrl();
		}
		return fallback.getBaseUrl();
	}
}
