package com.axlor.predictionassistantanalyzer.gui;

public class DisplayableContractInfo {

    private String nonUniqueContractId, buyYes, change, minsFromCurrent, timestamp;

    public DisplayableContractInfo(String nonUniqueContractId, String buyYes, String timestamp) {
        this.nonUniqueContractId = nonUniqueContractId;
        this.buyYes = buyYes;
        this.timestamp = timestamp;
    }

    public String getNonUniqueContractId() {
        return nonUniqueContractId;
    }

    public String getBuyYes() {
        return buyYes;
    }

    public String getChange() {
        return change;
    }

    public String getMinsFromCurrent() {
        return minsFromCurrent;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setChange(String change) {
        this.change = change;
    }

    public void setMinsFromCurrent(String minsFromCurrent) {
        this.minsFromCurrent = minsFromCurrent;
    }

    @Override
    public String toString() {
        return "DisplayableContractInfo{" +
                "nonUniqueContractId='" + nonUniqueContractId + '\'' +
                ", buyYes='" + buyYes + '\'' +
                ", change='" + change + '\'' +
                ", minsBeforeCurrent='" + minsFromCurrent + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
