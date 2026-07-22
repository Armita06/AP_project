module client.frontend {
    requires javafx.controls;
    requires javafx.fxml;


    opens client.frontend to javafx.fxml;
    exports client.frontend;
}