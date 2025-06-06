package com.ericsson.exception;

public class UserAlreadyExistsException extends RuntimeException {
 
	private static final long serialVersionUID = 7504104301909771188L;

	public UserAlreadyExistsException(String message) {
        super(message);
    }
}
