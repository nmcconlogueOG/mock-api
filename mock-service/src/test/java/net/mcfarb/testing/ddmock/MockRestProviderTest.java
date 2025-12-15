package net.mcfarb.testing.ddmock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import net.mcfarb.testing.ddmock.model.MockRestGeneratorInfo;
import net.mcfarb.testing.ddmock.model.MockRestMethodInfo;
import net.mcfarb.testing.ddmock.sample.SampleData;
import net.mcfarb.testing.ddmock.service.JsonProcessor;
import net.mcfarb.testing.ddmock.service.MockRestProvider;

public class MockRestProviderTest {

	private ObjectMapper objectMapper = new ObjectMapper();
	private JsonProcessor jsonProcessor = new JsonProcessor();
	private MockRestProvider mockRestProvider = new MockRestProvider();

	private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss");

	@BeforeEach
	public void setup() {
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).setDateFormat(dateFormat);

		jsonProcessor.setObjectMapper(this.objectMapper);
		mockRestProvider.setJsonProcessor(jsonProcessor);
	}

	@Test
	public void testLoadMockRestInfoFromJson() throws Exception {
		MockRestGeneratorInfo mockRestInfo = jsonProcessor
				.buildMockRestInfoObjectFromJson("mockdata/" + this.getClass().getSimpleName());

		assertNotNull(mockRestInfo);
		assertTrue(mockRestInfo.getMockRestApis().size() > 0);
		assertTrue(mockRestInfo.getMockObjects().size() > 0);
	}

	@Test
	public void testInitializeRestProvider() throws Exception {
		MockRestGeneratorInfo mockRestInfo = jsonProcessor
				.buildMockRestInfoObjectFromJson("mockdata/" + this.getClass().getSimpleName());

		mockRestProvider.initialize(mockRestInfo);

		// Verify objects were built
		assertNotNull(mockRestProvider.getObjectMap());
		assertTrue(mockRestProvider.getObjectMap().size() > 0);
	}

	@Test
	public void testFindRestMethodWithPathParameter() throws Exception {
		MockRestGeneratorInfo mockRestInfo = jsonProcessor
				.buildMockRestInfoObjectFromJson("mockdata/" + this.getClass().getSimpleName());

		mockRestProvider.initialize(mockRestInfo);

		// Test path parameter matching: /api/v1/users/{userId}
		MockRestMethodInfo method = mockRestProvider.findRestMethod("/api/v1/users/123", "GET", null);

		assertNotNull(method);
		assertEquals("/api/v1/users/{userId}", method.getPath());
		assertEquals("GET", method.getHttpMethod());
		assertEquals("user1", method.getReturnId());
		assertEquals(200, method.getStatusCode());

		// Get the response object
		Object response = mockRestProvider.getResponseObject(method);
		assertNotNull(response);
		assertTrue(response instanceof SampleData);
		SampleData user = (SampleData) response;
		assertEquals("John Doe", user.getData1());
		assertEquals(123L, user.getData2());
	}

	@Test
	public void testFindRestMethodWithQueryParameters() throws Exception {
		MockRestGeneratorInfo mockRestInfo = jsonProcessor
				.buildMockRestInfoObjectFromJson("mockdata/" + this.getClass().getSimpleName());

		mockRestProvider.initialize(mockRestInfo);

		// Test query parameter matching: /api/v1/users?active=true
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("active", "true");

		MockRestMethodInfo method = mockRestProvider.findRestMethod("/api/v1/users", "GET", queryParams);

		assertNotNull(method);
		assertEquals("/api/v1/users", method.getPath());
		assertEquals("userList", method.getReturnId());

		// Get the response object
		Object response = mockRestProvider.getResponseObject(method);
		assertNotNull(response);
		assertTrue(response instanceof List);
		List<SampleData> users = (List<SampleData>) response;
		assertEquals(2, users.size());
		assertEquals("User 1", users.get(0).getData1());
		assertEquals("User 2", users.get(1).getData1());
	}

	@Test
	public void testFindRestMethodWithWildcard() throws Exception {
		MockRestGeneratorInfo mockRestInfo = jsonProcessor
				.buildMockRestInfoObjectFromJson("mockdata/" + this.getClass().getSimpleName());

		mockRestProvider.initialize(mockRestInfo);

		// Test wildcard matching: /api/v1/products/*
		MockRestMethodInfo method = mockRestProvider.findRestMethod("/api/v1/products/abc123", "GET", null);

		assertNotNull(method);
		assertEquals("/api/v1/products/*", method.getPath());
		assertEquals("product1", method.getReturnId());

		// Get the response object
		Object response = mockRestProvider.getResponseObject(method);
		assertNotNull(response);
		assertTrue(response instanceof SampleData);
		SampleData product = (SampleData) response;
		assertEquals("Product ABC", product.getData1());
		assertEquals(999L, product.getData2());
	}

	@Test
	public void testFindRestMethodNotFound() throws Exception {
		MockRestGeneratorInfo mockRestInfo = jsonProcessor
				.buildMockRestInfoObjectFromJson("mockdata/" + this.getClass().getSimpleName());

		mockRestProvider.initialize(mockRestInfo);

		// Test non-existent path
		MockRestMethodInfo method = mockRestProvider.findRestMethod("/api/v1/notfound", "GET", null);

		assertNull(method);
	}

	@Test
	public void testExtractPathParameters() throws Exception {
		MockRestGeneratorInfo mockRestInfo = jsonProcessor
				.buildMockRestInfoObjectFromJson("mockdata/" + this.getClass().getSimpleName());

		mockRestProvider.initialize(mockRestInfo);

		// Test extracting path parameters
		Map<String, String> pathParams = mockRestProvider.extractPathParameters("/api/v1/users/{userId}",
				"/api/v1/users/123");

		assertNotNull(pathParams);
		assertEquals(1, pathParams.size());
		assertEquals("123", pathParams.get("userId"));
	}
}
