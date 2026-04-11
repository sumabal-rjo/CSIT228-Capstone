package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller for main dashboard.
 */
public class MainController {

    @FXML
    private TableView<?> productTable;

    @FXML
    private TableColumn<?, ?> idColumn;

    @FXML
    private TableColumn<?, ?> nameColumn;

    @FXML
    private TableColumn<?, ?> priceColumn;

    @FXML
    private TableColumn<?, ?> qtyColumn;

    @FXML
    public void initialize() {
        // TODO: Load data
    }
}