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

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Component
@FxmlView("ContractHistoryScene.fxml")
public class ContractHistorySceneController {

    @Autowired
    SnapshotService snapshotService;

    @Autowired
    ContractHistoryService contractHistoryService;

    private int TIMEFRAME_DEFAULT = 120;

    private int timeFrameMins = TIMEFRAME_DEFAULT; //changeable
    private int nonUniqueContractId;
    private int nonUniqueMarketId;
    private String url = "null";
    private final FxWeaver fxWeaver;
    private List<DisplayableContractInfo> contractHistoryList;

    @FXML
    private Label currentTimeframeLabel;

    @FXML
    private javafx.scene.control.TextField timeframeTextField;

    @FXML
    private Button refreshButton;

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
        timeFrameMins = TIMEFRAME_DEFAULT;
        boolean buildable = setTitle();

        if(buildable){
            contractInfoLabel.setText("Downloading Data and Building UI, this may take a moment...");
            Platform.runLater(()->{
                contractHistoryList = contractHistoryService.getContractHistory(nonUniqueMarketId, nonUniqueContractId);
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
            //if(i==0 || !contractHistoryList.get(i).getBuyYes().equals(contractHistoryList.get(i - 1).getBuyYes())){
                DisplayableContractInfo dci = contractHistoryList.get(i);
                if(Math.abs(Integer.parseInt(dci.getMinsFromCurrent())) > timeFrameMins){
                    System.out.println("reached break at min from current = " + dci.getMinsFromCurrent());
                    series.getData().add(new XYChart.Data(Integer.parseInt(dci.getMinsFromCurrent()), Double.parseDouble(dci.getBuyYes())));
                    break;
                }
                series.getData().add(new XYChart.Data(Integer.parseInt(dci.getMinsFromCurrent()), Double.parseDouble(dci.getBuyYes())));
            //}
        }
        contractHistoryChart.getData().clear();
        contractHistoryChart.getData().add(series);

        chart_yAxis.setAutoRanging(false);
        chart_yAxis.setLowerBound(0.0);
        chart_yAxis.setUpperBound(1.0);
        chart_yAxis.setTickUnit(0.05);

        currentTimeframeLabel.setText("current=" + timeFrameMins + " mins.");
    }

    private boolean setTitle() {
        try {
            Market market = getLatestMarketContainingContract(nonUniqueContractId);
            if(market == null){return false;}
            url = market.getUrl();
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
    void sma10_checkboxClicked(ActionEvent event) {
        System.out.println("sma10 checkbox event");
        updateChart();
    }

    @FXML
    void sma60_checkboxClicked(ActionEvent event) {
        System.out.println("sma60 checkbox event");
        updateChart();
    }

    private void updateChart() {
        try {
            timeFrameMins = Integer.parseInt(timeframeTextField.getText());
        }catch(Exception e){
            System.out.println("parse exception in updateChart method");
            timeFrameMins = TIMEFRAME_DEFAULT;
        }
        if(timeFrameMins < 10){
            System.out.println("timeframeMins < 10, setting to default.");
            timeFrameMins = TIMEFRAME_DEFAULT;
        }

        buildContractHistoryChart();
        if(sma10_checkbox.isSelected()){
            build_sma10Chart();
        }
        if(sma60_checkbox.isSelected()){
            build_sma60Chart();
        }
    }

    private void build_sma60Chart() {
        //todo
        XYChart.Series series = new XYChart.Series();
        series.setName("60min SMA");

        for (int i = 0; i < contractHistoryList.size(); i++) {
            //if(i==0 || !contractHistoryList.get(i).getSma60().equals(contractHistoryList.get(i - 1).getSma60())){
                DisplayableContractInfo dci = contractHistoryList.get(i);
                if(Math.abs(Integer.parseInt(dci.getMinsFromCurrent())) > timeFrameMins){
                    System.out.println("sma60: reached break at min from current = " + dci.getMinsFromCurrent());
                    if(Double.parseDouble(dci.getSma60()) == 0.0){continue;}
                    series.getData().add(new XYChart.Data(Integer.parseInt(dci.getMinsFromCurrent()), Double.parseDouble(dci.getSma60())));
                    break;
                }
                if(Double.parseDouble(dci.getSma60()) == 0.0){continue;}
                series.getData().add(new XYChart.Data(Integer.parseInt(dci.getMinsFromCurrent()), Double.parseDouble(dci.getSma60())));
            //}
        }
        contractHistoryChart.getData().add(series);
    }


    private void build_sma10Chart() {
        //todo
        XYChart.Series series = new XYChart.Series();
        series.setName("10min SMA");

        for (int i = 0; i < contractHistoryList.size(); i++) {
            //if(i==0 || !contractHistoryList.get(i).getSma60().equals(contractHistoryList.get(i - 1).getSma60())){
            DisplayableContractInfo dci = contractHistoryList.get(i);
            if(Math.abs(Integer.parseInt(dci.getMinsFromCurrent())) > timeFrameMins){
                System.out.println("sma10: reached break at min from current = " + dci.getMinsFromCurrent());
                if(Double.parseDouble(dci.getSma10()) == 0.0){continue;}
                series.getData().add(new XYChart.Data(Integer.parseInt(dci.getMinsFromCurrent()), Double.parseDouble(dci.getSma10())));
                break;
            }
            if(Double.parseDouble(dci.getSma10()) == 0.0){continue;}
            series.getData().add(new XYChart.Data(Integer.parseInt(dci.getMinsFromCurrent()), Double.parseDouble(dci.getSma10())));
            //}
        }
        contractHistoryChart.getData().add(series);

    }

    public void openUrlButtonClicked(javafx.scene.input.MouseEvent event) {
        if (!Desktop.isDesktopSupported()) {
            System.out.println("Desktop is not supported for some reason... Unix system?");
        }
        if (Desktop.isDesktopSupported() && !url.equals("null")) {
            try {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (URISyntaxException ex) {
                    System.out.println("caught uri syntax exception");
                }
            } catch (IOException ex) {
                System.out.println("Failed to open market URL.");
            }
        }
    }

    public void refreshButtonClicked(javafx.scene.input.MouseEvent event) {
        System.out.println("Refresh button clicked");
        updateChart();
    }
}
