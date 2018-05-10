package pt.ulisboa.tecnico.sdis.kerby;

public class BadTicketRequestException extends Exception {
	private static final long serialVersionUID = 1L;

	public BadTicketRequestException() {
	}

	public BadTicketRequestException(String message) {
		super(message);
	}

	public BadTicketRequestException(Throwable cause) {
		super(cause);
	}

	public BadTicketRequestException(String message, Throwable cause) {
		super(message, cause);
	}

}
