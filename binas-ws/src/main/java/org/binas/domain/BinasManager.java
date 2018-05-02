package org.binas.domain;

import org.binas.domain.exception.*;
import org.binas.station.ws.*;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

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
	private int Q = 3;

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

	public void setQ(int q) {
		Q = q;
	}

	/**
	 * Checks if station ID matches Binas' Station ID format
	 * @param stationId
	 * @return true if matches, false otherwise
	 */
	private boolean isValidStationId(String stationId) {
		return stationId.matches("^" + getStationWSName() + "[a-zA-Z0-9]+$"); 
	}
	
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
     * Gets user from email
     * @param email
     * @return Found user
     * @throws UserNotExistsException if not found
     */
	public synchronized User getUserByEmail(String email) throws UserNotExistsException {
		return UserManager.getInstance().getUserByEmail(email);
	}
	
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
	 * Asks all stations for the balance value of user with given email
	 * @param email
	 * @return most recent credit and tag
	 * @throws UserNotExistsException
	 * @throws InvalidEmailException
	 */
	private User maxBalance(String email) throws UserNotExistsException, InvalidEmailException{
		System.out.println("maxBalance");
		List<BalanceView> userInfo = new ArrayList<>();
		try {
			List<StationClient> stations = getAvailableStations();
			for(StationClient client : stations) {
				userInfo.add(client.getBalance(email));
			}
		}catch(UserNotExists_Exception iee) {
		}
		
		if(!userInfo.isEmpty()) {
			// Searches for most recent balance			
			BalanceView balance = Collections.max(userInfo, new BalanceViewComparator());
			
			int credit = balance.getValue();
			int tag = balance.getTag();
			
			//Update user remote replicas
			writeback(email, credit, tag);
			
			// Update user cache
			return UserManager.getInstance().activateUser(email,credit, tag);
			
		}
		
		throw new UserNotExistsException();
		
		
	}
	
	/**
	 * Updates user replicas
	 * @param email
	 * @param credit
	 * @param tag
	 * @throws InvalidEmailException
	 */
	private void writeback(String email, int credit, int tag) throws InvalidEmailException {
		WriteBackHandler handler = new WriteBackHandler();
		List<StationClient> stations = getAvailableStations();
		ArrayList<Future<?>> futures = new ArrayList<>();
		int i;

		for(StationClient client : stations) {
			futures.add(client.setBalanceAsync(email, credit, tag, handler));
		}

		while (true) {
			i=0;
			for (Future<?> f : futures) {
				if (f.isDone()) {
					if ( ++i == getQ() ) {
						return;
					}
				}
			}
		}
	}
	
	
    /**
     * Activates user
     * @param email used to register the new user
     * @return new User
     * @throws InvalidEmailException
     * @throws EmailExistsException
     */
	public synchronized User activateUser(String email) throws InvalidEmailException, EmailExistsException {
		
		/* Testes */
//		try {
//			StationClient client = new StationClient(uddiURL, "T08_Station1");
//			client.setBalance(email,300,1);
//			
//			client = new StationClient(uddiURL, "T08_Station2");
//			client.setBalance(email,50,2);
//		} catch (Exception sce) {
//			
//		}
		/* Testes Fim */
		
		try {
			UserManager.getInstance().getUserByEmail(email);
			throw new EmailExistsException();
		}catch(UserNotExistsException unee) {
			//User not in cache
		}
		
		try {
			maxBalance(email);
			
			//User exists in remote replica manager
			throw new EmailExistsException();
			
		}catch(UserNotExistsException unee) {
			int credit = getUserInitialPoints();
			int tag = 0;
			
			//Updates remote replicas
			writeback(email, credit, tag);
			
			//Updates cache
			return UserManager.getInstance().activateUser(email,credit, tag);

		}
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
	 * 
	 * @param email
	 * @return credit of given user's email
	 * @throws UserNotExistsException
	 */
	public synchronized int getCredit(String email) throws UserNotExistsException {
		try {
			return getUserByEmail(email).getCredit();
		}catch(UserNotExistsException unee) {	
		}
		try {
			return maxBalance(email).getCredit();
		}catch(InvalidEmailException iee) {
		}
	
		return 0;
	}

	/**
	 * Rents a bina from a given stations
	 * @param stationId
	 * @param email
	 * @throws UserNotExistsException
	 * @throws InvalidStationException
	 * @throws NoBinaAvailException
	 * @throws AlreadyHasBinaException
	 * @throws NoCreditException
	 */
	public synchronized void rentBina(String stationId, String email) throws UserNotExistsException, InvalidStationException,
			NoBinaAvailException, AlreadyHasBinaException, NoCreditException {

		User user = getUserByEmail(email);
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
			user.setCredit(user.getCredit() - 1);
			user.setHasBina(true);

		} catch (NoBinaAvail_Exception nbae) {
			throw new NoBinaAvailException();

		} catch (StationClientException sce) {
			throw new InvalidStationException();
		}

	}

	/**
	 * Returns information for given station id in view form
	 * @param stationId
	 * @return StationView with station's information
	 * @throws InvalidStationException
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
	 * Returns given bina to the station if associated to proper email
	 * @param stationId
	 * @param email user's email
	 * @throws InvalidStationException
	 * @throws UserNotExistsException
	 * @throws NoSlotAvail_Exception
	 * @throws NoBinaRentedException
	 */
	public synchronized void returnBina(String stationId, String email)
			throws InvalidStationException, UserNotExistsException, NoSlotAvail_Exception, NoBinaRentedException {

		User user = getUserByEmail(email);
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

		} catch (StationClientException e) {
			throw new InvalidStationException();
		}
	}

    /**
     * Initializes station with given parameters
     * @param stationId target station
     * @param x
     * @param y
     * @param capacity
     * @param returnPrize
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

	/**
	 * Sorts Stations views by distance to given coordinate point
	 * @param stations
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


    class WriteBackHandler implements AsyncHandler<SetBalanceResponse> {
		@Override
		public void handleResponse(Response<SetBalanceResponse> res) {
			//   \( OwO )7  i'm useful
		}
	}
}
