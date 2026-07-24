package client.frontend.controller;

import client.frontend.MainApplication;
import client.frontend.api.ApiClient;
import client.frontend.util.SessionManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
            messageLabel.setText("لطفاً نام کاربری و رمز عبور را وارد کنید.");
            return;
        }

        messageLabel.setStyle("-fx-text-fill: blue;");
        messageLabel.setText("در حال برقراری ارتباط...");

        JsonObject jsonRequest = new JsonObject();
        jsonRequest.addProperty("username", username.trim());
        jsonRequest.addProperty("password", password);

        ApiClient.post("/api/users/login", jsonRequest.toString()).thenAccept(response -> {
            Platform.runLater(() -> {
                try {
                    if (response.statusCode() == 200 && response.body() != null) {
                        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

                        if (jsonObject.has("token") && jsonObject.has("role")) {
                            String token = jsonObject.get("token").getAsString();
                            String role = jsonObject.get("role").getAsString();

                            SessionManager.getInstance().login(token, username, role);

                            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("Dashboard.fxml"));
                            Scene scene = new Scene(fxmlLoader.load());
                            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                            stage.setScene(scene);
                            stage.setTitle("داشبورد");
                            stage.centerOnScreen();
                        } else {
                            messageLabel.setStyle("-fx-text-fill: red;");
                            messageLabel.setText("اطلاعات دریافتی از سرور نامعتبر است.");
                        }
                    } else {
                        messageLabel.setStyle("-fx-text-fill: red;");
                        messageLabel.setText("نام کاربری یا رمز عبور اشتباه است.");
                    }
                } catch (Exception e) {
                    messageLabel.setStyle("-fx-text-fill: red;");
                    messageLabel.setText("خطا در پردازش اطلاعات.");
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
    protected void onRegisterLinkClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("Register.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setTitle("ثبت‌نام");
            stage.setScene(scene);
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطا در باز کردن صفحه ثبت‌نام.");
        }
    }
}