package org.binas.station.ws.it;

import static org.junit.Assert.assertEquals;

import org.binas.station.ws.BalanceView;
import org.binas.station.ws.InvalidEmail_Exception;
import org.binas.station.ws.UserNotExists_Exception;
import org.junit.After;
import org.junit.Test;

/**
 * Class that tests Ping operation
 */
public class GetBalanceIT extends BaseIT {

	
	/* Station has replica of user. */
	@Test
	public void sucess() throws UserNotExists_Exception, InvalidEmail_Exception {
		client.setBalance("user@domain", 10, 0);
		BalanceView balance = client.getBalance("user@domain");
		assertEquals(10, balance.getValue());
		assertEquals(0, balance.getTag());
	}
	
	/* Station does not have replica of user*/
	@Test(expected=UserNotExists_Exception.class)
	public void failure() throws UserNotExists_Exception {
		client.getBalance("user@domain");

	}
	
	@After
	public void clean_up() {
		client.testClear();
	}
	
	

}
