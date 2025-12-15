package net.mcfarb.testing.ddmock.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.BeanInitializationException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.mcfarb.testing.ddmock.aspects.MockBuilderException;
import net.mcfarb.testing.ddmock.model.MockObject;
import net.mcfarb.testing.ddmock.model.MockRestGeneratorInfo;
import net.mcfarb.testing.ddmock.model.MockRestInfo;
import net.mcfarb.testing.ddmock.model.MockRestMethodInfo;

@Slf4j
public class MockRestProvider {

	private List<MockRestMethodInfo> restMethods = new ArrayList<>();
	private Map<String, Object> objectMap = new HashMap<>();

	@Setter
	JsonProcessor jsonProcessor;

	private boolean initialized = false;

	public void initialize(MockRestGeneratorInfo mockRestGeneratorInfo) throws BeanInitializationException {
		if (initialized) {
			log.warn("MockRestProvider is already initialized. Skipping initialization.");
			return;
		}
		if (mockRestGeneratorInfo == null) {
			throw new BeanInitializationException("MockRestGeneratorInfo cannot be null");
		}

		// Build objects from MockObject definitions
		mockRestGeneratorInfo.getMockObjects().forEach(this::buildObject);

		// Collect all REST methods from all REST APIs
		mockRestGeneratorInfo.getMockRestApis().forEach(this::collectRestMethods);

		initialized = true;
	}

	private void buildObject(MockObject mockObject) {
		try {
			if (mockObject.getFakeClass() != null) {
				Object fake = jsonProcessor.buildObject(mockObject, mockObject.getFakeClass(),
						mockObject.getGenericClass(), mockObject.getKeyClass(), mockObject.getValueClass(),
						mockObject.getVersion());
				objectMap.put(mockObject.getId(), fake);
			} else {
				throw new MockBuilderException(
						"Class name must be specified when defining MockObjects. Mock Object with id %s has no associated class.",
						mockObject.getId());
			}
		} catch (MockBuilderException e) {
			throw new RuntimeException(e);
		}
	}

	private void collectRestMethods(MockRestInfo restInfo) {
		String basePath = restInfo.getBasePath() != null ? restInfo.getBasePath() : "";
		restInfo.getMethods().forEach(method -> {
			// Combine base path with method path
			String fullPath = combinePaths(basePath, method.getPath());
			MockRestMethodInfo methodWithFullPath = new MockRestMethodInfo();
			methodWithFullPath.setPath(fullPath);
			methodWithFullPath.setHttpMethod(method.getHttpMethod());
			methodWithFullPath.setReturnId(method.getReturnId());
			methodWithFullPath.setStatusCode(method.getStatusCode());
			methodWithFullPath.setHeaders(method.getHeaders());
			methodWithFullPath.setQueryParameters(method.getQueryParameters());
			restMethods.add(methodWithFullPath);
		});
	}

	private String combinePaths(String basePath, String path) {
		if (basePath == null || basePath.isEmpty()) {
			return path;
		}
		if (path == null || path.isEmpty()) {
			return basePath;
		}
		// Remove trailing slash from basePath and leading slash from path if present
		String base = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
		String pathPart = path.startsWith("/") ? path : "/" + path;
		return base + pathPart;
	}

	public MockRestMethodInfo findRestMethod(String path, String httpMethod, Map<String, String> queryParams) {
		if (!initialized) {
			throw new BeanInitializationException(
					"MockRestProvider is not initialized. Please call initialize() before accessing methods.");
		}

		Optional<MockRestMethodInfo> method = restMethods.stream()
				.filter(m -> matchesRequest(m, path, httpMethod, queryParams))
				.findFirst();

		return method.orElse(null);
	}

	private boolean matchesRequest(MockRestMethodInfo method, String path, String httpMethod,
			Map<String, String> queryParams) {
		// Match path with wildcard support
		if (!matchesPath(method.getPath(), path)) {
			return false;
		}

		// Match HTTP method
		if (method.getHttpMethod() != null && !method.getHttpMethod().equalsIgnoreCase(httpMethod)) {
			return false;
		}

		// Match query parameters if specified
		if (method.getQueryParameters() != null && !method.getQueryParameters().isEmpty()) {
			if (queryParams == null) {
				return false;
			}
			// Check if all required query parameters match
			for (Map.Entry<String, String> entry : method.getQueryParameters().entrySet()) {
				String actualValue = queryParams.get(entry.getKey());
				if (actualValue == null || !actualValue.equals(entry.getValue())) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Matches a path pattern against an actual path.
	 * Supports wildcards: {paramName} or * for single path segments.
	 *
	 * Examples:
	 * - /users/{id} matches /users/123
	 * - /users/* matches /users/123
	 * - /api/v1/products/{productId}/reviews matches /api/v1/products/456/reviews
	 */
	private boolean matchesPath(String pattern, String actualPath) {
		if (pattern == null || actualPath == null) {
			return false;
		}

		// Exact match shortcut
		if (pattern.equals(actualPath)) {
			return true;
		}

		// Split paths by /
		String[] patternSegments = pattern.split("/");
		String[] actualSegments = actualPath.split("/");

		// Must have same number of segments
		if (patternSegments.length != actualSegments.length) {
			return false;
		}

		// Compare each segment
		for (int i = 0; i < patternSegments.length; i++) {
			String patternSegment = patternSegments[i];
			String actualSegment = actualSegments[i];

			// Check if it's a wildcard
			if (isWildcard(patternSegment)) {
				continue; // Wildcard matches anything
			}

			// Must be exact match
			if (!patternSegment.equals(actualSegment)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks if a path segment is a wildcard.
	 * Supports: {paramName} or *
	 */
	private boolean isWildcard(String segment) {
		if (segment == null || segment.isEmpty()) {
			return false;
		}
		// Check for * wildcard
		if ("*".equals(segment)) {
			return true;
		}
		// Check for {paramName} style path parameter
		if (segment.startsWith("{") && segment.endsWith("}")) {
			return true;
		}
		return false;
	}

	public Object getResponseObject(MockRestMethodInfo method) {
		if (method == null || method.getReturnId() == null) {
			return null;
		}
		return objectMap.get(method.getReturnId());
	}

	public Map<String, Object> getObjectMap() {
		return objectMap;
	}

	/**
	 * Extracts path parameters from an actual path based on a pattern.
	 * Returns a map of parameter names to their values.
	 *
	 * Example: pattern="/users/{userId}/posts/{postId}", actualPath="/users/123/posts/456"
	 * Returns: {"userId": "123", "postId": "456"}
	 */
	public Map<String, String> extractPathParameters(String pattern, String actualPath) {
		Map<String, String> params = new HashMap<>();

		if (pattern == null || actualPath == null || !matchesPath(pattern, actualPath)) {
			return params;
		}

		String[] patternSegments = pattern.split("/");
		String[] actualSegments = actualPath.split("/");

		for (int i = 0; i < patternSegments.length; i++) {
			String patternSegment = patternSegments[i];
			// Check if it's a named path parameter {paramName}
			if (patternSegment.startsWith("{") && patternSegment.endsWith("}")) {
				String paramName = patternSegment.substring(1, patternSegment.length() - 1);
				params.put(paramName, actualSegments[i]);
			}
		}

		return params;
	}
}
