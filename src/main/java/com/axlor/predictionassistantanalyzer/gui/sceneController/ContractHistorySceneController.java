package com.axlor.predictionassistantanalyzer.gui.sceneController;

import com.axlor.predictionassistantanalyzer.analyzers.ContractHistoryService;
import com.axlor.predictionassistantanalyzer.exception.NoSnapshotsInDatabaseException;
import com.axlor.predictionassistantanalyzer.gui.DisplayableContractInfo;
import com.axlor.predictionassistantanalyzer.gui.DisplayableMover;
import com.axlor.predictionassistantanalyzer.model.Contract;
import com.axlor.predictionassistantanalyzer.model.Market;
import com.axlor.predictionassistantanalyzer.service.SnapshotService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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


    @Value("${my.contractHistory.timeFrameDefault: 60}")
    private int TIMEFRAME_DEFAULT;

    private int timeFrameMins = TIMEFRAME_DEFAULT; //changeable
    private int nonUniqueContractId;
    private int nonUniqueMarketId;
    private String url = "null";
    private final FxWeaver fxWeaver;
    private List<DisplayableContractInfo> contractHistoryList;

    private TableColumn changeColumn;
    private TableColumn currentPriceColumn;
    private TableColumn minFromCurrentColumn;
    private TableColumn timestampColumn;

    @FXML
    private Label currentTimeframeLabel;

    @FXML
    private javafx.scene.control.TextField timeframeTextField;

    @FXML
    private Button refreshButton;

    @FXML // fx:id="sma10_checkbox"
    private CheckBox sma10_checkbox; // Value injected by FXMLLoader

    @FXML // fx:id="contractHistoryTableView"
    private TableView<DisplayableContractInfo> contractHistoryTableView; // Value injected by FXMLLoader

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
        //System.out.println("got to initialize with contractId: " + nonUniqueContractId);
        if(TIMEFRAME_DEFAULT < 10){
            TIMEFRAME_DEFAULT = 60;
        }

        timeFrameMins = TIMEFRAME_DEFAULT;
        boolean buildable = setTitle();

        if(buildable){
            contractInfoLabel.setText("Downloading Data and Building UI, this may take a moment...");
            Platform.runLater(()->{
                contractHistoryList = contractHistoryService.getContractHistory(nonUniqueMarketId, nonUniqueContractId);
                //System.out.println("Contract History List set.");
                if(contractHistoryList == null){return; }
                setTitle();
                buildContractHistoryChart();
                buildContractHistoryTableView();
                contractHistoryChart.setCreateSymbols(false);
            });
        }
        else{
            contractInfoLabel.setText("Could not build charts and table using Contract[" + nonUniqueContractId + "]. Contract may not exist in latest market data.");
        }
    }

    private void buildContractHistoryTableView() {
        setupColumns();

        ObservableList<DisplayableContractInfo> tableData = FXCollections.observableArrayList();
        if(contractHistoryList != null && !contractHistoryList.isEmpty()){
            tableData.addAll(contractHistoryList);
            contractHistoryTableView.setItems(tableData);
            setRowBackgroundColors();
        }
        else{
            tableData.add(new DisplayableContractInfo("---", "---","An error occurred, no contract info was found."));
            contractHistoryTableView.setItems(tableData);
        }
    }

    private void setRowBackgroundColors() {
        changeColumn.setCellFactory(column -> {
            return new TableCell<DisplayableMover, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : getItem());
                    setGraphic(null);
                    TableRow<DisplayableMover> currentRow = getTableRow();

                    try{
                        int change = Integer.parseInt(item);
                        if (!isEmpty()) {
                            if (change > 0)
                                currentRow.setStyle("-fx-background-color:green");
                            else if (change < 0)
                                currentRow.setStyle("-fx-background-color:red"); //light red sort of
                            else{
                                currentRow.setStyle("");//none
                            }
                        }
                    }catch(Exception ignored){}

                }//override updateItem
            };
        });
    }

    private void setupColumns() {
        changeColumn = new TableColumn("Change");
        changeColumn.setPrefWidth(100.0);
        currentPriceColumn = new TableColumn("Current");
        currentPriceColumn.setPrefWidth(200.0);
        minFromCurrentColumn = new TableColumn("Age");
        minFromCurrentColumn.setPrefWidth(100.0);
        timestampColumn = new TableColumn("Timestamp");
        timestampColumn.setPrefWidth(400.0);

        changeColumn.setCellValueFactory(new PropertyValueFactory<>("change"));
        currentPriceColumn.setCellValueFactory(new PropertyValueFactory<>("buyYes"));
        minFromCurrentColumn.setCellValueFactory(new PropertyValueFactory<>("minsFromCurrent"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestampToDisplay"));

        contractHistoryTableView.getColumns().clear();
        contractHistoryTableView.getColumns().addAll(changeColumn, currentPriceColumn, minFromCurrentColumn, timestampColumn);
        contractHistoryTableView.getColumns().forEach((col)-> col.setSortable(false));
    }

    private void buildContractHistoryChart() {
        XYChart.Series series = new XYChart.Series();
        series.setName("Contract History");

        for (DisplayableContractInfo dci : contractHistoryList) {
            if (Math.abs(Integer.parseInt(dci.getMinsFromCurrent())) > timeFrameMins) {
                System.out.println("reached break at min from current = " + dci.getMinsFromCurrent());
                series.getData().add(new XYChart.Data(Integer.parseInt(dci.getMinsFromCurrent()), Double.parseDouble(dci.getBuyYes())));
                break;
            }
            series.getData().add(new XYChart.Data(Integer.parseInt(dci.getMinsFromCurrent()), Double.parseDouble(dci.getBuyYes())));
        }
        contractHistoryChart.getData().clear();
        contractHistoryChart.getData().add(series);

        chart_yAxis.setAutoRanging(false);
        chart_yAxis.setLowerBound(0.0);
        chart_yAxis.setUpperBound(1.0);
        chart_yAxis.setTickUnit(0.01);

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
            if(marketContainsContract(market, nonUniqueContractId)){ return market;}
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
        updateChart();
    }

    @FXML
    void sma60_checkboxClicked(ActionEvent event) {
        updateChart();
    }

    private void updateChart() {
        try {
            timeFrameMins = Integer.parseInt(timeframeTextField.getText());
        } catch (Exception e) {
            System.out.println("parse exception in updateChart method");
            timeFrameMins = TIMEFRAME_DEFAULT;
        }
        if (timeFrameMins < 10) {
            System.out.println("timeframeMins < 10, setting to default.");
            timeFrameMins = TIMEFRAME_DEFAULT;
        }

        buildContractHistoryChart();
        if (sma10_checkbox.isSelected()) {build_sma10Chart();}
        if (sma60_checkbox.isSelected()) {build_sma60Chart();}

        contractHistoryChart.setCreateSymbols(false);
    }

    private void build_sma60Chart() {
        XYChart.Series series = new XYChart.Series();
        series.setName("60min SMA");

        for (DisplayableContractInfo dci : contractHistoryList) {
            if (Math.abs(Integer.parseInt(dci.getMinsFromCurrent())) > timeFrameMins) {
                //System.out.println("sma60: reached break at min from current = " + dci.getMinsFromCurrent());
                if (Double.parseDouble(dci.getSma60()) == 0.0) {
                    continue;
                }
                series.getData().add(new XYChart.Data(Integer.parseInt(dci.getMinsFromCurrent()), Double.parseDouble(dci.getSma60())));
                break;
            }
            if (Double.parseDouble(dci.getSma60()) == 0.0) {
                continue;
            }
            series.getData().add(new XYChart.Data(Integer.parseInt(dci.getMinsFromCurrent()), Double.parseDouble(dci.getSma60())));
        }
        contractHistoryChart.getData().add(series);
    }


    private void build_sma10Chart() {
        XYChart.Series series = new XYChart.Series();
        series.setName("10min SMA");

        for (DisplayableContractInfo dci : contractHistoryList) {
            if (Math.abs(Integer.parseInt(dci.getMinsFromCurrent())) > timeFrameMins) {
                //System.out.println("sma10: reached break at min from current = " + dci.getMinsFromCurrent());
                if (Double.parseDouble(dci.getSma10()) == 0.0) {
                    continue;
                }
                series.getData().add(new XYChart.Data(Integer.parseInt(dci.getMinsFromCurrent()), Double.parseDouble(dci.getSma10())));
                break;
            }
            if (Double.parseDouble(dci.getSma10()) == 0.0) {
                continue;
            }
            series.getData().add(new XYChart.Data(Integer.parseInt(dci.getMinsFromCurrent()), Double.parseDouble(dci.getSma10())));
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
        //System.out.println("Refresh button clicked");
        updateChart();
    }
}
