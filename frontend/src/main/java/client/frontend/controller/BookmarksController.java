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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.net.http.HttpResponse;

public class BookmarksController {

    @FXML private FlowPane bookmarksContainer;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        loadBookmarks();
    }

    private void loadBookmarks() {
        bookmarksContainer.getChildren().clear();
        messageLabel.setText("");

        try {
            HttpResponse<String> response = ApiClient.get("/api/bookmarks");

            if (response.statusCode() == 200 && response.body() != null) {
                JsonArray bookmarksArray = JsonParser.parseString(response.body()).getAsJsonArray();

                if (bookmarksArray.isEmpty()) {
                    messageLabel.setText("شما هیچ آگهی نشان‌شده‌ای ندارید.");
                    messageLabel.setTextFill(Color.GRAY);
                    messageLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
                    return;
                }

                for (JsonElement element : bookmarksArray) {
                    JsonObject bookmarkObj = element.getAsJsonObject();
                    if (bookmarkObj.has("advertisement") && !bookmarkObj.get("advertisement").isJsonNull()) {
                        JsonObject ad = bookmarkObj.getAsJsonObject("advertisement");
                        VBox adCard = createBookmarkCard(ad);
                        bookmarksContainer.getChildren().add(adCard);
                    }
                }
            } else {
                messageLabel.setText("خطا در دریافت لیست علاقه‌مندی‌ها.");
                messageLabel.setTextFill(Color.RED);
            }
        } catch (Exception e) {
            messageLabel.setText("خطا در ارتباط با سرور.");
            messageLabel.setTextFill(Color.RED);
        }
    }

    private VBox createBookmarkCard(JsonObject ad) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #dcdde1; -fx-border-radius: 5; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(220);

        String title = ad.has("title") && !ad.get("title").isJsonNull() ? ad.get("title").getAsString() : "بدون عنوان";
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setWrapText(true);

        String priceText = ad.has("price") && !ad.get("price").isJsonNull() ? String.format("%,.0f تومان", ad.get("price").getAsDouble()) : "توافقی";
        Label priceLabel = new Label(priceText);
        priceLabel.setTextFill(Color.web("#27ae60"));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        Button removeBtn = new Button("حذف از علاقه‌مندی‌ها");
        removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        removeBtn.setMaxWidth(Double.MAX_VALUE);

        Long adId = ad.has("id") && !ad.get("id").isJsonNull() ? ad.get("id").getAsLong() : -1L;

        removeBtn.setOnAction(e -> removeBookmark(adId));

        card.getChildren().addAll(titleLabel, priceLabel, removeBtn);
        return card;
    }

    private void removeBookmark(Long adId) {
        if (adId == -1L) return;
        try {
            HttpResponse<String> response = ApiClient.post("/api/bookmarks/toggle/" + adId, "{}");
            if (response.statusCode() == 200) {
                loadBookmarks();
            } else {
                messageLabel.setText("خطا در حذف آگهی از علاقه‌مندی‌ها.");
                messageLabel.setTextFill(Color.RED);
            }
        } catch (Exception e) {
            messageLabel.setText("خطای ارتباط با سرور.");
            messageLabel.setTextFill(Color.RED);
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