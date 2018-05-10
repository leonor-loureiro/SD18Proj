package example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import pt.ulisboa.tecnico.sdis.kerby.cli.*;
import pt.ulisboa.tecnico.sdis.kerby.*;

import java.security.Key;


public class KerbyExperiment {

    public static void main(String[] args) throws Exception {
        System.out.println("Hi!");

        System.out.println();

        // receive arguments
        System.out.printf("Received %d arguments%n", args.length);
        // Check arguments
        String uddiURL = null;
        String wsName = null;
        String wsURL = null;
        if (args.length == 1) {
            wsURL = args[0];
        } else if (args.length >= 2) {
            uddiURL = args[0];
            wsName = args[1];
            System.out.println("args = [" + args + "]");
        }


        System.out.println();

        // load configuration properties
        try {
            InputStream inputStream = KerbyExperiment.class.getClassLoader().getResourceAsStream("config.properties");
            // variant for non-static methods:
            // InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties");

            Properties properties = new Properties();
            properties.load(inputStream);

            System.out.printf("Loaded %d properties%n", properties.size());

        } catch (IOException e) {
            System.out.printf("Failed to load configuration: %s%n", e);
        }

        System.out.println();

        /////////////////////////////////////////////////////////////////////////////////////
		// client-side code experiments
        ////////////////////////////////////////////////////////////////////////////////////
        System.out.println("Experiment with Kerberos client-side processing");
        KerbyClient client = null;


        if (wsURL != null) {
            System.out.printf("Creating client for server at %s%n", wsURL);
            client = new KerbyClient(wsURL);
        } else if (uddiURL != null) {
            System.out.printf("Creating client using UDDI at %s for server with name %s%n", uddiURL, wsName);
            client = new KerbyClient(uddiURL, wsName);
        }

        Key clientKey = SecurityHelper.generateKeyFromPassword("WxzsYKnJn");
        Key serverKey = SecurityHelper.generateKeyFromPassword("t5h9O9B2");
        String mailClient = "alice@T08.binas.org";
        String mailServer = "binas@T08.binas.org";
        // the following remote invocations are just basic examples
        // the actual tests are made using JUnit

        System.out.println("Invoke dummy()...");
        SessionKeyAndTicketView result = client.requestTicket(mailClient, mailServer,
                new Random().nextLong(), 60 /* seconds */);
        System.out.print("Result: ");
        System.out.println(result);

        CipheredView cipheredSessionKey = result.getSessionKey(); System.out.println("cipheredSessionKey = " + cipheredSessionKey);

        CipheredView cipheredTicket = result.getTicket(); System.out.println("cipheredTicket = " + cipheredTicket);

        SessionKey sessionKey = new SessionKey(cipheredSessionKey, clientKey); System.out.println("sessionKey = " + sessionKey);

        // create auth
        Date currDate = new Date();
        Auth auth = new Auth(mailClient, currDate);

        System.out.println("creating authenticator");
        System.out.println("auth = " + auth);

        CipheredView cipheredAuth = auth.cipher(sessionKey.getKeyXY());
        System.out.println();
        System.out.println();

        //////////////////////////////////////////////////////////////////
		// server-side code experiments
        //////////////////////////////////////////////////////////////////

        System.out.println("Experiment with Kerberos server-side processing");

        System.out.println("Opening ticket with key");

        Ticket ticket = new Ticket(cipheredTicket, serverKey);
        System.out.println("ticket = " + ticket);

        System.out.println("Getting time");
        long timeDiff = ticket.getTime2().getTime() - ticket.getTime1().getTime();
        System.out.println("timeDiff = " + timeDiff);
        int VALID_DURATION = 30;
        System.out.println("timediff should be lower than " + VALID_DURATION * 1000 + " : " + (timeDiff < VALID_DURATION * 1000 ));

        System.out.println();

        System.out.println("unciphering user authentication and verifying if it matches");
        Auth verify = new Auth(cipheredAuth, sessionKey.getKeyXY());
        System.out.println("verify if received " + verify.getX() + " equals " + ticket.getX());

        System.out.println();
		
		System.out.println("Bye!");
    }
}
