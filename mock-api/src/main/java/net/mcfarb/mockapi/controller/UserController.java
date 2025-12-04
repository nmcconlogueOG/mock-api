package net.mcfarb.mockapi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User REST controller that handles all requests starting with /api/user.
 * All endpoint behavior is driven by configuration loaded from JSON files.
 *
 * The configuration is searched to find matching methods based on:
 * - Path pattern (e.g., /api/user/{id})
 * - HTTP method (GET, POST, PUT, DELETE, etc.)
 * - Query parameters (optional)
 *
 * See src/main/resources/mockdata/user.json for endpoint configuration.
 */
@RestController
@RequestMapping("/api/user")
public class UserController extends BaseRestController {

	@Override
	protected String getBasePath() {
		return "api/user";
	}

	@Override
	protected String getConfigFileName() {
		return "user";
	}
}
