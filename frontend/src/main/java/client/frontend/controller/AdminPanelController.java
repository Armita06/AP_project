package client.frontend.controller;

import client.frontend.MainApplication;
import client.frontend.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.net.http.HttpResponse;
import java.util.Optional;

public class AdminPanelController {

    @FXML private VBox pendingAdsContainer;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        messageLabel.setText("");
        loadPendingAds();
    }

    private void loadPendingAds() {
        pendingAdsContainer.getChildren().clear();

        try {
            HttpResponse<String> response = ApiClient.get("/api/admin/ads/pending");

            if (response.statusCode() == 200 && response.body() != null) {
                JsonArray adsArray = JsonParser.parseString(response.body()).getAsJsonArray();

                if (adsArray.isEmpty()) {
                    Label emptyLabel = new Label("هیچ آگهی در انتظار تاییدی وجود ندارد.");
                    emptyLabel.setTextFill(Color.GRAY);
                    emptyLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
                    pendingAdsContainer.getChildren().add(emptyLabel);
                    return;
                }

                for (JsonElement element : adsArray) {
                    JsonObject ad = element.getAsJsonObject();
                    VBox adCard = createAdminAdCard(ad);
                    pendingAdsContainer.getChildren().add(adCard);
                }
            } else {
                messageLabel.setText("خطا در دریافت لیست آگهی‌ها.");
                messageLabel.setTextFill(Color.RED);
            }
        } catch (Exception e) {
            messageLabel.setText("خطا در ارتباط با سرور.");
            messageLabel.setTextFill(Color.RED);
        }
    }

    private VBox createAdminAdCard(JsonObject ad) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-background-radius: 5;");

        String title = ad.has("title") && !ad.get("title").isJsonNull() ? ad.get("title").getAsString() : "بدون عنوان";
        Label titleLabel = new Label("عنوان: " + title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        String desc = ad.has("description") && !ad.get("description").isJsonNull() ? ad.get("description").getAsString() : "بدون توضیحات";
        Label descLabel = new Label("توضیحات: " + desc);
        descLabel.setWrapText(true);

        String priceText = ad.has("price") && !ad.get("price").isJsonNull() ? String.format("%,.0f تومان", ad.get("price").getAsDouble()) : "توافقی";
        Label priceLabel = new Label("قیمت: " + priceText);

        HBox buttonBox = new HBox(10);
        Button approveBtn = new Button("تایید آگهی");
        approveBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");

        Button rejectBtn = new Button("رد آگهی");
        rejectBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        Long adId = ad.has("id") && !ad.get("id").isJsonNull() ? ad.get("id").getAsLong() : -1L;

        approveBtn.setOnAction(e -> approveAd(adId));
        rejectBtn.setOnAction(e -> rejectAd(adId));

        buttonBox.getChildren().addAll(approveBtn, rejectBtn);
        card.getChildren().addAll(titleLabel, descLabel, priceLabel, buttonBox);
        return card;
    }

    private void approveAd(Long adId) {
        if (adId == -1L) return;
        try {
            HttpResponse<String> response = ApiClient.put("/api/admin/ads/approve/" + adId, "{}");
            if (response.statusCode() == 200) {
                messageLabel.setText("آگهی با موفقیت تایید شد.");
                messageLabel.setTextFill(Color.GREEN);
                loadPendingAds();
            } else {
                messageLabel.setText("خطا در تایید آگهی.");
                messageLabel.setTextFill(Color.RED);
            }
        } catch (Exception e) {
            messageLabel.setText("خطا در ارتباط با سرور.");
            messageLabel.setTextFill(Color.RED);
        }
    }

    private void rejectAd(Long adId) {
        if (adId == -1L) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("رد آگهی");
        dialog.setHeaderText("دلیل رد آگهی را وارد کنید:");
        dialog.setContentText("دلیل:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String reason = result.get().trim();
            if (reason.isEmpty()) {
                messageLabel.setText("برای رد آگهی باید دلیل ذکر شود.");
                messageLabel.setTextFill(Color.RED);
                return;
            }

            try {
                JsonObject jsonRequest = new JsonObject();
                jsonRequest.addProperty("reason", reason);
                HttpResponse<String> response = ApiClient.put("/api/admin/ads/reject/" + adId, jsonRequest.toString());

                if (response.statusCode() == 200) {
                    messageLabel.setText("آگهی با موفقیت رد شد.");
                    messageLabel.setTextFill(Color.GREEN);
                    loadPendingAds();
                } else {
                    messageLabel.setText("خطا در رد آگهی.");
                    messageLabel.setTextFill(Color.RED);
                }
            } catch (Exception e) {
                messageLabel.setText("خطا در ارتباط با سرور.");
                messageLabel.setTextFill(Color.RED);
            }
        }
    }

    @FXML
    protected void onBackClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("Dashboard.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("سامانه ثبت آگهی دست دوم - داشبورد");
        } catch (Exception e) {
            messageLabel.setText("خطا در بازگشت به داشبورد.");
            messageLabel.setTextFill(Color.RED);
        }
    }
    @FXML
    protected void onViewStatsClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("AdminStats.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("داشبورد آماری");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}