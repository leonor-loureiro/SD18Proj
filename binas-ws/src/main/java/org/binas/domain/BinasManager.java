package org.binas.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.binas.domain.exception.AlreadyHasBinaException;
import org.binas.domain.exception.EmailExistsException;
import org.binas.domain.exception.InvalidEmailException;
import org.binas.domain.exception.InvalidStationException;
import org.binas.domain.exception.NoBinaAvailException;
import org.binas.domain.exception.NoBinaRentedException;
import org.binas.domain.exception.NoCreditException;
import org.binas.domain.exception.UserNotExistsException;
import org.binas.station.ws.*;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

/**
 * Domain Root of Binas servers
 * 
 * Handles connection and information of the Binas' System
 * Manages connection to stations and requests from clients
 * @author T08
 *
 */
public class BinasManager {

	/* Binas Manager ID */
	private String id;

	/* UDDI url */
	private String uddiURL;

	/* Station name org template */
	private String stationWSName;
	
	/* Quorum */
	private int Q;

	// Singleton -------------------------------------------------------------


	private BinasManager() {
	}

	/**
	 * SingletonHolder is loaded on the first execution of Singleton.getInstance()
	 * or the first access to SingletonHolder.INSTANCE, not before.
	 */
	private static class SingletonHolder {
		private static final BinasManager INSTANCE = new BinasManager();
	}

	public static synchronized BinasManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
/////////////////////// GETTERS/SETTERS /////////////////////////////

	public void setId(String id) {
		this.id = id;
	}

	public String getId(String id) {
		return this.id;
	}

	public void setUserInitialPoints(int userInitialPoints) {
		UserManager.getInstance().setUserInitialPoints(userInitialPoints);
	}
	
	public int getUserInitialPoints() {
		return UserManager.getInstance().getUserInitialPoints();
	}

	public void setUddiUrl(String uddiUrl) {
		this.uddiURL = uddiUrl;
	}

	public String getUddiUrl() {
		return uddiURL;
	}

	public void setStationWSName(String stationWSName) {
		this.stationWSName = stationWSName;
	}

	public String getStationWSName() {
		return this.stationWSName;
	}

	public int getQ() {
		return Q;
	}

	/**
	 * calculates Q based on the number of stations
	 * @param nStations
	 */
	public void setQ(int nStations) {
		Q = (int) Math.floor(nStations / 2) + 1; // such math wow
	}

//////////////////// BINAS OPERATIONS //////////////////////////
	
	/**
	 * Lists n stations closest to coordinates in crescent order. Connects to UDDI
	 * to get all stations and then filters.
	 * 
	 * @param n number of stations to return
	 * @param coords client position
	 * @return List of StationView's
	 * @throws StationClientException
	 */
	public List<StationView> listStations(int n, CoordinatesView coords) throws StationClientException {
		if (coords.getX() < 0 || coords.getY() < 0)
			return new ArrayList<>();
		List<StationView> stations = new ArrayList<>();
		List<StationClient> as = getAvailableStations();

		for (StationClient station : as)
			stations.add(station.getInfo());
		
		stations = sortStationViewsByDistance(stations, coords);
		
		if( n > stations.size())
			n = stations.size();
		if (n < 0)
			n = 0;
		
		return stations.subList(0, n);
	}

	
    /**
     * Activates user
     * @param email used to register the new user
     * @return new User
     * @throws InvalidEmailException
     * @throws EmailExistsException
     */
	public synchronized User activateUser(String email) throws InvalidEmailException, EmailExistsException {
				
		try {
			UserManager.getInstance().getUserByEmail(email);
			throw new EmailExistsException();
		}catch(UserNotExistsException unee) {
			//User not in cache
		}
		
		try {
			readFromReplicas(email, true);
			
			//User exists in remote replica manager
			throw new EmailExistsException();
			
		}catch(UserNotExistsException unee) {
			int credit = getUserInitialPoints();
			int tag = 0;
			
			//Updates remote replicas
			writeToReplicas(email, credit, tag);
			
			//Updates cache
			return UserManager.getInstance().activateUser(email,credit, tag);

		}
	}



	/**
	 * finds credit of given user
	 * @param email user's email
	 * @return credit of given user
	 * @throws UserNotExistsException when the user does not exist locally nor in replicas
	 */
	public synchronized int getCredit(String email) throws UserNotExistsException {
		try {
			return getUserByEmail(email).getCredit();
		}catch(UserNotExistsException unee) {	
		}
		try {
			return readFromReplicas(email, true).getCredit();
		}catch(InvalidEmailException iee) {
			throw new UserNotExistsException();
		}
	}

	/**
	 * Rents a bina from a given stations if user has enough credit for the rent
	 * @param stationId station's identifier
	 * @param email user's identifier
	 * @throws UserNotExistsException no user was found with given email
	 * @throws InvalidStationException the station doesn't exist or wasn't available
	 * @throws NoBinaAvailException the station doesn't have a bina available to be rented
	 * @throws AlreadyHasBinaException the user already has a bina
	 * @throws NoCreditException the user doesnt not have enough credit to rent
	 */
	public synchronized void rentBina(String stationId, String email) throws UserNotExistsException, InvalidStationException,
			NoBinaAvailException, AlreadyHasBinaException, NoCreditException {

		User user = null;
		try{
			//checks if user is in cache
			user = getUserByEmail(email);
			
		}catch(UserNotExistsException unee) {
			try{
				//checks if exists remote replica of user
				user = readFromReplicas(email, false);
				
			}catch(InvalidEmailException iee) {
			}
		}
		
		if (user.getHasBina()) {
			throw new AlreadyHasBinaException();
		}
		if (user.getCredit() < 1) {
			throw new NoCreditException();
		}
		if(stationId == null || !isValidStationId(stationId))
			throw new InvalidStationException();
		
		try {
			StationClient client = new StationClient(uddiURL, stationId);
			client.getBina();
			user.removeOneCredit();
			user.setHasBina(true);
			user.updateTag();

			//Update remote replicas of user
			try {
				writeToReplicas(email, user.getCredit(), user.getTag()+1);
			}catch(InvalidEmailException iee) {
			}
			
		} catch (NoBinaAvail_Exception nbae) {
			throw new NoBinaAvailException();

		} catch (StationClientException sce) {
			throw new InvalidStationException();
		}

	}

	/**
	 * Returns information for given station id in view form
	 * @param stationId station's identifier
	 * @return StationView with station's information
	 * @throws InvalidStationException station does not exist or isn't available
	 */
	public StationView getInfoStation(String stationId) throws InvalidStationException {
		
		if(stationId == null || !isValidStationId(stationId))
			throw new InvalidStationException();
		
		try {
			StationClient client = new StationClient(uddiURL, stationId);
			return client.getInfo();
		} catch (StationClientException sce) {
			throw new InvalidStationException();
		}
	}

	/**
	 * Returns given bina to the station by a certain user
	 * @param stationId station's identifier
	 * @param email user's email
	 * @throws InvalidStationException when station does not exist or isn't available
	 * @throws UserNotExistsException when there was no user found with given email
	 * @throws NoSlotAvail_Exception when there are no slots available at station for bina to be returned
	 * @throws NoBinaRentedException when user does not have any bina rented
	 */
	public synchronized void returnBina(String stationId, String email)
			throws InvalidStationException, UserNotExistsException, NoSlotAvail_Exception, NoBinaRentedException {

		User user = null;
		try{
			//checks if user is in cache
			user = getUserByEmail(email);
			
		}catch(UserNotExistsException unee) {
			try{
				//checks if exists remote replica of user
				user = readFromReplicas(email, false);
				
			}catch(InvalidEmailException iee) {
			}
		}

		if (!user.getHasBina()) {
			throw new NoBinaRentedException();
		}

		if(stationId == null || !isValidStationId(stationId))
			throw new InvalidStationException();
		
		try {
			StationClient client = new StationClient(uddiURL, stationId);
			int bonus = client.returnBina();
			user.setHasBina(false);
			user.receiveBonus(bonus);
			user.updateTag();
			
			//Update remote replicas of user
			try {
				writeToReplicas(email, user.getCredit(), user.getTag() + 1);
			}catch(InvalidEmailException iee) {
			}

		} catch (StationClientException e) {
			throw new InvalidStationException();
		}
	}

////////////////// BINAS TEST OPERATIONS ////////////////////
	
    /**
     * Pings all stations
     * @param wsName pingerID
     * @return concatenated result
     */
    public String testPing(String wsName) {
        return getAvailableStations()
                .stream()
                .map(station -> station.testPing(wsName))
                .collect(Collectors.joining("\n", "", "\n"));
    }
	
	/**
	 * clears all stations and user information
	 */
	public synchronized void clear() {
		UserManager.getInstance().clear();
		for (StationClient station : getAvailableStations())
			station.testClear();
		
	}
	
	/**
	 * clears all user information saved locally in Binas
	 */
	public synchronized void clearCache() {
		UserManager.getInstance().clear();
	}
		
	
	
	
    /**
     * Initializes station with given parameters
     * @param stationId target station
     * @param x x coordinate
     * @param y y coordinate
     * @param capacity station's capacity
     * @param returnPrize station's prize for returning a bina
     * @throws StationClientException
     * @throws Exception
     */
	public void initStation(String stationId, int x, int y, int capacity, int returnPrize)
			throws StationClientException, Exception {
		try {
			StationClient client = new StationClient(uddiURL, stationId);
			client.testInit(x, y, capacity, returnPrize);
		} catch (StationClientException e) {
			throw new InvalidStationException();
		} catch (BadInit_Exception e) {
			throw new Exception();
		}
	}
	
////////////////////////// AUX /////////////////////////////////
	
	/**
	 * Checks if station ID matches Binas' Station ID format
	 * @param stationId station's identifier
	 * @return true if matches, false otherwise
	 */
	private boolean isValidStationId(String stationId) {
		return stationId.matches("^" + getStationWSName() + "[a-zA-Z0-9]+$"); 
	}
	


    /**
     * Gets user from email
     * @param email user's identifier
     * @return Found user
     * @throws UserNotExistsException if not found
     */
	public synchronized User getUserByEmail(String email) throws UserNotExistsException {
		return UserManager.getInstance().getUserByEmail(email);
	}
	

	/**
	 * @return Available Stations' connection
	 */
	private List<StationClient> getAvailableStations() {
		List<StationClient> clients = new ArrayList<>();
		try {
			UDDINaming uddi = new UDDINaming(uddiURL);
			Collection<UDDIRecord> uddiRecords = uddi.listRecords(stationWSName + "%%%%%%%%%%%");

			for (UDDIRecord record : uddiRecords)
				clients.add(new StationClient(record.getUrl()));
			
		} catch (StationClientException | UDDINamingException e) {
			System.out.println("No stations available or found.");
		}
		return clients;
	}

	
///////////////////// REMOTE REPLICAS READ/WRITE //////////////////////////////

	/**
	 * Asks all stations for the balance value of user with given email
	 * @param email user's identifier
	 * @param read
	 * 		  true: if called by read operation (w/ writeToReplicas phase)
	 * 		  false: if called by write operation (n/ writeToReplicas phase)
	 * @return user with most recent credit and tag
	 * @throws UserNotExistsException no user was found with given email
	 * @throws InvalidEmailException the email given isn't valid
	 */
	private User readFromReplicas(String email, boolean read) throws UserNotExistsException, InvalidEmailException{
		List<BalanceView> userInfo;
		List<Future<?> > futures = new ArrayList<>();
		ReadFromReplicasCallBackHandler handler = new ReadFromReplicasCallBackHandler(); //callback handler
		List<StationClient> stations = getAvailableStations();

		// adds a callback for each replica
		for(StationClient client : stations) {
			futures.add(client.getBalanceAsync(email, handler));
		}

		// wait for all quorum responses
		int completed;
		while(true){
			completed = 0;
			for ( Future<?> fu : futures)
				if( fu.isDone())
					completed++;

			if(completed >= getQ()){
				break;
			}else{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// sleep interrupted is not a relevant error in this context
				}
			}
		}

		// convert GetBalanceResponse to Balance view list
		userInfo = handler.getResponses().stream().map(GetBalanceResponse::getBalance).collect(Collectors.toList());

		if(!userInfo.isEmpty()) {
			// Searches for most recent balance through tag
			BalanceView balance = Collections.max(userInfo, new BalanceViewComparator());
			
			int credit = balance.getValue();
			int tag = balance.getTag();
			
			//Writeback phase requested
			if(read) { 
				boolean doWriteback = false;
				// Checks if remote replicas not all up to date
				for(BalanceView b : userInfo) {
					if(b.getTag() != balance.getTag()) {
						doWriteback = true;
						break;
					}
				}
				if(doWriteback) {
					writeToReplicas(email, credit, tag); //Update user remote replicas
				}
			}
			return UserManager.getInstance().activateUser(email,credit, tag); 	// Update user cache
		}
		// if handler's response is empty, means all responses had userNotExistsException
		throw new UserNotExistsException();
	}
	
	/**
	 * Updates local users' info with information found in replicas
	 * @param email user's identifier
	 * @param credit user's balance
	 * @param tag version of user's edit
	 * @throws InvalidEmailException the email doesn't not match a valid format
	 */
	private void writeToReplicas(String email, int credit, int tag) throws InvalidEmailException {
		WriteToReplicasHandler handler = new WriteToReplicasHandler();
		List<StationClient> stations = getAvailableStations();
		ArrayList<Future<?>> futures = new ArrayList<>();
		int responses, errors;

		for(StationClient client : stations) {
			futures.add(client.setBalanceAsync(email, credit, tag, handler));
		}

		while (true) {
			responses=0; // number of responses completed
			errors=0; // number of errors
			for (Future<?> f : futures) {
				if (f.isDone()) {
					// check for errors
					try {
						f.get();
                        responses++;
                    } catch (InterruptedException | ExecutionException e1) {
                        errors++;
                    }
					// check error quorum
					if (errors == getQ()) {
					    throw new InvalidEmailException();
                    }
					// check response quorum
					if ( responses == getQ() ) {
						return;
					}
				}
			}
		}
	}
	
////////////////////////// COMPARATORS ////////////////////////////////
	/**
	 * Sorts Stations views by distance to given coordinate point
	 * @param stations list of stations
	 * @param coord coordinate point
	 * @return sorted List
	 */
	private List<StationView> sortStationViewsByDistance(List<StationView> stations, CoordinatesView coord){
		Collections.sort(stations, new StationComparator(coord));
		return stations;
	}

	/**
	 * Comparator class for comparing views based on its distance to a given point
	 *
	 */
	private class StationComparator implements Comparator<StationView> {
        private double x, y;

        private StationComparator(CoordinatesView cv) {
            this.x = cv.getX();
            this.y = cv.getY();
        }

        @Override
        public int compare(StationView s1, StationView s2) {
            CoordinatesView c1 = s1.getCoordinate();
            CoordinatesView c2 = s2.getCoordinate();
            double dist1 = Math.pow(c1.getX() - this.x, 2) + Math.pow(c1.getY() - this.y, 2);
            double dist2 = Math.pow(c2.getX() - this.x, 2) + Math.pow(c2.getY() - this.y, 2);
            return (int) (dist1 - dist2);
        }
    }
	
	
	/**
	 * Comparator class for comparing balance views based on its tag
	 *
	 */
	private class BalanceViewComparator implements Comparator<BalanceView> {
        @Override
        public int compare(BalanceView b1, BalanceView b2) {
            return (b1.getTag() - b2.getTag());
        }
    }
	
	
	
////////////////////////////// CALLBACK HANDLERS //////////////////////////////////////

	/**
	 * <joke>
	 * You may think you know what the following code does.
	 * But you dont. Trust me.
	 * Fiddle with it, and youll spend many a sleepless
	 * night cursing the moment you thought youd be clever
	 * enough to "optimize" the code below.
	 * Now close this file and go play with something else.
	 * </joke>
	 * Function handles the callbacks from writes
	 */
    class WriteToReplicasHandler implements AsyncHandler<SetBalanceResponse> {
		@Override
		public void handleResponse(Response<SetBalanceResponse> res) {
			//   \( OwO )7  i'm useful
		}
	}


	/**
	 * Class used to handle callback funtions in readFromReplicas method
	 */
	class ReadFromReplicasCallBackHandler implements AsyncHandler<GetBalanceResponse>{
		private ArrayList<GetBalanceResponse> resultList = new ArrayList<>(Q);

		/**
		 *
		 * @param response the values of all callback functions so far.
		 */
		@Override
		public synchronized void handleResponse(Response<GetBalanceResponse> response){
			try{
				resultList.add(response.get());
			} catch (InterruptedException e) {
				System.out.println("Caught interrupted exception.");
				System.out.print("Cause: ");
				System.out.println(e.getCause());
			} catch (ExecutionException e) {
				System.out.println("Caught execution exception.");
				System.out.print("Cause: ");
				System.out.println(e.getCause());
			}
		}

		/**
		 * @return the values of all callback functions that executed so far within this handler
		 */
		public synchronized List<GetBalanceResponse> getResponses(){
			return resultList;
		}
	}

}
