package com.axlor.predictionassistantanalyzer.analyzers;

import com.axlor.predictionassistantanalyzer.gui.DisplayableMover;
import com.axlor.predictionassistantanalyzer.model.Snapshot;
import com.axlor.predictionassistantanalyzer.service.SnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MoverService {

    @Autowired
    SnapshotService snapshotService;

    public List<DisplayableMover> getDisplayableMoversList(String minMovementString, String timeFrame) {
        try{
            int minMovement = Integer.parseInt(minMovementString);
            int timeFrameMins = Integer.parseInt(timeFrame);
            return findMovers(minMovement, timeFrameMins);
        }catch(Exception e){
            //System.out.println("Parsing Strings to int failed, returning null");
            return null;
        }
    }

    private List<DisplayableMover> findMovers(int minMovement, int timeFrameMins) throws Exception {
        List<DisplayableMover> moversList = new ArrayList<>();

        long now = System.currentTimeMillis();
        long start = now - (1000*60*timeFrameMins);

        Snapshot current = snapshotService.getLatestSnapshot();
        Snapshot startingPoint = snapshotService.getSnapshotClosesToTimestamp(start);

        if(Math.abs(current.getTimestamp() - startingPoint.getTimestamp()) > timeFrameMins*1.2*1000*60){ //20% error
            throw new Exception("Could not find Snapshot close enough to " + timeFrameMins + " minutes ago.");
        }

        //for each contract in each market, compare to starting snapshot's equivalent
        for (int marketIndex = 0; marketIndex < current.getMarkets().size(); marketIndex++) {
            for (int contractIndex = 0; contractIndex < current.getMarkets().get(marketIndex).getContracts().size(); contractIndex++) {
                double currentBuyYes = current.getMarkets().get(marketIndex).getContracts().get(contractIndex).getBestBuyYesCost();
                try {
                    double startBuyYes = getBuyYes(startingPoint, current.getMarkets().get(marketIndex).getContracts().get(contractIndex).getId());

                    if(currentBuyYes == 0.0 || startBuyYes == 0.0){ //when no shares are available PredictIt marks cost at 0
                        continue;
                    }
                    if(currentBuyYes>= 0.98 || startBuyYes >= 0.98){ //ignore 'solved' markets. They won't move difference calc can get messy if no shares are available to buy/sell
                        continue;
                    }

                    double difference = currentBuyYes - startBuyYes;
                    difference = difference*100.0;
                    int differenceI = (int) difference;

                    if(Math.abs(differenceI) >= minMovement){
                        //create DisplayableMover
                        String sign = "+";
                        if(differenceI < 0){sign="-";}

                        String contractNameToUse;
                        if(current.getMarkets().get(marketIndex).getContracts().get(contractIndex).getName().equals(current.getMarkets().get(marketIndex).getName())){
                            contractNameToUse = "Yes/No"; //if contract name = market name, replace contract name with Yes/No
                        }
                        else{ //use normal contract name
                            contractNameToUse = current.getMarkets().get(marketIndex).getContracts().get(contractIndex).getName();
                        }

                        DisplayableMover moverToAdd = new DisplayableMover(
                                sign,
                                String.valueOf(Math.abs(differenceI)),
                                new DecimalFormat("#.##").format(current.getMarkets().get(marketIndex).getContracts().get(contractIndex).getBestBuyYesCost()),
                                ("BuyYes -- " + contractNameToUse + " -- " + current.getMarkets().get(marketIndex).getName()),
                                String.valueOf(current.getMarkets().get(marketIndex).getContracts().get(contractIndex).getId()),
                                String.valueOf(current.getMarkets().get(marketIndex).getId()),
                                current.getMarkets().get(marketIndex).getUrl()
                        );
                        //add to moversList
                        moversList.add(moverToAdd);
                    }
                }catch(Exception e){
                    //System.out.println(e.getMessage());
                }

            }//for each contract
        }//for each market
        Collections.sort(moversList); //ascending
        Collections.reverse(moversList); //descending
        return moversList;
    }

    private double getBuyYes(Snapshot startingPoint, int contractId) throws Exception {
        for (int i = 0; i < startingPoint.getMarkets().size(); i++) {
            for (int j = 0; j < startingPoint.getMarkets().get(i).getContracts().size(); j++) {
                if(startingPoint.getMarkets().get(i).getContracts().get(j).getId() == contractId){
                    return startingPoint.getMarkets().get(i).getContracts().get(j).getBestBuyYesCost();
                }
            }
        }
        throw new Exception("Contract with id " + contractId + " not found in Snapshot[" + startingPoint.getHashId() + "].");
    }
}
