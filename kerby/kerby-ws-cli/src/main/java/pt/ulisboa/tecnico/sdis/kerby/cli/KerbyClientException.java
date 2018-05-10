package pt.ulisboa.tecnico.sdis.kerby.cli;

public class KerbyClientException extends Exception {

	private static final long serialVersionUID = 1L;

	public KerbyClientException() {
	}

	public KerbyClientException(String message) {
		super(message);
	}

	public KerbyClientException(Throwable cause) {
		super(cause);
	}

	public KerbyClientException(String message, Throwable cause) {
		super(message, cause);
	}

}
