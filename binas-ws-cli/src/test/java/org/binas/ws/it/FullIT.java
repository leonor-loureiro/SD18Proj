package org.binas.ws.it;

import java.util.ArrayList;
import java.util.List;

import org.binas.ws.BadInit_Exception;
import org.binas.ws.CoordinatesView;
import org.binas.ws.EmailExists_Exception;
import org.binas.ws.InvalidEmail_Exception;
import org.binas.ws.StationView;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Test suite
 */
public class FullIT extends BaseIT {
	
	private int initialCredits = 10;
	private int noInitialCredits = 0;
	
	private String mail = "@test";
	private String stationId1 = "T08_Station1";
	private int x1 = 22, y1= 7, capacity1 = 6, reward1 = 2;
	private String stationId2 = "T08_Station2";
	private int x2 = 80, y2= 20, capacity2 = 7, reward2 = 1;
	private String stationId3 = "T08_Station3";
	private int x3 = 51, y3= 51, capacity3 = 5, reward3 = 0;
	private int allCapacity = capacity1 + capacity2 + capacity3;
	private List<String> stations = new ArrayList<>();
	
	@Before
	public void setUp() throws BadInit_Exception, EmailExists_Exception, InvalidEmail_Exception {
	}
	

	@Test
	public void fullSuccess() throws Exception {
		
		int i = 0;
		
		client.testInit(initialCredits);
    	client.testInitStation(stationId1, x1, y1, capacity1, reward1);	
    	client.testInitStation(stationId2, x2, y2, capacity2, reward2);
    	client.testInitStation(stationId3, x3, y3, capacity3, reward3);
    	
    	for (i = 0; i < allCapacity; i++) {
    		client.activateUser(i + mail);    		
    	}
    	
		
		for(i = 0; i < allCapacity; i++) {
			if(client.getInfoStation(stationId1).getAvailableBinas() > 0)
				client.rentBina(stationId1, i + mail);
			
			else if(client.getInfoStation(stationId2).getAvailableBinas() > 0)
				client.rentBina(stationId2, i + mail);
			
			else if(client.getInfoStation(stationId3).getAvailableBinas() > 0)
				client.rentBina(stationId3, i + mail);
			
			else Assert.fail(); // if all capacity wasn't reached but no binas are available
		}
		
		Assert.assertEquals(capacity1, client.getInfoStation(stationId1).getTotalGets());
		Assert.assertEquals(capacity2, client.getInfoStation(stationId2).getTotalGets());
		Assert.assertEquals(capacity3, client.getInfoStation(stationId3).getTotalGets());
		
		
		CoordinatesView coord = new CoordinatesView();
		coord.setX(x3); 
		coord.setY(y3);
		List<StationView> stations = client.listStations(new Integer(7), coord);
		Assert.assertEquals(stationId3, stations.get(0).getId() );
		
		for(i = 0; i < allCapacity; i++) {
			if(client.getInfoStation(stationId3).getFreeDocks() > 0)
				client.returnBina(stationId3, i + mail);
			
			else if(client.getInfoStation(stationId2).getFreeDocks() > 0)
				client.returnBina(stationId2, i + mail);
			
			else if(client.getInfoStation(stationId1).getFreeDocks() > 0)
				client.returnBina(stationId1, i + mail);
			
			else Assert.fail(); // if all capacity wasn't reached but no binas are available
		}
		Assert.assertEquals(capacity3, client.getInfoStation(stationId3).getTotalReturns());
		Assert.assertEquals(capacity2, client.getInfoStation(stationId2).getTotalReturns());
		Assert.assertEquals(capacity1, client.getInfoStation(stationId1).getTotalReturns());
		
		
		
	}
    
    @After
    public void tearDown() {
    	client.testClear();
    }

}
