package org.binas.ws.cli;

import org.binas.ws.UserView;

import com.sun.xml.ws.fault.ServerSOAPFaultException;

import binas.ws.handler.BinasServerHandler;
import binas.ws.handler.KerberosClientHandler;
import binas.ws.handler.MACHandler;
import binas.ws.handler.PrettyLogHandler;

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
        String fase = null;
      
        uddiURL = args[0];
        wsName = args[1];
        fase = args[2];
        
        if(args[3].equals("no"))
        	PrettyLogHandler.setShowLog(false);
        
        
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


        
        /*******************************
	    	Security Demonstration
	    
	     	F1 - Normal function
	     	F2 - Resistance to attack
	  **********************************/
        
        final String stationID = "T08_Station1";
        
        // Users login info
    	final String username1 = "alice@T08.binas.org";
    	final String user1Password = "WxzsYKnJn";
    	
    	final String username2 = "charlie@T08.binas.org";
    	
    	// Server login info
    	final String servername = "binas@T08.binas.org";
    	final String serverPassword = "t5h9O9B2";
    	
    	// Set user, server and attackMode
    	KerberosClientHandler.setUser(username1, user1Password);
    	KerberosClientHandler.setServer(servername);
    	BinasServerHandler.setServer(servername, serverPassword);
    	
    	if(fase.equals("F1")) {
    		
	    	KerberosClientHandler.setAttackMode(KerberosClientHandler.NO_ATTACK);
	    	MACHandler.setAttackMode(MACHandler.NO_ATTACK);
	    	
	    	// Set new users initial credit
	    	 client.testInit(20);
	    	
	    	// Activate user 
	    	UserView user = client.activateUser("alice@T08.binas.org");
	        System.out.println("User " + user.getEmail() + " sucessfully created.\n\n");
	        
	        // Alter user state
	        client.rentBina(stationID, username1);
	        System.out.println("Bina rented from " + stationID + "\n\n");
	        
	        // Protected read
	        int credit = client.getCredit(username1);
	        System.out.println("User " + user.getEmail() + " with credit " + credit+ "\n\n");
	
	        
	        // Operation that does not alter the state of any user
	        String pingResult = client.testPing("Hello friend!");
	        System.out.println(pingResult);
    	}
        
    	else {
    		
	        /**
	         * REPLAY ATTACK
	         */
	        System.out.println("\n====================  REPLAY ATTACK  ======================\n");
	        KerberosClientHandler.setAttackMode(KerberosClientHandler.REPLAY_ATTACK);
	        try {
	        	client.getCredit(username1);     	
	        }catch(ServerSOAPFaultException e) {
	        	System.out.println("\nReplay attack prevented.\n");
	        	System.out.println(e.getMessage() + "\n");
	        }
	        
	        /**
	         * INTEGRITY ATTACK 
	         */
	        
	        System.out.println("\n====================  INTEGRITY ATTACK ======================\n");
	        KerberosClientHandler.setAttackMode(KerberosClientHandler.NO_ATTACK);
	        MACHandler.setAttackMode(MACHandler.CORRUPT_CONTENT);
	        
	        try {
	        	client.getCredit(username1);     	
	        }catch(ServerSOAPFaultException e) {
	        	System.out.println("\nIntegrity attack prevented.\n");
	        	System.out.println(e.getMessage() + "\n");
	        }
	        
	        /**
	         * USER WITHOUT PERMISSIONS FOR THE OPERATION
	         */
	        
	        System.out.println("\n====================  USER WITHOUT PERMISSIONS FOR THE OPERATION ======================\n");
	        KerberosClientHandler.setAttackMode(KerberosClientHandler.NO_ATTACK);
	        MACHandler.setAttackMode(MACHandler.NO_ATTACK);
	        
	        try {
	        	client.getCredit(username2);     	
	        }catch(ServerSOAPFaultException e) {
	        	System.out.println("\nUser stoped from reading info of another user.\n");
	        	System.out.println(e.getMessage() + "\n");
	        }
	        
	        try {
	        	client.rentBina(stationID, username2);     	
	        }catch(ServerSOAPFaultException e) {
	        	System.out.println("\nUser stoped from altering state of another user.\n");
	        	System.out.println(e.getMessage() + "\n");
	        }
    	}
        
        
    	
        
    	
	 }
}

