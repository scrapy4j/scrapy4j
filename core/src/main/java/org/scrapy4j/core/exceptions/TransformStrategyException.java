package org.scrapy4j.core.exceptions;

public class TransformStrategyException extends RuntimeException {

    public TransformStrategyException() {
        super();
    }

    public TransformStrategyException(String message) {
        super(message);
    }

    public TransformStrategyException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransformStrategyException(Throwable cause) {
        super(cause);
    }
}
