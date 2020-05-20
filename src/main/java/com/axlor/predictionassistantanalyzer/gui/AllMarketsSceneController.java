package com.axlor.predictionassistantanalyzer.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
@FxmlView("AllMarketsScene.fxml")
public class AllMarketsSceneController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private MenuBar menuBar;

    @FXML
    private AnchorPane titlePane;

    @FXML
    private AnchorPane rightPane;

    @FXML
    private Label titleLabel;

    @FXML
    private AnchorPane leftPane;

    @FXML
    private Label label_query;

    @FXML
    private Label label_trackedMarkets1;

    @FXML
    private TableView<?> allMarketsTable;

    @FXML
    private TextField textField_query;

    @FXML
    private Tooltip query_textfield_tooltip;

    @FXML
    void initialize() {
        System.out.println("Got into init() in fxml controller class");

        assert menuBar != null : "fx:id=\"menuBar\" was not injected: check your FXML file 'pa_analyzer_allMarketsScene.fxml'.";
        assert titlePane != null : "fx:id=\"titlePane\" was not injected: check your FXML file 'pa_analyzer_allMarketsScene.fxml'.";
        assert rightPane != null : "fx:id=\"rightPane\" was not injected: check your FXML file 'pa_analyzer_allMarketsScene.fxml'.";
        assert titleLabel != null : "fx:id=\"titleLabel\" was not injected: check your FXML file 'pa_analyzer_allMarketsScene.fxml'.";
        assert leftPane != null : "fx:id=\"leftPane\" was not injected: check your FXML file 'pa_analyzer_allMarketsScene.fxml'.";
        assert label_query != null : "fx:id=\"label_query\" was not injected: check your FXML file 'pa_analyzer_allMarketsScene.fxml'.";
        assert label_trackedMarkets1 != null : "fx:id=\"label_trackedMarkets1\" was not injected: check your FXML file 'pa_analyzer_allMarketsScene.fxml'.";
        assert allMarketsTable != null : "fx:id=\"allMarketsTable\" was not injected: check your FXML file 'pa_analyzer_allMarketsScene.fxml'.";
        assert textField_query != null : "fx:id=\"textField_query\" was not injected: check your FXML file 'pa_analyzer_allMarketsScene.fxml'.";
        assert query_textfield_tooltip != null : "fx:id=\"query_textfield_tooltip\" was not injected: check your FXML file 'pa_analyzer_allMarketsScene.fxml'.";

        System.out.println("Got to end of init() in fxml controller class");
    }
}
