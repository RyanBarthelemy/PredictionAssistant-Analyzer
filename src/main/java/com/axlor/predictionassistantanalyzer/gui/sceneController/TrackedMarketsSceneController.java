package com.axlor.predictionassistantanalyzer.gui.sceneController;

import com.axlor.predictionassistantanalyzer.exception.MarketNotFoundException;
import com.axlor.predictionassistantanalyzer.exception.NoSnapshotsInDatabaseException;
import com.axlor.predictionassistantanalyzer.gui.DisplayableMC;
import com.axlor.predictionassistantanalyzer.gui.TrackedMarkets;
import com.axlor.predictionassistantanalyzer.model.Contract;
import com.axlor.predictionassistantanalyzer.model.Market;
import com.axlor.predictionassistantanalyzer.service.MarketService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@FxmlView("TrackedMarketsScene.fxml")
public class TrackedMarketsSceneController {

    @Autowired
    TrackedMarkets trackedMarketsIds;

    @Autowired
    MarketService marketService;

    private final FxWeaver fxWeaver;

    private List<Market> trackedMarkets;

    private TableColumn marketIdColumn;
    private TableColumn contractIdColumn;
    private TableColumn nameColumn;
    private TableColumn buyYesColumn;
    private TableColumn buyNoColumn;
    private TableColumn sellYesColumn;
    private TableColumn sellNoColumn;


    @FXML // fx:id="titlePane"
    private AnchorPane titlePane; // Value injected by FXMLLoader

    @FXML // fx:id="rightPane"
    private AnchorPane rightPane; // Value injected by FXMLLoader

    @FXML // fx:id="titleLabel"
    private Label titleLabel; // Value injected by FXMLLoader

    @FXML // fx:id="leftPane"
    private AnchorPane leftPane; // Value injected by FXMLLoader

    @FXML // fx:id="moversButton"
    private Button moversButton; // Value injected by FXMLLoader

    @FXML // fx:id="contractHistoryButton"
    private Button contractHistoryButton; // Value injected by FXMLLoader

    @FXML // fx:id="trackedMarketsButton"
    private Button trackedMarketsButton; // Value injected by FXMLLoader

    @FXML // fx:id="allMarketsButton"
    private Button allMarketsButton; // Value injected by FXMLLoader

    @FXML // fx:id="untrackMarketButton"
    private Button untrackMarketButton; // Value injected by FXMLLoader

    @FXML // fx:id="label_trackedMarkets"
    private Label label_trackedMarkets; // Value injected by FXMLLoader

    @FXML // fx:id="trackedMarketsTable"
    private TableView<DisplayableMC> trackedMarketsTable; // Value injected by FXMLLoader

    public TrackedMarketsSceneController(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
    }

    @FXML
    void initialize(){
        System.out.println("Got into TrackedMarketsSceneController initialize() method.");
        setupColumns();
        Platform.runLater(()->{
            try {
                setTrackedMarketsList();
            } catch (NoSnapshotsInDatabaseException | MarketNotFoundException e) {
                System.out.println("Couldn't get tracked market(s) from database for some reason.");
            }
            setupTableContextMenu();
            updateTrackedMarketsTableView();
        });
    }

    private void setupTableContextMenu() {
        //todo: setup the context menu in tracked markets scene
        //still need to think about what I want in this menu. Probably just the buttons that are available...
            //untrack(which also reloads list, openContractHistory window, manual refresh?
    }

    private void updateTrackedMarketsTableView() {
        //create table data
        ObservableList<DisplayableMC> tableData = FXCollections.observableArrayList();
        if (trackedMarkets != null) {
            for (Market market : trackedMarkets) {
                    //add market data to tableData
                    tableData.add(new DisplayableMC(
                            String.valueOf(market.getId()),
                            "---",
                            market.getName(),
                            "---",
                            "---",
                            "---",
                            "---",
                            market.getUrl()
                    ));

                    for (Contract contract : market.getContracts()) {
                        //add contract data for each contract in this 'market'

                        //for formatting, to make table look nicer
                        String nameToUse = "";
                        if (market.getName().equals(contract.getName())) {
                            nameToUse = "Yes / No";
                        } else {
                            nameToUse = contract.getName();
                        }

                        tableData.add(new DisplayableMC(
                                String.valueOf(market.getId()),
                                String.valueOf(contract.getId()),
                                "         " + nameToUse,
                                String.valueOf(contract.getBestBuyYesCost()),
                                String.valueOf(contract.getBestBuyNoCost()),
                                String.valueOf(contract.getBestSellYesCost()),
                                String.valueOf(contract.getBestSellNoCost()),
                                market.getUrl()
                        ));
                    }//for each contract
            }//for each market
            if (tableData.isEmpty()) {
                tableData.add(new DisplayableMC("---", "---", "Could not find any Market or Contract with any of the query terms.", "---", "---", "---", "---", "---"));
            }
            trackedMarketsTable.setItems(tableData);
        }//if latest!=null
        else {
            tableData.add(new DisplayableMC("---", "---", "Database error, could not get any Snaphot Market info from DB.", "---", "---", "---", "---", "---"));
        }
    }

    private void setTrackedMarketsList() throws NoSnapshotsInDatabaseException, MarketNotFoundException {
        trackedMarkets = new ArrayList<>();
        if(trackedMarketsIds.getTrackedMarketsList() == null){return;}

        for (int i = 0; i < trackedMarketsIds.getTrackedMarketsList().size(); i++) {
            trackedMarkets.add(marketService.getLatestMarketInfo(trackedMarketsIds.getTrackedMarketsList().get(i)));
        }
    }

    private void setupColumns() {
        marketIdColumn = new TableColumn("MID");
        marketIdColumn.setPrefWidth(40.0);
        contractIdColumn = new TableColumn("CID");
        contractIdColumn.setPrefWidth(40.0);
        nameColumn = new TableColumn("Name");
        nameColumn.setPrefWidth(400.0);
        buyYesColumn = new TableColumn("BuyYes");
        buyYesColumn.setPrefWidth(50.0);
        buyNoColumn = new TableColumn("BuyNo");
        buyNoColumn.setPrefWidth(50.0);
        sellYesColumn = new TableColumn("SellYes");
        sellYesColumn.setPrefWidth(50.0);
        sellNoColumn = new TableColumn("SellNo");
        sellNoColumn.setPrefWidth(50.0);

        marketIdColumn.setCellValueFactory(new PropertyValueFactory<>("marketId"));
        contractIdColumn.setCellValueFactory(new PropertyValueFactory<>("contractId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        buyYesColumn.setCellValueFactory(new PropertyValueFactory<>("buyYes"));
        buyNoColumn.setCellValueFactory(new PropertyValueFactory<>("buyNo"));
        sellYesColumn.setCellValueFactory(new PropertyValueFactory<>("sellYes"));
        sellNoColumn.setCellValueFactory(new PropertyValueFactory<>("sellNo"));

        trackedMarketsTable.getColumns().addAll(marketIdColumn, contractIdColumn, nameColumn, buyYesColumn, buyNoColumn, sellYesColumn, sellNoColumn);
        trackedMarketsTable.getColumns().forEach((col) -> col.setSortable(false));//table gets messed up if columns are sorted in any way and cannot return to normal.
    }

    @FXML
    void allMarketsButtonClicked(MouseEvent event) {
        System.out.println("All Markets button clicked");

        Stage thisStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent parent = fxWeaver.loadView(AllMarketsSceneController.class);
        if (parent == null) {
            System.out.println("parent not created successfully...");
        }
        Scene scene = new Scene(parent);
        thisStage.setScene(scene);
        thisStage.show();
    }

    @FXML
    void trackedMarketsButtonClicked(MouseEvent event) {
        System.out.println("Tracked Markets button clicked. We are there, so doing nothing");
        return;
    }

    @FXML
    void moversButtonClicked(MouseEvent event) {
        System.out.println("Movers button clicked.");
        //todo: change scene
    }

    @FXML
    void contractHistoryButtonClicked(MouseEvent event) {
        System.out.println("Contract History button clicked");
        //todo: open new stage with this contract's history tableview displayed and some options for visual charts/whatever
    }

    @FXML
    void untrackMarketButtonClicked(MouseEvent event) {
        System.out.println("Untrack Market button clicked");
        //todo: get the id of the selected row's market and untrack it.
    }
}
