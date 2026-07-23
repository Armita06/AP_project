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

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    protected void onLoginButtonClick(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("نام کاربری و رمز عبور نباید خالی باشند.");
            return;
        }

        try {
            String jsonBody = "{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}";
            ApiClient.post("/auth/login", jsonBody);

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("ورود موفقیت‌آمیز بود.");

        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("ارتباط با سرور برقرار نشد.");
        }
    }

    @FXML
    protected void onRegisterLinkClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("Register.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setTitle("ثبت‌نام در سامانه");
            stage.setScene(scene);

        } catch (IOException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("فایل صفحه ثبت‌نام هنوز ساخته نشده است.");
        }
    }
}