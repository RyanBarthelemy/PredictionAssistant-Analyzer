package com.axlor.predictionassistantanalyzer.gui;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class TrackedMarkets implements Serializable {

    public static final long serialVersionUID = 762456012754379535L;

    private List<Integer> trackedMarketsList = new ArrayList();

    public boolean track(int marketId){

        for (Integer integer : trackedMarketsList) {
            if (integer == marketId) {
                return false;
            } //already tracking this market.
        }
        trackedMarketsList.add(marketId);
        return save();
    }

    public boolean untrack(int marketId){
        for (int i = 0; i < trackedMarketsList.size(); i++) {
            if(trackedMarketsList.get(i)==marketId){
                trackedMarketsList.remove(i);
                save();
                return true;
            }
        }
        return false;
    }

    private boolean save(){
        File saveFile = new File("trackedMarkets_obj");
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile));
            oos.writeObject(this);

            oos.flush();
            oos.close();
            System.out.println("Successfully saved trackedMarkets_obj object to " + saveFile.getName());
            return true;

        } catch (IOException ex) {
            System.out.println("Caught IOException, did NOT save network to file:" + saveFile.getName());
            return false;
        }
    }

    @PostConstruct
    private void loadAtCreation(){
        File inputFile = new File("trackedMarkets_obj");
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(inputFile));
            TrackedMarkets trackedMarkets = (TrackedMarkets) ois.readObject();
            ois.close();

            this.trackedMarketsList = trackedMarkets.trackedMarketsList;
            System.out.println("Successfully loaded tracked markets.");
            System.out.println("Market IDs: " + trackedMarketsList);

        } catch (IOException ex) {
            System.out.println("Caught an IOException trying to load data from file: " + inputFile.getName());
            System.out.println("Returning null.");
        } catch (ClassNotFoundException ex) {
            System.out.println("Caught ClassNotFoundException trying to caste object to ContractProblemData, returning null.");
        }
    }

    public List<Integer> getTrackedMarketsList() {
        return trackedMarketsList;
    }
}
