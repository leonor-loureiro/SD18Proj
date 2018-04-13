package org.binas.ws.it;

import org.binas.ws.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite
 */
public class GetInfoStationIT extends BaseIT {
    private String email1 = "notAdmin@test";
    private String email2 = "test@notAdmin";
    private String stationId1 = "T08_Station1";
    private String stationId2 = "T08_Station2";
    private String stationId3 = "T08_Station3";
    private int initialCredits = 10;
    private int noInitialCredits = 0;
    final private int x1 = 0, y1= 0,  capacity1 = 6,  reward1 = 2;
    final private int x2 = 0, y2= 20, capacity2 = 12, reward2 = 1;
    final private int x3 = 0, y3= 40, capacity3 = 20, reward3 = 0;

    @Before
    public void setUp() throws BadInit_Exception, EmailExists_Exception, InvalidEmail_Exception, AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception {
        client.testInit(initialCredits);
        client.testInitStation(stationId1, x1, y1, capacity1, reward1);
        client.testInitStation(stationId2, x2, y2, capacity2, reward2);
        client.testInitStation(stationId3, x3, y3, capacity3, reward3);
        client.activateUser(email1);
    }

    @Test
    public void testNewStation() throws InvalidStation_Exception {
        StationView sInfo = client.getInfoStation(stationId1);
        Assert.assertEquals(stationId1, sInfo.getId());
        Assert.assertEquals(x1, sInfo.getCoordinate().getX().intValue());
        Assert.assertEquals(y1, sInfo.getCoordinate().getY().intValue());
        Assert.assertEquals(capacity1, sInfo.getCapacity());
        Assert.assertEquals(0, sInfo.getFreeDocks());
        Assert.assertEquals(0, sInfo.getTotalGets());
        Assert.assertEquals(0, sInfo.getTotalReturns());
    }

    @Test
    public void testOneGetReturn() throws Exception {
        client.rentBina(stationId1, email1);
        client.returnBina(stationId1, email1);
        StationView sInfo = client.getInfoStation(stationId1);
        Assert.assertEquals(stationId1, sInfo.getId());
        Assert.assertEquals(x1, sInfo.getCoordinate().getX().intValue());
        Assert.assertEquals(y1, sInfo.getCoordinate().getY().intValue());
        Assert.assertEquals(capacity1, sInfo.getCapacity());
        Assert.assertEquals(0, sInfo.getFreeDocks());
        Assert.assertEquals(1, sInfo.getTotalGets());
        Assert.assertEquals(1, sInfo.getTotalReturns());
    }

    @Test
    public void testChangedStation() throws Exception {
        client.rentBina(stationId1, email1);
        client.returnBina(stationId1, email1);
        StationView sInfo = client.getInfoStation(stationId1);
        Assert.assertEquals(stationId1, sInfo.getId());
        Assert.assertEquals(x1, sInfo.getCoordinate().getX().intValue());
        Assert.assertEquals(y1, sInfo.getCoordinate().getY().intValue());
        Assert.assertEquals(capacity1, sInfo.getCapacity());
        Assert.assertEquals(0, sInfo.getFreeDocks());
        Assert.assertEquals(1, sInfo.getTotalGets());
        Assert.assertEquals(1, sInfo.getTotalReturns());

        client.testInitStation(stationId1, x2, y2, capacity2, reward2);
        sInfo = client.getInfoStation(stationId1);
        Assert.assertEquals(stationId1, sInfo.getId());
        Assert.assertEquals(x2, sInfo.getCoordinate().getX().intValue());
        Assert.assertEquals(y2, sInfo.getCoordinate().getY().intValue());
        Assert.assertEquals(capacity2, sInfo.getCapacity());
        Assert.assertEquals(0, sInfo.getFreeDocks());
        Assert.assertEquals(1, sInfo.getTotalGets());
        Assert.assertEquals(1, sInfo.getTotalReturns());
    }

    @After
    public void tearDown() {
        client.testClear();
    }
}
