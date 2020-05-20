/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.axlor.predictionassistantanalyzer.analyzers.nn_pa.nn_generic;

import java.util.Random;

/**
 *
 * @author Axlor
 */
public class NN_Tools {

    private NN_Tools() {
    }

    public static double sigmoid(double z) {
        /*
        sigmoid(z) =       1
                      -----------
                       1 + e^(-z)
         */

        return (1 / (1 + Math.pow(Math.E, -1.0 * z)));
    }

    public static double sigmoid_p(double z) {
        //recall sigmoid from the sigmoid function elsewhere in this class.
        //For math reasons the derivative of sigmoid is pretty easy to code.
        //sigmoid_p(x) = [ sigmoid(x) * (1-sigmoid(x)) ]

        return (sigmoid(z) * (1 - sigmoid(z)));
    }

    //returns an array of size -size- where each index contains the value -value-
    public static double[] createArray(int size, double value) {
        if (size <= 0) {
            System.out.println("WARNING: Attempted to create an array of size 0, returning null.");
            return null;
        }
        double[] toReturn = new double[size];
        for (int i = 0; i < size; i++) {
            toReturn[i] = value;
        }
        return toReturn;
    }

    public static double[] createArrayOfRandomValues(int size, double high, double low) {
        if (size <= 0) {
            System.out.println("WARNING: Attempted to create an array of size 0, returning null.");
            return null;
        }

        Random rng = new Random(System.nanoTime());
        double[] toReturn = new double[size];
        for (int i = 0; i < size; i++) {
            toReturn[i] = rng.nextDouble() * (high - low) + low; //next double is [0,1] range, if 0 we get low as value, if 1 we get (high-low)+low = high. Anywhere in between scales onto our range
        }
        return toReturn;
    }

    public static double[][] createRandom_2D_array(int size_X, int size_Y, double high, double low) {
        if (size_X <= 0 || size_Y <= 0) {
            System.out.println("WARNING: Attempted to create 2D array of size 0 in one of the dimensions, returning null.");
            return null;
        }

        double[][] toReturn = new double[size_X][size_Y];
        for (int i = 0; i < size_X; i++) {
            toReturn[i] = createArrayOfRandomValues(size_Y, high, low);
        }

        return toReturn;
    }

    public static Integer[] createRandomIntegerArray_noDuplicates(int lowerBound, int upperBound, int amount) {

        lowerBound --;

        if(amount > (upperBound-lowerBound)){
            return null;
        }

        Integer[] values = new Integer[amount];
        for(int i = 0; i< amount; i++){
            int n = (int)(Math.random() * (upperBound-lowerBound+1) + lowerBound);
            while(containsValue(values, n)){
                n = (int)(Math.random() * (upperBound-lowerBound+1) + lowerBound);
            }
            values[i] = n;
        }
        return values;
    }

    public static <T extends Comparable<T>> boolean containsValue(T[] array, T value) {
        //returns true if the array -array- of type -T- contains value -value- of type -T-
        //returns false otherwise
        for (T arrayValue : array) {
            if (arrayValue != null && value.compareTo(arrayValue) == 0) {
                return true;
            }
        }
        return false;
    }

    public static int getIndexOfHighestValueInArray(double[] values){
        int currentHighestIndex = 0;
        for (int i = 0; i < values.length; i++) {
            if(values[i] > values[currentHighestIndex]){
                currentHighestIndex = i;
            }
        }
        return currentHighestIndex;
    }
    
    
}
