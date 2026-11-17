package kz.atu.lab12.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import kz.atu.lab12.model.Request;
import kz.atu.lab12.repository.RequestRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Optional;

public class RequestController {
    @FXML private TableView<Request> tableRequests;
    @FXML private TableColumn<Request, Number> colId;
    @FXML private TableColumn<Request, String> colTheme, colPriority, colStatus, colCreatedAt;

    @FXML private TextField txtTheme, txtSearch;
    @FXML private ComboBox<String> cmbPriority, cmbStatus;
    @FXML private ComboBox<String> filterPriority, filterStatus;

    @FXML private Button btnUpdate, btnDelete;
    @FXML private Label lblTotalCount, lblNewCount, lblInProgressCount, lblClosedCount, lblStatusMessage;

    private final RequestRepository repository = new RequestRepository();
    private final ObservableList<Request> items = FXCollections.observableArrayList();
    private FilteredList<Request> filteredData;

    @FXML
    private void initialize() {
        // Инициализация выпадающих списков
        cmbPriority.setItems(FXCollections.observableArrayList("Высокий", "Средний", "Низкий"));
        cmbStatus.setItems(FXCollections.observableArrayList("Новая", "В работе", "Закрыта"));
        cmbPriority.setValue("Средний");
        cmbStatus.setValue("Новая");

        filterPriority.setItems(FXCollections.observableArrayList("Все приоритеты", "Высокий", "Средний", "Низкий"));
        filterStatus.setItems(FXCollections.observableArrayList("Все статусы", "Новая", "В работе", "Закрыта"));
        filterPriority.setValue("Все приоритеты");
        filterStatus.setValue("Все статусы");

        // Настройка колонок таблицы
        colId.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().id()));
        colTheme.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().theme()));
        colPriority.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().priority()));
        colStatus.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().status()));
        colCreatedAt.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().createdAt()));

        setupBadgeCells();

        tableRequests.setPlaceholder(new Label("Список заявок пуст. Добавьте первую заявку ниже!"));

        // Фильтрация и сортировка
        filteredData = new FilteredList<>(items, p -> true);
        SortedList<Request> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableRequests.comparatorProperty());
        tableRequests.setItems(sortedData);

        txtSearch.textProperty().addListener((obs, oldV, newV) -> applyFilters());
        filterPriority.valueProperty().addListener((obs, oldV, newV) -> applyFilters());
        filterStatus.valueProperty().addListener((obs, oldV, newV) -> applyFilters());

        // Слушатель выбора строки
        tableRequests.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            boolean hasSelection = (newV != null);
            btnUpdate.setDisable(!hasSelection);
            btnDelete.setDisable(!hasSelection);
            if (hasSelection) {
                txtTheme.setText(newV.theme());
                cmbPriority.setValue(newV.priority());
                cmbStatus.setValue(newV.status());
            }
        });

        try {
            repository.initializeDatabase();
            refresh();
            setStatus("База данных успешно подключена", false);
        } catch (SQLException e) {
            showError("Не удалось подключиться к базе данных SQLite");
        }
    }

    private void setupBadgeCells() {
        // Цветовая индикация приоритета
        colPriority.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    switch (item) {
                        case "Высокий" -> badge.getStyleClass().add("badge-high");
                        case "Средний" -> badge.getStyleClass().add("badge-medium");
                        case "Низкий" -> badge.getStyleClass().add("badge-low");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // Цветовая индикация статуса
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    switch (item) {
                        case "Новая" -> badge.getStyleClass().add("badge-new");
                        case "В работе" -> badge.getStyleClass().add("badge-progress");
                        case "Закрыта" -> badge.getStyleClass().add("badge-closed");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
    }

    private void applyFilters() {
        String search = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        String prio = filterPriority.getValue();
        String stat = filterStatus.getValue();

        filteredData.setPredicate(req -> {
            boolean matchesSearch = search.isEmpty() || req.theme().toLowerCase().contains(search);
            boolean matchesPrio = prio == null || prio.equals("Все приоритеты") || req.priority().equals(prio);
            boolean matchesStat = stat == null || stat.equals("Все статусы") || req.status().equals(stat);
            return matchesSearch && matchesPrio && matchesStat;
        });
    }

    private void refresh() throws SQLException {
        items.setAll(repository.findAll());
        updateStats();
    }

    private void updateStats() {
        long total = items.size();
        long countNew = items.stream().filter(r -> "Новая".equals(r.status())).count();
        long countProg = items.stream().filter(r -> "В работе".equals(r.status())).count();
        long countClosed = items.stream().filter(r -> "Закрыта".equals(r.status())).count();

        lblTotalCount.setText(String.valueOf(total));
        lblNewCount.setText(String.valueOf(countNew));
        lblInProgressCount.setText(String.valueOf(countProg));
        lblClosedCount.setText(String.valueOf(countClosed));
    }

    private void validateInput() {
        if (txtTheme.getText() == null || txtTheme.getText().isBlank()) {
            throw new IllegalArgumentException("Введите тему заявки!");
        }
        if (cmbPriority.getValue() == null || cmbStatus.getValue() == null) {
            throw new IllegalArgumentException("Выберите приоритет и статус!");
        }
    }

    @FXML
    private void onAdd() {
        try {
            validateInput();
            repository.insert(txtTheme.getText().trim(), cmbPriority.getValue(), cmbStatus.getValue());
            refresh();
            clearFields();
            setStatus("Заявка успешно добавлена", false);
        } catch (IllegalArgumentException | SQLException e) {
            showError(e instanceof SQLException ? "Ошибка сохранения в БД" : e.getMessage());
        }
    }

    @FXML
    private void onUpdate() {
        try {
            Request selected = tableRequests.getSelectionModel().getSelectedItem();
            if (selected == null) {
                throw new IllegalArgumentException("Выберите заявку для изменения");
            }
            validateInput();
            repository.update(selected.id(), txtTheme.getText().trim(), cmbPriority.getValue(), cmbStatus.getValue());
            refresh();
            clearFields();
            setStatus("Заявка №" + selected.id() + " обновлена", false);
        } catch (IllegalArgumentException | SQLException e) {
            showError(e instanceof SQLException ? "Ошибка обновления в БД" : e.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        try {
            Request selected = tableRequests.getSelectionModel().getSelectedItem();
            if (selected == null) {
                throw new IllegalArgumentException("Выберите заявку для удаления");
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("Удаление заявки №" + selected.id());
            alert.setContentText("Вы действительно хотите удалить заявку «" + selected.theme() + "»?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                repository.delete(selected.id());
                refresh();
                clearFields();
                setStatus("Заявка №" + selected.id() + " удалена", false);
            }
        } catch (IllegalArgumentException | SQLException e) {
            showError(e instanceof SQLException ? "Ошибка удаления из БД" : e.getMessage());
        }
    }

    @FXML
    private void onRefresh() {
        try {
            refresh();
            txtSearch.clear();
            filterPriority.setValue("Все приоритеты");
            filterStatus.setValue("Все статусы");
            clearFields();
            setStatus("Данные обновлены", false);
        } catch (SQLException e) {
            showError("Ошибка обновления списка");
        }
    }

    @FXML
    private void onExportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Экспорт заявок в CSV");
        fileChooser.setInitialFileName("requests_export.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV файлы (*.csv)", "*.csv"));
        File file = fileChooser.showSaveDialog(tableRequests.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write('\ufeff'); // BOM для корректного открытия кириллицы в Excel
                writer.println("ID;Тема;Приоритет;Статус;Дата создания");
                for (Request r : filteredData) {
                    writer.println(r.id() + ";\"" + r.theme().replace("\"", "\"\"") + "\";" + r.priority() + ";" + r.status() + ";" + r.createdAt());
                }
                setStatus("Экспортировано в " + file.getName(), false);
            } catch (Exception e) {
                showError("Не удалось экспортировать файл: " + e.getMessage());
            }
        }
    }

    @FXML
    private void clearFields() {
        txtTheme.clear();
        cmbPriority.setValue("Средний");
        cmbStatus.setValue("Новая");
        tableRequests.getSelectionModel().clearSelection();
    }

    private void setStatus(String msg, boolean isError) {
        lblStatusMessage.setText(msg);
        lblStatusMessage.setStyle(isError ? "-fx-text-fill: #dc2626;" : "-fx-text-fill: #059669;");
    }

    private void showError(String message) {
        setStatus(message, true);
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
