package kz.atu.lab12;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class RequestApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(RequestApplication.class.getResource("request-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 920, 620);
        scene.getStylesheets().add(Objects.requireNonNull(
                RequestApplication.class.getResource("style.css")).toExternalForm());
        stage.setTitle("Система управления заявками — Вариант №12");
        stage.setMinWidth(820);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
