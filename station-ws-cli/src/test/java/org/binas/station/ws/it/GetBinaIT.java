package org.binas.station.ws.it;

import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.junit.After;
import org.junit.Test;

/**
 * Class that tests Ping operation
 */
public class GetBinaIT extends BaseIT {

	
	@Test
	public void sucess() throws NoBinaAvail_Exception, BadInit_Exception {
		client.testInit(10, 10, 20, 0);
		client.getBina();
	}
	
	@Test(expected=NoBinaAvail_Exception.class)
	public void failure() throws NoBinaAvail_Exception, BadInit_Exception {
		client.testInit(10, 10, 0, 0);
		client.getBina();

	}
	
	@After
	public void clean_up() {
		client.testClear();
	}
	
	

}
