package com.axlor.predictionassistantanalyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.List;

@Entity
public class Market {

	/*
	Why do we need marketUniqueID?:
		Let's say we persist snapshotA, it has a marketItem with an id given in the json, say id=111
		Let's say an hour later we persist snapshotB, it has new data so it gets persisted.
		The database attempts to persist marketItem with id=111 (that market is still open and still has that id=111)
		Now there are two marketItem objects with their primary key id=111 which throws an error.
		SnapshotA and SnapshotB each have a marketItem object with id=111 because that id represents the market that is open
				-it's url, name, etc
		The values in the contracts are different though so they both need to be persisted, but need different primary keys
		There is no good primary key we can use, so we generate one
	 */
	@Id
	@GeneratedValue
	private Integer marketUniqueID;

	@JsonProperty("timeStamp")
	private String timeStamp;

	@Transient
	@JsonProperty("image")
	private String image;

	@JsonProperty("name")
	private String name;

	@JsonProperty("id")
	private int id;

	@Transient
	@JsonProperty("shortName")
	private String shortName;

	@ElementCollection(fetch=FetchType.EAGER)
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER) //ALL, not persist
	@JsonProperty("contracts")
	private List<Contract> contracts;

	@JsonProperty("url")
	private String url;

	@JsonProperty("status")
	private String status;

	public Market(String timeStamp, String image, String name, int id, String shortName, List<Contract> contracts, String url, String status) {
		this.timeStamp = timeStamp;
		this.image = image;
		this.name = name;
		this.id = id;
		this.shortName = shortName;
		this.contracts = contracts;
		this.url = url;
		this.status = status;
	}

	public Market() {
	}

	public Integer getMarketUniqueID() {
		return marketUniqueID;
	}

	public void setMarketUniqueID(Integer marketUniqueID) {
		this.marketUniqueID = marketUniqueID;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public List<Contract> getContracts() {
		return contracts;
	}

	public void setContracts(List<Contract> contracts) {
		this.contracts = contracts;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "\n\nMarket{" +
				"marketUniqueID='" + marketUniqueID + '\'' +
				", timeStamp='" + timeStamp + '\'' +
				", image='" + image + '\'' +
				", name='" + name + '\'' +
				", id=" + id +
				", shortName='" + shortName + '\'' +
				", contracts=" + contracts +
				", url='" + url + '\'' +
				", status='" + status + '\'' +
				"\n}";
	}
}
