package com.jureg.wheelbase_server.shared.api.exception;

@SuppressWarnings("serial")
public class FieldAlreadyExistsException extends RuntimeException {

	public FieldAlreadyExistsException(String field) {
		super(field + " already exists");
	}
	
}
