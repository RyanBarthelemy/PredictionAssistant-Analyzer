package com.axlor.predictionassistantanalyzer.analyzers.nn_pa;

import com.axlor.predictionassistantanalyzer.exception.MarketNotFoundException;
import com.axlor.predictionassistantanalyzer.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class NN_Manager {

    @Autowired
    MarketService marketService;

    @PostConstruct
    public void testingStuff() throws MarketNotFoundException {

        //http://localhost:8080/markets/6704/contracts/22681 for testing
        ContractProblemData cpd = new ContractProblemData(6704, 22681, marketService);

    }

}
