package com.axlor.predictionassistantanalyzer.model.mini;

public class ContractMini {
    private String name;
    private int id;

    public ContractMini(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}