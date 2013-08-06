package com.paremus.demo.redis;

import java.util.Map;

public class Snapshot {
	
	public static enum Availability {
		NONE, READ, WRITE;
	}

	private final Snapshot.Availability availability;
	private final String fromURI;
	private final Map<String, Double> stockPrices;

	public Snapshot(Snapshot.Availability availability, String fromURI, Map<String, Double> stockPrices) {
		super();
		this.availability = availability;
		this.fromURI = fromURI;
		this.stockPrices = stockPrices;
	}

	public Snapshot.Availability getAvailability() {
		return availability;
	}

	public String getFromURI() {
		return fromURI;
	}
	
	public Map<String, Double> getStockPrices() {
		return stockPrices;
	}
}