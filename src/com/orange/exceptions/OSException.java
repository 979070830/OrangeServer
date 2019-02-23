package com.orange.exceptions;

public class OSException extends RuntimeException {

	public OSException(String message) {
		super(message);
	}
	
	public OSException(String message,Object data) {
		super(message);
	}
}
