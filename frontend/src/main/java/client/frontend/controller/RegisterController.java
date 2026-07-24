package client.frontend.controller;

import client.frontend.MainApplication;
import client.frontend.api.ApiClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Label messageLabel;

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
            messageLabel.setText("لطفاً تمامی فیلدها را پر کنید.");
            return;
        }

        messageLabel.setStyle("-fx-text-fill: blue;");
        messageLabel.setText("در حال ارتباط با سرور...");

        String jsonBody = String.format(
                "{\"fullName\":\"%s\", \"username\":\"%s\", \"password\":\"%s\", \"phoneNumber\":\"%s\", \"email\":\"%s\"}",
                name.trim(), username.trim(), password.trim(), phone.trim(), email.trim()
        );

        ApiClient.post("/api/users/register", jsonBody).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    messageLabel.setStyle("-fx-text-fill: green;");
                    messageLabel.setText("ثبت‌نام با موفقیت انجام شد. می‌توانید وارد شوید.");
                } else {
                    messageLabel.setStyle("-fx-text-fill: red;");
                    messageLabel.setText("خطا در ثبت‌نام. ممکن است نام کاربری تکراری باشد.");
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("خطا در ارتباط با سرور.");
            });
            return null;
        });
    }

    @FXML
    protected void onLoginLinkClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("Login.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("ورود");
            stage.setScene(scene);
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطا در باز کردن صفحه ورود.");
        }
    }
}