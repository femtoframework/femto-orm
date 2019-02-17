package org.femtoframework.orm;

/**
 * An exception to wrap SQLException or other internal exception
 */
public class RepositoryException extends Exception {

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
