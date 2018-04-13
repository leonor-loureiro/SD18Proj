package org.binas.ws.it;

import org.junit.After;
import org.junit.Assert;
import org.binas.ws.BadInit_Exception;
import org.binas.ws.EmailExists_Exception;
import org.binas.ws.InvalidEmail_Exception;
import org.binas.ws.UserView;
import org.junit.Test;


/**
 * Test suite
 */
public class ActivateUserIT extends BaseIT {
	
	/* Test email format */

    @Test
    public void sucess_1() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception {
    	client.testInit(10);
		UserView user = client.activateUser("username@domain");
		Assert.assertEquals("username@domain", user.getEmail());
		Assert.assertEquals(10, user.getCredit(),0);
		Assert.assertFalse(user.isHasBina());
    }
    
    @Test
    public void sucess_2() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception {
    	client.testInit(20);
		UserView user = client.activateUser("username.sdis@domain");
		Assert.assertEquals("username.sdis@domain", user.getEmail());
		Assert.assertEquals(20, user.getCredit(),0);
		Assert.assertFalse(user.isHasBina());
    }
    
    @Test
    public void sucess_3() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception {
    	client.testInit(15);
		UserView user = client.activateUser("username@domain.sdis");
		Assert.assertEquals("username@domain.sdis", user.getEmail());
		Assert.assertEquals(15, user.getCredit(),0);
		Assert.assertFalse(user.isHasBina());
    }
    
    @Test
    public void sucess_4() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception {
    	client.testInit(30);
		UserView user = client.activateUser("username.tecnico.leic@domain.ulisboa.tecnico.sdis");
		Assert.assertEquals("username.tecnico.leic@domain.ulisboa.tecnico.sdis", user.getEmail());
		Assert.assertEquals(30, user.getCredit(),0);
		Assert.assertFalse(user.isHasBina());
    }
    
    
    @Test(expected=InvalidEmail_Exception.class)
    public void invalidEmail_1() throws EmailExists_Exception, InvalidEmail_Exception {
		client.activateUser("username.@domain");
    }
    
    @Test(expected=InvalidEmail_Exception.class)
    public void invalidEmail_2() throws EmailExists_Exception, InvalidEmail_Exception {
		client.activateUser("@domain");
    }
    
    @Test(expected=InvalidEmail_Exception.class)
    public void invalidEmail_3() throws EmailExists_Exception, InvalidEmail_Exception {
    	client.activateUser("@");
    }
    
    @Test(expected=InvalidEmail_Exception.class)
    public void invalidEmail_4() throws EmailExists_Exception, InvalidEmail_Exception {
		client.activateUser("username");
    }
    
    @Test(expected=InvalidEmail_Exception.class)
    public void invalidEmail_5() throws EmailExists_Exception, InvalidEmail_Exception {
		client.activateUser("username@.domain");
    }
    
    @Test(expected=InvalidEmail_Exception.class)
    public void invalidEmail_6() throws EmailExists_Exception, InvalidEmail_Exception {
		client.activateUser(".username@domain");
    }
    
    @Test(expected=InvalidEmail_Exception.class)
    public void invalidEmail_7() throws EmailExists_Exception, InvalidEmail_Exception {
		client.activateUser("username@domain.");
    }
    
    @Test(expected=InvalidEmail_Exception.class)
    public void invalidEmail_8() throws EmailExists_Exception, InvalidEmail_Exception {
		client.activateUser("username@.");
    }
    
    @Test(expected=InvalidEmail_Exception.class)
    public void invalidEmail_9() throws EmailExists_Exception, InvalidEmail_Exception {
		client.activateUser(".@domain");
    }
    
    @Test(expected=InvalidEmail_Exception.class)
    public void invalidEmail_10() throws EmailExists_Exception, InvalidEmail_Exception {
		client.activateUser(null);
    }
    /* Test user unique */
    
    @Test(expected=EmailExists_Exception.class)
    public void emailExists() throws EmailExists_Exception, InvalidEmail_Exception {
    	client.activateUser("username@domain");
    	client.activateUser("username@domain");
    }
    
    @After
    public void tearDown() {
    	client.testClear();
    }

}
