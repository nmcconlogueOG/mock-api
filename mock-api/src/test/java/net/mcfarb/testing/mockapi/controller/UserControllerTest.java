package net.mcfarb.testing.mockapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import net.mcfarb.testing.mockapi.controller.UserController;


import java.util.List;
import java.util.Map;

/**
 * Test class for UserController that simulates API calls to a running controller.
 * Uses WebTestClient to test reactive endpoints without starting a full server.
 */
@WebFluxTest(UserController.class)
public class UserControllerTest {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	public void testGetAllUsers() {
		webTestClient
				.get()
				.uri("/api/user")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$").isArray()
				.jsonPath("$[0].name").isEqualTo("Alice Johnson")
				.jsonPath("$[1].name").isEqualTo("Bob Smith")
				.jsonPath("$[2].name").isEqualTo("Charlie Brown");
	}

	@Test
	public void testGetUserById() {
		webTestClient
				.get()
				.uri("/api/user/123")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.id").isEqualTo("1")
				.jsonPath("$.name").isEqualTo("John Doe")
				.jsonPath("$.email").isEqualTo("john@example.com")
				.jsonPath("$.role").isEqualTo("admin");
	}

	@Test
	public void testGetActiveUsers() {
		// Note: Query parameters in WebTestClient can be tricky
		// Alternative approach using uriBuilder for better query param handling
		webTestClient
				.get()
				.uri(uriBuilder -> uriBuilder
						.path("/api/user")
						.queryParam("status", "active")
						.build())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$").isArray();
				// The response will contain users with status=active based on configuration
	}

	@Test
	public void testCreateUser() {
		String newUser = """
				{
					"name": "Test User",
					"email": "test@example.com",
					"role": "user"
				}
				""";

		webTestClient
				.post()
				.uri("/api/user")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(newUser)
				.exchange()
				.expectStatus().isCreated()
				.expectBody()
				.jsonPath("$.id").isEqualTo("4")
				.jsonPath("$.name").isEqualTo("New User")
				.jsonPath("$.message").isEqualTo("User created successfully");
	}

	@Test
	public void testUpdateUser() {
		String updatedUser = """
				{
					"name": "Updated Name",
					"email": "updated@example.com",
					"role": "user"
				}
				""";

		webTestClient
				.put()
				.uri("/api/user/1")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(updatedUser)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.id").isEqualTo("1")
				.jsonPath("$.name").isEqualTo("Updated User")
				.jsonPath("$.message").isEqualTo("User updated successfully");
	}

	@Test
	public void testDeleteUser() {
		webTestClient
				.delete()
				.uri("/api/user/1")
				.exchange()
				.expectStatus().isEqualTo(204)
				.expectBody()
				.jsonPath("$.message").isEqualTo("User deleted successfully");
	}

	@Test
	public void testNonExistentEndpoint() {
		webTestClient
				.get()
				.uri("/api/user/search/advanced")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isNotFound()
				.expectBody()
				.jsonPath("$.error").isEqualTo("No mock configuration found for this endpoint")
				.jsonPath("$.path").isEqualTo("/api/user/search/advanced")
				.jsonPath("$.method").isEqualTo("GET");
	}

	/**
	 * Example using consumeWith to get the actual response body for more complex assertions
	 */
	@Test
	public void testGetAllUsersWithConsumeWith() {
		webTestClient
				.get()
				.uri("/api/user")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBodyList(Map.class)
				.consumeWith(response -> {
					List<Map> users = response.getResponseBody();
					assert users != null;
					assert users.size() == 3;
					assert users.get(0).get("name").equals("Alice Johnson");
					assert users.get(1).get("name").equals("Bob Smith");
					assert users.get(2).get("name").equals("Charlie Brown");
				});
	}

	/**
	 * Example using returnResult to get the response for manual inspection
	 */
	@Test
	public void testGetUserByIdWithReturnResult() {
		webTestClient
				.get()
				.uri("/api/user/123")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody(Map.class)
				.returnResult()
				.getResponseBody();

		// You can store the result and perform additional assertions
	}
}
