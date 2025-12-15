package net.mcfarb.testing.ddmock.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class MockRestGeneratorInfo {

	private List<MockRestInfo> mockRestApis = new ArrayList<>();
	private List<MockObject> mockObjects = new ArrayList<>();

	@JsonIgnore
	public MockObject getMockObjectById(String id) {
		Optional<MockObject> mockObject = mockObjects.stream().filter(mo -> id.equals(mo.getId())).findFirst();
		return (mockObject.isPresent()) ? mockObject.get() : null;
	}
}
