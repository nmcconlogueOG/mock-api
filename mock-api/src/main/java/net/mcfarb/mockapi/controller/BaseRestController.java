package net.mcfarb.mockapi.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.mcfarb.testing.ddmock.model.MockRestGeneratorInfo;
import net.mcfarb.testing.ddmock.model.MockRestMethodInfo;
import net.mcfarb.testing.ddmock.service.JsonProcessor;
import net.mcfarb.testing.ddmock.service.MockRestProvider;
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
			@RequestParam(required = false) MultiValueMap<String, String> queryParams) {

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
}
