package kz.atu.lab11.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import kz.atu.lab11.model.Smartphone;
import kz.atu.lab11.service.SmartphoneService;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

/** Контроллер: только реакция на события и обновление интерфейса. */
public class SmartphoneController {

    private static final String ALL = "Все";

    @FXML private TableView<Smartphone> tablePhones;
    @FXML private TableColumn<Smartphone, String> colName, colBrand;
    @FXML private TableColumn<Smartphone, Number> colPrice;
    @FXML private TextField txtName, txtBrand, txtPrice, txtSearch;
    @FXML private ComboBox<String> cmbBrandFilter;
    @FXML private Label lblCount, lblAverage, lblStatus, lblFormTitle;
    @FXML private Button btnAdd, btnUpdate, btnDelete;

    private final SmartphoneService service = new SmartphoneService();
    private FilteredList<Smartphone> filtered;

    @FXML
    private void initialize() {
        colName.setCellValueFactory(c -> c.getValue().nameProperty());
        colBrand.setCellValueFactory(c -> c.getValue().brandProperty());
        colPrice.setCellValueFactory(c -> c.getValue().priceProperty());

        // цена: разделители тысяч, знак валюты, выравнивание вправо
        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().remove("price-cell");
                } else {
                    setText(formatPrice(item.doubleValue()));
                    if (!getStyleClass().contains("price-cell")) {
                        getStyleClass().add("price-cell");
                    }
                }
            }
        });

        service.addPhone("Galaxy S24", "Samsung", 449_000);
        service.addPhone("iPhone 15", "Apple", 520_000);
        service.addPhone("Redmi Note 13", "Xiaomi", 119_000);

        filtered = new FilteredList<>(service.getPhones(), s -> true);
        SortedList<Smartphone> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tablePhones.comparatorProperty());
        tablePhones.setItems(sorted);
        tablePhones.setPlaceholder(new Label("Ничего не найдено. Измените поиск или фильтр."));

        refreshBrands();

        txtSearch.textProperty().addListener((o, a, b) -> applyFilter());
        cmbBrandFilter.valueProperty().addListener((o, a, b) -> applyFilter());

        // кнопки «Сохранить» и «Удалить» доступны только при выбранной строке
        btnUpdate.disableProperty().bind(
                tablePhones.getSelectionModel().selectedItemProperty().isNull());
        btnDelete.disableProperty().bind(
                tablePhones.getSelectionModel().selectedItemProperty().isNull());

        // при выборе строки заполняем поля для редактирования
        tablePhones.getSelectionModel().selectedItemProperty().addListener((o, old, s) -> {
            if (s != null) {
                txtName.setText(s.getName());
                txtBrand.setText(s.getBrand());
                txtPrice.setText(BigDecimal.valueOf(s.getPrice())
                        .stripTrailingZeros().toPlainString());
                lblFormTitle.setText("Редактирование: " + s.getBrand() + " " + s.getName());
            } else {
                lblFormTitle.setText("Новый смартфон");
            }
        });

        // Enter в полях формы: добавить (если ничего не выбрано) или сохранить
        txtName.setOnAction(e -> submitForm());
        txtBrand.setOnAction(e -> submitForm());
        txtPrice.setOnAction(e -> submitForm());

        updateStats();
    }

    private void submitForm() {
        if (tablePhones.getSelectionModel().getSelectedItem() == null) {
            onAdd();
        } else {
            onUpdate();
        }
    }

    /** Динамический список брендов, сохраняет текущий выбранный фильтр. */
    private void refreshBrands() {
        String current = cmbBrandFilter.getValue();
        ObservableList<String> items = FXCollections.observableArrayList(ALL);
        items.addAll(service.getBrands());
        cmbBrandFilter.setItems(items);
        cmbBrandFilter.setValue(items.contains(current) ? current : ALL);
    }

    private void applyFilter() {
        String q = txtSearch.getText().trim().toLowerCase();
        String brand = cmbBrandFilter.getValue();
        filtered.setPredicate(s -> s.getName().toLowerCase().contains(q)
                && (brand == null || brand.equals(ALL) || s.getBrand().equals(brand)));
        updateStats();
    }

    private void updateStats() {
        lblCount.setText("Найдено смартфонов: " + filtered.size()
                + " из " + service.getPhones().size());
        lblAverage.setText("Средняя цена в каталоге: " + formatPrice(service.averagePrice()));
    }

    private static String formatPrice(double p) {
        return String.format(Locale.forLanguageTag("ru"), "%,.0f ₸", p);
    }

    private double readPrice() {
        try {
            String text = txtPrice.getText().trim().replace(',', '.').replace(" ", "");
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Введите числовую цену, например 449000");
        }
    }

    private void showStatus(String message) {
        lblStatus.setText(message);
        lblStatus.getStyleClass().remove("status-ok");
        lblStatus.getStyleClass().add("status-ok");
    }

    @FXML
    private void onAdd() {
        try {
            String name = txtName.getText().trim();
            service.addPhone(txtName.getText(), txtBrand.getText(), readPrice());
            refreshBrands();
            applyFilter();
            clearFields();
            showStatus("Добавлено: " + name);
            txtName.requestFocus();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onUpdate() {
        try {
            Smartphone s = tablePhones.getSelectionModel().getSelectedItem();
            service.updatePhone(s, txtName.getText(), txtBrand.getText(), readPrice());
            refreshBrands();
            applyFilter();
            showStatus("Изменения сохранены");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        Smartphone s = tablePhones.getSelectionModel().getSelectedItem();
        if (s == null) {
            showError("Смартфон не выбран");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText(null);
        confirm.setContentText("Удалить «" + s.getBrand() + " " + s.getName() + "»?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String name = s.getName();
            service.deletePhone(s);
            refreshBrands();
            applyFilter();
            clearFields();
            showStatus("Удалено: " + name);
        }
    }

    @FXML
    private void onClear() {
        clearFields();
        lblStatus.setText("");
    }

    @FXML
    private void onResetFilters() {
        txtSearch.clear();
        cmbBrandFilter.setValue(ALL);
    }

    private void clearFields() {
        tablePhones.getSelectionModel().clearSelection();
        txtName.clear();
        txtBrand.clear();
        txtPrice.clear();
    }

    private void showError(String m) {
        lblStatus.setText("");
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Ошибка");
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }
}
