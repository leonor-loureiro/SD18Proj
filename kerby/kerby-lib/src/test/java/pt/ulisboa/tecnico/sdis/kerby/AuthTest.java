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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test suite
 */
public class AuthTest {

	// static members

	// one-time initialization and clean-up

	@BeforeClass
	public static void oneTimeSetUp() {

	}

	@AfterClass
	public static void oneTimeTearDown() {

	}

	// members
	private Auth clerk;

	// initialization and clean-up for each test

	@Before
	public void setUp() {
		clerk = newTestAuth();
	}

	@After
	public void tearDown() {
		clerk = null;
	}

	// helpers
	
	/** Create a test auth. */
	private Auth newTestAuth() {
		return newTestAuth(new Date(),"C");
	}
	
	/** Create a test auth with the specified times and string. */
	private Auth newTestAuth(Date t1, String x) {
		Auth auth = new Auth(x, t1);
		return auth;
	}
	
	// tests

	@Test
	public void testCreateAuth() throws Exception {
		String x = "C";
		Date currDate = new Date();
		Auth auth = newTestAuth(currDate,x);

		// compare contents individually
		assertEquals(/* expected */ "C", /* actual */ auth.getX());
		assertEquals(/* expected */ currDate, /* actual */ auth.getTimeRequest());
	}

	@Test
	public void testAuthToString() throws Exception {
		String string = clerk.toString();
		assertNotNull(string);
		assertTrue(string.trim().length() > 0);
	}

	@Test
	public void testCreateEqualsAuth() throws Exception {
		
		Auth auth1 = newTestAuth();
		Auth auth2 = newTestAuth();
		
		auth2.setTimeRequest(auth1.getTimeRequest());
		auth2.setX(auth1.getX());
		

		// equals is not defined by JAX-B
		assertFalse(auth1.getView().equals(auth2.getView()));
		// clerk equals is correct
		assertTrue(auth1.equals(auth2));

		// make a (small) difference
		auth1.setX(auth2.getX() + "notTheSame");
		// clerk equals should detect difference
		assertFalse(auth1.equals(auth2));
	}

	@Test(expected = KerbyException.class)
	public void testValidateAuthIncompleteX() throws Exception {
		Auth auth = newTestAuth();
		auth.setX(null);
		auth.validate();
	}

	@Test(expected = KerbyException.class)
	public void testValidateAuthIncompleteTime1() throws Exception {
		Auth auth = newTestAuth();
		auth.getView().setTimeRequest(null);
		auth.validate();
	}

	@Test
	public void testMarshalAuth() throws Exception {
		
		Auth auth1 = newTestAuth(new Date(),"specific string");

		// convert to XML
		byte[] bytes = auth1.toXMLBytes("testAuth");

		// convert back to Java object
		Auth auth2 = newTestAuth();
		auth2.fromXMLBytes(bytes);

		// compare auths
		assertTrue(auth1.equals(auth2));
	}

	@Test
	public void testSealAuth() throws Exception {
		Auth auth1 = newTestAuth(new Date(),"specific string");

		// seal auth with server key
		final Key serverKey = generateKey();
		CipheredView cipheredAuth = auth1.cipher(serverKey);

		// convert back to Java object
		Auth auth2 = new Auth(cipheredAuth, serverKey);		

		// compare auths
		assertTrue(auth1.equals(auth2));
	}

}