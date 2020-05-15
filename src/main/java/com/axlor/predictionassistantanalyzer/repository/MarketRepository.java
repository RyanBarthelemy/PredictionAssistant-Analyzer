package com.axlor.predictionassistantanalyzer.repository;


import com.axlor.predictionassistantanalyzer.model.Market;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MarketRepository extends CrudRepository<Market, Integer> {

    /**
     *
     * @param id NOT THE PRIMARY KEY ID! This id represents the id field of the Market given by PredictIt. It is in the url for the market and elsewhere.
     * @return Returns a List of all Market objects with this PredictIt market id. There will be one per Snapshot saved to the database, showing a history of that market.
     */
    public List<Market> findMarketsById(int id);

}
