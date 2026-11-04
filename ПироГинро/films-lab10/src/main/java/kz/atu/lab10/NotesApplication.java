package kz.atu.lab10;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NotesApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(NotesApplication.class.getResource("notes-view.fxml"));
        Scene scene = new Scene(loader.load(), 1020, 640);
        scene.getStylesheets().add(
                NotesApplication.class.getResource("styles.css").toExternalForm());
        NotesController controller = loader.getController();
        stage.setOnCloseRequest(e -> {
            if (!controller.canClose()) e.consume();
        });
        stage.setMinWidth(900);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.setTitle("Кинотека");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
