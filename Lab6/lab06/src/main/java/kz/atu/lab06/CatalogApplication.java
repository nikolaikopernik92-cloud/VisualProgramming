package kz.atu.lab06;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CatalogApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                CatalogApplication.class.getResource("/catalog-view.fxml"));
        Parent root = loader.load();

        CatalogController controller = loader.getController();
        controller.setHostServices(getHostServices());

        Scene scene = new Scene(root, 900, 580);
        scene.getStylesheets().add(
                CatalogApplication.class.getResource("/style.css").toExternalForm());

        stage.setTitle("Каталог игр");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}