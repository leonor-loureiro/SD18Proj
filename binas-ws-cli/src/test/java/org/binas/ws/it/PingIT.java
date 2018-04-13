package org.binas.ws.it;

import org.junit.Assert;

import org.binas.ws.EmailExists_Exception;
import org.binas.ws.InvalidEmail_Exception;

import static org.junit.Assert.assertNotNull;

import org.binas.ws.BadInit_Exception;
import org.binas.ws.UserView;
import org.junit.Test;


/**
 * Test suite
 */
public class PingIT extends BaseIT {

   

    @Test
    public void pingEmptyTest() {
		assertNotNull(client.testPing("test"));
    }
}
