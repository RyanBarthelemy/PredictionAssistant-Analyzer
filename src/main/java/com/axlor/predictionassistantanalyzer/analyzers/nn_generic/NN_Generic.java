package com.axlor.predictionassistantanalyzer.analyzers.nn_generic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class NN_Generic implements Serializable{
    
    private final String FILENAME;

    private final double WEIGHT_INIT_LOW;
    private final double WEIGHT_INIT_HIGH;
    private final double BIAS_INIT_LOW;
    private final double BIAS_INIT_HIGH;

    private final double LEARNING_RATE;

    private final int NUM_OF_LAYERS;          //the sum of input layer + hidden layers + output layer. How many collections of neurons there are, separated by steps, don't overthink this, it is typically how many columns there are visually
    private final int LAYER_SIZES[];          //How many activations/neurons there are on each each layer
    private final int INPUT_LAYER_SIZE;
    private final int OUTPUT_LAYER_SIZE;

    private double[][] activations; //he calls this 'output' ====  [layer we are in][neuron in that layer] : value held is the prediction calculated?
    private double[][][] weights; //[layer we are in][neuron in that layer][neuron in previous layer? a^(L-1)?
    private double[][] bias; //[layer we are in][neuron in that layer]
    private double[][] errors;

    private double[][] activation_derivatives; //hold signmoid_prime of the -z- values

    public NN_Generic(int[] LAYER_SIZES, String networkFilename) {
        System.out.println("-------------------------------------------------------");
        System.out.println("Attempting to build generic Neural Network object:");
        WEIGHT_INIT_LOW = -1.0;
        WEIGHT_INIT_HIGH = 1.0;
        BIAS_INIT_LOW = -0.5;
        BIAS_INIT_HIGH = .5;
        LEARNING_RATE = 0.008;

        FILENAME = networkFilename;
        this.LAYER_SIZES = LAYER_SIZES;
        INPUT_LAYER_SIZE = LAYER_SIZES[0];
        OUTPUT_LAYER_SIZE = LAYER_SIZES[LAYER_SIZES.length - 1];

        NUM_OF_LAYERS = LAYER_SIZES.length;
        System.out.println("Total Number of Layers = " + NUM_OF_LAYERS + "  ------  1 input layer, " + (NUM_OF_LAYERS - 2) + " hidden layer(s), 1 output layer");

        System.out.println("Length of Target Vector matches length of Output Layer, continuing.");
        System.out.println("\nInitializing network components...");
        createNetworkComponents();

        System.out.println("Network Successfully Constructed");
        System.out.println("------------------------------------------------------------");

    }//constructor

    public NN_Generic(int[] LAYER_SIZES, String networkFilename, double initWeightLow, double initWeightHigh, double initBiasLow, double initBiasHigh, double learningRate) {
        System.out.println("-------------------------------------------------------");
        System.out.println("Attempting to build generic Neural Network object with extended parameters:");
        WEIGHT_INIT_LOW = initWeightLow;
        WEIGHT_INIT_HIGH = initWeightHigh;
        BIAS_INIT_LOW = initBiasLow;
        BIAS_INIT_HIGH = initBiasHigh;
        LEARNING_RATE = learningRate;

        FILENAME = networkFilename;
        this.LAYER_SIZES = LAYER_SIZES;
        INPUT_LAYER_SIZE = LAYER_SIZES[0];
        OUTPUT_LAYER_SIZE = LAYER_SIZES[LAYER_SIZES.length - 1];

        NUM_OF_LAYERS = LAYER_SIZES.length;
        System.out.println("Total Number of Layers = " + NUM_OF_LAYERS + "  ------  1 input layer, " + (NUM_OF_LAYERS - 2) + " hidden layer(s), 1 output layer");

        System.out.println("Checking Layer Sizes...");
        System.out.println("Length of Target Vector matches length of Output Layer, continuing.");
        System.out.println("\nInitializing network components...");
        createNetworkComponents();

        System.out.println("Network Successfully Constructed");
        System.out.println("------------------------------------------------------------");

    }//constructor

    private void createNetworkComponents() {
        activations = new double[NUM_OF_LAYERS][];
        weights = new double[NUM_OF_LAYERS][][];
        bias = new double[NUM_OF_LAYERS][];
        errors = new double[NUM_OF_LAYERS][];
        activation_derivatives = new double[NUM_OF_LAYERS][];

        System.out.println("Initialized: Weights, Bias, and Predictions containers");

        for (int i = 0; i < NUM_OF_LAYERS; i++) {
            activations[i] = new double[LAYER_SIZES[i]];
            activation_derivatives[i] = new double[LAYER_SIZES[i]];

            bias[i] = new double[LAYER_SIZES[i]];
            errors[i] = new double[LAYER_SIZES[i]];
            //randomize initial values for biases
            bias[i] = NN_Tools.createArrayOfRandomValues(LAYER_SIZES[i], BIAS_INIT_HIGH, BIAS_INIT_LOW);

            //weights are on every layer except the input layer, so we only want to do the following on the non-input layers
            if (i > 0) {
                weights[i] = new double[LAYER_SIZES[i]][LAYER_SIZES[i - 1]]; //the size of the 2d weight matrix depends on the layer size of current layer and of previous layer, do that for all layers except input layer where it wouldnt make sense
                //randomize initial values for weights, note weights[i] contains a 2D array so we need to create a random 2D array of correct sizes based on neuron numbers in layer i and layer i-1
                weights[i] = NN_Tools.createRandom_2D_array(LAYER_SIZES[i], LAYER_SIZES[i - 1], WEIGHT_INIT_HIGH, WEIGHT_INIT_LOW);
            }
        }

        System.out.println("Initialized various Vectors/Matrices for Weights, Bias, and Predictions.");
        System.out.println("Randomized initial values for Weights and Biases.");
        System.out.println("activations: " + Arrays.deepToString(activations));
        System.out.println("weights: " + Arrays.deepToString(weights));
        System.out.println("bias: " + Arrays.deepToString(bias));

    }

    //this calculates the prediction value of all neurons in the network and returns the final vector/array, that is the output layer of the network
    public double[] calculateActivationsOfAllLayers(double... input) {
        //check sizes to make sure they match
        if (input.length != INPUT_LAYER_SIZE) {
            System.out.println("ERROR: input parameter length != INPUT_LAYER_SIZE, no meaningful way to continue, halting program.");
            System.exit(-2);
        }

        activations[0] = input;

        //calculate all the hidden layers and output layer next: //make sure to start and index=1 since we don't need to do the input layer
        for (int layer = 1; layer < NUM_OF_LAYERS; layer++) {
            //for each layer, iterate over each neuron in that layer, here we do start at index 0 because we want to calc all neurons in that column/layer
            for (int neuron = 0; neuron < LAYER_SIZES[layer]; neuron++) {
                //now we need to actually calculate the prediction for that specific neuron.
                //we do that in two parts: 1)calculate -z- from prev layer, weights, and bias, AND 2) calculate prediction by normalizing -z- using sigmoid or RELU etc, I use sigmoid in NN_Tools
                double z = 0;
                for (int prevNeuron = 0; prevNeuron < LAYER_SIZES[layer - 1]; prevNeuron++) {
                    z = z + activations[layer - 1][prevNeuron] * weights[layer][neuron][prevNeuron]; //a^(1) = W * a^(0) + b, note we still need b, the bias
                }
                z = z + bias[layer][neuron]; //add the bias to the -z- of this specific neuron at [layer][neuron]

                //now we have -z-, we need to apply normalization to it.
                activations[layer][neuron] = NN_Tools.sigmoid(z);
                //NOTE: I find it simpler to calculate signmoid prime of my -z- values during the feed foward progress.
                //Yes it is part of the backpropagation algorithm but we don't store -z- anywhere and we'd need to calculate it again in back prop alg if we
                //      didn't just find sigmoid_prime values here in forward prop.
                activation_derivatives[layer][neuron] = NN_Tools.sigmoid_p(z);
            }
        }

        //we have now calculated and populated the prediction values for all neurons in the network.
        //The feed forward portion is completed for a single iteration of the network. This function now just needs to return the output layer of the network
        return activations[NUM_OF_LAYERS - 1];

    }

    public void trainNetwork(double[] input, double[] target) {

        if(input == null && target == null){
            System.out.println("Both input[] and target[] arrays are null, did not get passed in correctly to trainNetwork(...), check method call");
            System.out.println("Exiting~~~~~~");
            System.exit(-1);
        }
        
        if(input == null){
            System.out.println("input[] array is null, exiting~~~~~");
            System.exit(-1);
        }
        if(target == null){
            System.out.println("target[] array is null, exiting~~~~~");
            System.exit(-1);
        }
        

        if (input.length != INPUT_LAYER_SIZE || target.length != OUTPUT_LAYER_SIZE) {
            System.out.println("WARNING: input parameter length = " + input.length);
            System.out.println("WARNING: INPUT_LAYER_SIZE = " + INPUT_LAYER_SIZE);
            System.out.println("WARNING: target parameter length = " + target.length);
            System.out.println("WARNING: LAST_LAYER_SIZE = " + OUTPUT_LAYER_SIZE);
            System.out.println("WARNING: Attempted Backpropagation Algorithm with mismatched sizes of input size and input layer size OR mismatch of target size and last layer size. Exiting training function.");
            return;
        }
        //forward prop
        //you could update learning rate as you go and this would be a good spot to do it, but I just haven't found a meaningful way to do that yet.
        //Need to read some papers and see if it is actually worth...
        calculateActivationsOfAllLayers(input);
        calculateBackPropagationAndUpdate(target);
    }

    public void trainNetworkUsingTrainingDataSet(TrainingDataSet td_set, int iterations, int batchSizePerLoop) {
        
        if(td_set == null){
            System.out.println("TrainingDataSet object passed in to trainNetworkUsingTrainingDataSet(...) method was null. Exiting program~~~~~~");
            System.exit(-2);
        }
        
        if (td_set.INPUT_SIZE != this.INPUT_LAYER_SIZE || td_set.OUTPUT_SIZE != this.OUTPUT_LAYER_SIZE) {
            System.out.println("WARNING: Found size mismatch in input/output sizes of training data and network size. Please fix and try again.");
            return;
        }

        System.out.println("Training NN: This may take some time...");
        for (int i = 0; i < iterations; i++) {
            if (i % (iterations / 10) == 0) {
                System.out.println("Training NN: Iteration# " + i + " out of a total " + iterations);
                System.out.println("Average Cost across entire training data set: " + getAvgCost(td_set));
                System.out.println("Attempting to save network state:");
                this.saveNN(FILENAME);
            }

            
            //for each training iteration create a new batch on which to train the network
            TrainingDataSet batch = td_set.extractBatch(batchSizePerLoop);
            
            if(batch == null){
                System.out.println("TrainingDataSet batch object is null, was not created correctly by extractBatch(...) method. Exiting~~~~~");
                System.exit(-3);
            }
                                              
            for (int j = 0; j < batch.getSizeOfTrainingDataSet(); j++) {
                trainNetwork(batch.getInput(j), batch.getOutput(j));
            }
            
        }
    }

    public void calculateBackPropagationAndUpdate(double[] target) {

        //this for loop gives us the error values of the neurons for the output layer only, these are the final errors we wish to minimize to maximize accuracy
        for (int neuron = 0; neuron < LAYER_SIZES[NUM_OF_LAYERS - 1]; neuron++) {
            //let (pred-target) = x and y=0.5x^2 be cost, then dy/dx = x = (pred-target) = 1/2 dC/dPred
            //then we multiply by sigmoid_Prime of the -z- which we already calculated in forward propagation
            errors[NUM_OF_LAYERS - 1][neuron] = (activations[NUM_OF_LAYERS - 1][neuron] - target[neuron])
                    * activation_derivatives[NUM_OF_LAYERS - 1][neuron];
        }

        //now we need to calculate the errors for the rest of the layers.
        //Note: we go from right to left so to speak until we reach the input layer.
        //We do not need to calculate errors of the input layer though because those are just given measurements and it wouldn't make sense to do anything there
        //We are just finding error for all neurons on all hidden layers
        //we start at the right-most hidden layer, since our output layer is at index NUM_OF_LAYERS-1, our right most hidden layer is at index NUM_OF_LAYERS-2
        for (int layer = NUM_OF_LAYERS - 2; layer > 0; layer--) {
            for (int neuron = 0; neuron < LAYER_SIZES[layer]; neuron++) {
                double sum = 0;
                //in forward propagation the previous layer impacts the activations of the current layer, obvious since an activation is the weighted sum of previous layer activations
                //however, in back propagation we are impacted by the errors of the next layer, makes perfect sense if you think about it...
                //The level of wrongness for the final hidden layer depends on how wrong the output layer happens to be. etc, all the way back.
                //so we need to find the sum of all the products of weights*errors from the next layer, the layer just to the right so to speak of our current layer.
                for (int nextNeuron = 0; nextNeuron < LAYER_SIZES[layer + 1]; nextNeuron++) {
                    sum = sum + weights[layer + 1][nextNeuron][neuron] * errors[layer + 1][nextNeuron];
                }
                errors[layer][neuron] = sum * activation_derivatives[layer][neuron];
            }
        }

        //then we just need to update the weights based on these errors and the learning rate
        updateWeightsAndBiases();

    }

    private void updateWeightsAndBiases() {
        //doesnt matter what direction you update weights in since we won't use them again until all are updated
        //we start at layer=1 since the index 0 layer is the input layer that has no weights to be modified
        for (int layer = 1; layer < NUM_OF_LAYERS; layer++) {
            for (int neuron = 0; neuron < LAYER_SIZES[layer]; neuron++) {
                //for each neuron on our current layer, iterate through all weights influencing the activation of that neuron, and update it
                for (int prevNeuron = 0; prevNeuron < LAYER_SIZES[layer - 1]; prevNeuron++) {
                    //weights[layer][neuron][prevNeuron]
                    //weight change = negative of ::: learningRate * slope //slope~gradient --> dcost_weight -->derivative of cost with respect to weight
                    //this happens to be activation of previous neuron connecting to this neuron multiplied by the error of this neuron. EZ
                    //do that for all weights connecting to this neuron, update the weights as we go.
                    double weightChange = -1.0 * LEARNING_RATE * activations[layer - 1][prevNeuron] * errors[layer][neuron];
                    weights[layer][neuron][prevNeuron] = weights[layer][neuron][prevNeuron] + weightChange; //we already specificed we are changing by a negative amount, can move the minus here and remove the -1.0 off change calculation but it doesnt matter...
                }

                //each neuron is impacted by activations of multiple previous neurons, so we needed to loop to update the weights from the prevNeuron to this current neuron.
                //However, each neuron is only impacted by one bias, and the derivative of the cost with respect to that bias is easy to calculate from what we already have done via weights
                double biasChange = -1.0 * LEARNING_RATE * errors[layer][neuron];
                bias[layer][neuron] = bias[layer][neuron] + biasChange;
            }
        }
    }

    //find the sum of the (pred-target)^2 for all the neurons in the output layer, divide by how many neurons there are on that layer
    //this gives you the avg cost, might be called mean squared error or other things like that.
    public double getAvgCost(double[] input, double[] target) {
        //as always, check input and output sizes
        if (input.length != INPUT_LAYER_SIZE || target.length != OUTPUT_LAYER_SIZE) {
            System.out.println("WARNING: Attempted to call getAvgCost in NN_Generic using mismatched sizes of input or output, returning -1.");
            return -1;
        }

        this.calculateActivationsOfAllLayers(input);
        double cost = 0;
        for (int i = 0; i < target.length; i++) {
            //prediction = activation of final layer aka output layer at neuron i, subtract target from that, take to power of 2
            cost = cost + Math.pow(activations[NUM_OF_LAYERS - 1][i] - target[i], 2);
        }
        return cost / (4d * target.length);
    }

    //finds the mean squared error of all the datasets and finds the average of them.
    public double getAvgCost(TrainingDataSet tds) {
        double sumOfAvgs = 0;
        for (int i = 0; i < tds.getSizeOfTrainingDataSet(); i++) {
            sumOfAvgs = sumOfAvgs + getAvgCost(tds.getInput(i), tds.getOutput(i));
        }
        return sumOfAvgs / tds.getSizeOfTrainingDataSet();
    }
    
    
    public void saveNN(String file){
        File saveFile = new File(file);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile));
            oos.writeObject(this);
            
            oos.flush();
            oos.close();
            System.out.println("Successfully saved NN_Generic object to " + file);
            
        } catch (IOException ex) {
            System.out.println("Caught IOException, did NOT save network to file:" + file);
        }
        
    }
    
    public static NN_Generic loadNN(String file){
        File inputFile = new File(file);
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(inputFile));
            NN_Generic network = (NN_Generic) ois.readObject();
            ois.close();
            
            return network;
            
        } catch (IOException ex) {
            System.out.println("Caught an IOException trying to load data from file: " + file);
            System.out.println("Returning null.");
        } catch (ClassNotFoundException ex) {
            System.out.println("Caught ClassNotFoundException trying to caste object to NN_Generic, returning null.");
        }
          
        return null;
    }

}
