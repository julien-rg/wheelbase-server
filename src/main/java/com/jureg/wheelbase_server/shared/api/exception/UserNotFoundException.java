package com.jureg.wheelbase_server.shared.api.exception;

@SuppressWarnings("serial")
public class UserNotFoundException extends RuntimeException {

	public UserNotFoundException() {
        super("User not found");
    }
	
	public UserNotFoundException(String message) {
		super(message);
	}
	
}
