package org.binas.ws.cli;

import org.binas.ws.EmailExists_Exception;
import org.binas.ws.UserNotExists_Exception;
import org.binas.ws.UserView;

public class BinasClientApp {

    public static void main(String[] args) throws Exception {
        // Check arguments
        if (args.length == 0) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + BinasClientApp.class.getName()
                    + " wsURL OR uddiURL wsName");
            return;
        }
        String uddiURL = null;
        String wsName = null;
        String wsURL = null;
        if (args.length == 1) {
            wsURL = args[0];
        } else if (args.length >= 2) {
            uddiURL = args[0];
            wsName = args[1];
        }

		System.out.println(BinasClientApp.class.getSimpleName() + " running");

        // Create client
        BinasClient client = null;

        if (wsURL != null) {
            System.out.printf("Creating client for server at %s%n", wsURL);
            client = new BinasClient(wsURL);
        } else if (uddiURL != null) {
            System.out.printf("Creating client using UDDI at %s for server with name %s%n",
                uddiURL, wsName);
            client = new BinasClient(uddiURL, wsName);
        }


		 /***********************************
		    Fault Tolerance Demonstration
		    
		     F1 - Normal function
		     F2 - Fault tolerance
		  **********************************/
        
        
//		 String email = "username@domain";
//		 String stationID = "T08_Station1";
//		 int bonus = 5;
//		 
//		 client.testInitStation(stationID, 20, 40, 10, bonus);
//		 System.out.println(stationID + " initiallized with bonus " + bonus);
//		 
//		 
//		 /**
//		  * F1 - Creates user successfully
//		  * 
//		  * F2 - First execution: Creates user successfully
//		  *    - Second execution: User already exists (in remote replica manager)
//		  */
//		 try {
//			 client.testInit(20);
//			 
//			 UserView user = client.activateUser(email);
//			 System.out.println("User " + user.getEmail() + " sucessfully created with initial credit: " + user.getCredit());
//			 
//		 }catch(EmailExists_Exception eee) {
//			 System.out.println("Already exists user " + email + " with credit: " + client.getCredit(email));
//		 }
//		 
//		 
//		 client.rentBina(stationID, email);
//		 System.out.println("Bina rented from " + stationID);
//		 
//		 client.returnBina(stationID, email);
//		 System.out.println("Bina returned to " + stationID);
//		 
//		 
//		 System.out.println("User " + email + " current credit is " + client.getCredit(email));
//		 
        
        String pingResult = client.testPing("Hello friend!");
        System.out.println(pingResult);
		 
		 
		 
	 }
}

