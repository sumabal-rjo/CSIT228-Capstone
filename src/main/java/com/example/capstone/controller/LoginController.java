package com.example.capstone.controller;

import com.example.capstone.MainApplication;
import com.example.capstone.database.UserDAO;
import com.example.capstone.model.User;
import com.example.capstone.util.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private RadioButton adminRadio;
    @FXML private ToggleGroup roleGroup;
    @FXML private Label loginMessageLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        loginMessageLabel.setText("");
    }

    @FXML
    public void onLogin() {
        loginMessageLabel.setText("");

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showLoginMessage("Username and password are required.");
            return;
        }

        if (roleGroup.getSelectedToggle() == null) {
            showLoginMessage("Please select a role.");
            return;
        }

        User user = userDAO.login(username, password);

        if (user == null) {
            showLoginMessage("Invalid username or password.");
            passwordField.clear();
            return;
        }

        String selectedRole = adminRadio.isSelected() ? "admin" : "staff";
        if (!selectedRole.equalsIgnoreCase(user.getRole())) {
            showLoginMessage("Role mismatch. Please select the correct role.");
            passwordField.clear();
            return;
        }

        SessionManager.getInstance().setCurrentUser(user);
        openScreen("hello-view.fxml", "CoreStock Dashboard", 1180, 720);
    }

    @FXML
    public void onClearLogin() {
        usernameField.clear();
        passwordField.clear();
        adminRadio.setSelected(true);
        loginMessageLabel.setText("");
        usernameField.requestFocus();
    }

    @FXML
    public void onCreateAccountLink() {
        openScreen("CreateAccountView.fxml", "Create CoreStock Account", 920, 640);
    }

    private void openScreen(String fxmlFile, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(fxmlFile));
            Scene scene = new Scene(loader.load(), width, height);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            showLoginMessage("Could not open the next screen.");
        }
    }

    private void showLoginMessage(String message) {
        loginMessageLabel.setText(message);
        loginMessageLabel.setStyle("-fx-text-fill: #E53E3E; -fx-font-size: 11px;");
    }
}
