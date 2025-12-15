package net.mcfarb.testing.ddmock.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MockGeneratorInfo {

    private List<MockServiceInfo<? extends Object>> mockServices = new ArrayList<>();
    private List<MockObject> mockObjects = new ArrayList<>();

    public List<MockServiceInfo<?>> getMockServices() {
        return mockServices;
    }

    public void setMockServices(List<MockServiceInfo<?>> mockServices) {
        this.mockServices = mockServices;
    }

    public List<MockObject> getMockObjects() {
        return mockObjects;
    }

    public void setMockObjects(List<MockObject> mockObjects) {
        this.mockObjects = mockObjects;
    }

    @JsonIgnore
    public MockObject getMockObjectById(String id) {
        Optional<MockObject> mockObject = mockObjects.stream().filter(mo -> id.equals(mo.getId())).findFirst();
        return (mockObject.isPresent()) ? mockObject.get() : null;
    }
}