package org.binas.domain;

public class Station {
	
	/** Station identifier. */
	private String id;
	/** Station location coordinate. */
	private Coordinates coordinate;
	/** Station capacity. */
	private int capacity;
	/** Station counter of Binas Gets. */
    private int totalGets;
    /** Station counter of Binas Returns. */
    private int totalReturns;
    /** Station number of available Binas. */
    private int availableBinas;
    /** Station current number of free docks. */
    private int freeDocks;

    public Station (String id, Coordinates coordinate, int capacity, int totalGets, int totalReturns, int availableBinas, int freeDocks) {
    	this.id = id;
    	this.coordinate = coordinate;
    	this.capacity = capacity;
    	this.totalGets = totalGets;
    	this.totalReturns = totalReturns;
    	this.availableBinas = availableBinas;
    	this.freeDocks = freeDocks;
    }

 		
 	public void setId(String id) {
 		this.id = id;
 	}

 	public void setCapacity (int capacity) {
 		this.capacity = capacity;
 	}
 	
 	public void setCoordinate (Coordinates coordinate) {
 		this.coordinate = coordinate;
 	}
 	
 	public void setTotalGets (int totalGets) {
 		this.totalGets = totalGets;
 	}
 	
 	public void setTotalReturns (int totalReturns) {
 		this.totalReturns = totalReturns;
 	}
 	
 	public void setAvailableBinas (int availableBinas) {
 		this.availableBinas = availableBinas;
 	}
 	public void setFreeDocks (int freeDocks) {
 		this.freeDocks = freeDocks;
 	}
 	
 	// Getters -------------------------------------------------------------
 	
 	
    public String getId() {
    	return id;
    }
    
	public Coordinates getCoordinate() {
    	return coordinate;
    }
        
	public int getCapacity() {
		return capacity;
	}
    public int getTotalGets() {
    	return totalGets;
    }

    public int getTotalReturns() {
    	return totalReturns;
    }

    public int getAvailableBinas() {
    	return availableBinas;
    }
    
    public int getFreeDocks() {
    	return freeDocks;
    }
    

    	
}
