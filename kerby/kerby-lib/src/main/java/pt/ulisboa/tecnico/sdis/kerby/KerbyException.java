package pt.ulisboa.tecnico.sdis.kerby;

/**
 * Thrown to indicate a Kerberos error.
 * 
 * @author Miguel Pardal
 *
 */
public class KerbyException extends Exception {

	private static final long serialVersionUID = 1L;

	public KerbyException() {
	}

	public KerbyException(String message) {
		super(message);
	}

	public KerbyException(Throwable cause) {
		super(cause);
	}

	public KerbyException(String message, Throwable cause) {
		super(message, cause);
	}

	public KerbyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
