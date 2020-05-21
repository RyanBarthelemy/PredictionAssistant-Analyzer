package com.axlor.predictionassistantanalyzer.analyzers.nn_pa.nn_generic.nn_problems;

import com.axlor.predictionassistantanalyzer.analyzers.nn_pa.nn_generic.NN_Generic;
import com.axlor.predictionassistantanalyzer.analyzers.nn_pa.nn_generic.TrainingDataSet;

import java.io.File;

public class NN_MNIST_Test {

    public static void main(String[] args) {
        int[] LAYER_SIZES;
        int inputLayerSize;
        int outputLayerSize;
        String networkFilename = "NN_MNIST_obj";
        String mnistTDFilename = "mnist_trainingData_object";

        MNIST_Problem mnistTrain = get_MNIST_trainingData(mnistTDFilename);

        inputLayerSize = mnistTrain.getImagesPixelValues()[0].length;
        outputLayerSize = 10; //each neuron of the output layer represents a number between 0 and 9 inclusive. Hence the 10 output layer size.

        TrainingDataSet tds = new TrainingDataSet(inputLayerSize, outputLayerSize);
        System.out.println("Size of training set: " + mnistTrain.getNumImages());
        for (int i = 0; i < mnistTrain.getNumImages(); i++) {
            tds.addDataToList(mnistTrain.getImagesPixelValues()[i], getOutputVector(mnistTrain.getImageLabels()[i]));
            /*
            if (i % (mnistTrain.getNumImages() / 10) == 0) {
                System.out.println(i + ": " + "Input vector to add to TDS = " + Arrays.toString(mnistTrain.getImagesPixelValues()[i]));
            }
             */
        }

        LAYER_SIZES = new int[]{inputLayerSize, 70, 35, outputLayerSize};
        NN_Generic network = loadNetwork(LAYER_SIZES, networkFilename);
                
        System.out.println("\n-----------------------------------------------");
        System.out.println("Prior to Training:");
        evaluateNetwork(tds, network, mnistTrain);
        System.out.println("-----------------------------------------------");
        
        network.trainNetworkUsingTrainingDataSet(tds, 100, 10);

        //
        //
        //
        
        System.out.println("\n-----------------------------------------------");
        System.out.println("After Training:");
        evaluateNetwork(tds, network, mnistTrain);
        System.out.println("-----------------------------------------------");

        //TODO: data visualization
    }

    public static double[] getOutputVector(double digit) {
        double[] toReturn = null;

        if (digit == 0) {
            toReturn = new double[]{1.0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        }
        if (digit == 1) {
            toReturn = new double[]{0, 1.0, 0, 0, 0, 0, 0, 0, 0, 0};
        }
        if (digit == 2) {
            toReturn = new double[]{0, 0, 1.0, 0, 0, 0, 0, 0, 0, 0};
        }
        if (digit == 3) {
            toReturn = new double[]{0, 0, 0, 1.0, 0, 0, 0, 0, 0, 0};
        }
        if (digit == 4) {
            toReturn = new double[]{0, 0, 0, 0, 1.0, 0, 0, 0, 0, 0};
        }
        if (digit == 5) {
            toReturn = new double[]{0, 0, 0, 0, 0, 1.0, 0, 0, 0, 0};
        }
        if (digit == 6) {
            toReturn = new double[]{0, 0, 0, 0, 0, 0, 1.0, 0, 0, 0};
        }
        if (digit == 7) {
            toReturn = new double[]{0, 0, 0, 0, 0, 0, 0, 1.0, 0, 0};
        }
        if (digit == 8) {
            toReturn = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 1.0, 0};
        }
        if (digit == 9) {
            toReturn = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1.0};
        }
        //yes there is a better way to do this, still debugging this though

        //System.out.println("digit = " + digit + " ---- returning output vector: " + Arrays.toString(toReturn));
        if (toReturn == null) {
            throw new Error("ERROR: Attempted to create output vector from label's digit but input parameter digit was not in range [0,9]");
        }

        return toReturn;

    }

    private static int getGuess(double[] resultVector) {
        int guess = -1;
        double currMax = 0;

        for (int i = 0; i < resultVector.length; i++) {
            if (resultVector[i] > currMax) {
                guess = i;
                currMax = resultVector[i];
            }
        }

        return guess;

    }

    private static MNIST_Problem get_MNIST_trainingData(String file) {
        String imagesFilename = "C:\\Users\\Axlor\\Documents\\MNIST_ProblemDataSets\\train-images.idx3-ubyte";
        String labelsFilename = "C:\\Users\\Axlor\\Documents\\MNIST_ProblemDataSets\\train-labels.idx1-ubyte";

        File testFile = new File(file);
        if (testFile.exists()) {
            System.out.println("Found saved MNIST_Problem object, attempting to load file...");
            MNIST_Problem mnistTrain = MNIST_Problem.load_MNIST_data(file);

            if (mnistTrain == null) {
                System.out.println("Found " + file + " but failed to read it in. Creating new MNIST_Problem object from scratch...");
                mnistTrain = new MNIST_Problem(imagesFilename, labelsFilename);
                mnistTrain.save_MNIST_data(file);
            }

            System.out.println("Successfully read in MNIST_Problem data object.");
            return mnistTrain;

        } else {
            MNIST_Problem mnistTrain = new MNIST_Problem(imagesFilename, labelsFilename);
            mnistTrain.save_MNIST_data(file);
            return mnistTrain;
        }
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

    private static void evaluateNetwork(TrainingDataSet tds, NN_Generic network, MNIST_Problem mnistTrain) {
                //this for loop isnt working?
        int correct = 0;
        for (int i = 0; i < tds.getSizeOfTrainingDataSet(); i++) {

            /*
            if (i % (tds.getSizeOfTrainingDataSet() / 10) == 0) {
                System.out.println(i + ": " + Arrays.toString(network.calculateActivationsOfAllLayers(tds.getInput(i))));
                System.out.println("Guess at image# " + i + ": " + getGuess(network.calculateActivationsOfAllLayers(tds.getInput(i))));
                System.out.println("Target/Label at label# " + i + ": " + mnistTrain.getImageLabels()[i]);
                System.out.println("");
            }
             */
            if (getGuess(network.calculateActivationsOfAllLayers(tds.getInput(i))) == mnistTrain.getImageLabels()[i]) {
                correct++;
            }
        }

        System.out.println("Total correct: " + correct);
        System.out.println("Total possible: " + tds.getSizeOfTrainingDataSet());
        System.out.println("Percent Correct: %" + 100.0 * (correct / (double) tds.getSizeOfTrainingDataSet()));
    }
}
