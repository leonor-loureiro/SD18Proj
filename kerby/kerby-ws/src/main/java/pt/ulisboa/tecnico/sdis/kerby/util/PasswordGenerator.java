package pt.ulisboa.tecnico.sdis.kerby.util;


import java.security.SecureRandom;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;


public class PasswordGenerator {
	private static final SecureRandom randomGenerator = new SecureRandom();
	/** A String containing all possible characters that can be used to create a password. */
	private static final String possibleChars = "23456789ABCDEFGHJKLMNOPRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
	/** Minimum password length. */
	private static final int minLength = 6;
	/** Maximum password length. */
	private static final int maxLength = 10;
	private static final int groupsPerCampus = 1;
	private static final String[] campi = {"A", "T"};
	
	private static final String[] defaultUsers = {"alice", "binas", "charlie", "eve"};
	private static final String domain = "binas.org";
	private static final String headerWarning = "# test users only -- do NOT store actual user passwords on GitHub";
	
	
	/** Creates passwords for all default users in all groups of all campi. 
	 * To generate passwords a SecureRandom Generator is used. 
	 * Creates one file per group with default users and passwords a single master file containing all users and passwords */
	public static void generate() throws Exception {
		BufferedWriter fullWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("All-secrets.txt"), "utf-8"));
		int lengthDiff = maxLength - minLength;
		String groupNumber;
		
		for(String campus: campi) {
			for(int i = 1; i <= groupsPerCampus; i++) {
				if(i < 10)
					groupNumber = "0" + i;
				else
					groupNumber = "" + i;
				String filename = campus + groupNumber + "-secrets.txt";
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
				writer.write(headerWarning);
				writer.newLine();
				for(String defaultUser: defaultUsers) {
					int length = minLength + randomGenerator.nextInt(lengthDiff);
					String password = "";
					for(int j = 0; j < length; j++) {
						int index = randomGenerator.nextInt(possibleChars.length());
						password += possibleChars.charAt(index);
					}
					writer.write(defaultUser + "@" + campus + groupNumber + "." + domain + "," + password);
					fullWriter.write(defaultUser + "@" + campus + groupNumber + "." + domain + "," + password);
					writer.newLine();
					fullWriter.newLine();
				}
				writer.close();
			}
		}
		fullWriter.close();
		
	}
}