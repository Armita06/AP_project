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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.http.HttpResponse;

public class ProfileController {

    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        loadUserProfile();
    }

    private void loadUserProfile() {
        try {
            // نکته: بررسی کن که متد دریافت پروفایل در UserController دقیقاً روی این آدرس باشد
            HttpResponse<String> response = ApiClient.get("/api/users/profile");

            if (response.statusCode() == 200 && response.body() != null) {
                JsonObject user = JsonParser.parseString(response.body()).getAsJsonObject();

                if (user.has("fullName") && !user.get("fullName").isJsonNull()) nameField.setText(user.get("fullName").getAsString());
                if (user.has("username") && !user.get("username").isJsonNull()) usernameField.setText(user.get("username").getAsString());
                if (user.has("phoneNumber") && !user.get("phoneNumber").isJsonNull()) phoneField.setText(user.get("phoneNumber").getAsString());
                if (user.has("email") && !user.get("email").isJsonNull()) emailField.setText(user.get("email").getAsString());

            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("خطا در دریافت اطلاعات پروفایل.");
            }
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("خطای ارتباط با سرور.");
        }
    }

    @FXML
    protected void onSubmitClick(ActionEvent event) {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("فیلدهای اصلی (نام، تماس، ایمیل) نمی‌توانند خالی باشند.");
            return;
        }

        try {
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("fullName", name);
            jsonRequest.addProperty("phoneNumber", phone);
            jsonRequest.addProperty("email", email);

            // اگر کاربر رمزی وارد کرده بود، آن را هم برای آپدیت می‌فرستیم
            if (!password.isEmpty()) {
                jsonRequest.addProperty("password", password);
            }

            // نکته: بررسی کن که متد ویرایش پروفایل در بک‌اِند نیز روی این آدرس تنظیم شده باشد
            HttpResponse<String> response = ApiClient.put("/api/users/profile", jsonRequest.toString());

            if (response.statusCode() == 200) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("پروفایل شما با موفقیت بروزرسانی شد.");
                passwordField.clear(); // پاک کردن فیلد رمز پس از موفقیت
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("خطا در بروزرسانی پروفایل.");
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
