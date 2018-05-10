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
import static pt.ulisboa.tecnico.sdis.kerby.XMLHelper.dateToXML;

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
public class RequestTimeTest {

	// static members

	// one-time initialization and clean-up

	@BeforeClass
	public static void oneTimeSetUp() {

	}

	@AfterClass
	public static void oneTimeTearDown() {

	}

	// members
	private RequestTime wrapperClerk;

	// initialization and clean-up for each test

	@Before
	public void setUp() {
		
		RequestTimeView view = new RequestTimeView();
		view.setTimeRequest(dateToXML(new Date()));
		wrapperClerk = new RequestTime(view);
	}

	@After
	public void tearDown() {
		wrapperClerk = null;
	}

	// helpers

	private RequestTime newTestRequestTime() {
		RequestTime requestTime = new RequestTime(newTestRequestTimeView());
		return requestTime;
	}
	private RequestTime newTestRequestTime(Date t1) {
		RequestTime requestTime = new RequestTime(newTestRequestTimeView(t1));
		return requestTime;
		
	}	
	/** Create a test request time with the specified times and key. */
	private RequestTimeView newTestRequestTimeView(Date t1) {
		RequestTimeView requestTimeView = wrapperClerk.requestTimeBuild(t1);
		return requestTimeView;
	}

	/** Create a test request time. */
	private RequestTimeView newTestRequestTimeView()  {
		return newTestRequestTimeView(new Date());
	}

	// tests

	@Test
	public void testCreateRequestTime() throws Exception {
		// gets a calendar using the default time zone and locale
		final Calendar calendar = Calendar.getInstance();
		final Date t1 = calendar.getTime();
		RequestTimeView view = newTestRequestTimeView(t1);

		// compare contents individually

		assertEquals(/* expected */ t1, /* actual */ xmlToDate(view.getTimeRequest()));
	}

	@Test
	public void testResquestTimeToString() throws Exception {
		RequestTimeView view = newTestRequestTimeView();
		wrapperClerk.setView(view);
		String string = wrapperClerk.requestTimeToString();
		assertNotNull(string);
		assertTrue(string.trim().length() > 0);
	}

	@Test
	public void testCreateEqualsRequestTime() throws Exception {
		final Calendar calendar = Calendar.getInstance();
		final Date t1 = calendar.getTime();
		RequestTimeView view1 = newTestRequestTimeView(t1);
		RequestTimeView view2 = newTestRequestTimeView(t1);

		// equals is not defined by JAX-B
		assertFalse(view1.equals(view2));
		// clerk equals is correct
		assertTrue(wrapperClerk.RequestTimeViewEquals(view1, view2));


		// make a (small) difference
		view2.setTimeRequest(dateToXML(new Date(456789)));
		// clerk equals should detect difference
		assertFalse(wrapperClerk.RequestTimeViewEquals(view1, view2));
	}

	@Test(expected = KerbyException.class)
	public void testValidateRequestTimeIncompleteTime() throws Exception {
		RequestTimeView view = newTestRequestTimeView();
		view.setTimeRequest(null);
		wrapperClerk.setView(view);
		wrapperClerk.validate();
	}

	@Test
	public void testMarshalRequestTime() throws Exception {
		final Calendar calendar = Calendar.getInstance();
		final Date t1 = calendar.getTime();
		RequestTime requestTimeClerk = newTestRequestTime(t1);
		
		// convert to XML
		byte[] bytes = requestTimeClerk.toXMLBytes("testRequestTime");

		RequestTime otherWrapper = newTestRequestTime();
		
		// convert back to Java object
		otherWrapper.fromXMLBytes(bytes);

		// compare requestTimes
		assertTrue(requestTimeClerk.equals(otherWrapper));
	}

	@Test
	public void testCipherRequestTime() throws Exception {
		final Calendar calendar = Calendar.getInstance();
		final Date t1 = calendar.getTime();
		RequestTime requestTime1 = newTestRequestTime(t1);
		
		// seal requestTime with server key
		final Key serverKey = generateKey();
		CipheredView cipheredRequestTimeView = requestTime1.cipher(serverKey);
		
		// decipher requestTime with server key
		RequestTime requestTime2 = new RequestTime(cipheredRequestTimeView,serverKey);
				
		
		// compare requestTimes
		assertTrue(requestTime1.equals(requestTime2));
	}

}
