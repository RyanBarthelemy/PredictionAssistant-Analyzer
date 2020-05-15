package com.axlor.predictionassistantanalyzer.analyzers;

import com.axlor.predictionassistantanalyzer.exception.NoSnapshotsInDatabaseException;
import com.axlor.predictionassistantanalyzer.exception.SnapshotCountMismatchException;
import com.axlor.predictionassistantanalyzer.exception.SnapshotNotFoundException;
import com.axlor.predictionassistantanalyzer.model.Snapshot;
import com.axlor.predictionassistantanalyzer.model.mini.SnapshotMini;
import com.axlor.predictionassistantanalyzer.service.SnapshotService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PA_ProblemData {

    private int inputTimeFrame = 60; //minutes

    private int timeBetweenSnapshots = 2; //2 minutes between Snapshots saved to DB, Prediction Assistant Data does this, we just need to know it.
    private int acceptedError = 3; //minutes. Input layer needs to span 'inputTimeFrame' minuts, but it probably isn't a perfect fit, how many minutes of error between first to last will we accept?

    private int predictionTime = 30; //minutes. How much time after the final input snapshot do we want out prediction to be for.
    private int outputErrorMax = 3;


    private int inputLayerSize; //number of neurons in input layer     \  These are different
    private int outputLayerSize; //number of neurons in output layer  /   These are different

    //the sizes of these lists need to be the same, the size of the Lists represents how man Problems/Sets of data we have to train with
    private List<double[]> inputLayers;  //probably noticeably better performance if these were both 2d arrays
    private List<double[]> outputLayers; //although we only build the problem data once, so maybe not a big deal, should profile it though

    public PA_ProblemData(SnapshotService snapshotService) throws SnapshotCountMismatchException, NoSnapshotsInDatabaseException, SnapshotNotFoundException {
        //this is where heavy lifting happens, we need to build the input layers and output layers from existing Snapshots in the database.
        //we want to look at every contract in the db... so we have a lot of data to get, validate, and parse.

        //each input layer is of size 3(n+1) where n is the number of Snapshots in a range of time.
        //PA_Data saves new data every 2 minutes lets say, so if we want to train the NN using 60 minute windows, we would need 30 Snapshots
        //This brings the input layer size to 3(30+1) = 93

        //Input Layer: all these need to be converted to doubles in some way. Completely fine as long as it is consistent, which it is.
        //1:Enumerated/category value: 0=BuyNo, 1=BuyYes
        //2:nonUniqueMarketId -- some Markets are more volatile than others, so this input could help maybe
        //3:nonUniqueContractId -- some Contracts are more volatile than others, this input could matter in training the NN
        //then we loop over Snapshots, getting the stuff we need:
        //4: contract buyYes cost
        //5: contract buyNo cost    --if buyNo goes down, it usually means buyYes goes up, so we should let NN have access to both
        //6: timestamp/1000.0        --timestamp needs to be a double, so we need to make it smaller, having it be to the second instead of millisecond is fine.
        //                                      --also, may or may not want timestamp, I want to play with this.
        //repeat 4-6 for n sets of Snapshots, 60 mins = 30 snapshots for example.

        //Output Layer: size 2, these are the actual buyYes and buyNo values from a Snapshot ~30 mins or however long AFTER the n'th Snapshot in InputLayer
        //check to make sure we have a continuous batch of Snapshots = range of Snapshots in inputLayer+30mins or whatever

        //This creates one entry to input and output layers, we want to create an entry for every Contract for every Snapshot window we can find that the data is available for.
        //Likely 10s of thousands. May need to cull.

        List<SnapshotMini> snapshots = snapshotService.getAllSnapshots_mini();
        //for each Snapshot, see if we have the Snapshots we need to build a Problem(input and output layers)
        for (int i = 0; i < snapshots.size(); i++) {
            List<Snapshot> problemSnapshots = getProblemSnapshotsFromSnapshot(snapshots.get(i), snapshotService);
            if (problemSnapshots == null) {
                System.out.println(i + ": Could not create problemSnapshots from starting snapshot id:" + snapshots.get(i).getHashID());
                continue;
            }
            System.out.println(i + ": Successfully created problemSnapshots from starting snapshot id: " + snapshots.get(i).getHashID());
        }

    }

    private List<Snapshot> getProblemSnapshotsFromSnapshot(SnapshotMini snapshotMini, SnapshotService snapshotService) throws SnapshotNotFoundException, NoSnapshotsInDatabaseException {
        List<Long> timestamps = snapshotService.getTimestamps();
        Collections.reverse(timestamps); //so timestamps are in ascending order, not descending order. Will make it easier to work with going forward
        Snapshot currentSnapshot = snapshotService.getSnapshot(snapshotMini.getHashID());
        long currentTimestamp = currentSnapshot.getTimestamp();

        return buildSnapshotList(currentTimestamp, timestamps, snapshotService);
    }

    private List<Snapshot> buildSnapshotList(long currentTimestamp, List<Long> timestamps, SnapshotService snapshotService) {
        //are there enough timestamps AFTER currentTimestamp including it?
        //if we are training the NN using the last 60 mins of data, we would need 30 snapshots for input
        //we want to predict what values will be 30 mins after last snapshots for output, so we want a timestamp = finalTimestamp+30mins, find closest, see if it is within 2 mins of required

        int indexOfCurrentTimestamp = timestamps.indexOf(currentTimestamp);
        if (indexOfCurrentTimestamp == -1) {
            System.out.println("null 1");
            return null;
        }

        //System.out.println("Index of current timestamp :" + indexOfCurrentTimestamp);
        int numOfRequiredSnapshotsForInput = (inputTimeFrame / timeBetweenSnapshots);
        //are there enough snapshots after our starting point to even try?
        if (indexOfCurrentTimestamp + numOfRequiredSnapshotsForInput > timestamps.size()) {
            System.out.println("null 2");
            return null;
        }

        //are the first snapshot and final snapshot roughly 'inputTimeFrame' minutes apart?
        int indexOfFinalTimestampNeededForInput = indexOfCurrentTimestamp + numOfRequiredSnapshotsForInput - 1; //say current index is 0 and we need 30 snapshots, our final timestamp index would be 29 = 0+30-1, careful of the off by one error
        long finalTimestampOfInput = timestamps.get(indexOfFinalTimestampNeededForInput);
        //System.out.println("Final Timestamp needed for input: " + finalTimestampOfInput);
        long actualTimeframe = (finalTimestampOfInput - currentTimestamp) / 60000; //minutes
        //System.out.println("Actual time frame: " + actualTimeframe);
        long actualError = Math.abs(actualTimeframe - inputTimeFrame);
        if (actualError > acceptedError) {
            System.out.println("null 3: ActualError:" + actualError + " ---- AcceptedError:" + acceptedError);
            return null;
        }

        //is the snapshot required for the output layer close enough to what we need for it to work?
        long theoreticalTimestampForOutput = finalTimestampOfInput + (predictionTime * 60000);
        Snapshot outputSnapshot = snapshotService.getSnapshotClosesToTimestamp(theoreticalTimestampForOutput);
        long actualTimestampForOutput = outputSnapshot.getTimestamp();
        long outputErrorActual = Math.abs(theoreticalTimestampForOutput - actualTimestampForOutput) / 60000;
        if (outputErrorActual > outputErrorMax) {
            System.out.println("null 4: outputErrorActual:" + outputErrorActual + " --- outputErrorMax:" + outputErrorMax);
            return null;
        }

        //everything is good, create the list of snapshots. First 'n' snapshots are input, final 1 snapshot is output.
        List<Snapshot> snapshotsToUse = new ArrayList<>();
        for (int i = indexOfCurrentTimestamp; i < indexOfFinalTimestampNeededForInput; i++) {
            snapshotsToUse.add(
                snapshotService.getSnapshotClosesToTimestamp(timestamps.get(i))
            );
        }
        snapshotsToUse.add(outputSnapshot); //add the output layer snapshot

        return snapshotsToUse;
    }

    public int getInputLayerSize() {
        return inputLayerSize;
    }

    public int getOutputLayerSize() {
        return outputLayerSize;
    }

    public int getNumOfProblems() {
        return inputLayers.size();
    }

    public List<double[]> getInputLayers() {
        return inputLayers;
    }

    public List<double[]> getOutputLayers() {
        return outputLayers;
    }
}