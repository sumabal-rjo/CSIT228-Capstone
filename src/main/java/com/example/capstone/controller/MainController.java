package com.example.capstone.controller;

import com.example.capstone.database.CategoryDAO;
import com.example.capstone.database.InventoryLogDAO;
import com.example.capstone.database.ProductDAO;
import com.example.capstone.database.SaleDAO;
import com.example.capstone.database.SupplierDAO;
import com.example.capstone.database.UserDAO;
import com.example.capstone.model.Category;
import com.example.capstone.model.InventoryLog;
import com.example.capstone.model.Product;
import com.example.capstone.model.Sale;
import com.example.capstone.model.Supplier;
import com.example.capstone.model.User;
import com.example.capstone.report.InventoryReportGenerator;
import com.example.capstone.util.AsyncLoader;
import com.example.capstone.util.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainController {

    @FXML private Label roleLabel;
    @FXML private Label companyLabel;
    @FXML private Label statSkuLabel;
    @FXML private Label statValueLabel;
    @FXML private Label statLowLabel;
    @FXML private Label selectedStockLabel;
    @FXML private TextField searchField;
    @FXML private TextField stockAmountField;
    @FXML private ComboBox<Category> filterCategoryCombo;
    @FXML private ComboBox<String> stockFilterCombo;

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, String> supplierColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> qtyColumn;

    @FXML private TableView<Product> lowStockTable;
    @FXML private TableColumn<Product, Integer> lowIdColumn;
    @FXML private TableColumn<Product, String> lowNameColumn;
    @FXML private TableColumn<Product, String> lowCategoryColumn;
    @FXML private TableColumn<Product, Integer> lowQtyColumn;
    @FXML private TableColumn<Product, Integer> lowThresholdColumn;

    @FXML private ComboBox<Product> saleProductCombo;
    @FXML private TextField saleQuantityField;
    @FXML private TextField saleNoteField;
    @FXML private Label saleTotalLabel;
    @FXML private TableView<Sale> salesTable;
    @FXML private TableColumn<Sale, Integer> saleIdColumn;
    @FXML private TableColumn<Sale, String> saleProductColumn;
    @FXML private TableColumn<Sale, Integer> saleQtyColumn;
    @FXML private TableColumn<Sale, Double> saleUnitColumn;
    @FXML private TableColumn<Sale, Double> saleSubtotalColumn;
    @FXML private TableColumn<Sale, String> saleDateColumn;
    @FXML private TableColumn<Sale, String> saleUserColumn;

    @FXML private TableView<InventoryLog> logsTable;
    @FXML private TableColumn<InventoryLog, Integer> logIdColumn;
    @FXML private TableColumn<InventoryLog, String> logProductColumn;
    @FXML private TableColumn<InventoryLog, String> logUserColumn;
    @FXML private TableColumn<InventoryLog, String> logTypeColumn;
    @FXML private TableColumn<InventoryLog, Integer> logBeforeColumn;
    @FXML private TableColumn<InventoryLog, Integer> logChangeColumn;
    @FXML private TableColumn<InventoryLog, Integer> logAfterColumn;
    @FXML private TableColumn<InventoryLog, String> logNoteColumn;
    @FXML private TableColumn<InventoryLog, String> logDateColumn;

    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, Integer> catIdColumn;
    @FXML private TableColumn<Category, String> catNameColumn;
    @FXML private TableColumn<Category, String> catDescColumn;

    @FXML private TableView<Supplier> supplierTable;
    @FXML private TableColumn<Supplier, Integer> supIdColumn;
    @FXML private TableColumn<Supplier, String> supNameColumn;
    @FXML private TableColumn<Supplier, String> supContactColumn;
    @FXML private TableColumn<Supplier, String> supPhoneColumn;
    @FXML private TableColumn<Supplier, String> supEmailColumn;
    @FXML private TableColumn<Supplier, String> supAddressColumn;

    @FXML private TextField profileNameField;
    @FXML private TextField profileCompanyField;
    @FXML private TextField profileEmailField;
    @FXML private TextField profileUsernameField;
    @FXML private TextField profilePasswordField;
    @FXML private Label profileRoleLabel;

    @FXML private VBox productsPane;
    @FXML private VBox lowStockPane;
    @FXML private VBox salesPane;
    @FXML private VBox logsPane;
    @FXML private VBox categoriesPane;
    @FXML private VBox suppliersPane;
    @FXML private VBox profilePane;

    @FXML private Button navProducts;
    @FXML private Button navLowStock;
    @FXML private Button navSales;
    @FXML private Button navLogs;
    @FXML private Button navCategories;
    @FXML private Button navSuppliers;
    @FXML private Button navProfile;
    @FXML private Button addProductButton;
    @FXML private Button editProductButton;
    @FXML private Button deleteProductButton;
    @FXML private Button increaseStockButton;
    @FXML private Button decreaseStockButton;
    @FXML private Button reportButton;

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final InventoryLogDAO inventoryLogDAO = new InventoryLogDAO();
    private final UserDAO userDAO = new UserDAO();
    private final InventoryReportGenerator reportGenerator = new InventoryReportGenerator();

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<Product> lowStockList = FXCollections.observableArrayList();
    private final ObservableList<Category> categoryList = FXCollections.observableArrayList();
    private final ObservableList<Supplier> supplierList = FXCollections.observableArrayList();
    private final ObservableList<Sale> saleList = FXCollections.observableArrayList();
    private final ObservableList<InventoryLog> logList = FXCollections.observableArrayList();

    private static final String ACTIVE_STYLE = "-fx-background-color: #FFFDF8; -fx-background-radius: 8; -fx-text-fill: #7A1E2C; -fx-font-size: 13; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding: 10 12;";
    private static final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-background-radius: 8; -fx-text-fill: #FFFDF8; -fx-font-size: 13; -fx-alignment: CENTER_LEFT; -fx-padding: 10 12;";

    @FXML
    public void initialize() {
        setupTables();
        setupFilters();
        setupSelection();
        setupRoleAccess();
        showProducts();
        loadDataInBackground();
    }

    private void setupTables() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        qtyColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        productTable.setItems(productList);
        productTable.setRowFactory(table -> lowStockRow());

        lowIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        lowNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        lowCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        lowQtyColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        lowThresholdColumn.setCellValueFactory(new PropertyValueFactory<>("lowStockThreshold"));
        lowStockTable.setItems(lowStockList);

        saleIdColumn.setCellValueFactory(new PropertyValueFactory<>("saleId"));
        saleProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        saleQtyColumn.setCellValueFactory(new PropertyValueFactory<>("quantitySold"));
        saleUnitColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        saleSubtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        saleDateColumn.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        saleUserColumn.setCellValueFactory(new PropertyValueFactory<>("recordedBy"));
        salesTable.setItems(saleList);

        logIdColumn.setCellValueFactory(new PropertyValueFactory<>("logId"));
        logProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        logUserColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        logTypeColumn.setCellValueFactory(new PropertyValueFactory<>("changeType"));
        logBeforeColumn.setCellValueFactory(new PropertyValueFactory<>("quantityBefore"));
        logChangeColumn.setCellValueFactory(new PropertyValueFactory<>("quantityChange"));
        logAfterColumn.setCellValueFactory(new PropertyValueFactory<>("quantityAfter"));
        logNoteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
        logDateColumn.setCellValueFactory(new PropertyValueFactory<>("loggedAt"));
        logsTable.setItems(logList);

        catIdColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        catNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        catDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        categoryTable.setItems(categoryList);

        supIdColumn.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        supNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        supContactColumn.setCellValueFactory(new PropertyValueFactory<>("contactName"));
        supPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        supEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        supAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        supplierTable.setItems(supplierList);
    }

    private TableRow<Product> lowStockRow() {
        return new TableRow<Product>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                setStyle(!empty && product != null && product.isLowStock() ? "-fx-background-color: rgba(212,160,23,0.16);" : "");
            }
        };
    }

    private void setupFilters() {
        stockFilterCombo.setItems(FXCollections.observableArrayList("All Stock", "Available", "Low Stock", "Out of Stock"));
        stockFilterCombo.setValue("All Stock");
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyProductFilters());
        filterCategoryCombo.valueProperty().addListener((obs, oldValue, newValue) -> applyProductFilters());
        stockFilterCombo.valueProperty().addListener((obs, oldValue, newValue) -> applyProductFilters());
        saleProductCombo.valueProperty().addListener((obs, oldValue, newValue) -> updateSaleTotal());
        saleQuantityField.textProperty().addListener((obs, oldValue, newValue) -> updateSaleTotal());
    }

    private void setupSelection() {
        productTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateSelectedStockLabel(newValue));
        updateSelectedStockLabel(null);
    }

    private void setupRoleAccess() {
        boolean loggedIn = SessionManager.getInstance().isLoggedIn();
        boolean admin = !loggedIn || SessionManager.getInstance().isAdmin();
        String role = admin ? "Admin" : "Staff";
        roleLabel.setText(role + " Access");
        companyLabel.setText(getCompanyDisplayName());
        addProductButton.setVisible(true);
        addProductButton.setManaged(true);
        editProductButton.setVisible(true);
        editProductButton.setManaged(true);
        deleteProductButton.setVisible(admin);
        deleteProductButton.setManaged(admin);
        increaseStockButton.setVisible(true);
        increaseStockButton.setManaged(true);
        decreaseStockButton.setVisible(true);
        decreaseStockButton.setManaged(true);
        reportButton.setVisible(admin);
        reportButton.setManaged(admin);
        navLogs.setVisible(admin);
        navLogs.setManaged(admin);
        navCategories.setVisible(admin);
        navCategories.setManaged(admin);
        navSuppliers.setVisible(admin);
        navSuppliers.setManaged(admin);
        fillProfile();
    }

    private void loadDataInBackground() {
        AsyncLoader.run(
                new AsyncLoader.DataTask<DashboardData>() {
                    @Override
                    public DashboardData run() {
                        DashboardData data = new DashboardData();
                        data.products = productDAO.getAll();
                        data.lowStock = productDAO.getLowStock();
                        data.categories = categoryDAO.getAll();
                        data.suppliers = supplierDAO.getAll();
                        data.sales = saleDAO.getSoldItems();
                        data.logs = inventoryLogDAO.getAll();
                        data.summary = productDAO.getSummary();
                        data.totalValue = productDAO.getTotalValue();
                        return data;
                    }
                },
                new AsyncLoader.DataHandler<DashboardData>() {
                    @Override
                    public void handle(DashboardData data) {
                        productList.setAll(data.products);
                        lowStockList.setAll(data.lowStock);
                        categoryList.setAll(data.categories);
                        supplierList.setAll(data.suppliers);
                        saleList.setAll(data.sales);
                        logList.setAll(data.logs);
                        filterCategoryCombo.setItems(FXCollections.observableArrayList(data.categories));
                        saleProductCombo.setItems(FXCollections.observableArrayList(data.products));
                        updateStatCards(data.summary, data.totalValue);
                    }
                },
                new AsyncLoader.ErrorHandler() {
                    @Override
                    public void handle(Exception e) {
                        showAlert("Could not load dashboard data.");
                    }
                }
        );
    }

    private void reloadAllData() {
        List<Product> products = productDAO.getAll();
        productList.setAll(products);
        lowStockList.setAll(productDAO.getLowStock());
        categoryList.setAll(categoryDAO.getAll());
        supplierList.setAll(supplierDAO.getAll());
        saleList.setAll(saleDAO.getSoldItems());
        logList.setAll(inventoryLogDAO.getAll());
        saleProductCombo.setItems(FXCollections.observableArrayList(products));
        filterCategoryCombo.setItems(FXCollections.observableArrayList(categoryList));
        refreshStatCards();
    }

    private void applyProductFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
        Category category = filterCategoryCombo.getValue();
        Integer categoryId = category == null ? null : category.getCategoryId();
        String stockFilter = toStockFilter(stockFilterCombo.getValue());
        productList.setAll(productDAO.searchWithFilters(keyword, categoryId, stockFilter));
    }

    private String toStockFilter(String label) {
        if ("Available".equals(label)) {
            return "AVAILABLE";
        }
        if ("Low Stock".equals(label)) {
            return "LOW";
        }
        if ("Out of Stock".equals(label)) {
            return "OUT";
        }
        return "ALL";
    }

    @FXML public void onClearFilters() {
        searchField.clear();
        filterCategoryCombo.setValue(null);
        stockFilterCombo.setValue("All Stock");
        productList.setAll(productDAO.getAll());
    }

    @FXML public void showProducts() { showPane(productsPane, navProducts); }
    @FXML public void showLowStock() { lowStockList.setAll(productDAO.getLowStock()); showPane(lowStockPane, navLowStock); }
    @FXML public void showSales() { saleList.setAll(saleDAO.getSoldItems()); showPane(salesPane, navSales); }
    @FXML public void showLogs() {
        if (!canViewAdminOnlyPages()) {
            showAlert("Inventory logs are only available to Admin users.");
            showProducts();
            return;
        }
        logList.setAll(inventoryLogDAO.getAll());
        showPane(logsPane, navLogs);
    }

    private boolean canViewAdminOnlyPages() {
        return !SessionManager.getInstance().isLoggedIn() || SessionManager.getInstance().isAdmin();
    }

    @FXML public void showCategories() { categoryList.setAll(categoryDAO.getAll()); showPane(categoriesPane, navCategories); }
    @FXML public void showSuppliers() { supplierList.setAll(supplierDAO.getAll()); showPane(suppliersPane, navSuppliers); }
    @FXML public void showProfile() { fillProfile(); showPane(profilePane, navProfile); }

    private void showPane(VBox selectedPane, Button selectedButton) {
        VBox[] panes = {productsPane, lowStockPane, salesPane, logsPane, categoriesPane, suppliersPane, profilePane};
        Button[] buttons = {navProducts, navLowStock, navSales, navLogs, navCategories, navSuppliers, navProfile};
        for (VBox pane : panes) {
            pane.setVisible(pane == selectedPane);
            pane.setManaged(pane == selectedPane);
        }
        for (Button button : buttons) {
            button.setStyle(button == selectedButton ? ACTIVE_STYLE : INACTIVE_STYLE);
        }
    }

    @FXML public void onAddProduct() { openProductDialog(null); }

    @FXML
    public void onEditProduct() {
        Product product = productTable.getSelectionModel().getSelectedItem();
        if (product == null) {
            showAlert("Select a product to edit.");
            return;
        }
        openProductDialog(product);
    }

    @FXML
    public void onDeleteProduct() {
        Product product = productTable.getSelectionModel().getSelectedItem();
        if (product == null) {
            showAlert("Select a product to delete.");
            return;
        }
        if (showConfirm("Delete " + product.getName() + "?")) {
            productDAO.delete(product.getProductId());
            reloadAllData();
        }
    }

    @FXML public void onIncreaseStock() { changeSelectedStock(getStockChangeAmount()); }
    @FXML public void onDecreaseStock() { changeSelectedStock(-getStockChangeAmount()); }

    @FXML
    public void onRecordSale() {
        Product product = saleProductCombo.getValue();
        if (product == null || saleQuantityField.getText().trim().isEmpty()) {
            showAlert("Choose a product and enter quantity sold.");
            return;
        }

        try {
            int quantity = Integer.parseInt(saleQuantityField.getText().trim());
            Integer userId = getCurrentUserId();
            boolean saved = saleDAO.recordSale(product, quantity, userId, saleNoteField.getText().trim());
            if (!saved) {
                showAlert("Sale was not saved. Check stock quantity.");
                return;
            }
            saleQuantityField.clear();
            saleNoteField.clear();
            reloadAllData();
            showSales();
        } catch (NumberFormatException e) {
            showAlert("Quantity sold must be a whole number.");
        }
    }

    @FXML
    public void onGenerateReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Inventory Report");
        fileChooser.setInitialFileName(reportGenerator.defaultFilename("txt"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Report", "*.txt"));
        File file = fileChooser.showSaveDialog(productsPane.getScene().getWindow());
        if (file == null) {
            return;
        }
        int count = reportGenerator.generateTextSummary(file);
        showAlert(count >= 0 ? "Report generated successfully." : "Report generation failed.");
    }

    @FXML
    public void onLogout() {
        if (!showConfirm("Log out of CoreStock?")) {
            return;
        }

        try {
            SessionManager.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/capstone/LoginView.fxml"));
            Scene scene = new Scene(loader.load(), 920, 640);
            Stage stage = (Stage) productsPane.getScene().getWindow();
            stage.setTitle("CoreStock Login");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            showAlert("Could not return to login screen.");
        }
    }

    @FXML
    public void onSaveProfile() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            showAlert("Profile editing is available after login.");
            return;
        }
        User user = SessionManager.getInstance().getCurrentUser();
        if (!profilePasswordField.getText().trim().isEmpty() && profilePasswordField.getText().trim().length() < 8) {
            showAlert("Password must be at least 8 characters.");
            return;
        }
        user.setFullName(profileNameField.getText().trim());
        user.setCompanyName(profileCompanyField.getText().trim());
        user.setEmail(profileEmailField.getText().trim());
        user.setUsername(profileUsernameField.getText().trim());
        if (!profilePasswordField.getText().trim().isEmpty()) {
            user.setPassword(profilePasswordField.getText().trim());
        }
        if (userDAO.updateProfile(user)) {
            User updatedUser = userDAO.getById(user.getUserId());
            SessionManager.getInstance().setCurrentUser(updatedUser == null ? user : updatedUser);
            fillProfile();
            companyLabel.setText(getCompanyDisplayName());
            showAlert("Profile updated.");
        } else {
            showAlert("Profile update failed.");
        }
    }

    @FXML
    public void onDeleteAccount() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            showAlert("You must be logged in to delete an account.");
            return;
        }

        if (!showConfirm("Delete this account and return to login?")) {
            return;
        }

        int userId = SessionManager.getInstance().getCurrentUser().getUserId();
        if (!userDAO.delete(userId)) {
            showAlert("Account could not be deleted.");
            return;
        }

        onLogoutWithoutConfirm();
    }

    @FXML public void onAddCategory() { openCategoryDialog(null); }
    @FXML public void onEditCategory() {
        Category category = categoryTable.getSelectionModel().getSelectedItem();
        if (category == null) { showAlert("Select a category to edit."); return; }
        openCategoryDialog(category);
    }
    @FXML public void onDeleteCategory() {
        Category category = categoryTable.getSelectionModel().getSelectedItem();
        if (category == null) { showAlert("Select a category to delete."); return; }
        if (showConfirm("Delete category " + category.getName() + "?")) {
            categoryDAO.delete(category.getCategoryId());
            reloadAllData();
        }
    }
    @FXML public void onAddSupplier() { openSupplierDialog(null); }
    @FXML public void onEditSupplier() {
        Supplier supplier = supplierTable.getSelectionModel().getSelectedItem();
        if (supplier == null) { showAlert("Select a supplier to edit."); return; }
        openSupplierDialog(supplier);
    }
    @FXML public void onDeleteSupplier() {
        Supplier supplier = supplierTable.getSelectionModel().getSelectedItem();
        if (supplier == null) { showAlert("Select a supplier to delete."); return; }
        if (showConfirm("Delete supplier " + supplier.getName() + "?")) {
            supplierDAO.delete(supplier.getSupplierId());
            reloadAllData();
        }
    }

    private void openProductDialog(Product oldProduct) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/capstone/AddProductView.fxml"));
            Parent root = loader.load();
            AddProductController controller = loader.getController();
            controller.setCategories(categoryDAO.getAll());
            controller.setSuppliers(supplierDAO.getAll());
            if (oldProduct != null) {
                controller.prefill(oldProduct);
            }

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle(oldProduct == null ? "Add Product" : "Edit Product");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                Product product = controller.getProduct();
                if (oldProduct == null) {
                    productDAO.add(product);
                } else {
                    product.setProductId(oldProduct.getProductId());
                    productDAO.update(product);
                }
                reloadAllData();
            }
        } catch (IOException e) {
            showAlert("Could not open product form.");
        }
    }

    private void openCategoryDialog(Category oldCategory) {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle(oldCategory == null ? "Add Category" : "Edit Category");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField nameField = styledField(oldCategory == null ? "" : oldCategory.getName());
        TextField descField = styledField(oldCategory == null ? "" : oldCategory.getDescription());
        VBox box = new VBox(10, styledLabel("Category Name"), nameField, styledLabel("Description"), descField);
        dialog.getDialogPane().setContent(box);
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK && !nameField.getText().trim().isEmpty()) {
                Category category = oldCategory == null ? new Category() : oldCategory;
                category.setName(nameField.getText().trim());
                category.setDescription(descField.getText().trim());
                return category;
            }
            return null;
        });
        dialog.showAndWait();
        Category result = dialog.getResult();
        if (result != null) {
            if (oldCategory == null) { categoryDAO.add(result); } else { categoryDAO.update(result); }
            reloadAllData();
        }
    }

    private void openSupplierDialog(Supplier oldSupplier) {
        Dialog<Supplier> dialog = new Dialog<>();
        dialog.setTitle(oldSupplier == null ? "Add Supplier" : "Edit Supplier");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField nameField = styledField(oldSupplier == null ? "" : oldSupplier.getName());
        TextField contactField = styledField(oldSupplier == null ? "" : oldSupplier.getContactName());
        TextField phoneField = styledField(oldSupplier == null ? "" : oldSupplier.getPhone());
        TextField emailField = styledField(oldSupplier == null ? "" : oldSupplier.getEmail());
        TextField addressField = styledField(oldSupplier == null ? "" : oldSupplier.getAddress());
        VBox box = new VBox(10, styledLabel("Company Name"), nameField, styledLabel("Contact Person"), contactField,
                styledLabel("Phone"), phoneField, styledLabel("Email"), emailField, styledLabel("Address"), addressField);
        dialog.getDialogPane().setContent(box);
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK && !nameField.getText().trim().isEmpty()) {
                Supplier supplier = oldSupplier == null ? new Supplier() : oldSupplier;
                supplier.setName(nameField.getText().trim());
                supplier.setContactName(contactField.getText().trim());
                supplier.setPhone(phoneField.getText().trim());
                supplier.setEmail(emailField.getText().trim());
                supplier.setAddress(addressField.getText().trim());
                return supplier;
            }
            return null;
        });
        dialog.showAndWait();
        Supplier result = dialog.getResult();
        if (result != null) {
            if (oldSupplier == null) { supplierDAO.add(result); } else { supplierDAO.update(result); }
            reloadAllData();
        }
    }

    private void changeSelectedStock(int changeAmount) {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert("Select a product first.");
            return;
        }
        String note = changeAmount > 0 ? "Stock increased from dashboard" : "Stock decreased from dashboard";
        if (!productDAO.adjustStock(selectedProduct.getProductId(), changeAmount, note, getCurrentUserId())) {
            showAlert("Stock update failed.");
            return;
        }
        int selectedProductId = selectedProduct.getProductId();
        reloadAllData();
        selectProductById(selectedProductId);
    }

    private int getStockChangeAmount() {
        try {
            int amount = Integer.parseInt(stockAmountField.getText().trim());
            if (amount > 0) {
                return amount;
            }
        } catch (Exception e) {
            return 1;
        }
        return 1;
    }

    private void selectProductById(int productId) {
        for (Product product : productList) {
            if (product.getProductId() == productId) {
                productTable.getSelectionModel().select(product);
                productTable.scrollTo(product);
                updateSelectedStockLabel(product);
                return;
            }
        }
        updateSelectedStockLabel(null);
    }

    private void updateSaleTotal() {
        Product product = saleProductCombo.getValue();
        try {
            int quantity = saleQuantityField.getText().trim().isEmpty() ? 0 : Integer.parseInt(saleQuantityField.getText().trim());
            double total = product == null ? 0 : product.getPrice() * quantity;
            saleTotalLabel.setText(String.format("Total: P %,.2f", total));
        } catch (NumberFormatException e) {
            saleTotalLabel.setText("Total: P 0.00");
        }
    }

    private Integer getCurrentUserId() {
        if (SessionManager.getInstance().isLoggedIn()) {
            return SessionManager.getInstance().getCurrentUser().getUserId();
        }
        return null;
    }

    private void fillProfile() {
        if (SessionManager.getInstance().isLoggedIn()) {
            User user = SessionManager.getInstance().getCurrentUser();
            profileNameField.setText(user.getFullName());
            profileCompanyField.setText(user.getCompanyName() == null ? "" : user.getCompanyName());
            profileEmailField.setText(user.getEmail() == null ? "" : user.getEmail());
            profileUsernameField.setText(user.getUsername());
            profilePasswordField.clear();
            profileRoleLabel.setText(user.isAdmin() ? "Admin" : "Staff");
        } else {
            profileNameField.setText("Not logged in");
            profileCompanyField.setText("");
            profileEmailField.setText("");
            profileUsernameField.setText("");
            profilePasswordField.setText("");
            profileRoleLabel.setText("Demo Admin");
        }
    }

    private String getCompanyDisplayName() {
        if (SessionManager.getInstance().isLoggedIn()) {
            String companyName = SessionManager.getInstance().getCurrentUser().getCompanyName();
            if (companyName != null && !companyName.trim().isEmpty()) {
                return companyName;
            }
        }
        return "CoreStock Company";
    }

    private void refreshStatCards() {
        updateStatCards(productDAO.getSummary(), productDAO.getTotalValue());
    }

    private void updateStatCards(int[] summary, double totalValue) {
        statSkuLabel.setText(String.valueOf(summary[0]));
        statValueLabel.setText(String.format("P %,.2f", totalValue));
        statLowLabel.setText(String.valueOf(summary[1]));
    }

    private void updateSelectedStockLabel(Product product) {
        selectedStockLabel.setText(product == null ? "Selected Stock: --" : "Selected Stock: " + product.getQuantity());
    }

    private void onLogoutWithoutConfirm() {
        try {
            SessionManager.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/capstone/LoginView.fxml"));
            Scene scene = new Scene(loader.load(), 920, 640);
            Stage stage = (Stage) productsPane.getScene().getWindow();
            stage.setTitle("CoreStock Login");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            showAlert("Could not return to login screen.");
        }
    }

    private boolean showConfirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(null);
        alert.showAndWait();
        return alert.getResult() == ButtonType.OK;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private TextField styledField(String value) {
        TextField textField = new TextField(value);
        textField.setStyle("-fx-background-color: #FFF9EC; -fx-text-fill: #2B1B1F; -fx-border-color: #D4A017; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12;");
        return textField;
    }

    private Label styledLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 11; -fx-text-fill: #6F5A5D;");
        return label;
    }

    private static class DashboardData {
        List<Product> products;
        List<Product> lowStock;
        List<Category> categories;
        List<Supplier> suppliers;
        List<Sale> sales;
        List<InventoryLog> logs;
        int[] summary;
        double totalValue;
    }
}
