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
public class TicketTest {

	// static members

	// one-time initialization and clean-up

	@BeforeClass
	public static void oneTimeSetUp() {

	}

	@AfterClass
	public static void oneTimeTearDown() {

	}

	// members
	private Ticket clerk;

	// initialization and clean-up for each test

	@Before
	public void setUp() {
		clerk = new Ticket();
	}

	@After
	public void tearDown() {
		clerk = null;
	}

	// helpers
	
	private Ticket newTestTicket() throws NoSuchAlgorithmException {
		Ticket ticket = new Ticket();
		ticket.setView(newTestTicketView());
		return ticket;
	}

	private Ticket newTestTicket(Date t1, Date t2, Key key) {
		Ticket ticket = new Ticket();
		ticket.setView(newTestTicketView(t1,t2,key));
		return ticket;
	}


	private Ticket newTestTicket(Date t1, Date t2) throws NoSuchAlgorithmException {
		Ticket ticket = new Ticket();
		ticket.setView(newTestTicketView(t1,t2));
		return ticket;
	}

	
	/** Create a test ticket with the specified times and key. */
	private TicketView newTestTicketView(Date t1, Date t2, Key key) {
		TicketView ticketView = new Ticket("C", "S", t1, t2, key).getView();
		return ticketView;
	}

	/** Create a test ticket with the specified times. 
	 * @throws NoSuchAlgorithmException */
	private TicketView newTestTicketView(Date t1, Date t2) throws NoSuchAlgorithmException {
		final Key key = SecurityHelper.generateKey();
		TicketView ticketView = new Ticket("C", "S", t1, t2, key).getView();
		return ticketView;
	}

	/** Create a test ticket. 
	 * @throws NoSuchAlgorithmException */
	private TicketView newTestTicketView() throws NoSuchAlgorithmException {
		final Calendar calendar = Calendar.getInstance();
		final Date t1 = calendar.getTime();
		calendar.add(Calendar.SECOND, 60);
		final Date t2 = calendar.getTime();
		return newTestTicketView(t1, t2);
	}

	// tests

	@Test
	public void testCreateTicket() throws Exception {
		// gets a calendar using the default time zone and locale
		final Calendar calendar = Calendar.getInstance();
		final Date t1 = calendar.getTime();
		calendar.add(Calendar.SECOND, 60);
		final Date t2 = calendar.getTime();
		final Key key = SecurityHelper.generateKey();
		TicketView ticket = newTestTicketView(t1, t2, key);

		// compare contents individually
		assertEquals(/* expected */ "C", /* actual */ ticket.getX());
		assertEquals(/* expected */ "S", /* actual */ ticket.getY());
		assertEquals(/* expected */ t1, /* actual */ xmlToDate(ticket.getTime1()));
		assertEquals(/* expected */ t2, /* actual */ xmlToDate(ticket.getTime2()));
		assertArrayEquals(/* expected */ key.getEncoded(), /* actual */ ticket.getEncodedKeyXY());

		final Key recodedKey = recodeKey(ticket.getEncodedKeyXY());
		assertEquals(/* expected */ key, /* actual */ recodedKey);
	}

	@Test
	public void testTicketToString() throws Exception {
		Ticket ticket = newTestTicket();
		String string = ticket.toString();
		assertNotNull(string);
		assertTrue(string.trim().length() > 0);
	}

	@Test
	public void testCreateEqualsTicket() throws Exception {
		Ticket ticket1 = newTestTicket();
		Ticket ticket2 = newTestTicket();
		
		ticket2.setKeyXY(ticket1.getKeyXY());
		ticket2.setTime1(ticket1.getTime1());
		ticket2.setTime2(ticket1.getTime2());
		ticket2.setX(ticket1.getX());
		ticket2.setY(ticket1.getY());
		

		
		// equals is not defined by JAX-B
		assertFalse(ticket1.getView().equals(ticket2.getView()));
		// clerk equals is correct
		assertTrue(ticket1.equals(ticket2));

		// make a (small) difference
		ticket1.setY(ticket2.getY() + "notTheSame");
		// clerk equals should detect difference
		assertFalse(ticket1.equals(ticket2));
	}

	@Test(expected = KerbyException.class)
	public void testValidateTicketIncompleteX() throws Exception {
		Ticket ticket = newTestTicket();
		ticket.setX(null);
		ticket.validate();
	}

	@Test(expected = KerbyException.class)
	public void testValidateTicketIncompleteY() throws Exception {
		Ticket ticket = newTestTicket();
		ticket.setY(null);
		ticket.validate();
	}

	@Test(expected = KerbyException.class)
	public void testValidateTicketIncompleteTime1() throws Exception {
		Ticket ticket = newTestTicket();
		ticket.getView().setTime1(null);
		ticket.validate();
	}

	@Test(expected = KerbyException.class)
	public void testValidateTicketIncompleteTime2() throws Exception {
		Ticket ticket = newTestTicket();
		ticket.getView().setTime2(null);
		ticket.validate();
	}

	@Test(expected = KerbyException.class)
	public void testValidateTicketIncompleteKey() throws Exception {
		Ticket ticket = newTestTicket();
		ticket.getView().setEncodedKeyXY(null);
		ticket.validate();
	}

	@Test(expected = KerbyException.class)
	public void testValidateTicketWrongTimes() throws Exception {
		Ticket ticket = newTestTicket();
		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, -60);
		final Date beforeT1 = calendar.getTime();

		ticket.setTime2(beforeT1);
		ticket.validate();
	}

	@Test
	public void testMarshalTicket() throws Exception {
		Ticket ticket1 = newTestTicket();

		// convert to XML
		byte[] bytes = ticket1.toXMLBytes("testTicket");

		// convert back to Java object
		Ticket ticket2 = newTestTicket(new Date(),new Date());
		ticket2.fromXMLBytes(bytes);

		// compare tickets
		assertTrue(ticket1.equals(ticket2));
	}

	@Test
	public void testSealTicket() throws Exception {
		Ticket ticket1 = newTestTicket();

		// seal ticket with server key
		final Key serverKey = generateKey();
		CipheredView cipheredTicket = ticket1.cipher(serverKey);

		// convert back to Java object
		Ticket ticket2 = new Ticket(cipheredTicket, serverKey);		

		// compare tickets
		assertTrue(ticket1.equals(ticket2));
	}

}