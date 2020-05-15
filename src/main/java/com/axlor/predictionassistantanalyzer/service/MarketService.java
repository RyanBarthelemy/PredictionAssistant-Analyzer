package com.axlor.predictionassistantanalyzer.service;

import com.axlor.predictionassistantanalyzer.exception.MarketNotFoundException;
import com.axlor.predictionassistantanalyzer.exception.NoSnapshotsInDatabaseException;
import com.axlor.predictionassistantanalyzer.model.Market;
import com.axlor.predictionassistantanalyzer.model.Snapshot;
import com.axlor.predictionassistantanalyzer.model.mini.ContractMini;
import com.axlor.predictionassistantanalyzer.model.mini.MarketMini;
import com.axlor.predictionassistantanalyzer.repository.MarketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles requesting data from various repositories and organizing that data into a form the controllers require.
 */
@Service
public class MarketService {

    @Autowired
    SnapshotService snapshotService;

    @Autowired
    MarketRepository marketRepository;

    /**
     * Gets relevant info for user to understand what Markets are currently available. Omits history and specific contract prices.
     *
     * @return Returns a List of MarketMini objects that contain info the user needs to understand what Markets there are and how to find them.
     * @throws NoSnapshotsInDatabaseException Thrown when no data is in database.
     */
    public List<MarketMini> getMarketsList() throws NoSnapshotsInDatabaseException {
        //I don't really like how verbose and ugly this is. Could probably be done better with lambdas maybe?
        //It works and is not expensive in terms of querying the database though, so sticking with it for now.
        Snapshot current = snapshotService.getLatestSnapshot();

        List<MarketMini> marketMinis = new ArrayList<>();
        List<ContractMini> contractMinis;

        for (int i = 0; i < current.getMarkets().size(); i++) {
            contractMinis = new ArrayList<>();
            for (int j = 0; j < current.getMarkets().get(i).getContracts().size(); j++) {
                ContractMini contractMini = new ContractMini(
                        current.getMarkets().get(i).getContracts().get(j).getName(),
                        current.getMarkets().get(i).getContracts().get(j).getId());
                contractMinis.add(contractMini);
            }//contractMini list creation loop
            MarketMini marketMini = new MarketMini(
                    current.getMarkets().get(i).getName(),
                    current.getMarkets().get(i).getId(),
                    "/api/markets/" + current.getMarkets().get(i).getId(),
                    contractMinis
            );
            marketMinis.add(marketMini);
        }//marketMini list creation loop
        return marketMinis;
    }

    /**
     * Gets market history by non unique id.
     *
     * @param marketId the market id given by PredictIt, not the unique primary key id.
     * @return A List of Market objects from most recent instance to oldest instance of that PredictIt market in the database
     * @throws MarketNotFoundException Thrown when Market with parameter marketId cannot be found.
     */
    public List<Market> getMarketHistoryByNonUniqueId(int marketId) throws MarketNotFoundException {
        List<Market> marketHistoryList = marketRepository.findMarketsById(marketId);

        if(marketHistoryList == null || marketHistoryList.isEmpty()){
            throw new MarketNotFoundException(marketId);
        }
        return marketHistoryList;
    }

    /**
     * Gets latest market info by non-unique id
     *
     * @param marketId the market id given by PredictIt, not the unique primary key id.
     * @return The most recent Market instance with this PredictIt id
     * @throws NoSnapshotsInDatabaseException Thrown when no data is in database, if there is no Snapshot then there are no Markets
     * @throws MarketNotFoundException        Thrown when Market with parameter marketId cannot be found.
     */
    public Market getLatestMarketInfo(int marketId) throws NoSnapshotsInDatabaseException, MarketNotFoundException {
        List<Market> latestMarkets = snapshotService.getLatestSnapshot().getMarkets();
        for (Market latestMarket : latestMarkets) {
            if (latestMarket.getId() == marketId) {
                return latestMarket;
            }
        }
        throw new MarketNotFoundException("No Market with PredictIt marketId: '" + marketId + "' was found in the latest Snapshot data. Market may be closed, check url of Market.");
    }
}
