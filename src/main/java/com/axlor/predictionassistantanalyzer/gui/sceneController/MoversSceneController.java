package com.axlor.predictionassistantanalyzer.gui.sceneController;

import com.axlor.predictionassistantanalyzer.analyzers.MoverService;
import com.axlor.predictionassistantanalyzer.gui.DisplayableMover;
import com.axlor.predictionassistantanalyzer.gui.TrackedMarkets;
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
import javafx.scene.control.TextField;
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
import java.util.List;

@Component
@FxmlView("MoversScene.fxml")
public class MoversSceneController {

    private static final String DEFAULT_MIN_MOVEMENT_STRING = "1"; //cents
    private static final String DEFAULT_TIMEFRAME_STRING = "10"; //minutes

    @Autowired
    MoverService moverService;

    @Autowired
    TrackedMarkets trackedMarkets;

    @Autowired
    ContractHistorySceneController contractHistorySceneController;

    private final FxWeaver fxWeaver;

    private TableColumn changeColumn;
    private TableColumn currentPriceColumn;
    private TableColumn nameColumn;
    private TableColumn contractIdColumn;
    private TableColumn marketIdColumn;

    private ContextMenu moversTableContextMenu;

    private List<DisplayableMover> displayableMoverList;

    @FXML
    private TextField timeFrameTextField;

    @FXML
    private Label label_Movers;

    @FXML
    private Label titleLabel;

    @FXML
    private AnchorPane leftPane;

    @FXML
    private Button moversButton;

    @FXML
    private Button trackedMarketsButton;

    @FXML
    private Button allMarketsButton;

    @FXML
    private AnchorPane titlePane;

    @FXML
    private TableView<DisplayableMover> moversTableView;

    @FXML
    private AnchorPane rightPane;

    @FXML
    private Button refreshButton;

    @FXML
    private Button contractHistoryButton;

    @FXML
    private TextField minMovementTextField;

    @FXML
    private Label currentTimeFrameLabel;

    @FXML
    private Label currentMinMovementLabel;


    public MoversSceneController(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
    }

    @FXML
    void initialize() {
        setupColumns();
        Platform.runLater(() -> {
            setDisplayableMoverList();
            setupContextMenu();
            updateMoversTableView();
        });
    }

    private void setupContextMenu() {
        moversTableContextMenu = new ContextMenu();

        MenuItem menuItem1 = new MenuItem("Open URL");
        MenuItem menuItem2 = new MenuItem("Track Selected Market");
        MenuItem menuItem3 = new MenuItem("Open Contract History");

        moversTableContextMenu.getItems().addAll(menuItem1, menuItem2, menuItem3);

        //--menuItem1-------------------------------------------------------------------------------------------------\\
        moversTableView.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            if (event.getButton() == MouseButton.SECONDARY) { //right click
                moversTableContextMenu.show(moversTableView, event.getScreenX(), event.getScreenY());
            }
        });
        menuItem1.setOnAction((ActionEvent event) -> {
            DisplayableMover displayableMover = moversTableView.getSelectionModel().getSelectedItem();

            if (displayableMover == null) {
                System.out.println("displayableMover object is null, probably shouldn't be...");
            }
            if (!Desktop.isDesktopSupported()) {
                System.out.println("Desktop is not supported for some reason... Unix system?");
            }
            if (Desktop.isDesktopSupported() && displayableMover != null && !displayableMover.getUrl().equals("--")) {
                try {
                    try {
                        Desktop.getDesktop().browse(new URI(displayableMover.getUrl()));
                    } catch (URISyntaxException ex) {
                        System.out.println("Failed to open market URL.");
                    }
                } catch (IOException ex) {
                    System.out.println("Failed to open market URL.");
                }
            }
        });
        //--------------------------------------------------------------------------------------------------------\\
        //menuItem2 -- track
        menuItem2.setOnAction((ActionEvent event) -> {
            trackSelectedMarket();
        });
        //menuItem3 -- open contract history window
        menuItem3.setOnAction((ActionEvent event) -> {
            DisplayableMover displayableMover = moversTableView.getSelectionModel().getSelectedItem();
            if (displayableMover != null && !displayableMover.getNonUniqueContractId().equals("--")) {
                openContractHistoryWindow(displayableMover.getNonUniqueContractId());
            }
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

    private void trackSelectedMarket() {
        if (!moversTableView.getSelectionModel().getSelectedItem().getNonUniqueMarketId().equals("--")) {
            trackedMarkets.track(
                    Integer.parseInt(moversTableView.getSelectionModel().getSelectedItem().getNonUniqueMarketId())
            );
        }
    }

    private void updateMoversTableView() {
        ObservableList<DisplayableMover> tableData = FXCollections.observableArrayList();

        if (displayableMoverList != null) {
            if (!displayableMoverList.isEmpty()) {
                tableData.addAll(displayableMoverList);
                moversTableView.setItems(tableData);
                setRowBackgroundColors();
            } else { //displayableMoverList is empty
                tableData.add(new DisplayableMover(
                        "", "--", "--",
                        "No Movers found for given parameters.",
                        "--", "--", "--"));
                moversTableView.setItems(tableData);
            }
        } else {
            //displayableMoverList is null
            tableData.add(new DisplayableMover(
                    "", "--", "--",
                    "No Movers found for given parameters.",
                    "--", "--", "--"));
            moversTableView.setItems(tableData);
        }
    }

    private void setRowBackgroundColors() {
        changeColumn.setCellFactory(column -> {
            return new TableCell<DisplayableMover, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : getItem().toString());
                    setGraphic(null);
                    TableRow<DisplayableMover> currentRow = getTableRow();
                    if (!isEmpty()) {
                        if (item.contains("+"))
                            currentRow.setStyle("-fx-background-color:green");
                        else if (item.contains("-"))
                            currentRow.setStyle("-fx-background-color:red"); //light red sort of
                    }
                }//override updateItem
            };
        });
    }

    private void setDisplayableMoverList() {
        displayableMoverList = moverService.getDisplayableMoversList(
                minMovementTextField.getText(),
                timeFrameTextField.getText()
        );
        currentTimeFrameLabel.setText("current=" + timeFrameTextField.getText());
        currentMinMovementLabel.setText("current=" + minMovementTextField.getText());

        if (displayableMoverList == null) {
            displayableMoverList = moverService.getDisplayableMoversList(
                    minMovementTextField.getText(),
                    DEFAULT_TIMEFRAME_STRING
            );
            currentTimeFrameLabel.setText("current=" + DEFAULT_TIMEFRAME_STRING);
            currentMinMovementLabel.setText("current=" + minMovementTextField.getText());
        }

        if (displayableMoverList == null) {
            displayableMoverList = moverService.getDisplayableMoversList(
                    DEFAULT_MIN_MOVEMENT_STRING,
                    timeFrameTextField.getText()
            );
            currentTimeFrameLabel.setText("current=" + timeFrameTextField.getText());
            currentMinMovementLabel.setText("current=" + DEFAULT_MIN_MOVEMENT_STRING);
        }

        if (displayableMoverList == null) {
            displayableMoverList = moverService.getDisplayableMoversList(
                    DEFAULT_MIN_MOVEMENT_STRING,
                    DEFAULT_TIMEFRAME_STRING
            );
            currentTimeFrameLabel.setText("current=" + DEFAULT_TIMEFRAME_STRING);
            currentMinMovementLabel.setText("current=" + DEFAULT_MIN_MOVEMENT_STRING);
        }
    }

    private void setupColumns() {
        changeColumn = new TableColumn("Change");
        changeColumn.setPrefWidth(50.0);
        currentPriceColumn = new TableColumn("Current");
        currentPriceColumn.setPrefWidth(100.0);
        nameColumn = new TableColumn("Name");
        nameColumn.setPrefWidth(600.0);
        contractIdColumn = new TableColumn("CID");
        contractIdColumn.setPrefWidth(50.0);
        marketIdColumn = new TableColumn("MID");
        marketIdColumn.setPrefWidth(50.0);

        changeColumn.setCellValueFactory(new PropertyValueFactory<>("change"));
        currentPriceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        contractIdColumn.setCellValueFactory(new PropertyValueFactory<>("nonUniqueContractId"));
        marketIdColumn.setCellValueFactory(new PropertyValueFactory<>("nonUniqueMarketId"));

        moversTableView.getColumns().addAll(changeColumn, currentPriceColumn, nameColumn, contractIdColumn, marketIdColumn);
        moversTableView.getColumns().forEach((col) -> col.setSortable(false));
    }

    @FXML
    void allMarketsButtonClicked(MouseEvent event) {
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
        Stage thisStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent parent = fxWeaver.loadView(TrackedMarketsSceneController.class);
        if (parent == null) {
            System.out.println("parent not created successfully...");
        }
        Scene scene = new Scene(parent);
        scene.getStylesheets().add("modenaDark.css");
        thisStage.setScene(scene);
        thisStage.show();
    }

    @FXML
    void moversButtonClicked(MouseEvent event) {
        //we are already here
    }

    @FXML
    void contractHistoryButtonClicked(MouseEvent event) {
        DisplayableMover displayableMover = moversTableView.getSelectionModel().getSelectedItem();
        if (displayableMover != null && !displayableMover.getNonUniqueContractId().equals("--")) {
            openContractHistoryWindow(displayableMover.getNonUniqueContractId());
        }
    }

    @FXML
    void refreshButtonClicked(MouseEvent event) {
        //System.out.println("Refresh Button clicked.");
        refreshButton.setText("Plz Wait...");
        Platform.runLater(() -> {
            setDisplayableMoverList();
            updateMoversTableView();
            refreshButton.setText("Refresh");
        });
    }
}

