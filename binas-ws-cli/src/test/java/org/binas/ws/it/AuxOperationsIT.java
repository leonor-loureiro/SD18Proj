package org.binas.ws.it;

import org.binas.ws.BadInit_Exception;
import org.binas.ws.EmailExists_Exception;
import org.binas.ws.InvalidEmail_Exception;
import org.binas.ws.StationView;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite
 */
public class AuxOperationsIT extends BaseIT {
	private String email1 = "notAdmin@test";
	private String email2 = "test@notAdmin";
	private int initialCredits = 10;
	private int noInitialCredits = 0;
	private String stationId1 = "T08_Station1";
	private int x1 = 22, y1 = 7, capacity1 = 6, reward1 = 2;
	private String stationId2 = "T08_Station2";
	private int x2 = 80, y2 = 20, capacity2 = 12, reward2 = 1;
	private String stationId3 = "T08_Station3";
	private int x3 = 50, y3 = 50, capacity3 = 20, reward3 = 0;

	@Before
	public void setUp() throws BadInit_Exception, EmailExists_Exception, InvalidEmail_Exception {

	}

	@Test
	public void userCreditsInitialization() throws Exception {
		client.testInit(initialCredits);
		client.activateUser(email1);
		
		client.testInit(initialCredits + 5);
		client.activateUser(email2);
		Assert.assertEquals(initialCredits, client.getCredit(email1));
		Assert.assertEquals(initialCredits + 5, client.getCredit(email2));
	}
	
	@Test
	public void stationInitialization() throws Exception {
		client.testInitStation(stationId1, x1, y1, capacity1, reward1);
		StationView station1 = client.getInfoStation(stationId1);
		
		client.testInitStation(stationId1, x2, y2, capacity2, reward2);
		StationView station2 = client.getInfoStation(stationId1);
		
		Assert.assertEquals(station1.getCapacity(), capacity1);
		Assert.assertEquals(station1.getId(), stationId1);
		
		Assert.assertEquals(station2.getCapacity(), capacity2);
		Assert.assertEquals(station2.getId(), stationId1);
	}	
	
	@Test
	public void infoClearing() throws Exception {
		client.testInit(initialCredits);
		client.testInitStation(stationId1, x1, y1, capacity1, reward1);
		client.activateUser(email1);
		
		client.testClear();
		
		client.testInit(initialCredits);
		client.testInitStation(stationId1, x1, y1, capacity1, reward1);
		client.activateUser(email1);
	}

	@After
	public void tearDown() {
		client.testClear();
	}

}
