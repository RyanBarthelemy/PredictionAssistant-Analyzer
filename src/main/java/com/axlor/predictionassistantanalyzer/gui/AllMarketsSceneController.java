package com.axlor.predictionassistantanalyzer.gui;

import com.axlor.predictionassistantanalyzer.exception.NoSnapshotsInDatabaseException;
import com.axlor.predictionassistantanalyzer.model.Contract;
import com.axlor.predictionassistantanalyzer.model.Market;
import com.axlor.predictionassistantanalyzer.service.SnapshotService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@FxmlView("AllMarketsScene.fxml")
public class AllMarketsSceneController {

    @Autowired
    SnapshotService snapshotService;

    private List<Market> latestMarkets;

    private TableColumn marketIdColumn;
    private TableColumn contractIdColumn;
    private TableColumn nameColumn;
    private TableColumn buyYesColumn;
    private TableColumn buyNoColumn;
    private TableColumn sellYesColumn;
    private TableColumn sellNoColumn;

    @FXML // fx:id="titleLabel"
    private Label titleLabel; // Value injected by FXMLLoader

    @FXML // fx:id="leftPane"
    private AnchorPane leftPane; // Value injected by FXMLLoader

    @FXML // fx:id="moversButton"
    private Button moversButton; // Value injected by FXMLLoader

    @FXML // fx:id="trackedMarketsButton"
    private Button trackedMarketsButton; // Value injected by FXMLLoader

    @FXML // fx:id="label_trackedMarkets1"
    private Label label_trackedMarkets1; // Value injected by FXMLLoader

    @FXML // fx:id="allMarketsTable"
    private TableView<DisplayableMC> allMarketsTable; // Value injected by FXMLLoader

    @FXML // fx:id="allMarketsButton"
    private Button allMarketsButton; // Value injected by FXMLLoader

    @FXML // fx:id="query_textfield_tooltip"
    private Tooltip query_textfield_tooltip; // Value injected by FXMLLoader

    @FXML // fx:id="menuBar"
    private MenuBar menuBar; // Value injected by FXMLLoader

    @FXML // fx:id="titlePane"
    private AnchorPane titlePane; // Value injected by FXMLLoader

    @FXML // fx:id="rightPane"
    private AnchorPane rightPane; // Value injected by FXMLLoader

    @FXML // fx:id="trackSelectedMarketButton"
    private Button trackSelectedMarketButton; // Value injected by FXMLLoader

    @FXML // fx:id="query_textField"
    private TextField query_textField; // Value injected by FXMLLoader

    @FXML // fx:id="label_query"
    private Label label_query; // Value injected by FXMLLoader

    @FXML
    void initialize(){
        System.out.println("Got into init() in fxml controller class");

        //setup columns
        setupColumns();
        setCurrentMarketsList();

        updateAllMarketsTableView();

        System.out.println("Got to end of init() in fxml controller class");
    }

    private void setCurrentMarketsList() {
        try {
            latestMarkets = snapshotService.getLatestSnapshot().getMarkets();
        } catch (NoSnapshotsInDatabaseException e) {
            System.out.println("Caught NoSnapshotsInDatabaseException.");
            latestMarkets = null;
        }
    }

    public void updateAllMarketsTableView() {
        //create table data
        ObservableList<DisplayableMC> tableData = FXCollections.observableArrayList();
        if(latestMarkets!= null) {
            for (Market market : latestMarkets) {
                if (matchesQuery(market)) {
                    //add market data to tableData
                    tableData.add(new DisplayableMC(
                            String.valueOf(market.getId()),
                            "---",
                            market.getName(),
                            "---",
                            "---",
                            "---",
                            "---")
                    );

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
                                String.valueOf(contract.getBestSellNoCost())
                        ));
                    }//for each contract
                }//if market's name matches query words
            }//for each market
            if(tableData.isEmpty()){
                tableData.add(new DisplayableMC("---", "---", "Could not find any Market or Contract with any of the query terms.", "---", "---", "---", "---"));
            }
            allMarketsTable.setItems(tableData);
        }//if latest!=null
        else{
            tableData.add(new DisplayableMC("---", "---", "Database error, could not get any Snaphot Market info from DB.", "---", "---", "---", "---"));

        }

    }

    private boolean matchesQuery(Market market) {
        if(query_textField==null || query_textField.getText()==null || query_textField.getText().equals("")){return true;}

        //ignore case difference
        String marketName = market.getName().toLowerCase();
        String queryText = query_textField.getText().toLowerCase();

        ArrayList<String> queryPhrases = getPhrases(queryText);
        //for each phrase, see if market name or any market contract contains that phrase
        for(String phrase: queryPhrases){
            if(marketName.contains(phrase)){
                return true;
            }
            for (Contract contract: market.getContracts()){
                if(contract.getName().toLowerCase().contains(phrase)){
                    return true;
                }
            }
        }
        return false;
    }

    private ArrayList<String> getPhrases(String queryText) {
        ArrayList<String> searchTerms = new ArrayList();
        String[] terms = queryText.split(",");
        for (String term : terms) {
            String trimmedTerm = term.trim();
            if(trimmedTerm != null && !"".equals(trimmedTerm)){
                searchTerms.add(trimmedTerm);
            }
        }
        return searchTerms;
    }

    private void setupColumns() {
        marketIdColumn = new TableColumn("MID");
        contractIdColumn = new TableColumn("CID");
        nameColumn = new TableColumn("Name");
        buyYesColumn = new TableColumn("BuyYes");
        buyNoColumn = new TableColumn("BuyNo");
        sellYesColumn = new TableColumn("SellYes");
        sellNoColumn = new TableColumn("SellNo");

        marketIdColumn.setCellValueFactory(new PropertyValueFactory<>("marketId"));
        contractIdColumn.setCellValueFactory(new PropertyValueFactory<>("contractId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        buyYesColumn.setCellValueFactory(new PropertyValueFactory<>("buyYes"));
        buyNoColumn.setCellValueFactory(new PropertyValueFactory<>("buyNo"));
        sellYesColumn.setCellValueFactory(new PropertyValueFactory<>("sellYes"));
        sellNoColumn.setCellValueFactory(new PropertyValueFactory<>("sellNo"));

        allMarketsTable.getColumns().addAll(marketIdColumn, contractIdColumn, nameColumn, buyYesColumn, buyNoColumn, sellYesColumn, sellNoColumn);
        allMarketsTable.getColumns().forEach((col)-> col.setSortable(false));//table gets messed up if columns are sorted in any way and cannot return to normal.
    }

    @FXML
    void trackedMarketsButtonClicked(MouseEvent event) {
        System.out.println("trackedMarketsButtonClicked clicked");
        //change scene
    }

    @FXML
    void moversButtonClicked(MouseEvent event) {
        System.out.println("moversButtonClicked clicked");
        //change scene
    }

    @FXML
    void query_TextFieldChanged(KeyEvent event) {
        System.out.println("Query TextField changed");
        updateAllMarketsTableView();
    }

    @FXML
    void trackSelectedMarketButton(MouseEvent event) {
        System.out.println("trackSelectedMarketButton clicked");
        //change scene
    }

}
