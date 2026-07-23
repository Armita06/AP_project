package client.frontend.controller;

import client.frontend.MainApplication;
import client.frontend.api.ApiClient;
import client.frontend.util.SessionManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.http.HttpResponse;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
public class AdDetailsController {

    @FXML private ImageView adImageView;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label categoryLabel;
    @FXML private Label cityLabel;
    @FXML private Label sellerLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label messageLabel;
    @FXML private HBox buyerActionsBox;
    @FXML private HBox ownerActionsBox;

    private Long currentAdId;
    private String sellerUsername;

    public void initData(Long adId) {
        this.currentAdId = adId;
        loadAdDetails();
    }

    private void loadAdDetails() {
        try {
            HttpResponse<String> response = ApiClient.get("/api/ads/" + currentAdId);

            if (response.statusCode() == 200 && response.body() != null) {
                JsonObject ad = JsonParser.parseString(response.body()).getAsJsonObject();

                titleLabel.setText(ad.has("title") && !ad.get("title").isJsonNull() ? ad.get("title").getAsString() : "بدون عنوان");

                if (ad.has("price") && !ad.get("price").isJsonNull()) {
                    priceLabel.setText(String.format("%,.0f تومان", ad.get("price").getAsDouble()));
                } else {
                    priceLabel.setText("توافقی");
                }

                categoryLabel.setText(ad.has("category") && !ad.get("category").isJsonNull() ? ad.get("category").getAsString() : "نامشخص");
                cityLabel.setText(ad.has("city") && !ad.get("city").isJsonNull() ? ad.get("city").getAsString() : "نامشخص");

                sellerUsername = ad.has("seller") && !ad.get("seller").isJsonNull() ? ad.get("seller").getAsString() : "نامشخص";
                sellerLabel.setText(sellerUsername);

                descriptionLabel.setText(ad.has("description") && !ad.get("description").isJsonNull() ? ad.get("description").getAsString() : "بدون توضیحات");

                if (ad.has("imageUrl") && !ad.get("imageUrl").isJsonNull()) {
                    String imageVal = ad.get("imageUrl").getAsString();

                    String finalUrl = imageVal.startsWith("http") ? imageVal : "http://localhost:8080/uploads/" + imageVal;

                    Image image = new Image(finalUrl, true);
                    adImageView.setImage(image);
                } else {
                    adImageView.setImage(null);
                }

                String currentUser = SessionManager.getInstance().getUsername();
                if (currentUser != null && currentUser.equals(sellerUsername)) {
                    buyerActionsBox.setVisible(false);
                    buyerActionsBox.setManaged(false);
                    ownerActionsBox.setVisible(true);
                    ownerActionsBox.setManaged(true);
                }
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("خطا در دریافت اطلاعات آگهی.");
            }
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطا در ارتباط با سرور.");
        }
    }

    @FXML
    protected void onBookmarkClick(ActionEvent event) {
        try {
            HttpResponse<String> response = ApiClient.post("/api/bookmarks/toggle/" + currentAdId, "{}");
            if (response.statusCode() == 200) {
                JsonObject res = JsonParser.parseString(response.body()).getAsJsonObject();
                String msg = res.has("message") && !res.get("message").isJsonNull() ? res.get("message").getAsString() : "عملیات انجام شد.";
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText(msg);
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("خطا در تغییر وضعیت علاقه‌مندی.");
            }
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطا در ارتباط با سرور.");
        }
    }

    @FXML
    protected void onChatClick(ActionEvent event) {
        try {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("ارسال پیام");
            dialog.setHeaderText("اولین پیام خود را برای فروشنده بنویسید:");
            dialog.setContentText("پیام:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String msg = result.get().trim();
                if (!msg.isEmpty()) {
                    JsonObject body = new JsonObject();
                    body.addProperty("content", msg);

                    HttpResponse<String> response = ApiClient.post("/api/chat/send-to-ad/" + currentAdId, body.toString());

                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        messageLabel.setStyle("-fx-text-fill: green;");
                        messageLabel.setText("پیام ارسال شد. برای مشاهده چت به بخش 'پیام‌ها' مراجعه کنید.");
                    } else {
                        messageLabel.setStyle("-fx-text-fill: red;");
                        messageLabel.setText("خطا در ارسال پیام.");
                    }
                }
            }
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطا در ارتباط با سرور.");
        }
    }

    @FXML
    protected void onEditClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("EditAd.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            EditAdController controller = fxmlLoader.getController();
            controller.initData(currentAdId);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("ویرایش آگهی");
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطا در باز کردن صفحه ویرایش.");
        }
    }

    @FXML
    protected void onSoldClick(ActionEvent event) {
        try {
            HttpResponse<String> response = ApiClient.put("/api/ads/sold/" + currentAdId, "{}");
            if (response.statusCode() == 200) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("وضعیت آگهی با موفقیت به فروخته شده تغییر یافت.");
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("خطا در تغییر وضعیت آگهی.");
            }
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطا در ارتباط با سرور.");
        }
    }

    @FXML
    protected void onDeleteClick(ActionEvent event) {
        try {
            HttpResponse<String> response = ApiClient.delete("/api/ads/delete/" + currentAdId);
            if (response.statusCode() == 200) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("آگهی با موفقیت حذف شد.");
                ownerActionsBox.setVisible(false);
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("خطا در حذف آگهی.");
            }
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطا در ارتباط با سرور.");
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
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطا در بازگشت به داشبورد.");
        }
    }
}