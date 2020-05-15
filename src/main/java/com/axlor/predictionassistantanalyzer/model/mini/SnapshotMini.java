package com.axlor.predictionassistantanalyzer.model.mini;

/*
    This class represents a Snapshot and how to find in using this application's REST API.
    When a user makes a GET request to "../snapshots", we want to show them a list of all snapshots and where to find a specific one.
    However, getting every Snapshot from the database is extremely expensive and we'd be showing/retrieving a bunch of data they don't need (probably).

    The "../snapshots" mapping will return a JSON representation of a list of these objects. The user then has Time/Date of a snapshot, its unique ID, and a link to that Snapshot's data.
 */
public class SnapshotMini {

    private Integer hashID;
    private String timestampDisplay;
    private String href;

    public SnapshotMini(Integer hashID, String timestampDisplay, String href) {
        this.hashID = hashID;
        this.timestampDisplay = timestampDisplay;
        this.href = href;
    }

    public Integer getHashID() {
        return hashID;
    }

    public void setHashID(Integer hashID) {
        this.hashID = hashID;
    }

    public String getTimestampDisplay() {
        return timestampDisplay;
    }

    public void setTimestampDisplay(String timestampDisplay) {
        this.timestampDisplay = timestampDisplay;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
