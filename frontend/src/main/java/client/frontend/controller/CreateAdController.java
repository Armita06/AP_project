package client.frontend.controller;

import client.frontend.MainApplication;
import client.frontend.api.ApiClient;
import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.http.HttpResponse;

public class CreateAdController {

    @FXML private TextField titleField;
    @FXML private TextField categoryField;
    @FXML private TextField cityField;
    @FXML private TextField priceField;
    @FXML private TextArea descriptionField;
    @FXML private Label messageLabel;

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
            messageLabel.setText("لطفاً تمامی فیلدهای ستاره‌دار را پر کنید.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr.trim());
            if (price < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("مبلغ باید به صورت عددی و مثبت باشد.");
            return;
        }

        try {
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("title", title.trim());
            jsonRequest.addProperty("description", description != null ? description.trim() : "");
            jsonRequest.addProperty("price", price);
            jsonRequest.addProperty("category", category.trim());
            jsonRequest.addProperty("city", city.trim());

            HttpResponse<String> response = ApiClient.post("/api/ads/create", jsonRequest.toString());

            if (response.statusCode() == 201) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("آگهی با موفقیت ثبت شد و در انتظار تایید مدیر است.");
                titleField.clear();
                categoryField.clear();
                cityField.clear();
                priceField.clear();
                descriptionField.clear();
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("خطا در ثبت آگهی.");
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