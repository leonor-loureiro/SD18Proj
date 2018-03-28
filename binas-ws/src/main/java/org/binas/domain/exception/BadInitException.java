package org.binas.domain.exception;

public class BadInitException extends Exception {
	private static final long serialVersionUID = 1L;

	public BadInitException () {
	}
	
	public BadInitException (String message) {
		super(message);
	}
}
