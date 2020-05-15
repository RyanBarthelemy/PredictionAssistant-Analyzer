package com.axlor.predictionassistantanalyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class Contract {

	/*
Why do we need contractUniqueID?:
	See explanation in Market.java
	Basically, the JsonProperty 'id' field cannot be used as a primary key in the database. We need to create one, so we generate it here.
 */
	@Id
	@GeneratedValue
	private Integer contractUniqueID;

	@Transient
	@JsonProperty("image")
	private String image;

	@JsonProperty("lastClosePrice")
	private double lastClosePrice;

	@JsonProperty("bestBuyNoCost")
	private double bestBuyNoCost;

	@JsonProperty("bestSellNoCost")
	private double bestSellNoCost;

	@Transient
	@JsonProperty("displayOrder")
	private int displayOrder; //not sure what this even is supposed to do lol

	@JsonProperty("dateEnd")
	private String dateEnd;

	@JsonProperty("bestSellYesCost")
	private double bestSellYesCost;

	@JsonProperty("bestBuyYesCost")
	private double bestBuyYesCost;

	@JsonProperty("lastTradePrice")
	private double lastTradePrice;

	@JsonProperty("name")
	private String name;

	@JsonProperty("id")
	private int id;

	@Transient
	@JsonProperty("shortName")
	private String shortName;

	@JsonProperty("status")
	private String status;

	public Contract(String image, double lastClosePrice, double bestBuyNoCost, double bestSellNoCost, int displayOrder, String dateEnd, double bestSellYesCost, double bestBuyYesCost, double lastTradePrice, String name, int id, String shortName, String status) {
		this.image = image;
		this.lastClosePrice = lastClosePrice;
		this.bestBuyNoCost = bestBuyNoCost;
		this.bestSellNoCost = bestSellNoCost;
		this.displayOrder = displayOrder;
		this.dateEnd = dateEnd;
		this.bestSellYesCost = bestSellYesCost;
		this.bestBuyYesCost = bestBuyYesCost;
		this.lastTradePrice = lastTradePrice;
		this.name = name;
		this.id = id;
		this.shortName = shortName;
		this.status = status;
	}

	public Contract() {
	}

	public Integer getContractUniqueID() {
		return contractUniqueID;
	}

	public void setContractUniqueID(Integer contractUniqueID) {
		this.contractUniqueID = contractUniqueID;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public double getLastClosePrice() {
		return lastClosePrice;
	}

	public void setLastClosePrice(double lastClosePrice) {
		this.lastClosePrice = lastClosePrice;
	}

	public double getBestBuyNoCost() {
		return bestBuyNoCost;
	}

	public void setBestBuyNoCost(double bestBuyNoCost) {
		this.bestBuyNoCost = bestBuyNoCost;
	}

	public double getBestSellNoCost() {
		return bestSellNoCost;
	}

	public void setBestSellNoCost(double bestSellNoCost) {
		this.bestSellNoCost = bestSellNoCost;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public String getDateEnd() {
		return dateEnd;
	}

	public void setDateEnd(String dateEnd) {
		this.dateEnd = dateEnd;
	}

	public double getBestSellYesCost() {
		return bestSellYesCost;
	}

	public void setBestSellYesCost(double bestSellYesCost) {
		this.bestSellYesCost = bestSellYesCost;
	}

	public double getBestBuyYesCost() {
		return bestBuyYesCost;
	}

	public void setBestBuyYesCost(double bestBuyYesCost) {
		this.bestBuyYesCost = bestBuyYesCost;
	}

	public double getLastTradePrice() {
		return lastTradePrice;
	}

	public void setLastTradePrice(double lastTradePrice) {
		this.lastTradePrice = lastTradePrice;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Contract{" +
				"contractUniqueID='" + contractUniqueID + '\'' +
				", image='" + image + '\'' +
				", lastClosePrice=" + lastClosePrice +
				", bestBuyNoCost=" + bestBuyNoCost +
				", bestSellNoCost=" + bestSellNoCost +
				", displayOrder=" + displayOrder +
				", dateEnd='" + dateEnd + '\'' +
				", bestSellYesCost=" + bestSellYesCost +
				", bestBuyYesCost=" + bestBuyYesCost +
				", lastTradePrice=" + lastTradePrice +
				", name='" + name + '\'' +
				", id=" + id +
				", shortName='" + shortName + '\'' +
				", status='" + status + '\'' +
				'}';
	}
}
