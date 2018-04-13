package org.binas.station.ws.it;

import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.StationView;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * Class that tests Ping operation
 */
public class GetInfoIT extends BaseIT {

	
	@Test
	public void sucess1() throws BadInit_Exception {
		
		client.testInit(10, 10, 20, 0);
		
		StationView view = client.getInfo();
		
		Assert.assertEquals(0, view.getFreeDocks());
		Assert.assertEquals(20, view.getCapacity());
		Assert.assertEquals(0, view.getTotalGets());
		Assert.assertEquals(0, view.getTotalReturns());
		Assert.assertEquals(10, view.getCoordinate().getX());
		Assert.assertEquals(10, view.getCoordinate().getY());
			
	}
	
	@Test(expected=BadInit_Exception.class)
	public void failure() throws BadInit_Exception {
			client.testInit(-10, 20, -10, 0);
	}
	
	@Test(expected=BadInit_Exception.class)
	public void sucess2() throws BadInit_Exception {
		client.testInit(300, 300, 0, 0);		
	}
		
	
	@After
	public void clean_up() {
		client.testClear();
	}
	
	

}
