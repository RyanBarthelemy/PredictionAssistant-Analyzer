package com.axlor.predictionassistantanalyzer.analyzers.nn_pa.nn_generic.nn_problems;

import com.axlor.predictionassistantanalyzer.analyzers.nn_pa.nn_generic.NN_Generic;
import com.axlor.predictionassistantanalyzer.analyzers.nn_pa.nn_generic.TrainingDataSet;

import java.util.Arrays;

public class NN_Test {

    public static void main(String[] args) {
        int[] LAYER_SIZES;
        String fileName = "NN_basicTest";

        LAYER_SIZES = new int[]{4, 3, 3, 2};

        NN_Generic network = new NN_Generic(LAYER_SIZES, fileName);
        
        String imagesFilename = "C:\\Users\\Axlor\\Documents\\MNIST_ProblemDataSets\\train-images.idx3-ubyte";
        String labelsFilename = "C:\\Users\\Axlor\\Documents\\MNIST_ProblemDataSets\\train-labels.idx1-ubyte";
        //MNIST_Problem mnistTrain = new MNIST_Problem(imagesFilename, labelsFilename);
        //TrainingDataSet tds = new TrainingDataSet(-1, 10);
        

        
        TrainingDataSet tds = new TrainingDataSet(4, 2);
        tds.addDataToList(new double[]{0.1, 0.2, 0.3, 0.4}, new double[]{0.9, 0.0});
        tds.addDataToList(new double[]{0.9, 0.8, 0.7, 0.6}, new double[]{0.1, 0.9});
        tds.addDataToList(new double[]{0.3, 0.8, 0.1, 0.4}, new double[]{0.3, 0.7});
        tds.addDataToList(new double[]{0.9, 0.8, 0.1, 0.2}, new double[]{0.7, 0.3});        
        
        
        network.trainNetworkUsingTrainingDataSet(tds, 10000000, 4);
        

        
        for (int i = 0; i < tds.getSizeOfTrainingDataSet(); i++) {
            System.out.println(Arrays.toString(network.calculateActivationsOfAllLayers(tds.getInput(i))));
        }

        //TODO: data visualization
    }

}




/*
package nn_generic;

import java.util.Arrays;
import nn_problems.MNIST_Problem;

public class NN_Test {

    public static void main(String[] args) {
        int[] LAYER_SIZES;
        int inputSize;
        int outputSize;

        String imagesFilename = "C:\\Users\\Axlor\\Documents\\MNIST_ProblemDataSets\\train-images.idx3-ubyte";
        String labelsFilename = "C:\\Users\\Axlor\\Documents\\MNIST_ProblemDataSets\\train-labels.idx1-ubyte";
        MNIST_Problem mnistTrain = new MNIST_Problem(imagesFilename, labelsFilename);

        inputSize = mnistTrain.getImagesPixelValues()[0].length;
        outputSize = 10;

        TrainingDataSet tds = new TrainingDataSet(inputSize, outputSize);

        LAYER_SIZES = new int[]{inputSize, 6, 3, outputSize};

        NN_Generic network = new NN_Generic(LAYER_SIZES);

        System.out.println("size of training set: " + mnistTrain.getNumImages());
        System.out.println("input vector at index 0 of image data matrix:");
        System.out.println(Arrays.toString(mnistTrain.getImagesPixelValues()[0]));
        System.out.println("");
        System.out.println("output vector generated from label data at index 59999 of label array");
        System.out.println(Arrays.toString(getOutputVector(mnistTrain.getImageLabels()[5999])));

        for (int i = 0; i < mnistTrain.getNumImages(); i++) {
            tds.addDataToList(mnistTrain.getImagesPixelValues()[i], getOutputVector(mnistTrain.getImageLabels()[i]));
            if (i % (mnistTrain.getNumImages()/ 10) == 0) {
                System.out.println(i + ": " + "Input vector to add to TDS = " + Arrays.toString(mnistTrain.getImagesPixelValues()[i]));
            }

        }

        network.trainNetworkUsingTrainingDataSet(tds, 10000, 500);

        
        //this for loop isnt working
        for (int i = 0; i < tds.getSizeOfTrainingDataSet(); i++) {

            if (i % (tds.getSizeOfTrainingDataSet() / 10) == 0) {
                System.out.println(i + ": " + Arrays.toString(network.calculateActivationsOfAllLayers(tds.getInput(i))));
            }
        }

        //TODO: save/load
        //TODO: data visualization
    }

    public static double[] getOutputVector(double digit) {

        if (digit == 0) {
            return new double[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        }
        if (digit == 1) {
            return new double[]{0, 1, 0, 0, 0, 0, 0, 0, 0, 0};
        }
        if (digit == 2) {
            return new double[]{0, 0, 1, 0, 0, 0, 0, 0, 0, 0};
        }
        if (digit == 3) {
            return new double[]{0, 0, 0, 1, 0, 0, 0, 0, 0, 0};
        }
        if (digit == 4) {
            return new double[]{0, 0, 0, 0, 1, 0, 0, 0, 0, 0};
        }
        if (digit == 5) {
            return new double[]{0, 0, 0, 0, 0, 1, 0, 0, 0, 0};
        }
        if (digit == 6) {
            return new double[]{0, 0, 0, 0, 0, 0, 1, 0, 0, 0};
        }
        if (digit == 7) {
            return new double[]{0, 0, 0, 0, 0, 0, 0, 1, 0, 0};
        }
        if (digit == 8) {
            return new double[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 0};
        }
        if (digit == 9) {
            return new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        }

        throw new Error("ERROR: Attempted to create output vector from label's digit but input parameter digit was not in range [0,9]");
    }
}

*/
