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

public class CreateAdController {
    @FXML private TextField titleField;
    @FXML private TextField categoryField;
    @FXML private TextField cityField;
    @FXML private TextField priceField;
    @FXML private TextArea descriptionField;
    @FXML private Label messageLabel;
    @FXML private Label imageNameLabel;

    private List<File> selectedImageFiles = new ArrayList<>();

    @FXML
    protected void onSelectImageClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("انتخاب عکس‌ها");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        List<File> files = fileChooser.showOpenMultipleDialog(stage);
        if (files != null && !files.isEmpty()) {
            selectedImageFiles = files;
            imageNameLabel.setText(files.size() + " عکس انتخاب شد");
            imageNameLabel.setStyle("-fx-text-fill: black;");
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
            messageLabel.setText("خطا در بازگشت به داشبورد.");
        }
    }

    @FXML
    protected void onSubmitClick(ActionEvent event) {
        String title = titleField.getText();
        String category = categoryField.getText();
        String city = cityField.getText();
        String priceStr = priceField.getText();
        String description = descriptionField.getText();

        if (title == null || title.trim().isEmpty() ||
                category == null || category.trim().isEmpty() ||
                city == null || city.trim().isEmpty() ||
                priceStr == null || priceStr.trim().isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("لطفاً فیلدهای ستاره‌دار را پر کنید.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr.trim());
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("قیمت نامعتبر است.");
            return;
        }

        try {
            List<String> uploadedUrls = new ArrayList<>();
            if (!selectedImageFiles.isEmpty()) {
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
            }

            String finalImageUrl = String.join(",", uploadedUrls);

            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("title", title.trim());
            jsonRequest.addProperty("description", description != null ? description.trim() : "");
            jsonRequest.addProperty("price", price);
            jsonRequest.addProperty("category", category.trim());
            jsonRequest.addProperty("city", city.trim());
            jsonRequest.addProperty("imageUrl", finalImageUrl);

            HttpResponse<String> response = ApiClient.post("/api/ads/create", jsonRequest.toString());
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("آگهی با موفقیت ثبت شد.");
                titleField.clear();
                categoryField.clear();
                cityField.clear();
                priceField.clear();
                descriptionField.clear();
                imageNameLabel.setText("عکسی انتخاب نشده");
                selectedImageFiles.clear();
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("خطا در ثبت آگهی.");
            }
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطا در ارتباط با سرور.");
        }
    }
}