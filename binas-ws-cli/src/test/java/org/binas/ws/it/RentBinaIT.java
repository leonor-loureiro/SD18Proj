package org.binas.ws.it;

import org.binas.ws.AlreadyHasBina_Exception;
import org.binas.ws.BadInit_Exception;
import org.binas.ws.EmailExists_Exception;
import org.binas.ws.InvalidEmail_Exception;
import org.binas.ws.InvalidStation_Exception;
import org.binas.ws.NoBinaAvail_Exception;
import org.binas.ws.NoCredit_Exception;
import org.binas.ws.UserNotExists_Exception;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Test suite
 */
public class RentBinaIT extends BaseIT {
	private String email = VALID_EMAIL;
	private int initialCredits = 10;
	private int noInitialCredits = 0;
	private String stationId1 = "T08_Station1";
	private int x1 = 22, y1= 7, capacity1 = 6, reward1 = 2;
	private String stationId2 = "T08_Station2";
	private int x2 = 80, y2= 20, capacity2 = 12, reward2 = 1;
	private String stationId3 = "T08_Station3";
	private int x3 = 50, y3= 50, capacity3 = 20, reward3 = 0;
	
	@Before
	public void setUp() throws BadInit_Exception, EmailExists_Exception, InvalidEmail_Exception {
		client.testInit(initialCredits);
    	client.testInitStation(stationId1, x1, y1, capacity1, reward1);
	}
	
    @Test
    public void success() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception {
		client.activateUser(email);
		client.rentBina(stationId1, email);
		Assert.assertEquals(capacity1 - 1, client.getInfoStation(stationId1).getAvailableBinas());
		Assert.assertEquals(initialCredits -1, client.getCredit(email));
    }

    @Test
    public void successSingleCredit() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception {
    	client.testInit(1);
    	client.activateUser(email);
		client.rentBina(stationId1, email);
		Assert.assertEquals(capacity1 - 1, client.getInfoStation(stationId1).getAvailableBinas());
		Assert.assertEquals(0, client.getCredit(email));
    }
    
//    @Test
//    public void successTwoUsers() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception {
//    	client.activateUser(email2);
//
//    	client.rentBina(stationId1, email);
//		Assert.assertEquals(capacity1 - 1, client.getInfoStation(stationId1).getAvailableBinas());
//		Assert.assertEquals(initialCredits -1, client.getCredit(email));
//
//		client.rentBina(stationId1, email2);
//		Assert.assertEquals(capacity1 - 2, client.getInfoStation(stationId1).getAvailableBinas());
//		Assert.assertEquals(initialCredits -1, client.getCredit(email2));
//
//    }
//
//
//    @Test()
//    public void SuccessMaxCapacityRents() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception {
//
//    	for(int i = 0; i < capacity1 ; i++) {
//    		client.activateUser(email2 + i);
//    		client.rentBina(stationId1, email2 + i);
//    	}
//		Assert.assertEquals(0, client.getInfoStation(stationId1).getAvailableBinas());
//    }
    
    
    
    @Test(expected = AlreadyHasBina_Exception.class)
    public void userAlreadyHasBina() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception {
		client.activateUser(email);
		client.rentBina(stationId1, email);
		client.rentBina(stationId1, email);
		client.rentBina(stationId1, email);
		client.rentBina(stationId1,	email);
    }
    
    @Test(expected = NoCredit_Exception.class)
    public void notEnoughCreditsForRent() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception {

    	client.testInit(noInitialCredits);
    	client.activateUser(email);

		client.rentBina(stationId1, email);
    }
    
//    @Test(expected=NoBinaAvail_Exception.class)
//    public void rentsExceedCapacity() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception {
//
//    	for(int i = 0; i < capacity1 + 1 ; i++) {
//    		client.activateUser(email2 + i);
//    		client.rentBina(stationId1, email2 + i);
//    	}
//    }
    
//    @Test(expected=UserNotExists_Exception.class)
//    public void userNotExistsBadDifferentUserInitialized() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception {
//
//    	client.rentBina(stationId1, "juan");
//	}
//
//    @Test(expected=UserNotExists_Exception.class)
//    public void userNotExistsEmptyEmail_1() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception {
//
//    	client.rentBina(stationId1, "");
//    }
//
//    @Test(expected=UserNotExists_Exception.class)
//    public void userNotExistsEmptyEmail_2() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception {
//
//    	client.rentBina(stationId1, " ");
//    }
//
//    @Test(expected=UserNotExists_Exception.class)
//    public void userNotExistsNullUser() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception {
//
//		client.rentBina(stationId1, null);
//    }
//
//
//    @Test(expected=UserNotExists_Exception.class)
//    public void userNotExistsUnitializedUser() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception {
//
//    	client.rentBina(stationId1, "steve@jobs.apple");
//    }
//
    @Test(expected=InvalidStation_Exception.class)
    public void emptyStationId_1() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception {
		client.activateUser(email);

		client.rentBina("", email);
    }

    @Test(expected=InvalidStation_Exception.class)
    public void emptyStationId_2() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception {
		client.activateUser(email);

    	client.rentBina(" ", email);
    }

    @Test(expected=InvalidStation_Exception.class)
    public void NullStationId() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception {
		client.activateUser(email);

    	client.rentBina(null, email);
    }

    @Test(expected=InvalidStation_Exception.class)
    public void specialCharacterStationId() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception {
		client.activateUser(email);

		client.rentBina("%", email);
    }

    @Test(expected=InvalidStation_Exception.class)
    public void nonExistantStationId() throws EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception, UserNotExists_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception {
		client.activateUser(email);

    	client.rentBina("badId", email);
    }
    
    @After
    public void tearDown() {
    	client.testClear();
    }

}
