package kz.atu.lab09;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("registration.fxml"));
        stage.setScene(new Scene(loader.load(), 840, 780));
        stage.setTitle("Заказ печати документов");
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
