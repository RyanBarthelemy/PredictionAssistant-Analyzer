//This class represents a set of input measurements and output target values that can be used to train a NN
package com.axlor.predictionassistantanalyzer.analyzers.nn_pa.nn_generic;

import java.util.ArrayList;

/*
Each element of the -data- arrayList will contain a 2D array
This example represents a single element of the data arrayList
ex: data arrayList at index 0 returns a 2D array [][]
[]
0: input = [1, 2, 5, 10, ....]     //size of this array at index 0 is INPUT_SIZE
1: output = [1, 0 , 0, 0, 1 ....]  //size of this array at index 1 is OUTPUT_SIZE
 */
public class TrainingDataSet {

    public final int INPUT_SIZE;
    public final int OUTPUT_SIZE;

    private ArrayList<double[][]> trainingDataList = new ArrayList<>();

    public TrainingDataSet(int inputSize, int outputSize) {
        this.INPUT_SIZE = inputSize;
        this.OUTPUT_SIZE = outputSize;
    }

    public int getINPUT_SIZE() {
        return INPUT_SIZE;
    }

    public int getOUTPUT_SIZE() {
        return OUTPUT_SIZE;
    }

    public void addDataToList(double[] inputs, double[] targets) {
        if (inputs.length != INPUT_SIZE || targets.length != OUTPUT_SIZE) {
            System.out.println("WARNING: Attempted to add a data set to the data set list but found a size mismatch among inputs or outputs/targets:");
            System.out.println("input parameter length = " + inputs.length);
            System.out.println("INPUT_SIZE = " + INPUT_SIZE);
            System.out.println("targets length " + targets.length);
            System.out.println("OUTPUT_SIZE = " + OUTPUT_SIZE);
            System.out.println("Did not add data to training set, please fix size mismatch(es) and try again.");
            return;
        }
        trainingDataList.add(new double[][]{inputs, targets});
    }

    //attempts to create a batch of training data from randomly selected training data available. That is, a randomly selected subset of possible training data in the trainingDataList
    //it does not repeat any training data elements
    //if batch size given by the parameter is too big or small it just returns this TrainingDataSet object for them to use instead.
    public TrainingDataSet extractBatch(int size) {
        if (size > 0 && size <= this.getSizeOfTrainingDataSet()) {
            Integer[] randomSelections = NN_Tools.createRandomIntegerArray_noDuplicates(0, trainingDataList.size()-1, size);
            //training data in traningDataList has now been randomly selected, the indices of which are in randomSelections, so we just add those to the set we wish to return
            TrainingDataSet randomizedSubsetBatch = new TrainingDataSet(INPUT_SIZE, OUTPUT_SIZE);

            for (int i : randomSelections) {
                randomizedSubsetBatch.addDataToList(getInput(i), getOutput(i));
            }
            return randomizedSubsetBatch;
        } else {
            System.out.println("WARNING: Batch size too big, returning original dataset object");
            return this;
        }

    }

    public double[] getInput(int index) {
        if (index >= 0 && index < trainingDataList.size()) {
            return trainingDataList.get(index)[0];
        } else {
            return null;
        }
    }

    public double[] getOutput(int index) {
        if (index >= 0 && index < trainingDataList.size()) {
            return trainingDataList.get(index)[1];
        } else {
            return null;
        }
    }

    public int getSizeOfTrainingDataSet() {
        return trainingDataList.size();
    }

}
