package client.frontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("سامانه ثبت آگهی دست‌دوم");

        stage.setResizable(true);

        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setMaximized(true);

        stage.setScene(scene);
        stage.show();
    }
}