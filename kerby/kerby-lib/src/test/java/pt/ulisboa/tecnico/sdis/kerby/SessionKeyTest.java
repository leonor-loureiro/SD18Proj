package pt.ulisboa.tecnico.sdis.kerby;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static pt.ulisboa.tecnico.sdis.kerby.SecurityHelper.generateKey;
import static pt.ulisboa.tecnico.sdis.kerby.SecurityHelper.recodeKey;
import static pt.ulisboa.tecnico.sdis.kerby.XMLHelper.dateToXML;
import static pt.ulisboa.tecnico.sdis.kerby.XMLHelper.xmlToDate;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test suite
 */
public class SessionKeyTest {

	// static members

	// one-time initialization and clean-up

	@BeforeClass
	public static void oneTimeSetUp() {

	}

	@AfterClass
	public static void oneTimeTearDown() {

	}

	// helpers
	
	private SessionKey newTestSessionKey() throws NoSuchAlgorithmException {
		final Key key = SecurityHelper.generateKey();
		long nounce = 1204L;
		return newTestSessionKey(key,nounce);
	}

	private SessionKey newTestSessionKey(Key key, long nounce) {
		SessionKey sessionKey = new SessionKey(key,nounce);
		return sessionKey;
	}

	// tests

	@Test
	public void testCreateSessionKey() throws Exception {
		
		long nounce = 12347856489451123L;
		final Key key = SecurityHelper.generateKey();
		SessionKey sessionKey = newTestSessionKey(key,nounce);

		// compare contents individually
		assertEquals(/* expected */ nounce, /* actual */ sessionKey.getNounce());
		assertArrayEquals(/* expected */ key.getEncoded(), /* actual */ sessionKey.getKeyXY().getEncoded());

		final Key recodedKey = recodeKey(sessionKey.getKeyXY().getEncoded());
		assertEquals(/* expected */ key, /* actual */ recodedKey);
	}

	@Test
	public void testSessionKeyToString() throws Exception {
		SessionKey sessionKey = newTestSessionKey();
		String string = sessionKey.toString();
		assertNotNull(string);
		assertTrue(string.trim().length() > 0);
	}

	@Test
	public void testCreateEqualsSessionKey() throws Exception {
		SessionKey sessionKey1 = newTestSessionKey();
		SessionKey sessionKey2 = newTestSessionKey();
		
		sessionKey2.setKeyXY(sessionKey1.getKeyXY());
		sessionKey2.setNounce(sessionKey1.getNounce());

		
		// equals is not defined by JAX-B
		assertFalse(sessionKey1.getView().equals(sessionKey2.getView()));
		// clerk equals is correct
		assertTrue(sessionKey1.equals(sessionKey2));

		// make a (small) difference
		sessionKey1.setNounce(sessionKey2.getNounce() + 25L);
		// clerk equals should detect difference
		assertFalse(sessionKey1.equals(sessionKey2));
	}

	@Test(expected = KerbyException.class)
	public void testValidateSessionKeyIncompleteKey() throws Exception {
		SessionKey sessionKey = newTestSessionKey();
		sessionKey.getView().setEncodedKeyXY(null);
		sessionKey.validate();
	}

	@Test
	public void testMarshalSessionKey() throws Exception {
		SessionKey sessionKey1 = newTestSessionKey();

		// convert to XML
		byte[] bytes = sessionKey1.toXMLBytes("testSessionKey");

		final Key key = SecurityHelper.generateKey();

		// convert back to Java object
		SessionKey sessionKey2 = newTestSessionKey(key,new Random().nextLong());
		sessionKey2.fromXMLBytes(bytes);

		// compare sessionKeys
		assertTrue(sessionKey1.equals(sessionKey2));
	}

	@Test
	public void testSealSessionKey() throws Exception {
		SessionKey sessionKey1 = newTestSessionKey();

		// seal sessionKey with server key
		final Key serverKey = generateKey();
		CipheredView cipheredSessionKey = sessionKey1.cipher(serverKey);

		// convert back to Java object
		SessionKey sessionKey2 = new SessionKey(cipheredSessionKey, serverKey);		

		// compare sessionKeys
		assertTrue(sessionKey1.equals(sessionKey2));
	}

}