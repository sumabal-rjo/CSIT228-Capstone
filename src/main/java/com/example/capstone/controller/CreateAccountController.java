package com.example.capstone.controller;

import com.example.capstone.MainApplication;
import com.example.capstone.database.UserDAO;
import com.example.capstone.model.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.regex.Pattern;

public class CreateAccountController {

    @FXML private TextField companyNameField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        roleCombo.getItems().setAll("Staff", "Admin");
        roleCombo.setValue("Staff");

    }

    @FXML
    public void onCreateAccount() {
        if (companyNameField.getText().trim().isEmpty()
                || fullNameField.getText().trim().isEmpty()
                || emailField.getText().trim().isEmpty()
                || usernameField.getText().trim().isEmpty()
                || passwordField.getText().isEmpty()) {
            showMessage("Company name, full name, email, username, and password are required.");
            return;
        }

        if (!isValidEmail(emailField.getText().trim())) {
            showMessage("Please enter a valid email address, like name@company.com.");
            emailField.requestFocus();
            return;
        }

        if (passwordField.getText().length() < 8) {
            showMessage("Password must be at least 8 characters.");
            return;
        }

        if (userDAO.usernameExists(usernameField.getText().trim())) {
            showMessage("Username is already taken.");
            return;
        }

        User user = new User();
        user.setCompanyName(companyNameField.getText().trim());
        user.setFullName(fullNameField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setUsername(usernameField.getText().trim());
        user.setPassword(passwordField.getText());
        user.setRole(roleCombo.getValue());

        if (!userDAO.add(user)) {
            showMessage("Account could not be created. Please check the database connection.");
            return;
        }

        showMessage("Account created. You can now sign in.");
        openLogin();
    }

    @FXML
    public void onBackToLogin() {
        openLogin();
    }

    private void openLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("LoginView.fxml"));
            Scene scene = new Scene(loader.load(), 920, 640);
            Stage stage = (Stage) companyNameField.getScene().getWindow();
            stage.setTitle("CoreStock Login");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            showMessage("Could not return to login.");
        }
    }

    private void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
