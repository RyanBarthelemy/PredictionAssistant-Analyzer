package com.axlor.predictionassistantanalyzer.model.mini;

import java.util.List;

public class MarketMini {
    private String name;
    private int id;
    private String url;
    private List<ContractMini> contractMinis;

    public MarketMini(String name, int id, String url, List<ContractMini> contractMinis) {
        this.name = name;
        this.id = id;
        this.url = url;
        this.contractMinis = contractMinis;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<ContractMini> getContractMinis() {
        return contractMinis;
    }

    public void setContractMinis(List<ContractMini> contractMinis) {
        this.contractMinis = contractMinis;
    }
}
