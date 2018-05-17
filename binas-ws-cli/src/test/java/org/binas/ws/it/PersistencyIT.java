package org.binas.ws.it;

import org.binas.ws.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test suite
 * Tests that user information remains the same altho Binas' cache was cleared
 * Simulating the effect that Binas crashing or went down would have
 */
public class PersistencyIT extends BaseIT {
	
	/* Test email format */
    private String email = VALID_EMAIL;
    private String stationId1 = "T08_Station1";

    /**
     * tests if a user created still exists even when binas' cache is cleared
     * simulates state of an activated user if binas crashes or shuts down
     */
    @Test
    public void sucess_user_persistent() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception {
        client.testInit(10);
		UserView user = client.activateUser(email);
		Assert.assertTrue(10 == user.getCredit());

        client.testClearCache();

        Assert.assertTrue(10 == client.getCredit(email));
    }

    /**
     * tests if user's credit changes remain aftre Binas' cache is cleared
     * simulates state of a binas going down after user rented and returned Bina
     */
    @Test
    public void sucess_user_persistent_2() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception, NoBinaAvail_Exception, NoCredit_Exception, InvalidStation_Exception, AlreadyHasBina_Exception, FullStation_Exception, NoBinaRented_Exception {
        client.testInit(10);
        client.testInitStation(stationId1, 5, 5, 5, 5);
        UserView user = client.activateUser(email);
        Assert.assertTrue(10 == user.getCredit());
        client.rentBina(stationId1, email);
        client.returnBina(stationId1, email);


        client.testClearCache(); //Clear Binas Cache to simulate Binas' crash/down

        // check if client still exists and correct credit applied
        Assert.assertTrue(10 - 1 + 5 == client.getCredit(email));
    }

    @After
    public void tearDown() {
    	client.testClear();
    }

}
