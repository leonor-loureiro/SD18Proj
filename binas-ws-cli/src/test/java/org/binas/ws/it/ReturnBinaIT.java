package org.binas.ws.it;

import org.binas.ws.AlreadyHasBina_Exception;
import org.binas.ws.BadInit_Exception;
import org.binas.ws.EmailExists_Exception;
import org.binas.ws.FullStation_Exception;
import org.binas.ws.InvalidEmail_Exception;
import org.binas.ws.InvalidStation_Exception;
import org.binas.ws.NoBinaAvail_Exception;
import org.binas.ws.NoBinaRented_Exception;
import org.binas.ws.NoCredit_Exception;
import org.binas.ws.UserNotExists_Exception;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Test suite
 */
public class ReturnBinaIT extends BaseIT {
	private String email1 = "notAdmin@test";
	private String email2 = "test@notAdmin";
	private int initialCredits = 10;
	private int noInitialCredits = 0;
	private String stationId1 = "T08_Station1";
	private int x1 = 22, y1= 7, capacity1 = 6, reward1 = 2;
	private String stationId2 = "T08_Station2";
	private int x2 = 80, y2= 20, capacity2 = 12, reward2 = 1;
	private String stationId3 = "T08_Station3";
	private int x3 = 50, y3= 50, capacity3 = 20, reward3 = 0;
	
	@Before
	public void setUp() throws BadInit_Exception, EmailExists_Exception, InvalidEmail_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception {
		client.testInit(initialCredits);
    	client.testInitStation(stationId1, x1, y1, capacity1, reward1);
    	client.activateUser(email1);
    	client.rentBina(stationId1, email1);
	}
	
    @Test
    public void success() throws EmailExists_Exception, InvalidEmail_Exception, FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {
		client.returnBina(stationId1, email1);
		Assert.assertEquals(capacity1, client.getInfoStation(stationId1).getAvailableBinas());
		Assert.assertEquals(initialCredits - 1 + reward1, client.getCredit(email1));
    }
    
    
    @Test
    public void successTwoUsers() throws EmailExists_Exception, InvalidEmail_Exception, FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception, AlreadyHasBina_Exception, NoBinaAvail_Exception, NoCredit_Exception {
    	client.activateUser(email2);
    	client.rentBina(stationId1, email2);
		
    	client.returnBina(stationId1, email1);
		Assert.assertEquals(capacity1 - 1, client.getInfoStation(stationId1).getAvailableBinas());

		client.returnBina(stationId1, email2);
		Assert.assertEquals(capacity1, client.getInfoStation(stationId1).getAvailableBinas());

    }
    
    
    @Test()
    public void SuccessMaxCapacityreturns() throws EmailExists_Exception, InvalidEmail_Exception, FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception, AlreadyHasBina_Exception, NoBinaAvail_Exception, NoCredit_Exception, BadInit_Exception {
    	client.testInitStation(stationId2, x2, y2, capacity2, capacity2);
    	
    	for(int i = 0; i < capacity2 ; i++) {    		
    		client.activateUser(email2 + i);
    		client.rentBina(stationId2, email2 + i);
    	}
    	
    	for(int i = 0; i < capacity2 ; i++) {    		
    		client.returnBina(stationId2, email2 + i);
    	}
    	
		Assert.assertEquals(capacity2, client.getInfoStation(stationId2).getAvailableBinas());

    }
    
    @Test(expected = NoBinaRented_Exception.class)
    public void userHasNoBina() throws EmailExists_Exception, InvalidEmail_Exception, FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {
		client.activateUser(email2);
    	client.returnBina(stationId1, email2);
    }
    
    @Test(expected = NoBinaRented_Exception.class)
    public void userAlreadyReturnedBina() throws EmailExists_Exception, InvalidEmail_Exception, FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {
		client.returnBina(stationId1, email1);
		client.returnBina(stationId1, email1);
    }
    
    @Test(expected=FullStation_Exception.class)
    public void returnsExceedCapacity() throws EmailExists_Exception, InvalidEmail_Exception, FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {
    	client.returnBina(stationId2, email1);
    }
    
    @Test(expected=UserNotExists_Exception.class)
    public void userNotExistsBadDiffereturnUserInitialized() throws EmailExists_Exception, InvalidEmail_Exception, FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {	
		
    	client.returnBina(stationId1, "juan");
	}
    
    @Test(expected=UserNotExists_Exception.class)
    public void userNotExistsEmptyEmail_1() throws EmailExists_Exception, InvalidEmail_Exception, FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {    	
		
    	client.returnBina(stationId1, "");
    }
    
    @Test(expected=UserNotExists_Exception.class)
    public void userNotExistsEmptyEmail_2() throws EmailExists_Exception, InvalidEmail_Exception, FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {        	
		
    	client.returnBina(stationId1, " ");
    }
    
    @Test(expected=UserNotExists_Exception.class)
    public void userNotExistsNullUser() throws EmailExists_Exception, InvalidEmail_Exception, FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {
    	
		client.returnBina(stationId1, null);
    }
    
    
    @Test(expected=UserNotExists_Exception.class)
    public void userNotExistsUnitializedUser() throws EmailExists_Exception, InvalidEmail_Exception, FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {
    	
    	client.returnBina(stationId1, "steve@jobs.apple");
    }
    
    @Test(expected=InvalidStation_Exception.class)
    public void emptyStationId_1() throws EmailExists_Exception, InvalidEmail_Exception, FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {
    	
    	client.returnBina("", email1);
    }
    
    @Test(expected=InvalidStation_Exception.class)
    public void emptyStationId_2() throws EmailExists_Exception, InvalidEmail_Exception, FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {
    	
    	client.returnBina(" ", email1);
    }
    
    @Test(expected=InvalidStation_Exception.class)
    public void NullStationId() throws EmailExists_Exception, InvalidEmail_Exception, FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {
    	
    	client.returnBina(null, email1);
    }
    
    @Test(expected=InvalidStation_Exception.class)
    public void specialCharacterStationId() throws EmailExists_Exception, InvalidEmail_Exception, FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {
    	
		client.returnBina("%", email1);
    }
    
    @Test(expected=InvalidStation_Exception.class)
    public void nonExistantStationId() throws EmailExists_Exception, InvalidEmail_Exception, FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {
    	
    	client.returnBina("badId", email1);
    }
    
    @After
    public void tearDown() {
    	client.testClear();
    }

}
