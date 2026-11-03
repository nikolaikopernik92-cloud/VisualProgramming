package kz.atu.lab;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label lblFio;

    @FXML
    private Label lblGroup;

    @FXML
    private Label lblSpecialty;

    @FXML
    public void initialize() {
        lblFio.setText("ФИО: Оразбаев Махсуд");
        lblGroup.setText("Группа: ИС 24-22");
        lblSpecialty.setText("Специальность: Информационные технологии");
    }
}