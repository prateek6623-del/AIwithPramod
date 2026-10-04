package com.qa.salesforce.exceptions;

/**
 * Unchecked exception for framework-level failures (configuration, driver setup,
 * page interactions). Keeps checked Selenium exceptions out of test signatures
 * while preserving the root cause for reporting.
 */
public class FrameworkException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FrameworkException(String message) {
        super(message);
    }

    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
