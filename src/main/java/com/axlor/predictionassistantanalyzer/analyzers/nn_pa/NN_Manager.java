package com.axlor.predictionassistantanalyzer.analyzers.nn_pa;

import com.axlor.predictionassistantanalyzer.analyzers.nn_generic.NN_Generic;
import com.axlor.predictionassistantanalyzer.analyzers.nn_generic.TrainingDataSet;
import com.axlor.predictionassistantanalyzer.exception.MarketNotFoundException;
import com.axlor.predictionassistantanalyzer.exception.NoSnapshotsInDatabaseException;
import com.axlor.predictionassistantanalyzer.model.Contract;
import com.axlor.predictionassistantanalyzer.model.Market;
import com.axlor.predictionassistantanalyzer.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class NN_Manager {

    @Autowired
    MarketService marketService;

    //http://localhost:8080/markets/6704/contracts/22689 for testing, hopefully pretty volatile

    @PostConstruct
    public void NN_create_train_evaluate_forTesting() throws MarketNotFoundException, NoSnapshotsInDatabaseException {
        int[] LAYER_SIZES;
        int inputLayerSize;
        int outputLayerSize;
        String networkFilename;

        //setup problem data object and file names.
        //ContractProblemData cpd = new ContractProblemData(6704, 22681, marketService);
        ContractProblemData cpd = get_ContractProblemData_fromFile(5883, 17474); //can extract these parameters
        networkFilename = "NNetwork_ContractProblemData_mid" + cpd.getNonUniqueMarketId() + "_cid" + cpd.getNonUniqueContractId() + "_obj";

        System.out.println("ContractProblemData object created with " + cpd.getNumOfProblems() + " problem data sets to train with.");

        //setup TDS object so we can train in manageable batches and save states along the way.
        inputLayerSize = cpd.getInputLayerSize();
        outputLayerSize = cpd.getOutputLayerSize();
        TrainingDataSet trainingDataSet = new TrainingDataSet(inputLayerSize, outputLayerSize);

        //add 'problems' to TDS
        for (int i = 0; i < cpd.getNumOfProblems(); i++) {
            trainingDataSet.addDataToList(cpd.getInputLayers().get(i), cpd.getOutputLayers().get(i));
        }

        LAYER_SIZES = new int[]{inputLayerSize, 200, 200, outputLayerSize};
        NN_Generic neural_network = loadNetwork(LAYER_SIZES, networkFilename);

        neural_network.trainNetworkUsingTrainingDataSet(trainingDataSet, 5000, 200);
        predictContract(cpd, neural_network);
    }

    public void predictContract(ContractProblemData contractProblemData, NN_Generic network) throws MarketNotFoundException, NoSnapshotsInDatabaseException {
        List<Market> marketHistory = marketService.getMarketHistoryByNonUniqueId(contractProblemData.getNonUniqueMarketId());
        double[] inputLayer = createInputLayerFromLatestContractData(contractProblemData.getNonUniqueContractId(), marketHistory, contractProblemData);
        if(inputLayer == null){
            //System.out.println("InputLayer failed to be created properly. Returning.");
        }
        double[] outputLayer = network.calculateActivationsOfAllLayers(inputLayer);
        evaluateOutputLayer(outputLayer, contractProblemData.getNonUniqueMarketId(), contractProblemData.getNonUniqueContractId());
    }

    private void evaluateOutputLayer(double[] outputLayer, int nonUniqueMarketId, int nonUniqueContractId) throws NoSnapshotsInDatabaseException, MarketNotFoundException {
        System.out.println("=========================================================================================================");
        System.out.println("Evaluation of Market[" + nonUniqueMarketId + "] Contract[" + nonUniqueContractId + "]");

        Market currentMarket = marketService.getLatestMarketInfo(nonUniqueMarketId);
        Contract currentContract = getContract(currentMarket,nonUniqueContractId);

        System.out.println("OutputLayer Raw:" + Arrays.toString(outputLayer));
        double predictedBuyYes = getPredictedBuyYes(outputLayer);
        double predictedBuyNo = getPredictedBuyNo(outputLayer);

        System.out.println("Current buyYes[" + currentContract.getBestBuyYesCost() + "]");
        System.out.println("Predicted buyYes[" + predictedBuyYes + "] + 10 minutes out");

        System.out.println("Current buyNo[" + currentContract.getBestBuyNoCost() + "]");
        System.out.println("Predicted buyNo[" + predictedBuyNo + "] + 10 minutes out");
        System.out.println("=========================================================================================================");
    }

    private double getPredictedBuyYes(double[] outputLayer) {
        int bestGuessIndex = -1;
        double confidenceValue = 0;
        for (int i = 0; i < 100; i++) {
            if(outputLayer[i] > confidenceValue){
                bestGuessIndex = i;
                confidenceValue = outputLayer[i];
            }
        }
        double buyYesP = (double) bestGuessIndex;
        buyYesP = buyYesP/100.0;
        return buyYesP;
    }

    private double getPredictedBuyNo(double[] outputLayer) {
        int bestGuessIndex = -1;
        double confidenceValue = 0;
        for (int i = 100; i < 200; i++) {
            if(outputLayer[i] > confidenceValue){
                bestGuessIndex = i;
                confidenceValue = outputLayer[i];
            }
        }
        double buyNoP = (double) bestGuessIndex;
        buyNoP = buyNoP - 100.0;
        buyNoP = buyNoP/100.0;
        return buyNoP;
    }

    private double[] createInputLayerFromLatestContractData(int nonUniqueContractId, List<Market> marketHistory, ContractProblemData contractProblemData) {
        int contractsNeeded = contractProblemData.getInputTimeFrame()/contractProblemData.getTimeBetweenSnapshots();
        //marketHistory is ordered from oldest to newest
        Collections.reverse(marketHistory); //now ordered newest to oldest
        List<Market> marketsNeeded = new ArrayList<>();
        for (int i = 0; i < contractsNeeded; i++) {
            marketsNeeded.add(marketHistory.get(i));
        }
        Collections.reverse(marketsNeeded); //ordered oldest to newest again but just the markets we need

        //see if we are using a timeframe that makes sense. //error check
        long timestampStart = java.sql.Timestamp.valueOf(marketsNeeded.get(0).getTimeStamp().replace("T", " ")).getTime();
        long timestampFinish = java.sql.Timestamp.valueOf(marketsNeeded.get(marketsNeeded.size()-1).getTimeStamp().replace("T", " ")).getTime();

        //System.out.println("timestampStart: " + timestampStart);
        //System.out.println("timestampFinish: " + timestampFinish);
        long difference = Math.abs(timestampFinish-timestampStart); //milli difference
        difference = difference/60000; //convert to mins difference
        long inputError = Math.abs(difference - contractProblemData.getInputTimeFrame());


        //System.out.println("difference between first and last timestamp: " + difference + " minutes");
        //System.out.println("Erorr of: " + inputError + " minutes. Accepted error: " + contractProblemData.getInputErrorMax() + " minutes.");
        if(Math.abs(difference - contractProblemData.getInputTimeFrame()) > contractProblemData.getInputErrorMax()){return null;}

        //build input
        boolean failed = false;
        List<Double> inputLayerList = new ArrayList<>();
        for (int i = 0; i < contractsNeeded; i++) {
            Contract contract = getContract(marketHistory.get(i), nonUniqueContractId);
            if(contract == null){failed = true; break;}
            inputLayerList.add(contract.getBestBuyYesCost());
            inputLayerList.add(contract.getBestBuyNoCost());
        }
        if(failed){return null;}
        if(inputLayerList.size() != contractProblemData.getInputLayerSize()){return null;}

        double[] inputLayer = new double[inputLayerList.size()];
        for (int i = 0; i < inputLayerList.size(); i++) {
            inputLayer[i] = inputLayerList.get(i);
        }

        return inputLayer;
    }

    private Contract getContract(Market market, int nonUniqueContractId) {
        for (int i = 0; i < market.getContracts().size(); i++) {
            if(market.getContracts().get(i).getId() == nonUniqueContractId){
                return market.getContracts().get(i);
            }
        }
        return null;
    }

    private ContractProblemData get_ContractProblemData_fromFile(int nonUniqueMarketId, int nonUniqueContractId) {
        String problemDataFilename = "ContractProblemData_mid" + nonUniqueMarketId + "_cid" + nonUniqueContractId + "_obj";
        ContractProblemData problemData = ContractProblemData.load_ContractProblem_data(problemDataFilename);

        if (problemData == null) {
            System.out.println("Could not load ContractProblemData from file to object for whatever reason, creating new problem data object.");
            System.out.println("This could take quite some time...");

            try {
                problemData = new ContractProblemData(nonUniqueMarketId, nonUniqueContractId, marketService);
            } catch (Exception e) {
                return null;
            }
            System.out.println("Saving problem data object for later use... this may take quite some time as well.");
            problemData.save_ContractProblem_data(problemDataFilename);

            return problemData;
        }
        System.out.println("Successfully loaded problem data from " + problemDataFilename);
        return problemData;
    }

    private static NN_Generic loadNetwork(int[] LAYER_SIZES, String file) {
        File testFile = new File(file);
        if (testFile.exists()) {
            System.out.println("Found saved NN object, attempting to load...");
            NN_Generic network = NN_Generic.loadNN(file);

            if (network == null) {
                System.out.println("Failed to load saved network. (could be different class id because of a minor change, etc). Creating new network.");
                network = new NN_Generic(LAYER_SIZES, file);
                return network;
            }

            System.out.println("The saved NN_Generic object was loaded successfully!");
            return network;

        } else {
            System.out.println("No saved network file found, creating new network.");
            return new NN_Generic(LAYER_SIZES, file);
        }
    }
}
