package kz.atu.lab06;

import javafx.application.HostServices;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CatalogController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFilterGenre;
    @FXML private ListView<String> listGenres;

    @FXML private TableView<Game> tableGames;
    @FXML private TableColumn<Game, String> colName;
    @FXML private TableColumn<Game, String> colGenre;
    @FXML private TableColumn<Game, String> colPlatform;
    @FXML private TableColumn<Game, Double> colRating;
    @FXML private TableColumn<Game, String> colLink;

    @FXML private TextField txtName;
    @FXML private ComboBox<String> cmbGenre;
    @FXML private ComboBox<String> cmbPlatform;
    @FXML private TextField txtRating;

    @FXML private Label lblCount;

    private final ObservableList<Game> games = FXCollections.observableArrayList();
    private FilteredList<Game> filteredGames;
    private HostServices hostServices;

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    public void initialize() {
        games.addAll(
                new Game("Minecraft", "Песочница", "PC", 9.3),
                new Game("God of War", "Экшен", "PlayStation", 9.5),
                new Game("GTA 6", "Экшен", "PlayStation", 0.0),
                new Game("War Thunder", "Экшен", "PC", 7.8),
                new Game("Blur", "Гонки", "Xbox", 7.5),
                new Game("Dota 2", "Стратегия", "PC", 9.0),
                new Game("The Witcher 3: Wild Hunt", "RPG", "PC", 9.7)
        );

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colPlatform.setCellValueFactory(new PropertyValueFactory<>("platform"));
        colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));

        // Столбец со ссылкой на поиск в Google
        colLink.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getName()));
        colLink.setCellFactory(col -> new TableCell<Game, String>() {
            private final Hyperlink link = new Hyperlink("Google");

            {
                link.setOnAction(e -> {
                    if (getItem() != null) {
                        openInGoogle(getItem());
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : link);
            }
        });

        filteredGames = new FilteredList<>(games, g -> true);
        tableGames.setItems(filteredGames);

        cmbGenre.getItems().addAll("RPG", "Экшен", "Спорт", "Гонки", "Песочница", "Стратегия");
        cmbPlatform.getItems().addAll("PC", "PlayStation", "Xbox", "Nintendo Switch", "Mobile");

        cmbFilterGenre.getItems().addAll("Все", "RPG", "Экшен", "Спорт", "Гонки", "Песочница", "Стратегия");
        cmbFilterGenre.setValue("Все");

        listGenres.getItems().addAll("Все игры", "RPG", "Экшен", "Спорт", "Гонки", "Песочница", "Стратегия");
        listGenres.getSelectionModel().select("Все игры");

        listGenres.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                String value = newV.equals("Все игры") ? "Все" : newV;
                if (!value.equals(cmbFilterGenre.getValue())) {
                    cmbFilterGenre.setValue(value);
                }
            }
        });

        cmbFilterGenre.setOnAction(e -> {
            String v = cmbFilterGenre.getValue();
            if (v != null) {
                String item = v.equals("Все") ? "Все игры" : v;
                if (!item.equals(listGenres.getSelectionModel().getSelectedItem())) {
                    listGenres.getSelectionModel().select(item);
                }
            }
            applyFilter();
        });

        txtSearch.textProperty().addListener((obs, oldV, newV) -> applyFilter());

        tableGames.getSelectionModel().selectedItemProperty().addListener((obs, oldV, selected) -> {
            if (selected != null) {
                txtName.setText(selected.getName());
                cmbGenre.setValue(selected.getGenre());
                cmbPlatform.setValue(selected.getPlatform());
                txtRating.setText(String.valueOf(selected.getRating()));
            }
        });

        applyFilter();
    }

    private void openInGoogle(String gameName) {
        if (hostServices == null) {
            return;
        }
        String query = URLEncoder.encode(gameName, StandardCharsets.UTF_8);
        hostServices.showDocument("https://www.google.com/search?q=" + query);
    }

    @FXML
    private void onAddClick() {
        String name = txtName.getText().trim();
        String genre = cmbGenre.getValue();
        String platform = cmbPlatform.getValue();
        String ratingText = txtRating.getText().trim().replace(',', '.');

        if (name.isBlank() || genre == null || platform == null || ratingText.isBlank()) {
            showError("Заполните все поля.");
            return;
        }

        try {
            double rating = Double.parseDouble(ratingText);
            if (rating < 0 || rating > 10) {
                showError("Рейтинг должен быть от 0 до 10.");
                return;
            }
            games.add(new Game(name, genre, platform, rating));
            clearInput();
            applyFilter();
        } catch (NumberFormatException e) {
            showError("Рейтинг должен быть числом.");
        }
    }

    @FXML
    private void onDeleteClick() {
        Game selected = tableGames.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите игру для удаления.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText(null);
        confirm.setContentText("Удалить игру «" + selected.getName() + "»?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            games.remove(selected);
            applyFilter();
        }
    }

    @FXML
    private void onClearFilterClick() {
        txtSearch.clear();
        cmbFilterGenre.setValue("Все");
        applyFilter();
    }

    private void applyFilter() {
        String search = txtSearch.getText().trim().toLowerCase();
        String genre = cmbFilterGenre.getValue();

        filteredGames.setPredicate(game -> {
            boolean matchesSearch = game.getName().toLowerCase().contains(search);
            boolean matchesGenre = genre == null
                    || genre.equals("Все")
                    || game.getGenre().equals(genre);
            return matchesSearch && matchesGenre;
        });
        updateCount();
    }

    private void updateCount() {
        lblCount.setText("Найдено записей: " + tableGames.getItems().size());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearInput() {
        txtName.clear();
        txtRating.clear();
        cmbGenre.getSelectionModel().clearSelection();
        cmbPlatform.getSelectionModel().clearSelection();
        tableGames.getSelectionModel().clearSelection();
        txtName.requestFocus();
    }
}