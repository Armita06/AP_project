package client.frontend.controller;

import client.frontend.MainApplication;
import client.frontend.api.ApiClient;
import client.frontend.util.SessionManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class ChatController {

    @FXML private Label conversationsMessageLabel;
    @FXML private ListView<String> conversationsList;
    @FXML private Label chatWithLabel;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private VBox messagesContainer;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;

    private List<JsonObject> conversations = new ArrayList<>();
    private Long currentConversationId = null;
    private String currentUser = SessionManager.getInstance().getUsername();

    @FXML
    public void initialize() {
        loadConversations();

        conversationsList.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            int index = newValue.intValue();
            if (index >= 0 && index < conversations.size()) {
                JsonObject selectedConv = conversations.get(index);
                currentConversationId = selectedConv.get("id").getAsLong();

                String chatTitle = "گفت‌وگو #" + currentConversationId;
                if (selectedConv.has("advertisement") && !selectedConv.get("advertisement").isJsonNull()) {
                    chatTitle = selectedConv.getAsJsonObject("advertisement").get("title").getAsString();
                }

                chatWithLabel.setText("در حال گفت‌وگو درباره: " + chatTitle);
                messageInput.setDisable(false);
                sendButton.setDisable(false);

                loadMessages(currentConversationId);
            }
        });
    }

    private void loadConversations() {
        try {
            HttpResponse<String> response = ApiClient.get("/api/chat/conversations");
            if (response.statusCode() == 200 && response.body() != null) {
                JsonArray array = JsonParser.parseString(response.body()).getAsJsonArray();
                conversations.clear();
                conversationsList.getItems().clear();

                for (JsonElement element : array) {
                    JsonObject conv = element.getAsJsonObject();
                    conversations.add(conv);

                    String display = "گفت‌وگو " + conv.get("id").getAsLong();
                    if (conv.has("advertisement") && !conv.get("advertisement").isJsonNull()) {
                        display = conv.getAsJsonObject("advertisement").get("title").getAsString();
                    }
                    conversationsList.getItems().add(display);
                }
            } else {
                conversationsMessageLabel.setText("خطا در بارگذاری گفت‌وگوها.");
            }
        } catch (Exception e) {
            conversationsMessageLabel.setText("خطا در ارتباط با سرور.");
        }
    }

    private void loadMessages(Long conversationId) {
        messagesContainer.getChildren().clear();
        try {
            HttpResponse<String> response = ApiClient.get("/api/chat/messages/" + conversationId);
            if (response.statusCode() == 200 && response.body() != null) {
                JsonArray array = JsonParser.parseString(response.body()).getAsJsonArray();

                for (JsonElement element : array) {
                    JsonObject msg = element.getAsJsonObject();

                    String sender = "";
                    if (msg.has("sender") && msg.get("sender").isJsonObject()) {
                        sender = msg.getAsJsonObject("sender").get("username").getAsString();
                    }

                    String text = msg.has("content") ? msg.get("content").getAsString() : "";
                    boolean isMe = currentUser != null && currentUser.equals(sender);

                    addMessageBubble(text, isMe);
                }
                Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));
            }
        } catch (Exception e) {
            chatWithLabel.setText("خطا در بارگذاری پیام‌ها.");
        }
    }

    private void addMessageBubble(String text, boolean isMe) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setPadding(new Insets(10));

        label.setStyle("-fx-background-radius: 10; -fx-font-size: 14px; " +
                (isMe ? "-fx-background-color: #dcf8c6;" : "-fx-background-color: #ffffff; -fx-border-color: #bdc3c7; -fx-border-radius: 10;"));

        HBox hbox = new HBox(label);
        hbox.setAlignment(isMe ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        hbox.setPadding(new Insets(5));

        messagesContainer.getChildren().add(hbox);
    }

    @FXML
    protected void onSendMessageClick(ActionEvent event) {
        String text = messageInput.getText().trim();
        if (text.isEmpty() || currentConversationId == null) return;

        try {
            JsonObject body = new JsonObject();
            body.addProperty("content", text);

            HttpResponse<String> response = ApiClient.post("/api/chat/reply/" + currentConversationId, body.toString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                addMessageBubble(text, true);
                messageInput.clear();
                Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));
            }
        } catch (Exception e) {
            chatWithLabel.setText("خطا در ارسال پیام.");
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
            e.printStackTrace();
        }
    }
}
