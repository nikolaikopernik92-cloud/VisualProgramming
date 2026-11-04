package kz.atu.lab08;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

public class RecipeDialogController {
    @FXML private TextField txtTitle;
    @FXML private TextArea txtIngredients;
    @FXML private TextArea txtSteps;

    private Recipe result;   // null, пока пользователь не нажал «Сохранить»

    /** Вызывается из главного контроллера после FXMLLoader.load(). */
    public void setRecipe(Recipe recipe) {
        if (recipe != null) {
            txtTitle.setText(recipe.getTitle());
            txtIngredients.setText(recipe.getIngredients());
            txtSteps.setText(recipe.getSteps());
        }
    }

    public Optional<Recipe> getResult() {
        return Optional.ofNullable(result);
    }

    @FXML
    private void onApply() {
        String title = txtTitle.getText().trim();
        if (title.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Введите название рецепта.");
            a.initOwner(txtTitle.getScene().getWindow());
            a.setHeaderText(null);
            a.showAndWait();
            return;
        }
        result = new Recipe(title, txtIngredients.getText().strip(), txtSteps.getText().strip());
        close();
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        ((Stage) txtTitle.getScene().getWindow()).close();
    }
}
