package com.axlor.predictionassistantanalyzer.analyzers;

import com.axlor.predictionassistantanalyzer.gui.DisplayableContractInfo;
import com.axlor.predictionassistantanalyzer.model.Contract;
import com.axlor.predictionassistantanalyzer.model.Market;
import com.axlor.predictionassistantanalyzer.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContractHistoryService {

    @Autowired
    MarketService marketService;

    //todo: fix reversed bug that is also impacting how it measures age of price instance.

    public List<DisplayableContractInfo> getContractHistory(int nonUniqueMarketId, int nonUniqueContractId) {

        List<DisplayableContractInfo> contractHistory = new ArrayList<>();
        try {
            //System.out.println("Start getting history");
            List<Market> marketHistory = marketService.getMarketHistoryByNonUniqueId(nonUniqueMarketId); //most recent to oldest.
            //System.out.println("Finished getting history");

            long mostRecentTimestamp = getLongTimestampFromString(marketHistory.get(0).getTimeStamp());
            //for each market instance, get DisplayableContractHistory info we need.
            for (Market market : marketHistory) {
                for (Contract contract : market.getContracts()) {
                    if (contract.getId() == nonUniqueContractId) {
                        //build the DCI object
                        DisplayableContractInfo dci = new DisplayableContractInfo(
                                String.valueOf(nonUniqueContractId),
                                new DecimalFormat("#.##").format(contract.getBestBuyYesCost()),
                                String.valueOf(getLongTimestampFromString(market.getTimeStamp()))
                        );
                        contractHistory.add(dci);
                    }
                }
            }
            //set unset DCI fields. Need change and minsBeforeCurrent. DCI objects in list are still ordered from most recent to oldest
            List<DisplayableContractInfo> contractHistoryLimited = new ArrayList<>();
            for (int i = 0; i < contractHistory.size(); i++) {
                DisplayableContractInfo dci = contractHistory.get(i);
                if(i+1 == contractHistory.size()){
                    dci.setChange("---");
                    dci.setMinsFromCurrent(minuteDifference(mostRecentTimestamp, dci.getTimestamp()));
                    contractHistoryLimited.add(dci);
                    setSMA_Values(contractHistoryLimited);
                    return contractHistoryLimited;
                }
                else{ //we can set the change because the next index exists.
                    dci.setChange(calculateChange(dci.getBuyYes(), contractHistory.get(i+1).getBuyYes()));
                    dci.setMinsFromCurrent(minuteDifference(mostRecentTimestamp, dci.getTimestamp()));

                    //---------PredictIt bug fix workaround------------------\\
                    //If something is rising rapidly in price, there may be no one left selling shares at the wtb price.
                    //If no shares are available to be bought at the current price, PI lists the price as 0.0
                    //This messes with price tracking, so I remove it. Eventually I will scrape shares available...
                    //...but that is not available through the api and is against ToS, so maybe I won't/
                    if(Double.parseDouble(dci.getBuyYes()) != 0){
                        contractHistoryLimited.add(dci);
                    }//--------------------------------------------------------\\
                }
            }
            setSMA_Values(contractHistoryLimited);
            return contractHistoryLimited;
        } catch (Exception e) {
            //System.out.println(e.getMessage());
            //e.printStackTrace();
            return null;
        }
    }

    private void setSMA_Values(List<DisplayableContractInfo> contractHistoryLimited) {
        //set sma60
        for (DisplayableContractInfo dci: contractHistoryLimited){
            List<DisplayableContractInfo> toUse = getDCIsToUse(dci, contractHistoryLimited, 60);
            double sum = 0.0;
            for (DisplayableContractInfo dciToUse: toUse){
                sum = sum + Double.parseDouble(dciToUse.getBuyYes());
            }
            double avg = sum / toUse.size();
            dci.setSma60(String.valueOf(avg));
        }

        //set sma10
        for (DisplayableContractInfo dci: contractHistoryLimited){
            List<DisplayableContractInfo> toUse = getDCIsToUse(dci, contractHistoryLimited, 10);
            double sum = 0.0;
            for (DisplayableContractInfo dciToUse: toUse){
                sum = sum + Double.parseDouble(dciToUse.getBuyYes());
            }
            double avg = sum / toUse.size();
            dci.setSma10(String.valueOf(avg));
        }

    }

    private List<DisplayableContractInfo> getDCIsToUse(DisplayableContractInfo dci, List<DisplayableContractInfo> contractHistoryLimited, int timeframe) {
        List<DisplayableContractInfo> toUse = new ArrayList<>();
        int minCurrent = Math.abs(Integer.parseInt(dci.getMinsFromCurrent()));
        int minMax = minCurrent + timeframe;

        for (DisplayableContractInfo displayableContractInfo : contractHistoryLimited) {
            if (Math.abs(Integer.parseInt(displayableContractInfo.getMinsFromCurrent())) >= minCurrent
                    && Math.abs(Integer.parseInt(displayableContractInfo.getMinsFromCurrent())) <= minMax) {
                toUse.add(displayableContractInfo);
            }
        }
        return toUse;
    }

    private String calculateChange(String buyYesThis, String buyYesNext) {
        double buyYesThisD = Double.parseDouble(buyYesThis);
        double buyYesNextD = Double.parseDouble(buyYesNext);
        return new DecimalFormat("#.##").format((buyYesThisD-buyYesNextD)*100);
    }

    private String minuteDifference(long mostRecentTimestamp, String timestamp) {
        long timestampL = Long.parseLong(timestamp);
        long milliDiff = mostRecentTimestamp - timestampL; //should be negative
        long minDiff = milliDiff / 60000;
        return String.valueOf(minDiff);
    }

    private long getLongTimestampFromString(String timeStamp) {
        return java.sql.Timestamp.valueOf(timeStamp.replace("T", " ")).getTime();
    }
}
