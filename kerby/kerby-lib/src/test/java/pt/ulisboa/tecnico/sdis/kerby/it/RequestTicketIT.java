package pt.ulisboa.tecnico.sdis.kerby.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import org.junit.Test;

import pt.ulisboa.tecnico.sdis.kerby.BadTicketRequest_Exception;
import pt.ulisboa.tecnico.sdis.kerby.CipheredView;
import pt.ulisboa.tecnico.sdis.kerby.SecurityHelper;
import pt.ulisboa.tecnico.sdis.kerby.SessionKey;
import pt.ulisboa.tecnico.sdis.kerby.SessionKeyAndTicketView;
import pt.ulisboa.tecnico.sdis.kerby.SessionKeyView;
import pt.ulisboa.tecnico.sdis.kerby.Ticket;
import pt.ulisboa.tecnico.sdis.kerby.TicketView;


/**
 * Test suite
 */
public class RequestTicketIT extends BaseIT {
	
	private static SecureRandom randomGenerator = new SecureRandom();
	private static final String VALID_CLIENT_NAME = "alice@CXX.binas.org";
	private static final String VALID_CLIENT_PASSWORD = "Zd8hqDu23t";
	private static final String VALID_SERVER_NAME = "binas@CXX.binas.org";
	private static final String VALID_SERVER_PASSWORD = "MTbvC3";
	private static final int VALID_DURATION = 30;
	
	// Valid Request Tests -------------------------------------------------------------
	
	@Test
	public void testValidRequest() throws Exception {
		final Key clientKey = getKey(VALID_CLIENT_PASSWORD);
		final Key serverKey = getKey(VALID_SERVER_PASSWORD);
		long nounce = randomGenerator.nextLong();
		
		SessionKeyAndTicketView result = client.requestTicket(VALID_CLIENT_NAME, VALID_SERVER_NAME, nounce, VALID_DURATION);
		
		CipheredView cipheredSessionKey = result.getSessionKey();
		CipheredView cipheredTicket = result.getTicket();
		
		SessionKey sessionKey = new SessionKey(cipheredSessionKey, clientKey);
		
		Ticket ticket = new Ticket(cipheredTicket, serverKey);
		long timeDiff = ticket.getTime2().getTime() - ticket.getTime1().getTime();
		
		
		assertEquals(nounce, sessionKey.getNounce());
		assertNotNull(sessionKey.getKeyXY());
		assertEquals(VALID_CLIENT_NAME, ticket.getX());
		assertEquals(VALID_SERVER_NAME, ticket.getY());
		assertEquals((long) VALID_DURATION * 1000, timeDiff);
		assertNotNull(ticket.getKeyXY());
		assertEquals(sessionKey.getKeyXY(), ticket.getKeyXY());
		
		/* System.out.println(ticket.toString());
		   System.out.println(sessionKey.toString()); */
	}
	
	@Test
	public void testValidDoubleRequest() throws Exception {
		final Key clientKey = getKey(VALID_CLIENT_PASSWORD);
		final Key serverKey = getKey(VALID_SERVER_PASSWORD);
		long nounce1 = randomGenerator.nextLong();
		long nounce2 = randomGenerator.nextLong();
		
		SessionKeyAndTicketView result1 = client.requestTicket(VALID_CLIENT_NAME, VALID_SERVER_NAME, nounce1, VALID_DURATION);
		SessionKeyAndTicketView result2 = client.requestTicket(VALID_CLIENT_NAME, VALID_SERVER_NAME, nounce2, VALID_DURATION);
		
		CipheredView cipheredSessionKey1 = result1.getSessionKey();
		CipheredView cipheredSessionKey2 = result2.getSessionKey();
		CipheredView cipheredTicket1 = result1.getTicket();
		CipheredView cipheredTicket2 = result2.getTicket();
		
		SessionKey sessionKey1 = new SessionKey(cipheredSessionKey1, clientKey);
		SessionKey sessionKey2 = new SessionKey(cipheredSessionKey2, clientKey);
		
		Ticket ticket1 = new Ticket(cipheredTicket1, serverKey);
		Ticket ticket2 = new Ticket(cipheredTicket2, serverKey);
		long timeDiff1 = ticket1.getTime2().getTime() - ticket1.getTime1().getTime();
		long timeDiff2 = ticket2.getTime2().getTime() - ticket2.getTime1().getTime();
		
		
		assertEquals(nounce1, sessionKey1.getNounce());
		assertEquals(nounce2, sessionKey2.getNounce());
		assertNotNull(sessionKey1.getKeyXY());
		assertNotNull(sessionKey2.getKeyXY());
		assertEquals(VALID_CLIENT_NAME, ticket1.getX());
		assertEquals(VALID_CLIENT_NAME, ticket2.getX());
		assertEquals(VALID_SERVER_NAME, ticket1.getY());
		assertEquals(VALID_SERVER_NAME, ticket2.getY());
		assertEquals((long) VALID_DURATION * 1000, timeDiff1);
		assertEquals((long) VALID_DURATION * 1000, timeDiff2);
		assertNotNull(ticket1.getKeyXY());
		assertNotNull(ticket2.getKeyXY());
		assertEquals(sessionKey1.getKeyXY(), ticket1.getKeyXY());
		assertEquals(sessionKey2.getKeyXY(), ticket2.getKeyXY());
		assertNotEquals(sessionKey1.getKeyXY(), sessionKey2.getKeyXY());
	}
	
	
	// Invalid Client Tests -------------------------------------------------------------
	
	@Test(expected = BadTicketRequest_Exception.class)
	public void testNullClient() throws Exception {
		long nounce = randomGenerator.nextLong();
		client.requestTicket(null, VALID_SERVER_NAME, nounce, VALID_DURATION);
	}
	
	@Test(expected = BadTicketRequest_Exception.class)
	public void testEmptyClient() throws Exception {
		long nounce = randomGenerator.nextLong();
		client.requestTicket("", VALID_SERVER_NAME, nounce, VALID_DURATION);
		
	}
	
	@Test(expected = BadTicketRequest_Exception.class)
	public void testWhitespaceClient() throws Exception {
		long nounce = randomGenerator.nextLong();
		client.requestTicket(" 	 ", VALID_SERVER_NAME, nounce, VALID_DURATION);
		
	}
	
	@Test(expected = BadTicketRequest_Exception.class)
	public void testInvalidClient() throws Exception {
		long nounce = randomGenerator.nextLong();
		client.requestTicket("invalidKerbyClient", VALID_SERVER_NAME, nounce, VALID_DURATION);		
	}

	
	// Invalid Server Tests -------------------------------------------------------------
	
	@Test(expected = BadTicketRequest_Exception.class)
	public void testNullServer() throws Exception {
		long nounce = randomGenerator.nextLong();
		client.requestTicket(VALID_CLIENT_NAME, null, nounce, VALID_DURATION);
	}
	
	@Test(expected = BadTicketRequest_Exception.class)
	public void testEmptyServer() throws Exception {
		long nounce = randomGenerator.nextLong();
		client.requestTicket(VALID_CLIENT_NAME, "", nounce, VALID_DURATION);
		
	}
	
	@Test(expected = BadTicketRequest_Exception.class)
	public void testWhitespaceServer() throws Exception {
		long nounce = randomGenerator.nextLong();
		client.requestTicket(VALID_CLIENT_NAME, " 	 ", nounce, VALID_DURATION);
		
	}
	
	@Test(expected = BadTicketRequest_Exception.class)
	public void testInvalidServer() throws Exception {
		long nounce = randomGenerator.nextLong();
		client.requestTicket(VALID_CLIENT_NAME, "invalidKerbyServer", nounce, VALID_DURATION);		
	}
	

	// Invalid Nounce Tests -------------------------------------------------------------

	@Test(expected = BadTicketRequest_Exception.class)
	public void testRepeatedNounce() throws Exception {
		long nounce = randomGenerator.nextLong();
		
		client.requestTicket(VALID_CLIENT_NAME, VALID_SERVER_NAME, nounce, VALID_DURATION);
		client.requestTicket(VALID_CLIENT_NAME, VALID_SERVER_NAME, nounce, VALID_DURATION);
	}
	
	@Test(expected = BadTicketRequest_Exception.class)
	public void testIntervalRepeatedNounce() throws Exception {
		long nounce1 = randomGenerator.nextLong();
		long nounce2 = randomGenerator.nextLong();
		
		client.requestTicket(VALID_CLIENT_NAME, VALID_SERVER_NAME, nounce1, VALID_DURATION);
		client.requestTicket(VALID_CLIENT_NAME, VALID_SERVER_NAME, nounce2, VALID_DURATION);
		client.requestTicket(VALID_CLIENT_NAME, VALID_SERVER_NAME, nounce1, VALID_DURATION);
	}

	
	// Duration Tests -------------------------------------------------------------

	@Test
	public void testMinDuration() throws Exception {
		long nounce = randomGenerator.nextLong();
		client.requestTicket(VALID_CLIENT_NAME, VALID_SERVER_NAME, nounce, 10);
	}
	
	@Test
	public void testMaxDuration() throws Exception {
		long nounce = randomGenerator.nextLong();
		client.requestTicket(VALID_CLIENT_NAME, VALID_SERVER_NAME, nounce, 300);
	}
	
	@Test(expected = BadTicketRequest_Exception.class)
	public void testLowerDuration() throws Exception {
		long nounce = randomGenerator.nextLong();
		client.requestTicket(VALID_CLIENT_NAME, VALID_SERVER_NAME, nounce, 9);
	}
	
	@Test(expected = BadTicketRequest_Exception.class)
	public void testHigherDuration() throws Exception {
		long nounce = randomGenerator.nextLong();
		client.requestTicket(VALID_CLIENT_NAME, VALID_SERVER_NAME, nounce, 301);
	}

	
	// Test Helpers -------------------------------------------------------------
	private Key getKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		return SecurityHelper.generateKeyFromPassword(password);
	}

}
