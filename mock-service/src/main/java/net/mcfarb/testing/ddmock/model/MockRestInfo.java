package net.mcfarb.testing.ddmock.model;

import java.util.List;

import lombok.Data;

@Data
public class MockRestInfo {

	private String basePath;
	private List<MockRestMethodInfo> methods;

}
