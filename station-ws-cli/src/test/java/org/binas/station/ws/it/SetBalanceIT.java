package org.binas.station.ws.it;

import static org.junit.Assert.assertEquals;

import org.binas.station.ws.BalanceView;
import org.binas.station.ws.UserNotExists_Exception;
import org.junit.After;
import org.junit.Test;

/**
 * Class that tests Ping operation
 */
public class SetBalanceIT extends BaseIT {

	/* Station has no replica of user. Creates one. */
	@Test
	public void createUser() {
		client.setBalance("user@domain", 10, 0);
		BalanceView balance = null;
		try {
			balance = client.getBalance("user@domain");
		}catch(UserNotExists_Exception unee) {}
		assertEquals(10, balance.getValue());
		assertEquals(0, balance.getTag());
	}
	
	/* Station updates replica of user, changing only the tag */
	@Test
	public void updateOnlyTag() {
		client.setBalance("user@domain", 10, 0);
		client.setBalance("user@domain", 10, 1);
		BalanceView balance = null;
		try {
			balance = client.getBalance("user@domain");
		}catch(UserNotExists_Exception unee) {}
		assertEquals(10, balance.getValue());
		assertEquals(1, balance.getTag());
	}
	
	/* Station updates replica of user */
	@Test
	public void updateValuAndTag() {
		client.setBalance("user@domain", 10, 0);
		client.setBalance("user@domain", 20, 1);
		BalanceView balance = null;
		try {
			balance = client.getBalance("user@domain");
		}catch(UserNotExists_Exception unee) {}
		assertEquals(20, balance.getValue());
		assertEquals(1, balance.getTag());
	}
	
	
	@After
	public void clean_up() {
		client.testClear();
	}
	
	

}
