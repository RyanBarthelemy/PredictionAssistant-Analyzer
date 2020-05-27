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

    public List<DisplayableContractInfo> getContractHistoryLast_XX_mins(int nonUniqueMarketId, int nonUniqueContractId, int timeFrameMins) {

        List<DisplayableContractInfo> contractHistory = new ArrayList<>();
        try {
            System.out.println("Start getting history");
            List<Market> marketHistory = marketService.getMarketHistoryByNonUniqueId(nonUniqueMarketId); //most recent to oldest.
            System.out.println("Finished getting history");

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
                    return contractHistoryLimited;
                }
                else{ //we can set the change because the next index exists.
                    dci.setChange(calculateChange(dci.getBuyYes(), contractHistory.get(i+1).getBuyYes()));
                    dci.setMinsFromCurrent(minuteDifference(mostRecentTimestamp, dci.getTimestamp()));
                    contractHistoryLimited.add(dci);
                    if(Math.abs(Integer.parseInt(minuteDifference(mostRecentTimestamp, dci.getTimestamp()))) > (timeFrameMins)){
                        //System.out.println("Hit the time mark");
                        return contractHistoryLimited;
                    }
                }
            }
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
        return null;
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
