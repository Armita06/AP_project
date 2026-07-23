package client.frontend.controller;

import client.frontend.MainApplication;
import client.frontend.api.ApiClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;

    @FXML
    private Label messageLabel;

    @FXML
    protected void onRegisterButtonClick(ActionEvent event) {
        String name = nameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();

        if (name == null || name.trim().isEmpty() ||
                username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                phone == null || phone.trim().isEmpty() ||
                email == null || email.trim().isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("لطفاً تمام فیلدها را پر کنید.");
            return;
        }

        try {
            String jsonBody = String.format(
                    "{\"name\":\"%s\", \"username\":\"%s\", \"password\":\"%s\", \"phone\":\"%s\", \"email\":\"%s\"}",
                    name, username, password, phone, email
            );
            ApiClient.post("/auth/register", jsonBody);

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("ثبت‌نام با موفقیت انجام شد.");

        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطا در ثبت‌نام یا ارتباط با سرور.");
        }
    }

    @FXML
    protected void onLoginLinkClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("Login.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setTitle("ورود به سیستم");
            stage.setScene(scene);

        } catch (IOException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("فایل صفحه ورود پیدا نشد.");
        }
    }
}