import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller интерактивного калькулятора.
 * Индивидуальный вариант №12: увеличение числа A на B процентов.
 * Формула: result = A + A * B / 100
 */
public class CalculatorController {

    @FXML
    private TextField txtNumber1;

    @FXML
    private TextField txtNumber2;

    @FXML
    private Label lblResult;

    @FXML
    private Label lblOperation;

    @FXML
    private Label lblCounter;

    // Счётчик успешно выполненных операций (Задание 4 самостоятельной части)
    private int operationCount = 0;

    /**
     * Общий обработчик для всех арифметических кнопок.
     * Источник события определяется через event.getSource().
     */
    @FXML
    private void onOperation(ActionEvent event) {

        // Проверка пустых полей
        if (txtNumber1.getText().isBlank() || txtNumber2.getText().isBlank()) {
            showError("Введите оба числа.");
            return;
        }

        try {
            double number1 = Double.parseDouble(txtNumber1.getText());
            double number2 = Double.parseDouble(txtNumber2.getText());

            // Определение нажатой кнопки через источник события
            Button button = (Button) event.getSource();
            String operation = button.getText();

            double result;

            switch (operation) {
                case "+":
                    result = number1 + number2;
                    break;

                case "-":
                    result = number1 - number2;
                    break;

                case "×":
                    result = number1 * number2;
                    break;

                case "÷":
                    if (number2 == 0) {
                        showError("Деление на ноль невозможно.");
                        return;
                    }
                    result = number1 / number2;
                    break;

                case "xʸ":
                    // Возведение number1 в степень number2
                    result = Math.pow(number1, number2);
                    break;

                case "%":
                    // Остаток от деления с проверкой делителя на ноль
                    if (number2 == 0) {
                        showError("Деление на ноль невозможно.");
                        return;
                    }
                    result = number1 % number2;
                    break;

                case "A+B%":
                    // Индивидуальный вариант №12: увеличение A на B процентов
                    result = number1 + (number1 * number2 / 100);
                    break;

                default:
                    return;
            }

            lblResult.setText(String.format("Результат: %.2f", result));
            lblOperation.setText("Операция: " + operation);

            operationCount++;
            lblCounter.setText("Выполнено операций: " + operationCount);

        } catch (NumberFormatException e) {
            showError("Введите корректные числа.");
        }
    }

    /**
     * Очистка формы: сбрасывает поля ввода и результат.
     */
    @FXML
    private void onClearClick() {
        txtNumber1.clear();
        txtNumber2.clear();
        lblResult.setText("Результат: 0");
        lblOperation.setText("Операция: —");
        txtNumber1.requestFocus();
    }

    /**
     * Завершение работы приложения.
     */
    @FXML
    private void onExitClick() {
        Platform.exit();
    }

    /**
     * Наведение курсора на кнопку операции — MouseEvent.
     */
    @FXML
    private void onMouseEntered() {
        lblOperation.setText("Выберите операцию");
    }

    /**
     * Уход курсора с кнопки операции — MouseEvent.
     */
    @FXML
    private void onMouseExited() {
        if (lblResult.getText().equals("Результат: 0")) {
            lblOperation.setText("Операция: —");
        }
    }

    /**
     * Блокировка полей ввода (изменение свойства disable).
     */
    @FXML
    private void onLockClick() {
        txtNumber1.setDisable(true);
        txtNumber2.setDisable(true);
    }

    /**
     * Разблокировка полей ввода.
     */
    @FXML
    private void onUnlockClick() {
        txtNumber1.setDisable(false);
        txtNumber2.setDisable(false);
        txtNumber1.requestFocus();
    }

    /**
     * Отображение сообщения об ошибке через Alert.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
