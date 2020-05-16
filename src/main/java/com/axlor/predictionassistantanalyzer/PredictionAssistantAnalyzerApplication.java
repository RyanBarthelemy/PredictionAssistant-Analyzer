package com.axlor.predictionassistantanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class PredictionAssistantAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PredictionAssistantAnalyzerApplication.class, args);
    }

}
