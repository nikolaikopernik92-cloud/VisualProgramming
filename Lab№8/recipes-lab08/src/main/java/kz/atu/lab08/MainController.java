package kz.atu.lab08;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainController {
    @FXML private ListView<Recipe> lvRecipes;
    @FXML private TextArea txtNotes;   // область просмотра выбранного рецепта
    @FXML private Label lblStatus;

    private final ObservableList<Recipe> recipes = FXCollections.observableArrayList();
    private Color currentColor = Color.BLACK;
    private File currentFile;          // путь известен -> "Сохранить" без выбора файла

    @FXML
    private void initialize() {
        // Примеры данных (без персональной информации)
        recipes.add(new Recipe("Омлет",
                "Яйца - 3 шт.\nМолоко - 50 мл\nСоль - щепотка\nМасло - 1 ст. л.",
                "1. Взбейте яйца с молоком и солью.\n2. Разогрейте масло на сковороде.\n3. Жарьте под крышкой 5 минут."));
        recipes.add(new Recipe("Салат овощной",
                "Огурцы - 2 шт.\nПомидоры - 2 шт.\nМасло - 2 ст. л.\nСоль - по вкусу",
                "1. Нарежьте овощи.\n2. Добавьте соль и масло.\n3. Перемешайте."));
        lvRecipes.setItems(recipes);
        lvRecipes.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, r) -> showDetails(r));
        lvRecipes.getSelectionModel().selectFirst();
        lblStatus.setText("Готово");
    }

    private void showDetails(Recipe r) {
        if (r == null) {
            txtNotes.clear();
            return;
        }
        txtNotes.setText(r.getTitle() + "\n\nИнгредиенты:\n" + r.getIngredients()
                + "\n\nПриготовление:\n" + r.getSteps());
    }

    // ---------- Дополнительное окно: редактирование записи ----------

    /** Открывает модальное окно; возвращает результат или empty при отмене. */
    private Optional<Recipe> openRecipeDialog(Recipe source) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("recipe-dialog.fxml"));
        Parent root = loader.load();
        RecipeDialogController controller = loader.getController();
        controller.setRecipe(source);               // передача данных в диалог

        Stage dialog = new Stage();
        dialog.initOwner(txtNotes.getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(source == null ? "Новый рецепт" : "Редактирование рецепта");
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
        return controller.getResult();              // результат обратно в главное окно
    }

    @FXML
    private void onAdd() {
        try {
            openRecipeDialog(null).ifPresent(r -> {
                recipes.add(r);
                lvRecipes.getSelectionModel().select(r);
                lblStatus.setText("Добавлен: " + r.getTitle());
            });
        } catch (IOException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void onEdit() {
        int idx = lvRecipes.getSelectionModel().getSelectedIndex();
        if (idx < 0) {
            lblStatus.setText("Выберите рецепт для редактирования");
            return;
        }
        try {
            openRecipeDialog(recipes.get(idx)).ifPresent(r -> {
                recipes.set(idx, r);
                lvRecipes.getSelectionModel().select(idx);
                showDetails(r);
                lblStatus.setText("Изменён: " + r.getTitle());
            });
        } catch (IOException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        int idx = lvRecipes.getSelectionModel().getSelectedIndex();
        if (idx < 0) return;
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Удалить рецепт «"
                + recipes.get(idx).getTitle() + "»?");
        a.setHeaderText(null);
        a.initOwner(txtNotes.getScene().getWindow());
        if (a.showAndWait().orElse(null) == javafx.scene.control.ButtonType.OK) {
            recipes.remove(idx);
            lblStatus.setText("Рецепт удалён");
        }
    }

    // ---------- Дополнительное окно: настройки (цвет текста) ----------

    @FXML
    private void onChooseColor() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("color-dialog.fxml"));
        Parent root = loader.load();
        ColorDialogController controller = loader.getController();
        controller.setInitialColor(currentColor);

        Stage dialog = new Stage();
        dialog.initOwner(txtNotes.getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Цвет текста");
        dialog.setScene(new Scene(root));
        dialog.showAndWait();

        controller.getSelectedColor().ifPresent(color -> {
            currentColor = color;
            txtNotes.setStyle("-fx-text-fill: " + toCss(color) + ";");
            lvRecipes.setStyle("-fx-text-fill: " + toCss(color) + ";");
            lblStatus.setText("Цвет текста изменён");
        });
    }

    private String toCss(Color c) {
        return String.format("#%02X%02X%02X",
                Math.round(c.getRed() * 255), Math.round(c.getGreen() * 255),
                Math.round(c.getBlue() * 255));
    }

    // ---------- Файлы ----------

    @FXML
    private void onOpen() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Открыть список рецептов");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));
        File file = chooser.showOpenDialog(txtNotes.getScene().getWindow());
        if (file == null) return;                   // отмена - ничего не меняем
        try {
            String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            List<Recipe> loaded = parse(text);
            if (loaded.isEmpty()) {
                showError("Файл не содержит рецептов в ожидаемом формате.");
                return;
            }
            recipes.setAll(loaded);
            currentFile = file;
            lvRecipes.getSelectionModel().selectFirst();
            lblStatus.setText("Открыт: " + file.getName());
        } catch (IOException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void onSave() {
        if (currentFile == null) {
            onSaveAs();
        } else {
            writeTo(currentFile);
        }
    }

    @FXML
    private void onSaveAs() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить список рецептов");
        chooser.setInitialFileName("recipes.txt");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));
        File file = chooser.showSaveDialog(txtNotes.getScene().getWindow());
        if (file == null) return;
        writeTo(file);
    }

    private void writeTo(File file) {
        try {
            Files.writeString(file.toPath(), serialize(), StandardCharsets.UTF_8);
            currentFile = file;
            lblStatus.setText("Сохранён: " + file.getName());
        } catch (IOException ex) {
            showError(ex.getMessage());
        }
    }

    /** Формат файла: блоки "=== Название", "[Ингредиенты]", "[Приготовление]". */
    private String serialize() {
        StringBuilder sb = new StringBuilder();
        for (Recipe r : recipes) {
            sb.append("=== ").append(r.getTitle()).append("\n")
              .append("[Ингредиенты]\n").append(r.getIngredients()).append("\n")
              .append("[Приготовление]\n").append(r.getSteps()).append("\n\n");
        }
        return sb.toString();
    }

    private List<Recipe> parse(String text) {
        List<Recipe> list = new ArrayList<>();
        String title = null;
        StringBuilder ing = new StringBuilder();
        StringBuilder steps = new StringBuilder();
        StringBuilder cur = null;
        for (String line : text.split("\\R", -1)) {
            if (line.startsWith("=== ")) {
                addRecipe(list, title, ing, steps);
                title = line.substring(4).trim();
                ing.setLength(0);
                steps.setLength(0);
                cur = null;
            } else if (line.equals("[Ингредиенты]")) {
                cur = ing;
            } else if (line.equals("[Приготовление]")) {
                cur = steps;
            } else if (cur != null) {
                cur.append(line).append("\n");
            }
        }
        addRecipe(list, title, ing, steps);
        return list;
    }

    private void addRecipe(List<Recipe> list, String title, StringBuilder ing, StringBuilder steps) {
        if (title != null && !title.isEmpty()) {
            list.add(new Recipe(title, ing.toString().strip(), steps.toString().strip()));
        }
    }

    // ---------- Прочее ----------

    @FXML
    private void onAbout() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.initOwner(txtNotes.getScene().getWindow());
        a.setTitle("О программе");
        a.setHeaderText("Список рецептов");
        a.setContentText("Лабораторная работа №8, вариант 12.\nJavaFX, FXML, FileChooser, ColorPicker.");
        a.showAndWait();
    }

    @FXML
    private void onExit() {
        Platform.exit();
    }

    private void showError(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Ошибка ввода-вывода");
        a.setHeaderText("Не удалось выполнить операцию");
        a.setContentText(message);
        a.showAndWait();
    }
}
