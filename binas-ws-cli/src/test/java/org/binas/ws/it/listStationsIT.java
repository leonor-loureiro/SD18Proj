package org.binas.ws.it;

import org.binas.ws.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Test suite
 */
public class listStationsIT extends BaseIT{
    private String email = VALID_EMAIL;
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
        client.activateUser(email);
    }

    @Test
    public void testClosest1st() {
        CoordinatesView cv = new CoordinatesView();
        cv.setX(0);
        cv.setY(0);
        List<StationView> result = client.listStations(3, cv);
        Assert.assertEquals(stationId1, result.remove(0).getId());
    }

    @Test
    public void testClosest2nd() {
        CoordinatesView cv = new CoordinatesView();
        cv.setX(0);
        cv.setY(20);
        List<StationView> result = client.listStations(3, cv);
        Assert.assertEquals(stationId2, result.remove(0).getId());
    }

    @Test
    public void testReturnOrderAsc() {
        CoordinatesView cv = new CoordinatesView();
        cv.setX(0);
        cv.setY(0);
        List<StationView> result = client.listStations(3, cv);
        Assert.assertEquals(stationId1, result.remove(0).getId());
        Assert.assertEquals(stationId2, result.remove(0).getId());
        Assert.assertEquals(stationId3, result.remove(0).getId());
    }

    @Test
    public void test4Stations() {
        CoordinatesView cv = new CoordinatesView();
        cv.setX(0);
        cv.setY(0);
        List<StationView> result = client.listStations(4, cv);
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void testReturnOrderDes() {
        CoordinatesView cv = new CoordinatesView();
        cv.setX(0);
        cv.setY(40);
        List<StationView> result = client.listStations(3, cv);
        Assert.assertEquals(stationId3, result.remove(0).getId());
        Assert.assertEquals(stationId2, result.remove(0).getId());
        Assert.assertEquals(stationId1, result.remove(0).getId());
    }

    @Test
    public void testReturnNumber() {
        CoordinatesView cv = new CoordinatesView();
        cv.setX(80);
        cv.setY(20);
        List<StationView> result = client.listStations(0, cv);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testNegativeCoords() {
        CoordinatesView cv = new CoordinatesView();
        cv.setX(-1);
        cv.setY(-3);
        List<StationView> result = client.listStations(0, cv);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testNullValue() {
        CoordinatesView cv = new CoordinatesView();
        cv.setX(80);
        cv.setY(20);
        List<StationView> result = client.listStations(null, cv);
        Assert.assertEquals(0, result.size());
    }

    @After
    public void tearDown() {
        client.testClear();
    }
}
