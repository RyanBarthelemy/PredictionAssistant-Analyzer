package com.axlor.predictionassistantanalyzer.analyzers;

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
public class NN_PA_Trainer {

    @Autowired
    SnapshotService snapshotService;

    @PostConstruct
    public void main_NN_method() throws SnapshotCountMismatchException, NoSnapshotsInDatabaseException, SnapshotNotFoundException { //will rename or something later.
        int[] LAYER_SIZES;
        int inputLayerSize;
        int outputLayerSize;
        String networkFilename = "NN_PA_obj";

        System.out.println("Building PA training data, this could take a long time...");
        PA_ProblemData pa_trainData = new PA_ProblemData(snapshotService); //this could take a while

        /*
        System.out.println("PA_ProblemData object created with " + pa_trainData.getNumOfProblems() + "problem data to train with.");

        inputLayerSize = pa_trainData.getInputLayerSize();
        outputLayerSize = pa_trainData.getOutputLayerSize();

        TrainingDataSet trainingDataSet = new TrainingDataSet(inputLayerSize, outputLayerSize);
        System.out.println("Size of training set: " + pa_trainData.getNumOfProblems());

        for (int i = 0; i < pa_trainData.getNumOfProblems(); i++) {
            trainingDataSet.addDataToList(pa_trainData.getInputLayers().get(i),pa_trainData.getOutputLayers().get(i));
        }

        LAYER_SIZES = new int[] {inputLayerSize, 200, 100, 100, outputLayerSize};
        //NN_Generic neural_network = loadNetwork(LAYER_SIZES, networkFilename);

        //neural_network.trainNetworkUsingTrainingDataSet(trainingDataSet,100,100);
*/
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
