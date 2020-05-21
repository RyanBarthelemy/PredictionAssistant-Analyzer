package com.axlor.predictionassistantanalyzer.gui;

public class DisplayableMC {

    private String marketId, contractId, name, buyYes, buyNo, sellYes, sellNo, marketUrl;

    public DisplayableMC(String marketId, String contractId, String name, String buyYes, String buyNo, String sellYes, String sellNo, String marketUrl) {
        this.marketId = marketId;
        this.contractId = contractId;
        this.name = name;
        this.buyYes = buyYes;
        this.buyNo = buyNo;
        this.sellYes = sellYes;
        this.sellNo = sellNo;
        this.marketUrl = marketUrl;
    }

    public String getMarketId() {
        return marketId;
    }

    public String getContractId() {
        return contractId;
    }

    public String getName() {
        return name;
    }

    public String getBuyYes() {
        return buyYes;
    }

    public String getBuyNo() {
        return buyNo;
    }

    public String getSellYes() {
        return sellYes;
    }

    public String getSellNo() {
        return sellNo;
    }

    public String getMarketUrl() {
        return marketUrl;
    }
}
