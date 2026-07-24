package client.frontend.controller;

import client.frontend.MainApplication;
import client.frontend.api.ApiClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class EditAdController {
    @FXML private TextField titleField;
    @FXML private TextField categoryField;
    @FXML private TextField cityField;
    @FXML private TextField priceField;
    @FXML private TextArea descriptionField;
    @FXML private Label messageLabel;
    @FXML private Label imageNameLabel;

    private Long currentAdId;
    private List<File> selectedImageFiles = new ArrayList<>();
    private String existingImageUrl = "";

    public void initData(Long adId) {
        this.currentAdId = adId;
        loadExistingData();
    }

    private void loadExistingData() {
        try {
            HttpResponse<String> response = ApiClient.get("/api/ads/" + currentAdId);
            if (response.statusCode() == 200 && response.body() != null) {
                JsonObject ad = JsonParser.parseString(response.body()).getAsJsonObject();
                if (ad.has("title") && !ad.get("title").isJsonNull()) titleField.setText(ad.get("title").getAsString());
                if (ad.has("category") && !ad.get("category").isJsonNull()) categoryField.setText(ad.get("category").getAsString());
                if (ad.has("city") && !ad.get("city").isJsonNull()) cityField.setText(ad.get("city").getAsString());
                if (ad.has("price") && !ad.get("price").isJsonNull()) priceField.setText(String.format("%.0f", ad.get("price").getAsDouble()));
                if (ad.has("description") && !ad.get("description").isJsonNull()) descriptionField.setText(ad.get("description").getAsString());
                if (ad.has("imageUrl") && !ad.get("imageUrl").isJsonNull()) {
                    existingImageUrl = ad.get("imageUrl").getAsString();
                    if(!existingImageUrl.trim().isEmpty()) {
                        int count = existingImageUrl.split(",").length;
                        imageNameLabel.setText(count + " عکس از قبل موجود است");
                    }
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
    protected void onSelectImageClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("انتخاب عکس‌های جدید");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        List<File> files = fileChooser.showOpenMultipleDialog(stage);
        if (files != null && !files.isEmpty()) {
            selectedImageFiles = files;
            imageNameLabel.setText(files.size() + " عکس جدید انتخاب شد");
            imageNameLabel.setStyle("-fx-text-fill: black;");
        }
    }

    @FXML
    protected void onSubmitClick(ActionEvent event) {
        String title = titleField.getText();
        String category = categoryField.getText();
        String city = cityField.getText();
        String priceStr = priceField.getText();
        String description = descriptionField.getText();

        if (title.trim().isEmpty() || category.trim().isEmpty() || city.trim().isEmpty() || priceStr.trim().isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("لطفاً فیلدهای ستاره‌دار را پر کنید.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr.trim());
        } catch (NumberFormatException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("قیمت نامعتبر است.");
            return;
        }

        try {
            String uploadedImageUrl = existingImageUrl;
            if (!selectedImageFiles.isEmpty()) {
                List<String> uploadedUrls = new ArrayList<>();
                for (File file : selectedImageFiles) {
                    HttpResponse<String> uploadResponse = ApiClient.uploadFile("/api/files/upload", file);
                    if (uploadResponse.statusCode() == 200 || uploadResponse.statusCode() == 201) {
                        String body = uploadResponse.body();
                        if (body.trim().startsWith("{")) {
                            JsonObject resObj = JsonParser.parseString(body).getAsJsonObject();
                            uploadedUrls.add(resObj.has("fileName") ? resObj.get("fileName").getAsString() : body);
                        } else {
                            uploadedUrls.add(body.trim());
                        }
                    }
                }
                if (!uploadedUrls.isEmpty()) {
                    uploadedImageUrl = String.join(",", uploadedUrls);
                }
            }

            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("title", title.trim());
            jsonRequest.addProperty("description", description != null ? description.trim() : "");
            jsonRequest.addProperty("price", price);
            jsonRequest.addProperty("category", category.trim());
            jsonRequest.addProperty("city", city.trim());
            jsonRequest.addProperty("imageUrl", uploadedImageUrl);

            HttpResponse<String> response = ApiClient.put("/api/ads/update/" + currentAdId, jsonRequest.toString());
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("آگهی با موفقیت ویرایش شد.");
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("خطا در ویرایش آگهی.");
            }
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطا در ارتباط با سرور.");
        }
    }

    @FXML
    protected void onCancelClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("AdDetails.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            AdDetailsController controller = fxmlLoader.getController();
            controller.initData(currentAdId);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("جزئیات آگهی");
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطا در بازگشت.");
        }
    }
}