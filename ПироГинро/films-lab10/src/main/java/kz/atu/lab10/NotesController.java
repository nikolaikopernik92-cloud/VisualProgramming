package kz.atu.lab10;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class NotesController {
    private static final String ALL = "Все статусы";
    private static final String SORT_NONE = "Порядок добавления";
    private static final String SORT_TITLE = "Название А-Я";
    private static final String SORT_RATING = "Сначала высокий рейтинг";

    @FXML private ListView<Note> listNotes;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFilter;
    @FXML private ComboBox<String> cmbSort;
    @FXML private TextField txtTitle;
    @FXML private TextField txtDirector;
    @FXML private TextField txtCast;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private Slider sldRating;
    @FXML private Label lblRating;
    @FXML private TextArea txtBody;
    @FXML private Label lblStatus;
    @FXML private Label lblStats;

    private final ObservableList<Note> notes = FXCollections.observableArrayList();
    private final FilteredList<Note> filtered = new FilteredList<>(notes, n -> true);
    private final SortedList<Note> sorted = new SortedList<>(filtered);
    private final NoteStorage storage = new NoteStorage();
    private boolean dirty = false;

    @FXML
    public void initialize() {
        listNotes.setItems(sorted);
        listNotes.setCellFactory(lv -> new FilmCell());

        cmbStatus.getItems().setAll(Note.STATUSES);
        cmbStatus.setValue(Note.WANT);

        cmbFilter.getItems().add(ALL);
        cmbFilter.getItems().addAll(Note.STATUSES);
        cmbFilter.setValue(ALL);

        cmbSort.getItems().setAll(SORT_NONE, SORT_TITLE, SORT_RATING);
        cmbSort.setValue(SORT_NONE);

        txtSearch.textProperty().addListener((o, a, b) -> applyFilter());
        cmbFilter.valueProperty().addListener((o, a, b) -> applyFilter());
        cmbSort.valueProperty().addListener((o, a, b) -> applySort());

        sldRating.valueProperty().addListener((o, a, b) ->
                lblRating.setText(ratingText((int) Math.round(b.doubleValue()))));
        lblRating.setText(ratingText(0));

        listNotes.getSelectionModel().selectedItemProperty().addListener((obs, old, note) -> {
            if (note != null) {
                txtTitle.setText(note.getTitle());
                txtDirector.setText(note.getDirector());
                txtCast.setText(note.getCast());
                txtBody.setText(note.getBody());
                cmbStatus.setValue(note.getStatus());
                sldRating.setValue(note.getRating());
            }
        });

        notes.addListener((ListChangeListener<Note>) c -> {
            dirty = true;
            updateStats();
        });

        notes.setAll(demoData());
        dirty = false;
        updateStats();
        lblStatus.setText("Готово. Загружено демо-записей: " + notes.size());
    }

    // ---------- фильтр, сортировка, статистика ----------

    private void applyFilter() {
        String q = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        String st = cmbFilter.getValue();
        filtered.setPredicate(n ->
                (q.isEmpty() || n.getTitle().toLowerCase().contains(q)
                        || n.getDirector().toLowerCase().contains(q)
                        || n.getCast().toLowerCase().contains(q))
                        && (st == null || ALL.equals(st) || st.equals(n.getStatus())));
    }

    private void applySort() {
        String s = cmbSort.getValue();
        if (SORT_TITLE.equals(s)) {
            sorted.setComparator(Comparator.comparing((Note n) -> n.getTitle().toLowerCase()));
        } else if (SORT_RATING.equals(s)) {
            sorted.setComparator(Comparator.comparingInt(Note::getRating).reversed());
        } else {
            sorted.setComparator(null);
        }
    }

    private void updateStats() {
        long rated = notes.stream().filter(n -> n.getRating() > 0).count();
        double avg = notes.stream().filter(n -> n.getRating() > 0)
                .mapToInt(Note::getRating).average().orElse(0);
        lblStats.setText("Фильмов: " + notes.size()
                + "   |   Средняя оценка: " + (rated == 0 ? "-" : String.format("%.1f", avg)));
    }

    // ---------- обработчики кнопок ----------

    @FXML
    private void onAdd() {
        String title = txtTitle.getText().trim();
        String body = txtBody.getText().trim();
        if (title.isBlank() || body.isBlank()) {
            error("Введите название фильма и краткое описание.");
            return;
        }
        Note note = new Note();
        readForm(note);
        notes.add(note);
        if (sorted.contains(note)) {
            listNotes.getSelectionModel().select(note);
        }
        lblStatus.setText("Добавлен фильм: " + title);
    }

    @FXML
    private void onEdit() {
        Note selected = listNotes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            error("Выберите фильм для изменения.");
            return;
        }
        if (txtTitle.getText().isBlank() || txtBody.getText().isBlank()) {
            error("Название и описание не могут быть пустыми.");
            return;
        }
        readForm(selected);
        dirty = true;
        applyFilter();
        applySort();
        updateStats();
        listNotes.refresh();
        if (sorted.contains(selected)) {
            listNotes.getSelectionModel().select(selected);
        }
        lblStatus.setText("Изменено: " + selected.getTitle());
    }

    @FXML
    private void onDelete() {
        Note selected = listNotes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            error("Выберите фильм.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Удалить «" + selected.getTitle() + "»?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            notes.remove(selected);
            clearForm();
            lblStatus.setText("Фильм удалён");
        }
    }

    @FXML
    private void onClear() {
        listNotes.getSelectionModel().clearSelection();
        clearForm();
        lblStatus.setText("Форма очищена");
    }

    @FXML
    private void onSave() {
        FileChooser chooser = chooser();
        chooser.setTitle("Сохранить список фильмов");
        chooser.setInitialFileName("films.json");
        File file = chooser.showSaveDialog(listNotes.getScene().getWindow());
        if (file == null) return;
        try {
            storage.save(file.toPath(), notes);
            dirty = false;
            lblStatus.setText("Сохранено: " + file.getName() + " (" + notes.size() + " шт.)");
        } catch (IOException ex) {
            error("Не удалось сохранить файл: " + ex.getMessage());
        }
    }

    @FXML
    private void onOpen() {
        if (!canDiscardChanges()) return;
        FileChooser chooser = chooser();
        chooser.setTitle("Открыть список фильмов");
        File file = chooser.showOpenDialog(listNotes.getScene().getWindow());
        if (file == null) return;
        try {
            List<Note> loaded = storage.load(file.toPath());
            notes.setAll(loaded);
            dirty = false;
            clearForm();
            lblStatus.setText("Загружено фильмов: " + notes.size());
        } catch (IOException ex) {
            error("Не удалось прочитать JSON. Текущие данные сохранены в приложении.");
        }
    }

    public boolean canClose() {
        return canDiscardChanges();
    }

    // ---------- вспомогательные методы ----------

    private void readForm(Note n) {
        n.setTitle(txtTitle.getText().trim());
        n.setDirector(txtDirector.getText().trim());
        n.setCast(txtCast.getText().trim());
        n.setBody(txtBody.getText().trim());
        n.setStatus(cmbStatus.getValue());
        n.setRating((int) Math.round(sldRating.getValue()));
    }

    private boolean canDiscardChanges() {
        if (!dirty) return true;
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Есть несохранённые изменения. Продолжить без сохранения?",
                ButtonType.YES, ButtonType.NO);
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void clearForm() {
        txtTitle.clear();
        txtDirector.clear();
        txtCast.clear();
        txtBody.clear();
        cmbStatus.setValue(Note.WANT);
        sldRating.setValue(0);
    }

    private FileChooser chooser() {
        FileChooser c = new FileChooser();
        c.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON (*.json)", "*.json"));
        return c;
    }

    private void error(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    /** Рейтинг 0..10 -> пять звёзд (по две единицы на звезду). */
    static String stars(int rating) {
        int full = (int) Math.round(rating / 2.0);
        return "★".repeat(full) + "☆".repeat(5 - full);
    }

    static String ratingText(int rating) {
        return rating == 0 ? "Без оценки" : stars(rating) + "   " + rating + " / 10";
    }

    private static String chipClass(String status) {
        if (Note.WATCHED.equals(status)) return "chip-watched";
        if (Note.WATCHING.equals(status)) return "chip-watching";
        return "chip-want";
    }

    /** Карточка фильма в списке. */
    private static class FilmCell extends ListCell<Note> {
        @Override
        protected void updateItem(Note n, boolean empty) {
            super.updateItem(n, empty);
            if (empty || n == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            Label title = new Label(n.getTitle());
            title.getStyleClass().add("film-title");

            Label meta = new Label(n.getDirector().isBlank() ? "Режиссёр не указан" : n.getDirector());
            meta.getStyleClass().add("film-meta");

            Label chip = new Label(n.getStatus());
            chip.getStyleClass().addAll("chip", chipClass(n.getStatus()));

            Label stars = new Label(n.getRating() == 0 ? "" : stars(n.getRating()) + " " + n.getRating());
            stars.getStyleClass().add("stars");

            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            HBox bottom = new HBox(8, chip, spacer, stars);
            bottom.setAlignment(Pos.CENTER_LEFT);

            VBox box = new VBox(3, title, meta, bottom);
            setText(null);
            setGraphic(box);
        }
    }

    private List<Note> demoData() {
        return List.of(
                new Note("Начало", "Кристофер Нолан", "Леонардо ДиКаприо, Джозеф Гордон-Левитт, Эллен Пейдж",
                        9, Note.WATCHED, "Вор, крадущий секреты из снов, получает задание внедрить идею в сознание."),
                new Note("Интерстеллар", "Кристофер Нолан", "Мэттью Макконахи, Энн Хэтэуэй, Джессика Честейн",
                        9, Note.WATCHED, "Экспедиция через червоточину в поисках нового дома для человечества."),
                new Note("Матрица", "Лана и Лилли Вачовски", "Киану Ривз, Лоренс Фишберн, Кэрри-Энн Мосс",
                        9, Note.WATCHED, "Хакер Нео узнаёт, что привычная реальность - компьютерная симуляция."),
                new Note("Дюна: Часть вторая", "Дени Вильнёв", "Тимоти Шаламе, Зендея, Ребекка Фергюсон",
                        8, Note.WATCHING, "Пол Атрейдес объединяется с фременами в борьбе против Харконненов."),
                new Note("Остров проклятых", "Мартин Скорсезе", "Леонардо ДиКаприо, Марк Руффало, Бен Кингсли",
                        0, Note.WANT, "Маршал расследует исчезновение пациентки психиатрической клиники."),
                new Note("Паразиты", "Пон Джун-хо", "Сон Кан-хо, Чхве У-щик, Пак Со-дам",
                        0, Note.WANT, "Чёрная комедия о бедной семье, проникающей в дом богачей."),
                new Note("Бегущий по лезвию 2049", "Дени Вильнёв", "Райан Гослинг, Харрисон Форд, Ана де Армас",
                        0, Note.WANT, "Офицер К. раскрывает тайну, способную разрушить остатки общества."),
                new Note("Зелёная миля", "Фрэнк Дарабонт", "Том Хэнкс, Майкл Кларк Дункан, Дэвид Морс",
                        10, Note.WATCHED, "Надзиратель и необычный заключённый с даром исцеления."),
                new Note("Джокер", "Тодд Филлипс", "Хоакин Феникс, Роберт Де Ниро, Зази Битц",
                        7, Note.WATCHING, "История Артура Флека и его превращения в Джокера."),
                new Note("Кочевник", "Сергей Бодров", "Куно Бекер, Джейсон Скотт Ли, Марк Дакаскос",
                        6, Note.WANT, "Тарихи фильм: қазақ хандығының құрылуы / исторический фильм о казахском ханстве."),
                new Note("Побег из Шоушенка", "Фрэнк Дарабонт", "Тим Роббинс, Морган Фримен, Боб Гантон",
                        10, Note.WATCHED, "Банкир, несправедливо осуждённый за убийство, мечтает о свободе.")
        );
    }
}
