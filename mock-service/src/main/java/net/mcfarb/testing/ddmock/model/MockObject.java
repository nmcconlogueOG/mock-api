package net.mcfarb.testing.ddmock.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class MockObject {

    private String id;

    // optional field - not needed if you are only referencing this
    // class as a return value for a service, in that case reflection is used.
    private Class<?> fakeClass;

    // only needed if fakeClass is a Parameterized list etc.
    private Class<?> genericClass;

    @JsonIgnore
    private JsonNode objectValue;

    String valueAsString;
    private String stringValue;

    private Class<?> keyClass;

    private Class<?> valueClass;

    /**
     * When deserializing JSON Map, needs to specify the keyClass,
     * valueClass and version 2.0
     * please see sample json object4 in the file MockBuilderTest.json
     */
    private String version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("objectValue")
    public void setBody(JsonNode objectValue) {
        this.objectValue = objectValue;
    }

    @JsonProperty("objectValue")
    public JsonNode getObjectValue() {
        return this.objectValue;
    }

    @JsonProperty
    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String bodyAsString) {
        this.stringValue = bodyAsString;
    }

    @JsonProperty("class")
    public Class<?> getFakeClass() {
        return fakeClass;
    }

    @JsonProperty("class")
    public void setFakeClass(Class<?> fakeClass) {
        this.fakeClass = fakeClass;
    }

    public Class<?> getGenericClass() {
        return genericClass;
    }

    public void setGenericClass(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    public Class<?> getKeyClass() {
        return keyClass;
    }

    public void setKeyClass(Class<?> keyClass) {
        this.keyClass = keyClass;
    }

    public Class<?> getValueClass() {
        return valueClass;
    }

    public void setValueClass(Class<?> valueClass) {
        this.valueClass = valueClass;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
