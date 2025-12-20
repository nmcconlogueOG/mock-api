package net.mcfarb.testing.mockapi.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import net.mcfarb.testing.ddmock.model.MockRestGeneratorInfo;
import net.mcfarb.testing.ddmock.model.MockRestMethodInfo;
import net.mcfarb.testing.ddmock.service.JsonProcessor;
import net.mcfarb.testing.ddmock.service.MockRestProvider;
import net.mcfarb.testing.mockapi.model.User;
import net.mcfarb.testing.TestParent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceTest implements TestParent {

	private MockRestProvider mockRestProvider;
	private ObjectMapper objectMapper;

	@BeforeEach
	public void setup() throws Exception {
		objectMapper = new ObjectMapper();
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		JsonProcessor jsonProcessor = new JsonProcessor();
		jsonProcessor.setObjectMapper(objectMapper);

		mockRestProvider = new MockRestProvider();
		mockRestProvider.setJsonProcessor(jsonProcessor);

		MockRestGeneratorInfo mockRestInfo = jsonProcessor
				.buildMockRestInfoObjectFromJson("mockdata/UserServiceTest");

		mockRestProvider.initialize(mockRestInfo);
	}

	@Test
	public void testFindUserByIdEndpoint() {
		MockRestMethodInfo method = mockRestProvider
				.findRestMethod("/api/users/1", "GET", null);

		assertNotNull(method);
		assertEquals("/api/users/{id}", method.getPath());

		Object response = mockRestProvider.getResponseObject(method);
		User user = (User) response;
		assertEquals("Mock User", user.getName());
		assertEquals("mock@example.com", user.getEmail());
	}

	@Test
	public void testFindAllUsersEndpoint() {
		MockRestMethodInfo method = mockRestProvider
				.findRestMethod("/api/users", "GET", null);

		assertNotNull(method);
		assertEquals("/api/users", method.getPath());

		List<User> users = (List<User>) mockRestProvider.getResponseObject(method);
		assertEquals(2, users.size());
		assertEquals("Alice Mock", users.get(0).getName());
		assertEquals("Bob Mock", users.get(1).getName());
	}

	@Test
	public void testExtractPathParameters() {
		Map<String, String> pathParams = mockRestProvider
				.extractPathParameters("/api/users/{id}", "/api/users/123");

		assertEquals("123", pathParams.get("id"));
	}
}
