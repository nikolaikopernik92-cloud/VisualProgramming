package kz.atu.lab07;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;

public class EditorController {

    /** Команды, которые попадают в меню «Недавние команды». «Выход» не записывается. */
    private enum Command {
        NEW("Новый"), OPEN("Открыть"), SAVE("Сохранить"), SAVE_AS("Сохранить как"),
        UNDO("Отменить"), REDO("Повторить"),
        CUT("Вырезать"), COPY("Копировать"), PASTE("Вставить"),
        SELECT_ALL("Выделить всё"), ABOUT("О программе");

        private final String title;

        Command(String title) {
            this.title = title;
        }
    }

    private static final int MAX_RECENT = 5;
    private static final String APP_TITLE = "Редактор заметок";

    @FXML private TextArea txtEditor;
    @FXML private Label lblStatus;
    @FXML private Label lblInfo;
    @FXML private MenuItem miNew;
    @FXML private MenuItem miOpen;
    @FXML private MenuItem miSave;
    @FXML private MenuItem miSaveAs;
    @FXML private MenuItem miUndo;
    @FXML private MenuItem miRedo;
    @FXML private MenuItem miCut;
    @FXML private MenuItem miCopy;
    @FXML private MenuItem miPaste;
    @FXML private MenuItem miSelectAll;
    @FXML private Menu menuRecent;

    private Path currentFile;
    private boolean modified;

    private final Deque<Command> recent = new ArrayDeque<>();
    private final Map<Command, Runnable> actions = new EnumMap<>(Command.class);

    @FXML
    public void initialize() {
        // Единственное место, где команда связывается с реализацией
        actions.put(Command.NEW, this::doNew);
        actions.put(Command.OPEN, this::doOpen);
        actions.put(Command.SAVE, this::doSave);
        actions.put(Command.SAVE_AS, this::doSaveAs);
        actions.put(Command.UNDO, () -> txtEditor.undo());
        actions.put(Command.REDO, () -> txtEditor.redo());
        actions.put(Command.CUT, () -> txtEditor.cut());
        actions.put(Command.COPY, () -> txtEditor.copy());
        actions.put(Command.PASTE, () -> txtEditor.paste());
        actions.put(Command.SELECT_ALL, () -> txtEditor.selectAll());
        actions.put(Command.ABOUT, this::doAbout);

        miNew.setAccelerator(KeyCombination.keyCombination("Shortcut+N"));
        miOpen.setAccelerator(KeyCombination.keyCombination("Shortcut+O"));
        miSave.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));
        miSaveAs.setAccelerator(KeyCombination.keyCombination("Shortcut+Shift+S"));
        miUndo.setAccelerator(KeyCombination.keyCombination("Shortcut+Z"));
        miRedo.setAccelerator(KeyCombination.keyCombination("Shortcut+Y"));
        miCut.setAccelerator(KeyCombination.keyCombination("Shortcut+X"));
        miCopy.setAccelerator(KeyCombination.keyCombination("Shortcut+C"));
        miPaste.setAccelerator(KeyCombination.keyCombination("Shortcut+V"));
        miSelectAll.setAccelerator(KeyCombination.keyCombination("Shortcut+A"));

        txtEditor.textProperty().addListener((obs, oldText, newText) -> {
            modified = true;
            lblStatus.setText("Символов: " + newText.length());
            refreshInfo();
        });

        rebuildRecentMenu();

        // Окно появляется после загрузки FXML, поэтому подписку на закрытие делаем позже
        Platform.runLater(() -> {
            Window window = txtEditor.getScene().getWindow();
            if (window instanceof Stage stage) {
                stage.setOnCloseRequest(e -> {
                    if (!confirmDiscard()) {
                        e.consume();
                    }
                });
            }
            refreshInfo();
        });
    }

    // ---------- Обработчики из FXML (меню, панель, контекстное меню) ----------

    @FXML private void onNew()       { execute(Command.NEW); }
    @FXML private void onOpen()      { execute(Command.OPEN); }
    @FXML private void onSave()      { execute(Command.SAVE); }
    @FXML private void onSaveAs()    { execute(Command.SAVE_AS); }
    @FXML private void onUndo()      { execute(Command.UNDO); }
    @FXML private void onRedo()      { execute(Command.REDO); }
    @FXML private void onCut()       { execute(Command.CUT); }
    @FXML private void onCopy()      { execute(Command.COPY); }
    @FXML private void onPaste()     { execute(Command.PASTE); }
    @FXML private void onSelectAll() { execute(Command.SELECT_ALL); }
    @FXML private void onAbout()     { execute(Command.ABOUT); }

    @FXML
    private void onExit() {
        if (confirmDiscard()) {
            Platform.exit();
        }
    }

    // ---------- Вариант 12: история команд ----------

    /** Выполняет команду и запоминает её. Единая точка входа для меню, панели и «Недавних». */
    private void execute(Command command) {
        actions.get(command).run();
        remember(command);
    }

    private void remember(Command command) {
        recent.remove(command);       // без дубликатов
        recent.addFirst(command);     // последняя выполненная - сверху
        while (recent.size() > MAX_RECENT) {
            recent.removeLast();
        }
        rebuildRecentMenu();
    }

    private void rebuildRecentMenu() {
        menuRecent.getItems().clear();
        if (recent.isEmpty()) {
            MenuItem empty = new MenuItem("(пусто)");
            empty.setDisable(true);
            menuRecent.getItems().add(empty);
            return;
        }
        for (Command command : recent) {
            MenuItem item = new MenuItem(command.title);
            item.setOnAction(e -> execute(command));
            menuRecent.getItems().add(item);
        }
        MenuItem clear = new MenuItem("Очистить список");
        clear.setOnAction(e -> {
            recent.clear();
            rebuildRecentMenu();
        });
        menuRecent.getItems().addAll(new SeparatorMenuItem(), clear);
    }

    // ---------- Реализация команд ----------

    private void doNew() {
        if (!confirmDiscard()) return;
        txtEditor.clear();
        currentFile = null;
        setModified(false);
        lblStatus.setText("Новый документ");
    }

    private void doOpen() {
        if (!confirmDiscard()) return;
        FileChooser chooser = createChooser();
        File file = chooser.showOpenDialog(txtEditor.getScene().getWindow());
        if (file == null) return;
        try {
            txtEditor.setText(Files.readString(file.toPath(), StandardCharsets.UTF_8));
            currentFile = file.toPath();
            setModified(false);
            lblStatus.setText("Открыт: " + file.getName());
        } catch (IOException ex) {
            showError("Ошибка открытия: " + ex.getMessage());
        }
    }

    private void doSave() {
        if (currentFile == null && !chooseSaveFile()) return;
        writeCurrentFile();
    }

    private void doSaveAs() {
        if (!chooseSaveFile()) return;
        writeCurrentFile();
    }

    private void doAbout() {
        Alert a = new Alert(Alert.AlertType.INFORMATION,
                "Редактор заметок. Лабораторная работа №7.", ButtonType.OK);
        a.setHeaderText("О программе");
        a.showAndWait();
    }

    // ---------- Вспомогательные методы ----------

    private FileChooser createChooser() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));
        return chooser;
    }

    /** Показывает диалог сохранения. Возвращает false, если пользователь отменил выбор. */
    private boolean chooseSaveFile() {
        File file = createChooser().showSaveDialog(txtEditor.getScene().getWindow());
        if (file == null) return false;
        currentFile = file.toPath();
        return true;
    }

    private void writeCurrentFile() {
        try {
            Files.writeString(currentFile, txtEditor.getText(), StandardCharsets.UTF_8);
            setModified(false);
            lblStatus.setText("Сохранено: " + currentFile.getFileName());
        } catch (IOException ex) {
            showError("Ошибка сохранения: " + ex.getMessage());
        }
    }

    private void setModified(boolean value) {
        modified = value;
        refreshInfo();
    }

    /** Обновляет правую часть строки состояния и заголовок окна. */
    private void refreshInfo() {
        String text = txtEditor.getText();
        int lines = text.isEmpty() ? 1 : text.split("\n", -1).length;
        int words = text.isBlank() ? 0 : text.trim().split("\\s+").length;
        lblInfo.setText("Строк: " + lines + " | Слов: " + words + " | "
                + (modified ? "Изменён" : "Сохранён"));

        if (txtEditor.getScene() != null && txtEditor.getScene().getWindow() instanceof Stage stage) {
            String name = currentFile == null ? "Без имени" : currentFile.getFileName().toString();
            stage.setTitle(name + (modified ? " *" : "") + " — " + APP_TITLE);
        }
    }

    private boolean confirmDiscard() {
        if (!modified) return true;
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Есть несохранённые изменения. Продолжить без сохранения?",
                ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText("Несохранённые изменения");
        return a.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    private void showError(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        a.setHeaderText("Ошибка");
        a.showAndWait();
    }
}

