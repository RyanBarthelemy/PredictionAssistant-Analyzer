package com.axlor.predictionassistantanalyzer.analyzers.nn_pa;

import com.axlor.predictionassistantanalyzer.exception.MarketNotFoundException;
import com.axlor.predictionassistantanalyzer.model.Contract;
import com.axlor.predictionassistantanalyzer.model.Market;
import com.axlor.predictionassistantanalyzer.service.MarketService;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContractProblemData implements Serializable {

    private static final long serialVersionUID = -919690928730302807L;

    //input layer will look like this:
    //no 'headers'
    //1: minutes since first snapshot (timestampCurrent vs timestampBeginning)
    //2: buyYes
    //3: buyNo
    //....repeat for n snapshots
    //size of inputLayer = 3n

    //output layer:
    //100 index array. 10? mins after final Snapshot

    private int inputTimeFrame = 60; //mins
    private int timeBetweenSnapshots = 2; //mins, ideally a constructor param given by PA-Data
    private int inputErrorMax = 3; //mins, Input layer needs to span 'inputTimeFrame' minutes, but it probably isn't a perfect fit, how many minutes of error between first to last will we accept?

    private int predictionTimeFrame = 10; //mins, how far out do we want our prediction to be? we need this so we know what value to get for training.
    private int outputErrorMaxSeconds = 60; // mins, +/-. Same thing as inputErrorMax basically.

    private int inputLayerSize; //number of neurons in input layer     \  These are different
    private int outputLayerSize; //number of neurons in output layer  /   These are different

    //the sizes of these lists need to be the same, the size of the Lists represents how many training data we have to train with
    private List<double[]> inputLayers = new ArrayList<>();  //probably noticeably better performance if these were both 2d arrays
    private List<double[]> outputLayers = new ArrayList<>(); //although we only build the problem data once, so maybe not a big deal, should profile it though

    private int nonUniqueMarketId;
    private int nonUniqueContractId;

    public ContractProblemData(int nonUniqueMarketId, int nonUniqueContractId, MarketService marketService) throws MarketNotFoundException {

        this.nonUniqueMarketId = nonUniqueMarketId;
        this.nonUniqueContractId = nonUniqueContractId;

        inputLayerSize = (inputTimeFrame/timeBetweenSnapshots) * 2;
        outputLayerSize = 200;

        List<Market> marketHistory = marketService.getMarketHistoryByNonUniqueId(nonUniqueMarketId);
        System.out.println("Num of Market instances in market history for id[" + nonUniqueMarketId + "] --- " + marketHistory.size());
        System.out.println("Building input/output layers for Contract id[" + nonUniqueContractId + "]");

        //need to convert String timestamp to a timestamp we can use and compare, namely the 'long' type
        List<Long> timestamps = new ArrayList<>();
        for (Market market : marketHistory) {
            timestamps.add(
                    java.sql.Timestamp.valueOf(market.getTimeStamp().replace("T", " ")).getTime()
            );
        }

        /*//test
        for (int i = 0; i < timestamps.size(); i++) {
            System.out.println(i + " timestamp: " + timestamps.get(i));
        }
        */

        //timestamps are now in number form and ordered from earliest to latest (ascending). Also, timestamps list and marketHistory list are parallel lists. There indices align.
        //for each timestamp, see if we can build a training problem
        int numOfMarketInstancesNeededForInput = inputTimeFrame/timeBetweenSnapshots; // inputLayerSize
        for (int currentTimestampIndex = 0; currentTimestampIndex < timestamps.size(); currentTimestampIndex++) {

            //are there enough timestamps in range without going out of bounds?
            int finalIndex = currentTimestampIndex+numOfMarketInstancesNeededForInput-1;
            if(finalIndex >= timestamps.size()){
                //System.out.println(currentTimestampIndex + ": finalIndex is >= timestamps size, continuing.");
                continue;
            }

            //are we within the allotted time frame/error
            long firstTimestamp = timestamps.get(currentTimestampIndex);
            long finalTimestamp = timestamps.get(finalIndex);
            //System.out.println("InputLayer: Final index: " + finalIndex + " --- Final timestamp: " + finalTimestamp);
            long timeDiff = (finalTimestamp - firstTimestamp)/60000; //milli to min
            if(Math.abs(timeDiff - inputTimeFrame) > inputErrorMax){
                //System.out.println("timeDiff:" + timeDiff + " ------ inputErrorMax: " + inputErrorMax);
                //System.out.println(currentTimestampIndex + ": timeDiff is > inputErrorMax, continuing.");
                continue;
            }

            //input is possible, output 'should' be possible most of the time but not necessarily if there was a problem on PredictIt's end like maintenance or server down during those times or something...
            //get timestamp of final market instance for this problem, get the timestamp 'predictionTimeFrame' minutes after that.
            // See if it is within 'outputErrorMax' of expected time.
            long theoreticalOutputTimestamp = (predictionTimeFrame*60*1000) + finalTimestamp;
            Market outputMarket = getOutput(theoreticalOutputTimestamp, timestamps, marketHistory, outputErrorMaxSeconds);
            if(outputMarket == null){
                //System.out.println("outputMarket came back null. Continuing");
                continue;
            }

            //build input
            boolean failed = false;
            List<Double> inputLayerList = new ArrayList<>();
            for (int i = currentTimestampIndex; i <= finalIndex; i++) {
                Contract contract = getContract(marketHistory.get(i), nonUniqueContractId);
                if(contract == null){failed = true; break;}
                inputLayerList.add(contract.getBestBuyYesCost());
                inputLayerList.add(contract.getBestBuyNoCost());
            }
            if(failed){continue;}
            if(inputLayerList.size() != inputLayerSize){continue;}

            double[] inputLayer = new double[inputLayerSize];
            for (int i = 0; i < inputLayerList.size(); i++) {
                inputLayer[i] = inputLayerList.get(i);
            }

            //System.out.println(currentTimestampIndex+ ": Successfully created input layer:" + Arrays.toString(inputLayer));

            //build output layer
            double[] outputLayer = new double[outputLayerSize];
            Contract finalContract = getContract(outputMarket, nonUniqueContractId);
            if(finalContract==null){continue;} //should never happen
            double buyYesD = finalContract.getBestBuyYesCost();
            double buyNoD = finalContract.getBestBuyNoCost();
            //System.out.println("Final Contract: buyYes=[" + buyYesD + "], buyNo=[" + buyNoD + "]");
            Arrays.fill(outputLayer, 0.0);

            buyYesD = buyYesD*100.0; //convert cost in cents to index in output layer, $0.56 buyYes cost = index 56 in output layer.
            int buyYesI = (int) buyYesD;

            buyNoD = (buyNoD*100.0) + 100; //add 100 so this is in the second half of the output layer. $0.33 buy no = index 133 in output layer.
            int buyNoI = (int) buyNoD;

            //so those indices' values from 0 to 1.
            outputLayer[buyYesI] = 1;
            outputLayer[buyNoI] = 1;

            //System.out.println("Successfully created output layer:" + Arrays.toString(outputLayer));

            inputLayers.add(inputLayer);
            outputLayers.add(outputLayer);

        }//for loop of each timestamp/market instance

    }//constructor

    private Contract getContract(Market market, int nonUniqueContractId) {
        for (int i = 0; i < market.getContracts().size(); i++) {
            if(market.getContracts().get(i).getId() == nonUniqueContractId){
                return market.getContracts().get(i);
            }
        }
        return null;
    }


    private Market getOutput(long theoreticalOutputTimestamp, List<Long> timestamps, List<Market> marketHistory, int outputErrorMaxSeconds) {
        if(timestamps==null || marketHistory==null){return null;}
        int indexToUse = -1;

        long difference = Long.MAX_VALUE;
        for(int i = 0; i < timestamps.size(); i++){
            long timestampTemp = timestamps.get(i);
            if(Math.abs(timestampTemp - theoreticalOutputTimestamp) < difference){
                indexToUse = i;
                difference = Math.abs(timestampTemp - theoreticalOutputTimestamp); //milliseconds
            }
        }
        if(difference == Long.MAX_VALUE){
            //System.out.println("Never found potential timestamp");
        }
        if(indexToUse<0 || indexToUse >= timestamps.size()){
            return null;
        }

        //are we within error amount?
        if((difference/1000) > outputErrorMaxSeconds){
            //System.out.println("Difference between theoretical output timestamp and actual output timestamp: " + (difference/1000));
            //System.out.println("Accepted outputErrorMaxSeconds: " + outputErrorMaxSeconds + " --- Returning null");
            return null;
        }

        //System.out.println("Successfully found output market to use");
        return marketHistory.get(indexToUse);
    }

    public static ContractProblemData load_ContractProblem_data(String file){
        File inputFile = new File(file);
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(inputFile));
            ContractProblemData problem_data = (ContractProblemData) ois.readObject();
            ois.close();

            return problem_data;

        } catch (IOException ex) {
            System.out.println("Caught an IOException trying to load data from file: " + file);
            System.out.println("Returning null.");
        } catch (ClassNotFoundException ex) {
            System.out.println("Caught ClassNotFoundException trying to caste object to ContractProblemData, returning null.");
        }
        return null;
    }

    public void save_ContractProblem_data(String file){
        File saveFile = new File(file);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile));
            oos.writeObject(this);

            oos.flush();
            oos.close();
            System.out.println("Successfully saved ContractProblemData object to " + file);

        } catch (IOException ex) {
            System.out.println("Caught IOException, did NOT save network to file:" + file);
        }
    }

    public int getNonUniqueMarketId() {
        return nonUniqueMarketId;
    }

    public int getNonUniqueContractId() {
        return nonUniqueContractId;
    }

    public int getNumOfProblems() {
        return inputLayers.size();
    }

    public int getInputLayerSize() {
        return inputLayerSize;
    }

    public int getOutputLayerSize() {
        return outputLayerSize;
    }

    public List<double[]> getInputLayers() {
        return inputLayers;
    }

    public List<double[]> getOutputLayers() {
        return outputLayers;
    }

    public int getInputTimeFrame() {
        return inputTimeFrame;
    }

    public int getTimeBetweenSnapshots() {
        return timeBetweenSnapshots;
    }

    public int getInputErrorMax() {
        return inputErrorMax;
    }
}
