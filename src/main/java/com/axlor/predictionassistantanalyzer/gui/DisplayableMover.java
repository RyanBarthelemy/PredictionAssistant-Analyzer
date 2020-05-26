package com.axlor.predictionassistantanalyzer.gui;

public class DisplayableMover implements Comparable{

    private String sign, amount,
                   currentPrice, name, nonUniqueContractId, nonUniqueMarketId, Url, change;

    public DisplayableMover(String sign, String amount, String currentPrice, String name, String nonUniqueContractId, String nonUniqueMarketId, String url) {
        this.sign = sign;
        this.amount = amount;
        this.currentPrice = currentPrice;
        this.name = name;
        this.nonUniqueContractId = nonUniqueContractId;
        this.nonUniqueMarketId = nonUniqueMarketId;
        Url = url;
        this.change = sign + amount;
    }

    public String getSign() {
        return sign;
    }

    public String getAmount() {
        return amount;
    }

    public String getCurrentPrice() {
        return currentPrice;
    }

    public String getName() {
        return name;
    }

    public String getNonUniqueContractId() {
        return nonUniqueContractId;
    }

    public String getNonUniqueMarketId() {
        return nonUniqueMarketId;
    }

    public String getUrl() {
        return Url;
    }

    public String getChange() {
        return change;
    }

    @Override
    public int compareTo(Object o) {
        DisplayableMover that = (DisplayableMover) o;
        int thisAmount = Integer.parseInt(this.amount);
        int thatAmount = Integer.parseInt(that.amount);

        if(thisAmount > thatAmount){
            return 1;
        }
        else if(thisAmount == thatAmount){
            return compareCurrentPrice(that); //secondary comparison
        }
        else{
            return -1;
        }


    }

    private int compareCurrentPrice(DisplayableMover that) {
        double thisCurrentPrice = Double.parseDouble(this.currentPrice);
        double thatCurrentPrice = Double.parseDouble(that.currentPrice);
        if(thisCurrentPrice > thatCurrentPrice){
            return 1;
        }
        else if(thisCurrentPrice == thatCurrentPrice){
            return 0;
        }
        else{
            return -1;
        }
    }
}
