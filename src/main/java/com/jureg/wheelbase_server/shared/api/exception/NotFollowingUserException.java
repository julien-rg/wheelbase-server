package com.jureg.wheelbase_server.shared.api.exception;

@SuppressWarnings("serial")
public class NotFollowingUserException extends RuntimeException {

	public NotFollowingUserException() {
		super("Not following this user");
	}
	
}
