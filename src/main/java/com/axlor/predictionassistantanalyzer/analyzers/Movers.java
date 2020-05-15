package com.axlor.predictionassistantanalyzer.analyzers;

import com.axlor.predictionassistantanalyzer.exception.NoSnapshotsInDatabaseException;
import com.axlor.predictionassistantanalyzer.exception.SnapshotCountMismatchException;
import com.axlor.predictionassistantanalyzer.exception.SnapshotNotFoundException;
import com.axlor.predictionassistantanalyzer.model.Contract;
import com.axlor.predictionassistantanalyzer.model.Market;
import com.axlor.predictionassistantanalyzer.model.Snapshot;
import com.axlor.predictionassistantanalyzer.model.analyzer.MoveReport;
import com.axlor.predictionassistantanalyzer.service.MarketService;
import com.axlor.predictionassistantanalyzer.service.SnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class Movers {

    @Autowired
    MarketService marketService;

    @Autowired
    SnapshotService snapshotService;

    public void findBiggestMoversInLast_xx_mins(int minutes, int minMovement) throws SnapshotCountMismatchException, NoSnapshotsInDatabaseException, SnapshotNotFoundException {
        System.out.println("Attempting to find biggest movers in last " + minutes + " minutes...");

        Snapshot latestSnapshot = snapshotService.getLatestSnapshot();
        Snapshot startingSnapshot = getStartingSnapshot(minutes);

        System.out.println("Starting Snapshot id: " + startingSnapshot.getHashId() + "  ----  timestamp:" + startingSnapshot.getTimestamp());
        System.out.println("Ending Snapshot id: " + latestSnapshot.getHashId() + "  ----  timestamp:" + latestSnapshot.getTimestamp());
        System.out.print("Difference of " + (latestSnapshot.getTimestamp()-startingSnapshot.getTimestamp()) + " milliseconds, ");
        System.out.println("or " +((latestSnapshot.getTimestamp()-startingSnapshot.getTimestamp())/(60000.0)) + " minutes");

        List<MoveReport> moveReports = new ArrayList<MoveReport>();

        //for each market, if it exists in both snapshots, look at each contract, compare buyYes and buyNo between start and latest.
        List<Market> marketsToAnalyze = latestSnapshot.getMarkets();
        //for each Market in latest Snapshot...
        for (int i = 0; i < marketsToAnalyze.size(); i++) {
            //set the Market
            Market latestToAnalyze = marketsToAnalyze.get(i);
            //look for that Market in starting Snapshot
            for (int j = 0; j < startingSnapshot.getMarkets().size(); j++) {
                if(startingSnapshot.getMarkets().get(j).getId() == latestToAnalyze.getId()){
                    Market startingToAnalyze = startingSnapshot.getMarkets().get(j);
                    moveReports.addAll(createMoveReports(latestToAnalyze, startingToAnalyze));
                }//else, if we don't find a matching Market, just move on to the next one.
            }
        }

        //now we have our MoveReport list, we want them in order so we can find the biggest movers. //implement Comparable in MoveReport
        Collections.sort(moveReports);
        printMoveReports(moveReports, minMovement);
    }

    private void printMoveReports(List<MoveReport> moveReports, int minMovement) {
        System.out.println("===============================================================");
        System.out.println("Move Reports in order:\n");
        for (MoveReport report: moveReports){
            if(report.getDifference() >= 98 || report.getDifference() <= minMovement){
                continue; //ignore stuff that is wrong/we don't care about.
                //Note, when a Contract's buyYes or buyNo has no shares available (because no one wants to buy/sell them) then
                //  that value not applicable, but is noted in PredictIt's API as a value of 0.00, so we need to adjust our reporting to note that.
            }
            System.out.println(report);
            System.out.println();
        }
    }

    private List<MoveReport> createMoveReports(Market latestToAnalyze, Market startingToAnalyze) {
        List<MoveReport> moveReportsForSpecifiedMarket = new ArrayList<>();

        if(latestToAnalyze.getId() != startingToAnalyze.getId() || latestToAnalyze.getContracts().size() != startingToAnalyze.getContracts().size()){
            return moveReportsForSpecifiedMarket;
        }

        //for each contract, create a MoveReport for buyYes and buyNo
        for (int i = 0; i < latestToAnalyze.getContracts().size(); i++) {
            Contract contractLatest = latestToAnalyze.getContracts().get(i);
            Contract contractStart = startingToAnalyze.getContracts().get(i);

            moveReportsForSpecifiedMarket.add(
                    new MoveReport(
                        latestToAnalyze.getName(),
                        latestToAnalyze.getId(),
                        contractLatest.getName(),
                        contractLatest.getId(),
                        "buyYes",
                        contractStart.getBestBuyYesCost(),
                        contractLatest.getBestBuyYesCost())
            );

            moveReportsForSpecifiedMarket.add(
                    new MoveReport(
                            latestToAnalyze.getName(),
                            latestToAnalyze.getId(),
                            contractLatest.getName(),
                            contractLatest.getId(),
                            "buyNo",
                            contractStart.getBestBuyNoCost(),
                            contractLatest.getBestBuyNoCost())
            );
        }
        return moveReportsForSpecifiedMarket;
    }

    private Snapshot getStartingSnapshot(int minutes) {
        long startingPoint = System.currentTimeMillis() - (minutes * 60 * 1000);
        return snapshotService.getSnapshotClosesToTimestamp(startingPoint);
    }

    //@PostConstruct
    public void test() throws SnapshotCountMismatchException, NoSnapshotsInDatabaseException, SnapshotNotFoundException {
        System.out.println("Running test method");
        findBiggestMoversInLast_xx_mins(60, 5);
    }

}
