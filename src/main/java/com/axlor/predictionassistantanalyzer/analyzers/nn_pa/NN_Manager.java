package com.axlor.predictionassistantanalyzer.analyzers.nn_pa;

import com.axlor.predictionassistantanalyzer.analyzers.nn_generic.NN_Generic;
import com.axlor.predictionassistantanalyzer.analyzers.nn_generic.TrainingDataSet;
import com.axlor.predictionassistantanalyzer.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
public class NN_Manager {

    @Autowired
    MarketService marketService;

    //http://localhost:8080/markets/6704/contracts/22681 for testing

    @PostConstruct
    public void testingStuff() {
        int[] LAYER_SIZES;
        int inputLayerSize;
        int outputLayerSize;
        String networkFilename;

        //setup problem data object and file names.
        //ContractProblemData cpd = new ContractProblemData(6704, 22681, marketService);
        ContractProblemData cpd = get_ContractProblemData_fromFile(6704, 22681);
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

        LAYER_SIZES = new int[]{inputLayerSize, 200, 100, 100, outputLayerSize};
        NN_Generic neural_network = loadNetwork(LAYER_SIZES, networkFilename);

        while (true) {
            neural_network.trainNetworkUsingTrainingDataSet(trainingDataSet, 1000, 200);
        }
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
