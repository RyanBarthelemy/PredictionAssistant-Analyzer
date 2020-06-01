package com.axlor.predictionassistantanalyzer.gui;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DisplayableContractInfo {

    private String nonUniqueContractId, buyYes, change, minsFromCurrent, timestamp, sma10, sma60, timestampToDisplay;

    public DisplayableContractInfo(String nonUniqueContractId, String buyYes, String timestamp) {
        this.nonUniqueContractId = nonUniqueContractId;
        this.buyYes = buyYes;
        this.timestamp = timestamp;
        setTimestampToDisplay();
        sma10 = "0";
        sma60 = "0";
    }

    private void setTimestampToDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a EEEE, dd MMMM yyyy");
        Date date = new Date(Long.parseLong(timestamp));
        timestampToDisplay = sdf.format(date);
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

    public String getSma10() {
        return sma10;
    }

    public void setSma10(String sma10) {
        this.sma10 = sma10;
    }

    public String getSma60() {
        return sma60;
    }

    public void setSma60(String sma60) {
        this.sma60 = sma60;
    }

    public String getTimestampToDisplay() {
        return timestampToDisplay;
    }

    @Override
    public String toString() {
        return "DisplayableContractInfo{" +
                "nonUniqueContractId='" + nonUniqueContractId + '\'' +
                ", buyYes='" + buyYes + '\'' +
                ", change='" + change + '\'' +
                ", minsFromCurrent='" + minsFromCurrent + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", sma10='" + sma10 + '\'' +
                ", sma60='" + sma60 + '\'' +
                ", timestampToDisplay='" + timestampToDisplay + '\'' +
                '}';
    }
}
