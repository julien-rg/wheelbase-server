package com.jureg.wheelbase_server.shared.api.exception;

@SuppressWarnings("serial")
public class AlreadyFollowingException extends RuntimeException {

	public AlreadyFollowingException() {
		super("Already following this user");
	}
	
}
