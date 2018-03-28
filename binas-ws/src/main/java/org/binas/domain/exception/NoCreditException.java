package org.binas.domain.exception;

public class NoCreditException extends Exception {
	private static final long serialVersionUID = 1L;

	public NoCreditException () {
	}
	
	public NoCreditException (String message) {
		super(message);
	}
}
