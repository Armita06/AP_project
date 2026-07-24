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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.http.HttpResponse;
import java.util.Optional;

public class AdDetailsController {

    @FXML private ImageView adImageView;
    @FXML private Button prevImageBtn;
    @FXML private Button nextImageBtn;

    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label categoryLabel;
    @FXML private Label cityLabel;
    @FXML private Label sellerLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label messageLabel;
    @FXML private HBox buyerActionsBox;
    @FXML private HBox ownerActionsBox;
    @FXML private Label ratingDisplayLabel;
    @FXML private ComboBox<Integer> ratingComboBox;
    @FXML private HBox ratingBox;

    private Long currentAdId;
    private String sellerUsername;

    private String[] imageUrls = new String[0];
    private int currentImageIndex = 0;

    public void initData(Long adId) {
        this.currentAdId = adId;
        loadAdDetails();
    }

    private void loadSellerRating(String sellerUsername) {
        try {
            HttpResponse<String> response = ApiClient.get("/api/ratings/seller/" + sellerUsername);
            if (response.statusCode() == 200 && response.body() != null) {
                JsonObject stats = JsonParser.parseString(response.body()).getAsJsonObject();
                String ratingStr = "0";
                if (stats.has("averageScore") && !stats.get("averageScore").isJsonNull()) {
                    ratingStr = stats.get("averageScore").getAsString();
                }
                if (ratingStr.equals("0") || ratingStr.equals("0.0") || ratingStr.equals("NaN")) {
                    ratingDisplayLabel.setText("هنوز امتیازی ثبت نشده");
                } else {
                    try {
                        ratingDisplayLabel.setText(String.format("%.1f از ۵", Double.parseDouble(ratingStr)));
                    } catch (Exception e) {
                        ratingDisplayLabel.setText(ratingStr + " از ۵");
                    }
                }
            } else {
                ratingDisplayLabel.setText("خطا در دریافت");
            }
        } catch (Exception e) {
            ratingDisplayLabel.setText("خطا در دریافت");
        }
    }

    @FXML
    protected void onSubmitRatingClick(ActionEvent event) {
        Integer selectedRating = ratingComboBox.getValue();
        if (selectedRating == null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("لطفاً یک امتیاز انتخاب کنید.");
            return;
        }
        try {
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("score", selectedRating);
            jsonRequest.addProperty("comment", "");
            HttpResponse<String> response = ApiClient.post("/api/ratings/add/" + currentAdId, jsonRequest.toString());
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("امتیاز با موفقیت ثبت شد.");
                loadSellerRating(sellerUsername);
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("خطا در ثبت امتیاز یا شما قبلا امتیاز داده اید.");
            }
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطا در ارتباط با سرور.");
        }
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
                descriptionLabel.setText(ad.has("description") && !ad.get("description").isJsonNull() ? ad.get("description").getAsString() : "ندارد");

                // بررسی تصاویر و راه‌اندازی اسلایدر
                if (ad.has("imageUrl") && !ad.get("imageUrl").isJsonNull() && !ad.get("imageUrl").getAsString().trim().isEmpty()) {
                    imageUrls = ad.get("imageUrl").getAsString().split(",");
                    loadImageAtIndex(0);

                    if (imageUrls.length > 1) {
                        prevImageBtn.setVisible(true);
                        prevImageBtn.setManaged(true);
                        nextImageBtn.setVisible(true);
                        nextImageBtn.setManaged(true);
                        updateImageButtons();
                    } else {
                        prevImageBtn.setVisible(false);
                        prevImageBtn.setManaged(false);
                        nextImageBtn.setVisible(false);
                        nextImageBtn.setManaged(false);
                    }
                } else {
                    adImageView.setImage(new Image("https://placehold.co/400x300/e0e0e0/808080?text=No+Image", true));
                    prevImageBtn.setVisible(false);
                    prevImageBtn.setManaged(false);
                    nextImageBtn.setVisible(false);
                    nextImageBtn.setManaged(false);
                }

                String currentUser = SessionManager.getInstance().getUsername();
                if (currentUser != null && currentUser.equals(sellerUsername)) {
                    buyerActionsBox.setVisible(false);
                    buyerActionsBox.setManaged(false);
                    ownerActionsBox.setVisible(true);
                    ownerActionsBox.setManaged(true);
                    ratingBox.setVisible(false);
                    ratingBox.setManaged(false);
                }
                ratingComboBox.getItems().setAll(1, 2, 3, 4, 5);
                loadSellerRating(sellerUsername);
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("آگهی یافت نشد.");
            }
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطا در ارتباط با سرور.");
        }
    }

    private void loadImageAtIndex(int index) {
        if (imageUrls.length > 0 && index >= 0 && index < imageUrls.length) {
            String imageVal = imageUrls[index];
            String finalUrl = imageVal.startsWith("http") ? imageVal : "http://localhost:8080/uploads/" + imageVal;
            adImageView.setImage(new Image(finalUrl, true));
        }
    }

    private void updateImageButtons() {
        prevImageBtn.setDisable(currentImageIndex == 0);
        nextImageBtn.setDisable(currentImageIndex == imageUrls.length - 1);
    }

    @FXML
    protected void onPrevImageClick(ActionEvent event) {
        if (currentImageIndex > 0) {
            currentImageIndex--;
            loadImageAtIndex(currentImageIndex);
            updateImageButtons();
        }
    }

    @FXML
    protected void onNextImageClick(ActionEvent event) {
        if (currentImageIndex < imageUrls.length - 1) {
            currentImageIndex++;
            loadImageAtIndex(currentImageIndex);
            updateImageButtons();
        }
    }

    @FXML
    protected void onBookmarkClick(ActionEvent event) {
        try {
            HttpResponse<String> response = ApiClient.post("/api/bookmarks/toggle/" + currentAdId, "{}");
            if (response.statusCode() == 200) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("وضعیت نشان با موفقیت تغییر کرد.");
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("خطا در تغییر وضعیت نشان.");
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
            dialog.setHeaderText("ارسال پیام به فروشنده:");
            dialog.setContentText("متن پیام:");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                JsonObject body = new JsonObject();
                body.addProperty("content", result.get().trim());
                HttpResponse<String> response = ApiClient.post("/api/chat/send-to-ad/" + currentAdId, body.toString());
                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    messageLabel.setStyle("-fx-text-fill: green;");
                    messageLabel.setText("پیام ارسال شد.");
                } else {
                    messageLabel.setStyle("-fx-text-fill: red;");
                    messageLabel.setText("خطا در ارسال پیام.");
                }
            }
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطا در ارتباط.");
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
                messageLabel.setText("آگهی به عنوان فروخته شده ثبت شد.");
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("خطا در ثبت وضعیت.");
            }
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطا در ارتباط.");
        }
    }

    @FXML
    protected void onDeleteClick(ActionEvent event) {
        try {
            HttpResponse<String> response = ApiClient.delete("/api/ads/delete/" + currentAdId);
            if (response.statusCode() == 200) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("آگهی حذف شد.");
                ownerActionsBox.setVisible(false);
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("خطا در حذف.");
            }
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطا در ارتباط.");
        }
    }

    @FXML
    protected void onBackClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("Dashboard.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("داشبورد");
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطا در بازگشت.");
        }
    }
}