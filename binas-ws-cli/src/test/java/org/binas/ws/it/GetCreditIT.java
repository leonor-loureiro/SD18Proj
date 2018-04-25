package org.binas.ws.it;

import org.junit.After;
import org.junit.Assert;
import org.binas.ws.BadInit_Exception;
import org.binas.ws.EmailExists_Exception;
import org.binas.ws.InvalidEmail_Exception;
import org.binas.ws.UserNotExists_Exception;
import org.binas.ws.UserView;
import org.junit.Test;


/**
 * Test suite
 */
public class GetCreditIT extends BaseIT {
	
	/* Test email format */

    @Test
    public void sucess_1() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception {
    	client.testInit(10);
		client.activateUser("username@domain");
		int credit = client.getCredit("username@domain");
		Assert.assertEquals(10, credit);
    }
    
    @Test
    public void sucess_2() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception {
    	client.testInit(20);
		client.activateUser("username.sdis@domain");
		
		client.testInit(10);
		client.activateUser("username@domain");
		
		int credit = client.getCredit("username@domain");
		Assert.assertEquals(10, credit);
    }
    
    @Test
    public void sucess_3() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception {
    	client.testInit(20);
		client.activateUser("username.sdis@domain");
		
		client.testInit(10);
		client.activateUser("username@domain");
		
		client.testInit(15);
		client.activateUser("username@domain.sdis");
		
		int credit = client.getCredit("username@domain");
		Assert.assertEquals(10, credit);
    }
    
    @Test(expected=UserNotExists_Exception.class)
    public void userNotExists_1() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception {
    	client.testInit(20);
		client.activateUser("username.sdis@domain");
		client.getCredit("username@domain");
    }
    
    @Test(expected=UserNotExists_Exception.class)
    public void userNotExists_2() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception {
    	client.testInit(20);
		client.activateUser("username.sdis@domain");
		
		client.testInit(15);
		client.activateUser("username@domain.sdis");
		
		client.getCredit("username@domain");
    }
    
    @Test(expected=UserNotExists_Exception.class)
    public void userNotExists_3() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception {
    	client.testInit(20);
		client.activateUser("username.sdis@domain");
		client.getCredit(null);
    }
   
    
    @After
    public void tearDown() {
    	client.testClear();
    }

}
