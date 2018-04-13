package org.binas.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.binas.domain.exception.AlreadyHasBinaException;
import org.binas.domain.exception.EmailExistsException;
import org.binas.domain.exception.InvalidEmailException;
import org.binas.domain.exception.InvalidStationException;
import org.binas.domain.exception.NoBinaAvailException;
import org.binas.domain.exception.NoBinaRentedException;
import org.binas.domain.exception.NoCreditException;
import org.binas.domain.exception.UserNotExistsException;
import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.CoordinatesView;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.binas.station.ws.StationView;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

public class BinasManager {

	/* Binas Manager ID */
	private String id;

	/* UDDI url */
	private String uddiURL;

	/* Binas users */
	private Set<User> users = new HashSet<>();

	/* Station name org template */
	private String stationWSName;

	/* New user initial credit */
	private int userInitialPoints = 0;

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

	public synchronized void setUserInitialPoints(int userInitialPoints) {
		this.userInitialPoints = userInitialPoints;
	}

	public synchronized int getUserInitialPoints() {
		return this.userInitialPoints;
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
		for (User user : users) {
			if (user.getEmail().equals(email))
				return user;
		}
		throw new UserNotExistsException();
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
		List<StationView> clients = new ArrayList<>();

		for (StationClient station : getAvailableStations())
			clients.add(station.getInfo());
		
		clients = sortStationViewsByDistance(clients, coords);
		
		if( n > clients.size())
			n = clients.size() - 1;
		
		return clients.subList(0, n);
	}

    /**
     * Activates user
     * @param email used to register the new user
     * @return new User
     * @throws InvalidEmailException
     * @throws EmailExistsException
     */
	public synchronized User activateUser(String email) throws InvalidEmailException, EmailExistsException {
		if (email == null || !email.matches("^([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+@([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+$")) {
			throw new InvalidEmailException();
		}
		try {
			getUserByEmail(email);

		} catch (UserNotExistsException unee) {
			User user = new User(email, false, userInitialPoints);
			users.add(user);
			return user;
		}
		throw new EmailExistsException();
	}

	public synchronized void clearUsers() {
		users.clear();
	}

	public synchronized int getCredit(String email) throws UserNotExistsException {
		return getUserByEmail(email).getCredit();
	}

	public synchronized void rentBina(String stationId, String email) throws UserNotExistsException, InvalidStationException,
			NoBinaAvailException, AlreadyHasBinaException, NoCreditException {

		User user = getUserByEmail(email);
		if (user.getHasBina()) {
			throw new AlreadyHasBinaException();
		}
		if (user.getCredit() < 0) {
			throw new NoCreditException();
		}
		try {
			StationClient client = new StationClient(uddiURL, stationId);
			client.getBina();
			user.setHasBina(true);

		} catch (NoBinaAvail_Exception nbae) {
			throw new NoBinaAvailException();

		} catch (StationClientException sce) {
			throw new InvalidStationException();
		}

	}

	public StationView getInfoStation(String stationId) throws InvalidStationException {
		try {
			StationClient client = new StationClient(uddiURL, stationId);
			return client.getInfo();
		} catch (StationClientException sce) {
			throw new InvalidStationException();
		}
	}

	public synchronized void returnBina(String stationId, String email)
			throws InvalidStationException, UserNotExistsException, NoSlotAvail_Exception, NoBinaRentedException {

		User user = getUserByEmail(email);
		if (!user.getHasBina()) {
			throw new NoBinaRentedException();
		}

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
     * Initializes / resets a station.
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

	private List<StationView> sortStationViewsByDistance(List<StationView> stations, CoordinatesView coord){
		Collections.sort(stations, new StationComparator(coord));
		return stations;
	}

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
            double dist1 = Math.pow(c1.getX() - this.x, 2) + Math.pow(c2.getY() - this.y, 2);
            double dist2 = Math.pow(c2.getX() - this.x, 2) + Math.pow(c2.getY() - this.y, 2);
            return (int) (dist1 - dist2);
        }
    }
}
