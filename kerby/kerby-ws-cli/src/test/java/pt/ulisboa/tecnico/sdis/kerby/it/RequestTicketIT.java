package pt.ulisboa.tecnico.sdis.kerby.it;

import java.security.SecureRandom;

import org.junit.Test;




/**
 * Test suite
 */
public class RequestTicketIT extends BaseIT {
	
	private static SecureRandom randomGenerator = new SecureRandom();
	private static final String VALID_CLIENT_NAME = "alice@CXX.binas.org";
	private static final String VALID_SERVER_NAME = "binas@CXX.binas.org";
	private static final int VALID_DURATION = 30;
	
	@Test
	public void testSimpleRequest() throws Exception {
		long nounce = randomGenerator.nextLong();
		client.requestTicket(VALID_CLIENT_NAME, VALID_SERVER_NAME, nounce, VALID_DURATION);
	}

}
