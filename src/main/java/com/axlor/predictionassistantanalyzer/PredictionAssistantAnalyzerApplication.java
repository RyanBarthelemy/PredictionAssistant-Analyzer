package com.axlor.predictionassistantanalyzer;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PredictionAssistantAnalyzerApplication {

    public static void main(String[] args) {
        //SpringApplication.run(PredictionAssistantAnalyzerApplication.class, args);
        Application.launch(PA_AnalyzerGUI_Application.class, args);
    }

}
