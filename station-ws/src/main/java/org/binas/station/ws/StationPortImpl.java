package org.binas.station.ws;

/**
 * This class implements the Web Service port type (interface). The annotations
 * below "map" the Java class to the WSDL definitions.
 */
// TODO
// @WebService(endpointInterface = "org.binas.station.ws.StationPortType",
// wsdlLocation = "...",
// name ="StationWebService",
// portName = "...Port",
// targetNamespace="...",
// serviceName = "...Service"
// )
public class StationPortImpl { // implements StationPortType {

	/**
	 * The Endpoint manager controls the Web Service instance during its whole
	 * lifecycle.
	 */
	private StationEndpointManager endpointManager;

	/** Constructor receives a reference to the endpoint manager. */
	public StationPortImpl(StationEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
	}

	// Main operations -------------------------------------------------------

	// /** Retrieve information about station. */
	// @Override
	// public StationView getInfo() {
	// // TODO
	// return null;
	// }
	//
	// /** Return a bike to the station. */
	// @Override
	// public int returnBina() throws NoSlotAvail_Exception {
	// // TODO
	// return -1;
	// }
	//
	// /** Take a bike from the station. */
	// @Override
	// public void getBina() throws NoBinaAvail_Exception {
	// // TODO
	// }

	// Test Control operations -----------------------------------------------

	// /** Diagnostic operation to check if service is running. */
	// @Override
	// public String testPing(String inputMessage) {
	// // If no input is received, return a default name.
	// if (inputMessage == null || inputMessage.trim().length() == 0)
	// inputMessage = "friend";
	//
	// // If the station does not have a name, return a default.
	// String wsName = endpointManager.getWsName();
	// if (wsName == null || wsName.trim().length() == 0)
	// wsName = "Station";
	//
	// // Build a string with a message to return.
	// StringBuilder builder = new StringBuilder();
	// builder.append("Hello ").append(inputMessage);
	// builder.append(" from ").append(wsName);
	// return builder.toString();
	// }
	//
	// /** Return all station variables to default values. */
	// @Override
	// public void testClear() {
	// Station.getInstance().reset();
	// }
	//
	// /** Set station variables with specific values. */
	// @Override
	// public void testInit(int x, int y, int capacity, int returnPrize) throws
	// BadInit_Exception {
	// try {
	// Station.getInstance().init(x, y, capacity, returnPrize);
	// } catch (BadInitException e) {
	// throwBadInit("Invalid initialization values!");
	// }
	// }

	// View helpers ----------------------------------------------------------

	// /** Helper to convert a domain station to a view. */
	// private StationView buildStationView(Station station) {
	// StationView view = new StationView();
	// view.setId(station.getId());
	// view.setCoordinate(buildCoordinatesView(station.getCoordinates()));
	// view.setCapacity(station.getMaxCapacity());
	// view.setTotalGets(station.getTotalGets());
	// view.setTotalReturns(station.getTotalReturns());
	// view.setFreeDocks(station.getFreeDocks());
	// view.setAvailableBinas(station.getAvailableBinas());
	// return view;
	// }
	//
	// /** Helper to convert a domain coordinates to a view. */
	// private CoordinatesView buildCoordinatesView(Coordinates coordinates) {
	// CoordinatesView view = new CoordinatesView();
	// view.setX(coordinates.getX());
	// view.setY(coordinates.getY());
	// return view;
	// }

	// Exception helpers -----------------------------------------------------

	// /** Helper to throw a new NoBinaAvail exception. */
	// private void throwNoBinaAvail(final String message) throws
	// NoBinaAvail_Exception {
	// NoBinaAvail faultInfo = new NoBinaAvail();
	// faultInfo.message = message;
	// throw new NoBinaAvail_Exception(message, faultInfo);
	// }
	//
	// /** Helper to throw a new NoSlotAvail exception. */
	// private void throwNoSlotAvail(final String message) throws
	// NoSlotAvail_Exception {
	// NoSlotAvail faultInfo = new NoSlotAvail();
	// faultInfo.message = message;
	// throw new NoSlotAvail_Exception(message, faultInfo);
	// }
	//
	// /** Helper to throw a new BadInit exception. */
	// private void throwBadInit(final String message) throws BadInit_Exception {
	// BadInit faultInfo = new BadInit();
	// faultInfo.message = message;
	// throw new BadInit_Exception(message, faultInfo);
	// }

}
