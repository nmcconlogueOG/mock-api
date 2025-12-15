package net.mcfarb.testing.ddmock.aspects;

import com.fasterxml.jackson.core.JsonProcessingException;

public class MockBuilderException extends JsonProcessingException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MockBuilderException(String msg) {
        super(msg);
    }

    public MockBuilderException(String msg, Exception e) {
        super(msg, e);
    }

    public MockBuilderException(String msg, Object... parameters) {
        super(String.format(msg, parameters));
    }
}
