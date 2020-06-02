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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Component
@FxmlView("TrackedMarketsScene.fxml")
public class TrackedMarketsSceneController {

    @Autowired
    TrackedMarkets trackedMarketsIds;

    @Autowired
    MarketService marketService;

    @Autowired
    ContractHistorySceneController contractHistorySceneController;

    private final FxWeaver fxWeaver;

    private List<Market> trackedMarkets;

    private TableColumn marketIdColumn;
    private TableColumn contractIdColumn;
    private TableColumn nameColumn;
    private TableColumn buyYesColumn;
    private TableColumn buyNoColumn;
    private TableColumn sellYesColumn;
    private TableColumn sellNoColumn;

    private ContextMenu trackedMarketsTableContextMenu;


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
        trackedMarketsTableContextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("Open URL");
        MenuItem menuItem2 = new MenuItem("Untrack Selected Market");
        MenuItem menuItem3 = new MenuItem("Open Contract History");
        MenuItem menuItem4 = new MenuItem("Refresh");

        trackedMarketsTableContextMenu.getItems().addAll(menuItem1, menuItem2, menuItem3, menuItem4);

        //--menuItem1-------------------------------------------------------------------------------------------------\\
        trackedMarketsTable.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            if (event.getButton() == MouseButton.SECONDARY) { //right click
                trackedMarketsTableContextMenu.show(trackedMarketsTable, event.getScreenX(), event.getScreenY());
            }
        });
        menuItem1.setOnAction((ActionEvent event) -> {
            DisplayableMC displayableMC = trackedMarketsTable.getSelectionModel().getSelectedItem();

            if (displayableMC == null) {
                System.out.println("displayableMC object is null, probably shouldn't be...");
            }
            if (!Desktop.isDesktopSupported()) {
                System.out.println("Desktop is not supported for some reason... Unix system?");
            }
            if (Desktop.isDesktopSupported() && displayableMC != null && !displayableMC.getMarketUrl().equals("---")) {
                try {
                    try {
                        Desktop.getDesktop().browse(new URI(displayableMC.getMarketUrl()));
                    } catch (URISyntaxException ex) {
                        System.out.println("Failed to open market URL.");
                    }
                } catch (IOException ex) {
                    System.out.println("Failed to open market URL.");
                }
            }
        });
        //--------------------------------------------------------------------------------------------------------\\
        //menuItem2 -- untrack
        menuItem2.setOnAction((ActionEvent event) -> {
            untrackSelectedMarket();
        });
        //menuItem3 -- open contract history window
        menuItem3.setOnAction((ActionEvent event) -> {
            DisplayableMC displayableMC = trackedMarketsTable.getSelectionModel().getSelectedItem();
            if(displayableMC!=null && !displayableMC.getContractId().equals("---")){
                openContractHistoryWindow(displayableMC.getContractId());
            }
        });
        //menuItem4 -- refresh
        menuItem4.setOnAction((ActionEvent event) -> {
            try {
                setTrackedMarketsList();
                updateTrackedMarketsTableView();
            } catch (NoSnapshotsInDatabaseException | MarketNotFoundException ignored) {}
        });
    }

    private void openContractHistoryWindow(String nonUniqueContractId) {
        try {
            int cid = Integer.parseInt(nonUniqueContractId);
            contractHistorySceneController.setNonUniqueContractId(cid);

            Stage contractStage = new Stage();
            contractStage.setTitle("Prediction Assistant Analyzer - Contract History");
            Parent root = fxWeaver.loadView(ContractHistorySceneController.class);
            if(root==null){
                System.out.println("root/parent not created successfully...");
                return;
            }
            Scene scene = new Scene(root);
            scene.getStylesheets().add("modenaDark.css");
            contractStage.setScene(scene);
            contractStage.show();
        }catch(Exception e){
            System.out.println("Failed to create contractStage loading ContractHistorySceneController scene.");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
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
                                new DecimalFormat("#.##").format(contract.getBestBuyYesCost()),
                                new DecimalFormat("#.##").format(contract.getBestBuyNoCost()),
                                new DecimalFormat("#.##").format(contract.getBestSellYesCost()),
                                new DecimalFormat("#.##").format(contract.getBestSellNoCost()),
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
            trackedMarketsTable.setItems(tableData);
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
        scene.getStylesheets().add("modenaDark.css");
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
        Stage thisStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent parent = fxWeaver.loadView(MoversSceneController.class);
        if (parent == null) {
            System.out.println("parent not created successfully...");
        }
        Scene scene = new Scene(parent);
        scene.getStylesheets().add("modenaDark.css");
        thisStage.setScene(scene);
        thisStage.show();
    }

    @FXML
    void contractHistoryButtonClicked(MouseEvent event) {
        System.out.println("Contract History button clicked");
        DisplayableMC displayableMC = trackedMarketsTable.getSelectionModel().getSelectedItem();
        if(displayableMC!=null && !displayableMC.getContractId().equals("---")){
            openContractHistoryWindow(displayableMC.getContractId());
        }
    }

    @FXML
    void untrackMarketButtonClicked(MouseEvent event) {
        System.out.println("Untrack Market button clicked");
        untrackSelectedMarket();
    }

    private void untrackSelectedMarket() {
        DisplayableMC displayableMC = trackedMarketsTable.getSelectionModel().getSelectedItem();
        if (displayableMC != null && !displayableMC.getMarketId().equals("---")) {
            trackedMarketsIds.untrack(Integer.parseInt(displayableMC.getMarketId()));
            try {
                setTrackedMarketsList();
                updateTrackedMarketsTableView();
            } catch (NoSnapshotsInDatabaseException | MarketNotFoundException ignored) {}
        }
    }
}
