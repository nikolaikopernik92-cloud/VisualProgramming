package kz.atu.lab08;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RecipeApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        stage.setScene(new Scene(loader.load(), 820, 540));
        stage.setTitle("Список рецептов");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
