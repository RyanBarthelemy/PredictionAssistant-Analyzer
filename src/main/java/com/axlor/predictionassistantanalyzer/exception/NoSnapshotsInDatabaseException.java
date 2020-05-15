package com.axlor.predictionassistantanalyzer.exception;

public class NoSnapshotsInDatabaseException extends Exception{

    public NoSnapshotsInDatabaseException(){
        super("There are no Snapshots in the database to retrieve. If there IS data in your database, it isn't being retrieved correctly for some reason. GL!");
    }
}
