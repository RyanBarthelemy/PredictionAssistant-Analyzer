package com.axlor.predictionassistantanalyzer.exception;

public class SnapshotCountMismatchException extends Exception {
    public SnapshotCountMismatchException(){
        super("\"Sizes of hashList and timestampList do not match. Unable to build list of SnapshotMini objects for this reason.\" +\n" +
                "        \"It is likely a database write update was occurring at the same time as this request. Very hard to reproduce, try making request again.\" +\n" +
                "        \"If it consistently fails, contact admin.\"");
    }
}
