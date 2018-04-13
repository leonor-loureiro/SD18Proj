package org.binas.station.ws.it;

import org.binas.station.ws.it.BaseIT;

import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Class that tests Ping operation
 */
public class ReturnBinaIT extends BaseIT {

	@Before
	public void setup() throws BadInit_Exception {
		client.testInit(10, 10, 20, 0);	
	}
	@Test(expected=NoSlotAvail_Exception.class)
	public void failure() throws NoSlotAvail_Exception, BadInit_Exception {
		client.returnBina();
	}
	
	@Test
	public void sucess() throws NoBinaAvail_Exception, NoSlotAvail_Exception, BadInit_Exception {
		client.getBina();	
		client.returnBina();
	}
	
	@After
	public void clean_up() {
		client.testClear();
	}
	
	

}
