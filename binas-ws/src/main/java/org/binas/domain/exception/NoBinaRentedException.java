package org.binas.domain.exception;

public class NoBinaRentedException extends Exception {
	private static final long serialVersionUID = 1L;

	public NoBinaRentedException () {
	}
	
	public NoBinaRentedException (String message) {
		super(message);
	}
}
