package net.mcfarb.testing.mockapi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/tracker")
public class MockTracker extends BaseRestController{

    @Override
    protected String getBasePath() {
        return "api/v2/tracker";
    }

    @Override
    protected String getConfigFileName() {
        return "tracker";
    }

    @Override
    protected String getFallbackUrl() {
		return "http://localhost:9072"; // Use global configuration by default
	}
}
