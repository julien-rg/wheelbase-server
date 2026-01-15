package com.jureg.wheelbase_server.shared.api.exception;

@SuppressWarnings("serial")
public class CannotUnfollowYourselfException extends RuntimeException {

	public CannotUnfollowYourselfException() {
		super("Cannot unfollow yourself");
	}
	
}
