package com.ericsson.exception;

public class UserCreationException extends RuntimeException {

	private static final long serialVersionUID = -8378288158933949468L;

	public UserCreationException(String message) {
        super(message);
    }

    public UserCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
