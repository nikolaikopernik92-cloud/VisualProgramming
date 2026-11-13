package kz.atu.lab11;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SmartphoneApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                SmartphoneApplication.class.getResource("smartphone-view.fxml"));
        Scene scene = new Scene(loader.load(), 900, 680);
        scene.getStylesheets().add(
                SmartphoneApplication.class.getResource("styles.css").toExternalForm());
        stage.setTitle("Каталог смартфонов");
        stage.setMinWidth(760);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
