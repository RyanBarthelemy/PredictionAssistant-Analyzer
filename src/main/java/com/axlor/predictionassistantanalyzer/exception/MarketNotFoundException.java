package com.axlor.predictionassistantanalyzer.exception;

public class MarketNotFoundException extends Exception {
    public MarketNotFoundException(int nonUniqueId){
        super("Market with id: '" + nonUniqueId + "' does not exist in the database. Tip: Make sure to use id given by PredictIt for '/markets/{id}. This can be found in the url of the market or from the id field. It is NOT marketUniqueId.");
    }

    public MarketNotFoundException(String message){
        super(message);
    }
}
