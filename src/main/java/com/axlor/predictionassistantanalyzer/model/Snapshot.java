package com.axlor.predictionassistantanalyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.List;

@Entity
public class Snapshot{

	@Id
	private Integer hashId;

	@JsonProperty("markets")
	@ElementCollection(fetch=FetchType.EAGER)
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER) //ALL, not persist
	private List<Market> markets;

	private long timestamp;

	public Snapshot(List<Market> markets, long timestamp, Integer hashId) {
		this.markets = markets;
		this.timestamp = timestamp;
		this.hashId = hashId;
	}

	public Snapshot() {
	}

	public void setMarkets(List<Market> markets){
		this.markets = markets;
	}

	public List<Market> getMarkets(){
		return markets;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Integer getHashId() {
		return hashId;
	}

	public void setHashId(Integer hashId) {
		this.hashId = hashId;
	}

	@Override
	public String toString() {
		return "Snapshot{" +
				"hashId=" + hashId +
				", markets=" + markets +
				", timestamp=" + timestamp +
				'}';
	}
}
