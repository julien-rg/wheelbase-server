package com.jureg.wheelbase_server.shared.api.exception;

@SuppressWarnings("serial")
public class AccessDeniedException extends RuntimeException {

	public AccessDeniedException() {
        super("Access denied");
    }
	
	public AccessDeniedException(String message) {
		super(message);
	}
	
}
