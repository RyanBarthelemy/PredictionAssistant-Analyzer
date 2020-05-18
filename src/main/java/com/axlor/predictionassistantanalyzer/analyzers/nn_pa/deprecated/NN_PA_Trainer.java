package com.axlor.predictionassistantanalyzer.analyzers.nn_pa.deprecated;

import com.axlor.predictionassistantanalyzer.analyzers.nn_generic.NN_Generic;
import com.axlor.predictionassistantanalyzer.analyzers.nn_generic.TrainingDataSet;
import com.axlor.predictionassistantanalyzer.exception.NoSnapshotsInDatabaseException;
import com.axlor.predictionassistantanalyzer.exception.SnapshotCountMismatchException;
import com.axlor.predictionassistantanalyzer.exception.SnapshotNotFoundException;
import com.axlor.predictionassistantanalyzer.service.SnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
@Deprecated
public class NN_PA_Trainer {

    @Autowired
    SnapshotService snapshotService;

    @Autowired
    NN_PA_Evaluator evaluator;

    @PostConstruct
    public void main_NN_method() throws SnapshotCountMismatchException, NoSnapshotsInDatabaseException, SnapshotNotFoundException { //will rename or something later.
        int[] LAYER_SIZES;
        int inputLayerSize;
        int outputLayerSize;
        String networkFilename = "NN_PA_obj";
        String problemDataFilename = "PA_ProblemData_obj";

        PA_ProblemData pa_trainData = get_PA_ProblemData(problemDataFilename);

        System.out.println("PA_ProblemData object created with " + pa_trainData.getNumOfProblems() + " problem data sets to train with.");

        inputLayerSize = pa_trainData.getInputLayerSize();
        outputLayerSize = pa_trainData.getOutputLayerSize();

        TrainingDataSet trainingDataSet = new TrainingDataSet(inputLayerSize, outputLayerSize);
        System.out.println("Size of training set: " + pa_trainData.getNumOfProblems());

        for (int i = 0; i < pa_trainData.getNumOfProblems(); i++) {
            trainingDataSet.addDataToList(pa_trainData.getInputLayers().get(i), pa_trainData.getOutputLayers().get(i));
        }

        LAYER_SIZES = new int[]{inputLayerSize, 200, 100, 100, outputLayerSize};
        NN_Generic neural_network = loadNetwork(LAYER_SIZES, networkFilename);



        while (true) {
            neural_network.trainNetworkUsingTrainingDataSet(trainingDataSet, 100, 5);
            evaluator.predictContract(5883, 17474, pa_trainData, neural_network);
        }




    }


    private PA_ProblemData get_PA_ProblemData(String problemDataFilename) throws SnapshotCountMismatchException, NoSnapshotsInDatabaseException, SnapshotNotFoundException {
        System.out.println("Attempting to load problem data from file: " + problemDataFilename);
        PA_ProblemData problemData = PA_ProblemData.load_MNIST_data(problemDataFilename);

        if (problemData == null) {
            System.out.println("Could not load PA_ProblemData from file to object for whatever reason, creating new problem data object.");
            System.out.println("This could take quite some time...");

            try {
                problemData = new PA_ProblemData(snapshotService);
            } catch (Exception e) {
                return null;
            }
            System.out.println("Saving problem data object for later use... this may take quite some time as well.");
            problemData.save_PA_ProblemData(problemDataFilename);

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

            System.out.println("Saved NN_Generic object loaded successfully!");
            return network;

        } else {
            System.out.println("No saved network file found, creating new network.");
            NN_Generic network = new NN_Generic(LAYER_SIZES, file);
            return network;
        }
    }
}
