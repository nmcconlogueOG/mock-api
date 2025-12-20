package net.mcfarb.testing.mockapi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Product REST controller that handles all requests starting with /api/product.
 * All endpoint behavior is driven by configuration loaded from JSON files.
 *
 * The configuration is searched to find matching methods based on:
 * - Path pattern (e.g., /api/product/{id})
 * - HTTP method (GET, POST, PUT, DELETE, etc.)
 * - Query parameters (optional)
 *
 * See src/main/resources/mockdata/product.json for endpoint configuration.
 */
@RestController
@RequestMapping("/api/product")
public class ProductController extends BaseRestController {

	@Override
	protected String getBasePath() {
		return "api/product";
	}

	@Override
	protected String getConfigFileName() {
		return "product";
	}
}
