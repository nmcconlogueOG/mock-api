package net.mcfarb.testing.mockapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration test that starts the full Spring Boot application context.
 * This tests all controllers together in a more realistic environment.
 *
 * Use @SpringBootTest when you want to:
 * - Test the entire application context
 * - Test multiple controllers together
 * - Test with all beans and configurations loaded
 *
 * Use @WebFluxTest when you want to:
 * - Test a single controller in isolation
 * - Faster test execution
 * - More focused unit testing
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	public void testUserEndpointsIntegration() {
		// Test GET all users
		webTestClient
				.get()
				.uri("/api/user")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$").isArray()
				.jsonPath("$[0].name").exists();

		// Test GET user by ID
		webTestClient
				.get()
				.uri("/api/user/123")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.name").isEqualTo("John Doe");
	}

	@Test
	public void testProductEndpointsIntegration() {
		// Test GET all products
		webTestClient
				.get()
				.uri("/api/product")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$").isArray()
				.jsonPath("$[0].name").exists();

		// Test GET product by ID
		webTestClient
				.get()
				.uri("/api/product/101")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.name").isEqualTo("Laptop");
	}

	@Test
	public void testMultipleControllersWorkIndependently() {
		// Test that user and product controllers don't interfere with each other

		// User endpoint
		webTestClient
				.get()
				.uri("/api/user/123")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.name").isEqualTo("John Doe");

		// Product endpoint
		webTestClient
				.get()
				.uri("/api/product/101")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.name").isEqualTo("Laptop");

		// Verify they return different data structures
		webTestClient
				.get()
				.uri("/api/user")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].email").exists(); // Users have email

		webTestClient
				.get()
				.uri("/api/product")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].price").exists(); // Products have price
	}
}
