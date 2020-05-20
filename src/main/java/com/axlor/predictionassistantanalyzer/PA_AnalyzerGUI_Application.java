package com.axlor.predictionassistantanalyzer;

import com.axlor.predictionassistantanalyzer.gui.AllMarketsSceneController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class PA_AnalyzerGUI_Application extends Application {

    private ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Got into start method");
        primaryStage.setTitle("Prediction Assistant Analyzer");

        try {
            FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
            Parent parent = fxWeaver.loadView(AllMarketsSceneController.class);
            if(parent == null){
                System.out.println("parent not created successfully...");
            }
            Scene scene = new Scene(parent);
            primaryStage.setScene(scene);
            primaryStage.show();
        }
        catch(Exception e){
            System.out.println(e);
            stop();
        }
    }

    //called before start(), this init method is where we start the Spring Boot application that creates all our Services (etc) that we use.
    @Override
    public void init() {
        System.out.println("Got into init() method");
        String[] args = getParameters().getRaw().toArray(new String[0]);

        this.applicationContext = new SpringApplicationBuilder()
                .sources(PredictionAssistantAnalyzerApplication.class)
                .run(args);
    }

    //do tear down here
    @Override
    public void stop() {
        applicationContext.close();
        Platform.exit(); //Causes the JavaFX application to terminate (gracefully).
        System.exit(111); //Causes the Spring application to close including the downAndSave loop thread that was spawned.

    }

}
