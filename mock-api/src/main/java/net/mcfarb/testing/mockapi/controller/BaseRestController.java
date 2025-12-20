package net.mcfarb.testing.mockapi.controller;

import java.net.http.HttpRequest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.mcfarb.testing.ddmock.model.MockRestGeneratorInfo;
import net.mcfarb.testing.ddmock.model.MockRestMethodInfo;
import net.mcfarb.testing.ddmock.service.JsonProcessor;
import net.mcfarb.testing.ddmock.service.MockRestProvider;
import net.mcfarb.testing.mockapi.config.MockApiConfiguration;
import reactor.core.publisher.Mono;

/**
 * Base REST controller that provides generic endpoint handling based on configuration.
 * All endpoints are defined in JSON configuration files and matched dynamically.
 *
 * Subclasses should:
 * 1. Extend this class
 * 2. Override getBasePath() to specify which path prefix they handle (e.g., "api/user")
 * 3. Override getConfigFileName() to specify their JSON configuration file (e.g., "user")
 * 4. Use @RestController and @RequestMapping annotations to define the base path
 */
@Slf4j
public abstract class BaseRestController {

	protected MockRestProvider mockRestProvider;
	private JsonProcessor jsonProcessor;

	@Autowired(required = false)
	private MockApiConfiguration mockApiConfiguration;

	@Autowired(required = false)
	private WebClient webClient;

	/**
	 * Returns the base path prefix that this controller handles.
	 * For example: "api/user" or "api/product"
	 *
	 * This is used for logging and identification purposes.
	 * The actual path matching is done via @RequestMapping on the subclass.
	 */
	protected abstract String getBasePath();

	/**
	 * Returns the configuration file name (without .json extension) for this controller.
	 * For example: "user" will load from mockdata/user.json
	 */
	protected abstract String getConfigFileName();

	/**
	 * Returns the controller name used for configuration lookups.
	 * By default, uses the config file name.
	 * Override if you want a different name for configuration purposes.
	 *
	 * @return The controller name (e.g., "user", "product")
	 */
	protected String getControllerName() {
		return getConfigFileName();
	}

	/**
	 * Returns the fallback base URL for this controller.
	 * If not overridden, uses the global fallback URL from configuration.
	 * Override this method to provide controller-specific fallback URLs.
	 *
	 * @return The fallback base URL, or null to use the global configuration
	 */
	protected String getFallbackUrl() {
		return null; // Use global configuration by default
	}


	/**
	 * Initializes the MockRestProvider with the controller's specific configuration.
	 * This is called automatically after the bean is constructed.
	 */
	@PostConstruct
	protected void initialize() {
		try {
			log.info("[{}] Initializing with configuration from mockdata/{}.json", getBasePath(), getConfigFileName());

			// Setup ObjectMapper
			DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss");
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
					.setDateFormat(dateFormat);

			// Setup JsonProcessor
			jsonProcessor = new JsonProcessor();
			jsonProcessor.setObjectMapper(objectMapper);

			// Setup MockRestProvider
			mockRestProvider = new MockRestProvider();
			mockRestProvider.setJsonProcessor(jsonProcessor);

			// Load configuration from JSON file
			MockRestGeneratorInfo mockRestInfo = jsonProcessor
					.buildMockRestInfoObjectFromJson("mockdata/" + getConfigFileName());

			mockRestProvider.initialize(mockRestInfo);

			log.info("[{}] Initialized successfully with {} mock objects",
					getBasePath(), mockRestProvider.getObjectMap().size());

			if (isFallbackEnabled() && !getEffectiveFallbackUrl().contains(":")){
				throw new ParseException(getBasePath()+" falback url must include scheme", 0);
			}

		} catch (Exception e) {
			log.error("[{}] Failed to initialize controller", getBasePath(), e);
			throw new RuntimeException("Failed to initialize " + getBasePath() + " controller", e);
		}
	}

	/**
	 * Generic handler for all HTTP methods and paths under the base path.
	 * This method dynamically routes requests based on the configuration loaded in MockRestProvider.
	 *
	 * The configuration is searched to find a matching method based on:
	 * - Request path (supports path parameters like {id})
	 * - HTTP method (GET, POST, PUT, DELETE, etc.)
	 * - Query parameters (optional)
	 */
	@RequestMapping(value = "/**", method = {
			RequestMethod.GET,
			RequestMethod.POST,
			RequestMethod.PUT,
			RequestMethod.DELETE,
			RequestMethod.PATCH
	})
	public Mono<ResponseEntity<Object>> handleRequest(
			ServerHttpRequest request,
			@RequestParam(required = false) MultiValueMap<String, String> queryParams,
			@RequestBody(required = false) String requestBody) {

		String requestPath = request.getURI().getPath();
		String httpMethod = request.getMethod().name();

		log.debug("[{}] Handling request: {} {}", getBasePath(), httpMethod, requestPath);

		// Convert MultiValueMap to simple Map (taking first value of each param)
		Map<String, String> queryParamMap = null;
		if (queryParams != null && !queryParams.isEmpty()) {
			queryParamMap = queryParams.entrySet().stream()
					.collect(Collectors.toMap(
							Map.Entry::getKey,
							entry -> entry.getValue().get(0)
					));
		}

		// Find matching method in configuration
		MockRestMethodInfo methodInfo = mockRestProvider.findRestMethod(requestPath, httpMethod, queryParamMap);

		if (methodInfo == null) {
			log.warn("[{}] No mock configuration found for: {} {}", getBasePath(), httpMethod, requestPath);

			// Try fallback if enabled
			if (isFallbackEnabled()) {
				return proxyToFallback(request, requestPath, httpMethod, queryParams, requestBody);
			}

			// No fallback - return 404
			return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of(
							"error", "No mock configuration found for this endpoint",
							"path", requestPath,
							"method", httpMethod
					)));
		}

		// Extract path parameters if the pattern contains them
		Map<String, String> pathParams = mockRestProvider.extractPathParameters(methodInfo.getPath(), requestPath);
		if (!pathParams.isEmpty()) {
			log.debug("[{}] Extracted path parameters: {}", getBasePath(), pathParams);
		}

		// Get response object from configuration
		Object responseObject = mockRestProvider.getResponseObject(methodInfo);

		// Build response with configured status code and headers
		int statusCode = methodInfo.getStatusCode() != null ? methodInfo.getStatusCode() : HttpStatus.OK.value();
		ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(statusCode);

		// Add custom headers if configured
		if (methodInfo.getHeaders() != null && !methodInfo.getHeaders().isEmpty()) {
			methodInfo.getHeaders().forEach(responseBuilder::header);
		}

		log.debug("[{}] Returning response with status: {}", getBasePath(), statusCode);

		return Mono.just(responseBuilder.body(responseObject));
	}

	/**
	 * Helper method to check if a request path starts with this controller's base path.
	 * Useful for debugging and validation.
	 */
	protected boolean matchesBasePath(String requestPath) {
		String basePath = getBasePath();
		if (!basePath.startsWith("/")) {
			basePath = "/" + basePath;
		}
		return requestPath.startsWith(basePath);
	}

	/**
	 * Checks if fallback is enabled and a fallback URL is configured.
	 */
	private boolean isFallbackEnabled() {
		if (mockApiConfiguration == null || webClient == null) {
			return false;
		}
		String fallbackUrl = getEffectiveFallbackUrl();
		return mockApiConfiguration.getFallback().isEnabled() && fallbackUrl != null && !fallbackUrl.isEmpty();
	}

	/**
	 * Gets the effective fallback URL with the following priority:
	 * 1. Code-based override from getFallbackUrl()
	 * 2. Configuration-based controller-specific URL
	 * 3. Global fallback base URL
	 */
	private String getEffectiveFallbackUrl() {
		// Priority 1: Code-based override
		String codeFallbackUrl = getFallbackUrl();
		if (codeFallbackUrl != null && !codeFallbackUrl.isEmpty()) {
			return codeFallbackUrl;
		}

		// Priority 2 & 3: Configuration-based (handles both controller-specific and global)
		return mockApiConfiguration.getFallbackUrlForController(getControllerName());
	}

	/**
	 * Proxies the request to the fallback endpoint.
	 */
	private Mono<ResponseEntity<Object>> proxyToFallback(
			ServerHttpRequest request,
			String requestPath,
			String httpMethod,
			MultiValueMap<String, String> queryParams,
			String requestBody) {

		String fallbackUrl = getEffectiveFallbackUrl();
		String targetUrl = fallbackUrl + requestPath;

		String [] urlParts =fallbackUrl.split(":");
		String scheme = urlParts[0];
		String host = urlParts[1].substring(2);
		String port=(urlParts.length>2)? urlParts[2]:null;

		log.info("[{}] Proxying request to fallback: {} {}", getBasePath(), httpMethod, targetUrl);

		// Build WebClient request
		WebClient.RequestBodySpec requestSpec = webClient
				.method(org.springframework.http.HttpMethod.valueOf(httpMethod))
				.uri(uriBuilder -> {
					uriBuilder.scheme(scheme).host(host).port(port).path(requestPath);
					if (queryParams != null && !queryParams.isEmpty()) {
						queryParams.forEach(uriBuilder::queryParam);
					}
					return uriBuilder.build();
				});

		// Forward headers if configured
		if (mockApiConfiguration != null && mockApiConfiguration.getFallback().isForwardHeaders()) {
			HttpHeaders headers = request.getHeaders();
			headers.forEach((name, values) -> {
				// Skip certain headers that should not be forwarded
				if (!name.equalsIgnoreCase("host") && !name.equalsIgnoreCase("content-length")) {
					requestSpec.header(name, values.toArray(new String[0]));
				}
			});
		}

		// Add request body for non-GET requests
		if (requestBody != null && !httpMethod.equalsIgnoreCase("GET")) {
			requestSpec.bodyValue(requestBody);
		}

		// Execute request and handle response
		return requestSpec
				.retrieve()
				.toEntity(Object.class)
				.doOnSuccess(response -> log.debug("[{}] Fallback request succeeded with status: {}",
						getBasePath(), response.getStatusCode()))
				.doOnError(error -> log.error("[{}] Fallback request failed: {}",
						getBasePath(), error.getMessage()))
				.onErrorResume(error -> {
					log.error("[{}] Error proxying to fallback endpoint: {}", getBasePath(), error.getMessage());
					return Mono.just(ResponseEntity.status(HttpStatus.BAD_GATEWAY)
							.body(Map.of(
									"error", "Fallback endpoint error",
									"message", error.getMessage(),
									"path", requestPath
							)));
				});
	}
}
