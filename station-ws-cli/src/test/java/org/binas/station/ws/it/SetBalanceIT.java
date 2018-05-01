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
public class SetBalanceIT extends BaseIT {

	/* Station has no replica of user. Creates one. */
	@Test
	public void createUser() throws UserNotExists_Exception, InvalidEmail_Exception{
		client.setBalance("user@domain", 10, 0);
		BalanceView balance = client.getBalance("user@domain");
		
		assertEquals(10, balance.getValue());
		assertEquals(0, balance.getTag());
	}
	
	/* Station does no update since tag is the same*/
	@Test
	public void sameTag() throws UserNotExists_Exception, InvalidEmail_Exception{
		client.setBalance("user@domain", 10, 0);
		client.setBalance("user@domain", 20, 0);
		BalanceView balance = client.getBalance("user@domain");
	
		assertEquals(10, balance.getValue());
		assertEquals(0, balance.getTag());
	}
	
	/* Station updates replica of user */
	@Test
	public void updateValuAndTag() throws UserNotExists_Exception, InvalidEmail_Exception {
		client.setBalance("user@domain", 10, 0);
		client.setBalance("user@domain", 20, 1);
		BalanceView balance = client.getBalance("user@domain");
		
		assertEquals(20, balance.getValue());
		assertEquals(1, balance.getTag());
	}
	
	
	@Test(expected=InvalidEmail_Exception.class)
	public void invalidEmail1() throws InvalidEmail_Exception {
		client.setBalance("user@", 10, 0);
	}
	
	@Test(expected=InvalidEmail_Exception.class)
	public void invalidEmail2() throws InvalidEmail_Exception {
		client.setBalance("@domain", 10, 0);
	}
	
	@Test(expected=InvalidEmail_Exception.class)
	public void invalidEmail3() throws InvalidEmail_Exception {
		client.setBalance("user@domain.", 10, 0);
	}
	
	@Test(expected=InvalidEmail_Exception.class)
	public void invalidEmail4() throws InvalidEmail_Exception {
		client.setBalance("user.@domain", 10, 0);
	}
	
	@Test(expected=InvalidEmail_Exception.class)
	public void invalidEmail5() throws InvalidEmail_Exception {
		client.setBalance("", 10, 0);
	}
	
	@Test(expected=InvalidEmail_Exception.class)
	public void invalidEmail6() throws InvalidEmail_Exception {
		client.setBalance(" ", 10, 0);
	}
	
	@Test(expected=InvalidEmail_Exception.class)
	public void invalidEmail7() throws InvalidEmail_Exception {
		client.setBalance(null, 10, 0);
	}
	
	@Test
	public void validEmail1() throws InvalidEmail_Exception {
		client.setBalance("user@domain", 10, 0);
	}
	
	@Test
	public void validEmail2() throws InvalidEmail_Exception {
		client.setBalance("user.sdis@domain", 10, 0);
	}
	
	@Test
	public void validEmail3() throws InvalidEmail_Exception {
		client.setBalance("user@domain.tecnico", 10, 0);
	}
	
	@Test
	public void validEmail4() throws InvalidEmail_Exception {
		client.setBalance("user.sidis@domain.tecnico", 10, 0);
	}
	
	@Test
	public void validEmail5() throws InvalidEmail_Exception {
		client.setBalance("user.sdis.tecnico@domain", 10, 0);
	}
	
	@Test
	public void validEmail6() throws InvalidEmail_Exception {
		client.setBalance("user@domain.sdis.tecnico", 10, 0);
	}
	
	
	@After
	public void clean_up() {
		client.testClear();
	}
	
	

}
