package client.frontend.controller;

import client.frontend.MainApplication;
import client.frontend.api.ApiClient;
import client.frontend.util.SessionManager;
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

import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Button adminPanelButton;
    @FXML private TextField searchField;
    @FXML private TextField categoryField;
    @FXML private TextField cityField;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private FlowPane adsContainer;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        String username = SessionManager.getInstance().getUsername();
        String role = SessionManager.getInstance().getRole();

        welcomeLabel.setText("خوش آمدید، " + username);

        if ("ADMIN".equals(role)) {
            adminPanelButton.setVisible(true);
            adminPanelButton.setManaged(true);
        }

        sortComboBox.getItems().addAll("جدیدترین", "ارزان‌ترین", "گران‌ترین");
        sortComboBox.setValue("جدیدترین");

        loadAds();
    }

    private void loadAds() {
        errorLabel.setText("");
        adsContainer.getChildren().clear();

        try {
            StringBuilder query = new StringBuilder("/api/ads/search?");
            List<String> params = new ArrayList<>();

            if (searchField.getText() != null && !searchField.getText().trim().isEmpty()) {
                params.add("keyword=" + URLEncoder.encode(searchField.getText().trim(), StandardCharsets.UTF_8));
            }
            if (categoryField.getText() != null && !categoryField.getText().trim().isEmpty()) {
                params.add("category=" + URLEncoder.encode(categoryField.getText().trim(), StandardCharsets.UTF_8));
            }
            if (cityField.getText() != null && !cityField.getText().trim().isEmpty()) {
                params.add("city=" + URLEncoder.encode(cityField.getText().trim(), StandardCharsets.UTF_8));
            }
            if (minPriceField.getText() != null && !minPriceField.getText().trim().isEmpty()) {
                try {
                    Double.parseDouble(minPriceField.getText().trim());
                    params.add("minPrice=" + minPriceField.getText().trim());
                } catch (NumberFormatException e) {
                    errorLabel.setText("فرمت حداقل قیمت نامعتبر است.");
                    return;
                }
            }
            if (maxPriceField.getText() != null && !maxPriceField.getText().trim().isEmpty()) {
                try {
                    Double.parseDouble(maxPriceField.getText().trim());
                    params.add("maxPrice=" + maxPriceField.getText().trim());
                } catch (NumberFormatException e) {
                    errorLabel.setText("فرمت حداکثر قیمت نامعتبر است.");
                    return;
                }
            }

            String sortValue = sortComboBox.getValue();
            if ("ارزان‌ترین".equals(sortValue)) {
                params.add("sortBy=cheapest");
            } else if ("گران‌ترین".equals(sortValue)) {
                params.add("sortBy=expensive");
            } else {
                params.add("sortBy=newest");
            }

            query.append(String.join("&", params));
            HttpResponse<String> response = ApiClient.get(query.toString());

            if (response.statusCode() == 200 && response.body() != null) {
                JsonArray adsArray = JsonParser.parseString(response.body()).getAsJsonArray();
                for (JsonElement element : adsArray) {
                    JsonObject ad = element.getAsJsonObject();
                    VBox adCard = createAdCard(ad);
                    adsContainer.getChildren().add(adCard);
                }
                if (adsArray.isEmpty()) {
                    errorLabel.setText("آگهی مطابق با فیلتر یافت نشد.");
                    errorLabel.setTextFill(Color.GRAY);
                }
            } else {
                errorLabel.setText("خطا در دریافت لیست آگهی‌ها.");
                errorLabel.setTextFill(Color.RED);
            }
        } catch (Exception e) {
            errorLabel.setText("خطای ارتباط با سرور.");
            errorLabel.setTextFill(Color.RED);
        }
    }

    private VBox createAdCard(JsonObject ad) {

        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #dcdde1; -fx-border-radius: 5; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(220);

        String title = ad.has("title") && !ad.get("title").isJsonNull() ? ad.get("title").getAsString() : "بدون عنوان";
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setWrapText(true);

        String priceText = "توافقی";
        if (ad.has("price") && !ad.get("price").isJsonNull()) {
            priceText = String.format("%,.0f تومان", ad.get("price").getAsDouble());
        }
        Label priceLabel = new Label(priceText);
        priceLabel.setTextFill(Color.web("#27ae60"));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        String city = ad.has("city") && !ad.get("city").isJsonNull() ? ad.get("city").getAsString() : "نامشخص";
        Label cityLabel = new Label("شهر: " + city);
        cityLabel.setTextFill(Color.GRAY);

        String category = ad.has("category") && !ad.get("category").isJsonNull() ? ad.get("category").getAsString() : "نامشخص";
        Label categoryLabel = new Label("دسته: " + category);
        categoryLabel.setTextFill(Color.GRAY);

        card.getChildren().addAll(titleLabel, priceLabel, cityLabel, categoryLabel);

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
                    errorLabel.setText("خطا در باز کردن صفحه جزئیات آگهی.");
                    errorLabel.setTextFill(Color.RED);
                }
            }
        });

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #3498db; -fx-border-radius: 5; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(52,152,219,0.3), 5, 0, 0, 2);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-border-color: #dcdde1; -fx-border-radius: 5; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);"));

        javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        if (ad.has("imageUrl") && !ad.get("imageUrl").isJsonNull()) {
            String imageVal = ad.get("imageUrl").getAsString();

            String finalUrl = imageVal.startsWith("http") ? imageVal : "http://localhost:8080/uploads/" + imageVal;

            javafx.scene.image.Image image = new javafx.scene.image.Image(finalUrl, true);
            imageView.setImage(image);
        }

        card.getChildren().add(0, imageView);
        return card;
    }

    @FXML
    protected void onSearchClick(ActionEvent event) {
        loadAds();
    }

    @FXML
    protected void onClearFiltersClick(ActionEvent event) {
        searchField.clear();
        categoryField.clear();
        cityField.clear();
        minPriceField.clear();
        maxPriceField.clear();
        sortComboBox.setValue("جدیدترین");
        loadAds();
    }

    @FXML
    protected void onCreateAdClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("CreateAd.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("ثبت آگهی جدید");
        } catch (Exception e) {
            errorLabel.setText("خطا در باز کردن صفحه ثبت آگهی.");
            errorLabel.setTextFill(Color.RED);
        }
    }

    @FXML
    protected void onMessagesClick(ActionEvent event) {
    }

    @FXML
    protected void onBookmarksClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("Bookmarks.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("علاقه‌مندی‌های من");
        } catch (Exception e) {
            errorLabel.setText("خطا در باز کردن صفحه علاقه‌مندی‌ها.");
            errorLabel.setTextFill(Color.RED);
        }
    }

    @FXML
    protected void onMyAdsClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("MyAds.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("آگهی‌های من");
        } catch (Exception e) {
            errorLabel.setText("خطا در باز کردن صفحه آگهی‌های من.");
            errorLabel.setTextFill(Color.RED);
        }
    }

    @FXML
    protected void onAdminPanelClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("AdminPanel.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("پنل مدیریت");
        } catch (Exception e) {
            errorLabel.setText("خطا در باز کردن پنل مدیریت.");
            errorLabel.setTextFill(Color.RED);
        }
    }

    @FXML
    protected void onLogoutClick(ActionEvent event) {
        SessionManager.getInstance().logout();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("Login.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("ورود به سامانه");
            stage.centerOnScreen();
        } catch (Exception e) {
            errorLabel.setText("خطا در خروج از حساب کاربری.");
        }
    }
}