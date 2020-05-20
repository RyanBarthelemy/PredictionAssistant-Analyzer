package com.axlor.predictionassistantanalyzer.analyzers.nn_pa.deprecated;

import com.axlor.predictionassistantanalyzer.analyzers.nn_pa.nn_generic.NN_Generic;
import com.axlor.predictionassistantanalyzer.model.Snapshot;
import com.axlor.predictionassistantanalyzer.service.SnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@Deprecated
public class NN_PA_Evaluator {

    @Autowired
    SnapshotService snapshotService;

    //from contract id, get NN's prediction
    public void predictContract(int nonUniqueMarketId, int nonUniqueContractId, PA_ProblemData problemData, NN_Generic network){

        int acceptedErrorMins = 5;

        System.out.println("------------------------------------------------------------------");
        System.out.println("Beginning evaluation of contract id: " + nonUniqueContractId);
        //figure out Snapshots you need from list of timestamps
        int numSnapshotsNeeded = problemData.getInputTimeFrame()/problemData.getTimeBetweenSnapshots(); //30?
        System.out.println("Number of snapshots needed from the db: " + numSnapshotsNeeded);
            //error check it
        int numSnapErrorCheck = ((problemData.getInputLayerSize()-2)/3); //magic numbers, I know, go read Pa_ProblemData, this isn't good software engineering right now.
        if(numSnapshotsNeeded != numSnapErrorCheck){
            //System.out.println("Calculated number of snapshots needed did not match number calculated for error check. Returning");
            return;
        }

        //get the Snapshots needed if timestamps say we can.
        List<Long> timestamps = snapshotService.getTimestamps();
        List<Long> timestampsNeeded = new ArrayList<>();
        for (int i = 0; i < numSnapshotsNeeded; i++) {
            timestampsNeeded.add(timestamps.get(i));
        }

        //error checking time range
        long timeRange = Math.abs(timestampsNeeded.get(timestampsNeeded.size()-1) - timestampsNeeded.get(0));
        timeRange = timeRange/1000;//to seconds
        timeRange = timeRange/60;//to minutes
        //System.out.println("Last " + numSnapshotsNeeded + " Snapshots time range = " + timeRange + " minutes");
        //System.out.println("Required time range: " + problemData.getInputTimeFrame());
        if(Math.abs(problemData.getInputTimeFrame()-timeRange) > acceptedErrorMins){
            //System.out.println("Difference in required vs actual time range is too high, returning");
            return;
        }

        //timestamps acquired and time range is good, build input layer from contracts
        double[] inputLayer = new double[problemData.getInputLayerSize()];
        List<Double> inputLayerList = new ArrayList<>();

        inputLayerList.add((double) nonUniqueMarketId);
        inputLayerList.add((double) nonUniqueContractId);

        //loop:
        //buyYes
        //buyNo
        //timestamp/1000

        Collections.reverse(timestampsNeeded);
        for (int i = 0; i < timestampsNeeded.size(); i++) {
            Snapshot currentSnap = snapshotService.getSnapshotByTimestamp(timestampsNeeded.get(i));
            for (int j = 0; j < currentSnap.getMarkets().size(); j++) {
                if(currentSnap.getMarkets().get(j).getId() == nonUniqueMarketId){
                    for (int k = 0; k < currentSnap.getMarkets().get(j).getContracts().size(); k++) {
                        if(currentSnap.getMarkets().get(j).getContracts().get(k).getId() == nonUniqueContractId){
                            inputLayerList.add(currentSnap.getMarkets().get(j).getContracts().get(k).getBestBuyYesCost());
                            inputLayerList.add(currentSnap.getMarkets().get(j).getContracts().get(k).getBestBuyNoCost());
                            inputLayerList.add((double) timestampsNeeded.get(i)/1000);
                        }
                    }
                }
            }
        }

        //error check
        if(inputLayerList.size() != inputLayer.length){
            System.out.println("InputLayer List does not match required size of InputLayer Array. Returning.");
            return;
        }
        for (int i = 0; i < inputLayerList.size(); i++) {
            inputLayer[i] = inputLayerList.get(i);
        }
        //feed to NN, get outputLayer layer
        double[] outputLayer = network.calculateActivationsOfAllLayers(inputLayer);

        System.out.println("Prediction for Contract Id: " + nonUniqueContractId);
        System.out.println("Output: " + Arrays.toString(outputLayer));
        System.out.println("Input: " + Arrays.toString(inputLayer));

    }
}
