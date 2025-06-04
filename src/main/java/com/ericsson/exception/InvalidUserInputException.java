package com.ericsson.exception;

public class InvalidUserInputException extends RuntimeException {

	private static final long serialVersionUID = -3572126328805062497L;

	public InvalidUserInputException(String message) {
        super(message);
    }
}
