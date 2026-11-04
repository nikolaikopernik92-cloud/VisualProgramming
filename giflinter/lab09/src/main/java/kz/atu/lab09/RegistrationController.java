package kz.atu.lab09;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RegistrationController {

    private static final PseudoClass INVALID = PseudoClass.getPseudoClass("invalid");
    private static final System.Logger LOG =
            System.getLogger(RegistrationController.class.getName());

    @FXML private TextField txtName, txtEmail, txtOrderId, txtPages, txtCopies;
    @FXML private ComboBox<String> cmbFormat;
    @FXML private CheckBox chkConsent;
    @FXML private Label lblNameError, lblEmailError, lblOrderIdError,
            lblPagesError, lblCopiesError, lblFormatError, lblConsentError;
    @FXML private Button btnRegister;
    @FXML private TableView<PrintOrder> tblOrders;
    @FXML private TableColumn<PrintOrder, String> colId, colName, colEmail, colFormat;
    @FXML private TableColumn<PrintOrder, Number> colPages, colCopies;

    private final ObservableList<PrintOrder> orders = FXCollections.observableArrayList();
    private final Map<String, Control> controls = new LinkedHashMap<>();
    private final Map<String, Label> labels = new LinkedHashMap<>();

    @FXML
    private void initialize() {
        // таблица
        colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().id()));
        colName.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().name()));
        colEmail.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().email()));
        colPages.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().pages()));
        colCopies.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().copies()));
        colFormat.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().format()));
        tblOrders.setItems(orders);

        cmbFormat.getItems().addAll("A5", "A4", "A3");

        // карты "ключ -> контрол" и "ключ -> Label ошибки"
        controls.put("name", txtName);       labels.put("name", lblNameError);
        controls.put("email", txtEmail);     labels.put("email", lblEmailError);
        controls.put("orderId", txtOrderId); labels.put("orderId", lblOrderIdError);
        controls.put("pages", txtPages);     labels.put("pages", lblPagesError);
        controls.put("copies", txtCopies);   labels.put("copies", lblCopiesError);
        controls.put("format", cmbFormat);   labels.put("format", lblFormatError);
        controls.put("consent", chkConsent); labels.put("consent", lblConsentError);

        // TextFormatter: ограничение ввода символов
        txtPages.setTextFormatter(limit("\\d{0,3}"));
        txtCopies.setTextFormatter(limit("\\d?"));
        txtOrderId.setTextFormatter(limit("[A-Za-z0-9-]{0,7}"));

        // ошибка сразу после потери фокуса + перепроверка при исправлении
        controls.forEach((key, c) -> {
            c.focusedProperty().addListener((o, was, now) -> {
                // проверяем только если пользователь сам ушёл с поля, а окно осталось активным
                boolean windowActive = c.getScene() != null
                        && c.getScene().getWindow() != null
                        && c.getScene().getWindow().isFocused();
                if (!now && windowActive) refreshField(key);
            });
            if (c instanceof TextField tf)
                tf.textProperty().addListener((o, a, b) -> {
                    if (!labels.get(key).getText().isEmpty()) refreshField(key);
                });
        });
        cmbFormat.valueProperty().addListener((o, a, b) -> refreshField("format"));

        // кнопка заблокирована, пока нет согласия
        btnRegister.setDisable(true);
        chkConsent.selectedProperty().addListener((o, a, sel) -> {
            btnRegister.setDisable(!sel);
            refreshField("consent");
        });
    }

    private TextFormatter<String> limit(String regex) {
        return new TextFormatter<>(ch -> ch.getControlNewText().matches(regex) ? ch : null);
    }

    private ValidationResult validateForm() {
        Set<String> ids = orders.stream().map(PrintOrder::id).collect(Collectors.toSet());
        return RegistrationValidator.validate(
                txtName.getText(), txtEmail.getText(), txtOrderId.getText(),
                txtPages.getText(), txtCopies.getText(),
                cmbFormat.getValue(), chkConsent.isSelected(), ids);
    }

    private void setError(String key, String message) {
        labels.get(key).setText(message);
        controls.get(key).pseudoClassStateChanged(INVALID, !message.isEmpty());
    }

    private void refreshField(String key) {
        setError(key, validateForm().error(key));
    }

    private void showErrors(ValidationResult r) {
        controls.keySet().forEach(k -> setError(k, r.error(k)));
    }

    @FXML
    private void onRegister() {
        ValidationResult result = validateForm();
        showErrors(result);
        if (!result.isValid()) {
            alert(Alert.AlertType.WARNING, "Исправьте ошибки в форме");
            return;
        }
        try {
            orders.add(new PrintOrder(
                    txtOrderId.getText().trim().toUpperCase(),
                    txtName.getText().trim(),
                    txtEmail.getText().trim(),
                    Integer.parseInt(txtPages.getText().trim()),
                    Integer.parseInt(txtCopies.getText().trim()),
                    cmbFormat.getValue()));
            clearForm();
        } catch (RuntimeException e) {
            LOG.log(System.Logger.Level.ERROR, "Ошибка регистрации заказа", e);
            alert(Alert.AlertType.ERROR, "Не удалось завершить регистрацию");
        }
    }

    @FXML
    private void onClear() { clearForm(); }

    private void clearForm() {
        txtName.clear(); txtEmail.clear(); txtOrderId.clear();
        txtPages.clear(); txtCopies.clear();
        cmbFormat.setValue(null);
        chkConsent.setSelected(false);
        controls.keySet().forEach(k -> setError(k, ""));
    }

    @FXML
    private void onSaveCsv() {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("orders.csv");
        File file = fc.showSaveDialog(tblOrders.getScene().getWindow());
        if (file == null) return;
        StringBuilder sb = new StringBuilder("ID;ФИО;Email;Страниц;Копий;Формат\n");
        for (PrintOrder o : orders)
            sb.append(String.join(";", o.id(), csv(o.name()), csv(o.email()),
                    String.valueOf(o.pages()), String.valueOf(o.copies()), o.format()))
              .append('\n');
        try {
            Files.writeString(file.toPath(), sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.log(System.Logger.Level.ERROR, "Ошибка записи файла", e);
            alert(Alert.AlertType.ERROR, "Не удалось сохранить файл");
        }
    }

    private static String csv(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private void alert(Alert.AlertType type, String text) {
        new Alert(type, text).showAndWait();
    }
}
