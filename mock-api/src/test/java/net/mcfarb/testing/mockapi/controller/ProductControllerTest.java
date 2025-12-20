package net.mcfarb.testing.mockapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Test class for ProductController that simulates API calls to a running controller.
 * Uses WebTestClient to test reactive endpoints without starting a full server.
 */
@WebFluxTest(ProductController.class)
public class ProductControllerTest {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	public void testGetAllProducts() {
		webTestClient
				.get()
				.uri("/api/product")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$").isArray()
				.jsonPath("$[0].name").isEqualTo("Laptop")
				.jsonPath("$[0].price").isEqualTo(1299.99)
				.jsonPath("$[1].name").isEqualTo("Wireless Mouse");
	}

	@Test
	public void testGetProductById() {
		webTestClient
				.get()
				.uri("/api/product/101")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.id").isEqualTo("101")
				.jsonPath("$.name").isEqualTo("Laptop")
				.jsonPath("$.description").isEqualTo("High-performance laptop")
				.jsonPath("$.price").isEqualTo(1299.99)
				.jsonPath("$.category").isEqualTo("Electronics")
				.jsonPath("$.inStock").isEqualTo(true);
	}

	@Test
	public void testGetProductsByCategory() {
		webTestClient
				.get()
				.uri("/api/product/category/accessories")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$").isArray()
				.jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$[0].name").isEqualTo("Wireless Mouse")
				.jsonPath("$[0].category").isEqualTo("Accessories");
	}

	@Test
	public void testGetInStockProducts() {
		webTestClient
				.get()
				.uri("/api/product?inStock=true")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$").isArray()
				.jsonPath("$.length()").isEqualTo(4)
				.jsonPath("$[0].inStock").isEqualTo(true)
				.jsonPath("$[1].inStock").isEqualTo(true)
				.jsonPath("$[2].inStock").isEqualTo(true)
				.jsonPath("$[3].inStock").isEqualTo(true);
	}

	@Test
	public void testCreateProduct() {
		String newProduct = """
				{
					"name": "New Gadget",
					"description": "Latest gadget",
					"price": 199.99,
					"category": "Electronics",
					"inStock": true
				}
				""";

		webTestClient
				.post()
				.uri("/api/product")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(newProduct)
				.exchange()
				.expectStatus().isCreated()
				.expectBody()
				.jsonPath("$.id").isEqualTo("105")
				.jsonPath("$.name").isEqualTo("New Product")
				.jsonPath("$.message").isEqualTo("Product created successfully");
	}

	@Test
	public void testUpdateProduct() {
		String updatedProduct = """
				{
					"name": "Updated Laptop Pro",
					"description": "Professional laptop",
					"price": 1599.99,
					"category": "Electronics",
					"inStock": true
				}
				""";

		webTestClient
				.put()
				.uri("/api/product/101")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(updatedProduct)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.id").isEqualTo("101")
				.jsonPath("$.name").isEqualTo("Updated Laptop")
				.jsonPath("$.message").isEqualTo("Product updated successfully");
	}

	@Test
	public void testDeleteProduct() {
		webTestClient
				.delete()
				.uri("/api/product/101")
				.exchange()
				.expectStatus().isEqualTo(204)
				.expectBody()
				.jsonPath("$.message").isEqualTo("Product deleted successfully");
	}
}
