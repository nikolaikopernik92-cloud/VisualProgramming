package com.example.lab5;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

public class HelloController {

    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtGroup;

    @FXML private ComboBox<String> cbCity;
    @FXML private DatePicker dpBirthDate;
    @FXML private CheckBox chkConsent;

    @FXML private RadioButton rbCourse1;
    @FXML private RadioButton rbCourse2;
    @FXML private RadioButton rbCourse3;
    @FXML private RadioButton rbCourse4;
    @FXML private ToggleGroup courseGroup;

    @FXML private RadioButton rbFullTime;
    @FXML private RadioButton rbOnline;
    @FXML private ToggleGroup studyFormGroup;

    @FXML private CheckBox chkDormitory;
    @FXML private CheckBox chkScholarship;
    @FXML private CheckBox chkActivist;

    @FXML private Button btnCreate;
    @FXML private Label lblResult;

    @FXML
    public void initialize() {
        cbCity.getItems().addAll("Алматы", "Астана", "Шымкент", "Караганда", "Актобе");

        btnCreate.disableProperty().bind(
                txtFullName.textProperty().isEmpty().or(chkConsent.selectedProperty().not())
        );
    }

    @FXML
    private void onCreateClick() {
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String group = txtGroup.getText().trim();

        if (fullName.isBlank() || email.isBlank() || group.isBlank()) {
            showError("Заполните обязательные поля (ФИО, Email, Группа).");
            return;
        }

        if (!isEmailValid(email)) {
            showError("Некорректный Email! Должен содержать '@' и точку после неё.");
            txtEmail.requestFocus();
            return;
        }

        RadioButton selectedCourse = (RadioButton) courseGroup.getSelectedToggle();
        RadioButton selectedForm = (RadioButton) studyFormGroup.getSelectedToggle();

        if (selectedCourse == null || selectedForm == null) {
            showError("Выберите курс и форму обучения.");
            return;
        }

        LocalDate birthDate = dpBirthDate.getValue();
        String city = cbCity.getValue();

        lblResult.setText(
                "Студент: " + fullName + "\n" +
                        "Email: " + email + "\n" +
                        "Группа: " + group + "\n" +
                        "Город: " + (city != null ? city : "Не выбран") + "\n" +
                        "Дата рождения: " + (birthDate != null ? birthDate.toString() : "Не указана") + "\n" +
                        "Курс: " + selectedCourse.getText() + "\n" +
                        "Форма обучения: " + selectedForm.getText() + "\n" +
                        "Дополнительно: " + buildExtras()
        );
    }

    private boolean isEmailValid(String email) {
        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        return at > 0 && dot > at + 1 && dot < email.length() - 1;
    }

    private String buildExtras() {
        StringBuilder result = new StringBuilder();
        if (chkDormitory.isSelected()) result.append("общежитие; ");
        if (chkScholarship.isSelected()) result.append("стипендия; ");
        if (chkActivist.isSelected()) result.append("активист; ");

        return result.length() == 0 ? "не выбрано" : result.toString();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка ввода");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onClearClick() {
        txtFullName.clear();
        txtEmail.clear();
        txtGroup.clear();
        cbCity.getSelectionModel().clearSelection();
        dpBirthDate.setValue(null);

        if (courseGroup.getSelectedToggle() != null) {
            courseGroup.getSelectedToggle().setSelected(false);
        }
        if (studyFormGroup.getSelectedToggle() != null) {
            studyFormGroup.getSelectedToggle().setSelected(false);
        }

        chkDormitory.setSelected(false);
        chkScholarship.setSelected(false);
        chkActivist.setSelected(false);
        chkConsent.setSelected(false);

        lblResult.setText("Результат:");
        txtFullName.requestFocus();
    }

    @FXML
    private void onExitClick() {
        Platform.exit();
    }
}