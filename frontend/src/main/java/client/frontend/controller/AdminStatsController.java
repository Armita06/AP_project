package client.frontend.controller;

import client.frontend.MainApplication;
import client.frontend.api.ApiClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.http.HttpResponse;

public class AdminStatsController {

    @FXML private Label totalUsersLabel;
    @FXML private Label totalAdsLabel;
    @FXML private Label pendingAdsLabel;
    @FXML private Label totalReportsLabel;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        loadStatistics();
    }

    private void loadStatistics() {
        try {
            HttpResponse<String> response = ApiClient.get("/api/admin/stats");

            if (response.statusCode() == 200 && response.body() != null) {
                JsonObject stats = JsonParser.parseString(response.body()).getAsJsonObject();

                Platform.runLater(() -> {
                    totalUsersLabel.setText(stats.has("totalUsers") && !stats.get("totalUsers").isJsonNull() ? stats.get("totalUsers").getAsString() : "0");
                    totalAdsLabel.setText(stats.has("totalAds") && !stats.get("totalAds").isJsonNull() ? stats.get("totalAds").getAsString() : "0");
                    pendingAdsLabel.setText(stats.has("pendingAds") && !stats.get("pendingAds").isJsonNull() ? stats.get("pendingAds").getAsString() : "0");
                    totalReportsLabel.setText(stats.has("totalReports") && !stats.get("totalReports").isJsonNull() ? stats.get("totalReports").getAsString() : "0");
                });
            } else {
                Platform.runLater(() -> messageLabel.setText("خطا در دریافت اطلاعات آماری."));
            }
        } catch (Exception e) {
            Platform.runLater(() -> messageLabel.setText("خطا در ارتباط با سرور."));
        }
    }

    @FXML
    protected void onBackClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("AdminPanel.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("پنل مدیریت");
        } catch (Exception e) {
            messageLabel.setText("خطا در بازگشت به پنل مدیریت.");
        }
    }
}
