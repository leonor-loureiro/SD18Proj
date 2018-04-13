package org.binas.ws.it;

import org.junit.Assert;
import org.junit.Test;


/**
 * Test suite
 */
public class PingIT extends BaseIT {

    @Test
    public void pingEmptyTest() {
        String result = client.testPing("      ");
        Assert.assertEquals(3, result.split("\n").length);
    }

    @Test
    public void pingNullTest() {
        String result = client.testPing(null);
        Assert.assertEquals(3, result.split("\n").length);
    }

    @Test
    public void pingIDTest() {
        String result = client.testPing(client.getClass().getName());
        Assert.assertEquals(3, result.split("\n").length);
    }
}
