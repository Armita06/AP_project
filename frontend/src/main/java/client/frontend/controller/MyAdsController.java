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
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.net.http.HttpResponse;

public class MyAdsController {

    @FXML private FlowPane myAdsContainer;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        loadMyAds();
    }

    private void loadMyAds() {
        myAdsContainer.getChildren().clear();
        messageLabel.setText("");

        try {
            HttpResponse<String> response = ApiClient.get("/api/ads/my-ads");

            if (response.statusCode() == 200 && response.body() != null) {
                JsonArray adsArray = JsonParser.parseString(response.body()).getAsJsonArray();

                if (adsArray.isEmpty()) {
                    messageLabel.setText("شما هنوز هیچ آگهی ثبت نکرده‌اید.");
                    messageLabel.setTextFill(Color.GRAY);
                    messageLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
                    return;
                }

                for (JsonElement element : adsArray) {
                    JsonObject ad = element.getAsJsonObject();
                    VBox adCard = createMyAdCard(ad);
                    myAdsContainer.getChildren().add(adCard);
                }
            } else {
                messageLabel.setText("خطا در دریافت لیست آگهی‌های شما.");
                messageLabel.setTextFill(Color.RED);
            }
        } catch (Exception e) {
            messageLabel.setText("خطا در ارتباط با سرور.");
            messageLabel.setTextFill(Color.RED);
        }
    }

    private VBox createMyAdCard(JsonObject ad) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #dcdde1; -fx-border-radius: 5; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(220);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(190);
        imageView.setFitHeight(130);
        imageView.setPreserveRatio(true);

        if (ad.has("imageUrl") && !ad.get("imageUrl").isJsonNull()) {
            String imageVal = ad.get("imageUrl").getAsString();
            String finalUrl = imageVal.startsWith("http") ? imageVal : "http://localhost:8080/uploads/" + imageVal;
            Image image = new Image(finalUrl, true);
            imageView.setImage(image);
        }

        String title = ad.has("title") && !ad.get("title").isJsonNull() ? ad.get("title").getAsString() : "بدون عنوان";
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setWrapText(true);

        String priceText = ad.has("price") && !ad.get("price").isJsonNull() ? String.format("%,.0f تومان", ad.get("price").getAsDouble()) : "توافقی";
        Label priceLabel = new Label(priceText);
        priceLabel.setTextFill(Color.web("#27ae60"));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        // وضعیت آگهی
        String statusRaw = ad.has("status") && !ad.get("status").isJsonNull() ? ad.get("status").getAsString() : "UNKNOWN";
        String statusTranslated = translateStatus(statusRaw);
        Label statusLabel = new Label("وضعیت: " + statusTranslated);
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        statusLabel.setTextFill(getStatusColor(statusRaw));

        card.getChildren().addAll(imageView, titleLabel, priceLabel, statusLabel);

        card.setOnMouseClicked(event -> {
            if (ad.has("id") && !ad.get("id").isJsonNull()) {
                Long adId = ad.get("id").getAsLong();
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("AdDetails.fxml"));
                    Scene scene = new Scene(fxmlLoader.load());
                    AdDetailsController controller = fxmlLoader.getController();
                    controller.initData(adId);
                    Stage stage = (Stage) card.getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("جزئیات آگهی");
                } catch (Exception e) {
                    messageLabel.setText("خطا در باز کردن صفحه جزئیات.");
                    messageLabel.setTextFill(Color.RED);
                }
            }
        });

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #3498db; -fx-border-radius: 5; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(52,152,219,0.3), 5, 0, 0, 2);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-border-color: #dcdde1; -fx-border-radius: 5; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);"));

        return card;
    }

    private String translateStatus(String status) {
        switch (status.toUpperCase()) {
            case "PENDING": return "در انتظار تایید";
            case "APPROVED": return "تایید شده";
            case "REJECTED": return "رد شده";
            case "SOLD": return "فروخته شده";
            default: return "نامشخص";
        }
    }

    private Color getStatusColor(String status) {
        switch (status.toUpperCase()) {
            case "PENDING": return Color.web("#f39c12");
            case "APPROVED": return Color.web("#2ecc71");
            case "REJECTED": return Color.web("#e74c3c");
            case "SOLD": return Color.web("#7f8c8d");
            default: return Color.BLACK;
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
}
