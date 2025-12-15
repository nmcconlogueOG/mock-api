package net.mcfarb.testing.ddmock.model;

import java.util.Map;

import lombok.Data;

@Data
public class MockRestMethodInfo {

	private String path;
	private String httpMethod;
	private String returnId;
	private Integer statusCode;
	private Map<String, String> headers;
	private Map<String, String> queryParameters;

}
