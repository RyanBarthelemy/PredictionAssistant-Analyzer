package com.axlor.predictionassistantanalyzer.gui.sceneController;

import com.axlor.predictionassistantanalyzer.analyzers.ContractHistoryService;
import com.axlor.predictionassistantanalyzer.exception.NoSnapshotsInDatabaseException;
import com.axlor.predictionassistantanalyzer.gui.DisplayableContractInfo;
import com.axlor.predictionassistantanalyzer.model.Contract;
import com.axlor.predictionassistantanalyzer.model.Market;
import com.axlor.predictionassistantanalyzer.service.SnapshotService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.event.MouseEvent;
import java.util.List;

@Component
@FxmlView("ContractHistoryScene.fxml")
public class ContractHistorySceneController {

    @Autowired
    SnapshotService snapshotService;

    @Autowired
    ContractHistoryService contractHistoryService;

    private int timeFrameMins = 240; //changeable
    private int nonUniqueContractId;
    private int nonUniqueMarketId;
    private final FxWeaver fxWeaver;
    private List<DisplayableContractInfo> contractHistoryList;

    @FXML // fx:id="sma10_checkbox"
    private CheckBox sma10_checkbox; // Value injected by FXMLLoader

    @FXML // fx:id="contractHistoryTableView"
    private TableView<?> contractHistoryTableView; // Value injected by FXMLLoader

    @FXML // fx:id="contractInfoLabel"
    private Label contractInfoLabel; // Value injected by FXMLLoader

    @FXML // fx:id="chart_yAxis"
    private NumberAxis chart_yAxis; // Value injected by FXMLLoader

    @FXML // fx:id="chart_xAxis"
    private NumberAxis chart_xAxis; // Value injected by FXMLLoader

    @FXML // fx:id="contractHistoryChart"
    private LineChart<Number, Number> contractHistoryChart; // Value injected by FXMLLoader

    @FXML // fx:id="openUrlButton"
    private Button openUrlButton; // Value injected by FXMLLoader

    @FXML // fx:id="sma60_checkbox"
    private CheckBox sma60_checkbox; // Value injected by FXMLLoader

    public ContractHistorySceneController(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
        chart_xAxis = new NumberAxis();
        chart_yAxis = new NumberAxis();

        contractHistoryChart = new LineChart<>(chart_xAxis, chart_yAxis);
    }

    public void setNonUniqueContractId(int nonUniqueContractId) {
        this.nonUniqueContractId = nonUniqueContractId;
    }

    @FXML
    void initialize() {
        System.out.println("got to initialize with contractId: " + nonUniqueContractId);
        boolean buildable = setTitle();

        if(buildable){
            contractInfoLabel.setText("Downloading Data and Building UI, this may take a moment...");
            Platform.runLater(()->{
                contractHistoryList = contractHistoryService.getContractHistoryLast_XX_mins(nonUniqueMarketId, nonUniqueContractId, timeFrameMins);
                System.out.println("Contract History List set.");
                setTitle();
                buildContractHistoryChart();
                //buildContractHistoryTableView();
            });
        }
        else{
            contractInfoLabel.setText("Could not build charts and table using Contract[" + nonUniqueContractId + "]. Contract may not exist in latest market data.");
        }
    }

    private void buildContractHistoryChart() {
        XYChart.Series series = new XYChart.Series();
        series.setName("Contract History");

        /*
        for (DisplayableContractInfo dci: contractHistoryList){
            series.getData().add(new XYChart.Data(Integer.parseInt(dci.getMinsFromCurrent()), Double.parseDouble(dci.getBuyYes())));
            //System.out.println("added point: (" + Integer.parseInt(dci.getMinsFromCurrent()) + ", " + Double.parseDouble(dci.getBuyYes()) + ")");
        }
         */

        for (int i = 0; i < contractHistoryList.size(); i++) {
            if(i==0 || !contractHistoryList.get(i).getBuyYes().equals(contractHistoryList.get(i - 1).getBuyYes())){
                DisplayableContractInfo dci = contractHistoryList.get(i);
                series.getData().add(new XYChart.Data(Integer.parseInt(dci.getMinsFromCurrent()), Double.parseDouble(dci.getBuyYes())));
            }
        }

        contractHistoryChart.getData().add(series);
        chart_yAxis.setAutoRanging(false);
        chart_yAxis.setLowerBound(0.0);
        chart_yAxis.setUpperBound(1.0);
        chart_yAxis.setTickUnit(0.05);
    }

    private boolean setTitle() {
        try {
            Market market = getLatestMarketContainingContract(nonUniqueContractId);
            if(market == null){return false;}
            String contractName = getContractName(market, nonUniqueContractId);
            if(contractName==null){return false;}

            if(contractName.equals(market.getName())){
                contractInfoLabel.setText("BuyYes: Contract[" + nonUniqueContractId + "] -- " + "Yes/No" + " -- " + market.getName());
            }
            else {
                contractInfoLabel.setText("BuyYes: Contract[" + nonUniqueContractId + "] -- " + contractName + " -- " + market.getName());
            }
            nonUniqueMarketId = market.getId();
            return true;
        } catch (NoSnapshotsInDatabaseException e) {return false;}
    }

    private String getContractName(Market market, int nonUniqueContractId) {
        for(Contract c: market.getContracts()){
            if(c.getId() == nonUniqueContractId){
                return c.getName();
            }
        }
        return null;
    }

    private Market getLatestMarketContainingContract(int nonUniqueContractId) throws NoSnapshotsInDatabaseException {
        List<Market> latestMarkets = snapshotService.getLatestSnapshot().getMarkets();
        for (Market market: latestMarkets){
            if(marketContainsContract(market, nonUniqueContractId)){
                return market;
            }
        }
        return null;
    }

    private boolean marketContainsContract(Market market, int nonUniqueContractId) {
        for (Contract c: market.getContracts()){
            if(c.getId() == nonUniqueContractId){return true;}
        }
        return false;
    }

    @FXML
    void openUrlButtonClicked(MouseEvent event) {
        System.out.println("Open url button clicked");
    }

    @FXML
    void sma10_checkboxClicked(ActionEvent event) {
        System.out.println("sma10 checkbox event");
    }

    @FXML
    void sma60_checkboxClicked(ActionEvent event) {
        System.out.println("sma60 checkbox event");
    }

    public void openUrlButtonClicked(javafx.scene.input.MouseEvent event) {
        System.out.println("Open url button clicked");
    }
}
