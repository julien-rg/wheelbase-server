package com.jureg.wheelbase_server.shared.api.exception;

@SuppressWarnings("serial")
public class CannotFollowYourselfException extends RuntimeException {
	
	public CannotFollowYourselfException() {
		super("Cannot follow yourself");
	}
	
}
