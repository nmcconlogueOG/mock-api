package net.mcfarb.testing.ddmock.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.mcfarb.testing.ddmock.aspects.MockBuilderException;
import net.mcfarb.testing.ddmock.model.MockGeneratorInfo;
import net.mcfarb.testing.ddmock.model.MockObject;
import net.mcfarb.testing.ddmock.model.MockRestGeneratorInfo;

@Slf4j
public class JsonProcessor {

    @Setter
    private ObjectMapper objectMapper;

    private static final String VERSION = "2.0";

    public Object buildObject(MockObject jsonReturnObject, Class<?> returnType, Class<?> genericClass,
            Class<?> keyClass, Class<?> valueClass, String version)
            throws MockBuilderException {
        Object returnObject;
        try {
            if (returnType.isAssignableFrom(String.class)) {
                returnObject = jsonReturnObject.getStringValue();
            } else {
                if (returnType.isAssignableFrom(List.class)) {
                    if (genericClass == null) {
                        throw new NullPointerException(
                                "For List Types genericClass must not be null object Id = " + jsonReturnObject.getId());
                    }
                    returnObject = objectMapper.readValue(jsonReturnObject.getObjectValue().toString(),
                            objectMapper.getTypeFactory().constructParametricType(returnType, genericClass));
                } else if (Map.class.isAssignableFrom(returnType) && VERSION.equals(version)) {
                    if (keyClass == null || valueClass == null) {
                        throw new NullPointerException(
                                "For Map Types, the keyClass and valueClass must not be null and version must be 2.0, object Id = "
                                        + jsonReturnObject.getId());
                    }
                    returnObject = objectMapper.readValue(jsonReturnObject.getObjectValue().toString(),
                            objectMapper.getTypeFactory().constructMapType(HashMap.class, keyClass, valueClass));
                } else if (returnType.isAssignableFrom(ObjectNode.class)) {
                    returnObject = objectMapper.readTree(jsonReturnObject.getObjectValue().toString());
                } else {
                    returnObject = objectMapper.readValue(jsonReturnObject.getObjectValue().toString(), returnType);
                }
            }
            return returnObject;
        } catch (

        JsonProcessingException e) {
            throw new MockBuilderException("Error building objects ", e);
        }
    }

    public MockGeneratorInfo buildMockInfoObjectFromJson(String filename) throws IOException, URISyntaxException {
        log.info("building MockInfoGenerator from file {} ", filename);
        InputStream stream = getClass().getClassLoader().getResourceAsStream(filename + ".json");
        if (stream == null) {
            throw new FileNotFoundException("Could not find file " + filename + ".json");
        }
        return objectMapper.readValue(stream, MockGeneratorInfo.class);
    }

    public MockRestGeneratorInfo buildMockRestInfoObjectFromJson(String filename) throws IOException, URISyntaxException {
        log.info("building MockRestGeneratorInfo from file {} ", filename);
        InputStream stream = getClass().getClassLoader().getResourceAsStream(filename + ".json");
        if (stream == null) {
            throw new FileNotFoundException("Could not find file " + filename + ".json");
        }
        return objectMapper.readValue(stream, MockRestGeneratorInfo.class);
    }
}
