package com.axlor.predictionassistantanalyzer.model.analyzer;

public class MoveReport implements Comparable{
    private String marketName;
    private int nonUniqueMarketId;

    private String contractName;
    private int nonUniqueContractId;

    private String buyType; //bestBuyYes or bestBuyNo
    private double buyStart;
    private double buyFinish;
    private int difference;
    private String direction;

    public MoveReport(String marketName, int nonUniqueMarketId, String contractName, int nonUniqueContractId, String buyType, double buyStart, double buyFinish) {
        this.marketName = marketName;
        this.nonUniqueMarketId = nonUniqueMarketId;
        this.contractName = contractName;
        this.nonUniqueContractId = nonUniqueContractId;
        this.buyType = buyType;
        this.buyStart = buyStart;
        this.buyFinish = buyFinish;

        this.difference = (int) (Math.abs(buyFinish-buyStart)*100);

        this.direction = "+";
        if(buyFinish-buyStart < 0){
            this.direction = "-";
        }

    }

    public String getMarketName() {
        return marketName;
    }

    public int getNonUniqueMarketId() {
        return nonUniqueMarketId;
    }

    public String getContractName() {
        return contractName;
    }

    public int getNonUniqueContractId() {
        return nonUniqueContractId;
    }

    public String getBuyType() {
        return buyType;
    }

    public double getBuyStart() {
        return buyStart;
    }

    public double getBuyFinish() {
        return buyFinish;
    }

    public int getDifference() {
        return difference;
    }

    public String getDirection() {
        return direction;
    }

    @Override
    public int compareTo(Object o) {
        MoveReport that = (MoveReport) o;
        return Double.compare(this.difference, that.difference);
    }

    @Override
    public String toString() {
        return getNonUniqueMarketId() + "-" + getMarketName() + "\n" +
                "Contract: " + getContractName() + "\n" +
                "Difference: " + getDirection() + getDifference()
                ;
    }
}
