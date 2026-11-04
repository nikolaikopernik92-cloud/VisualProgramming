package kz.atu.lab08;

import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Optional;

public class ColorDialogController {
    @FXML private ColorPicker colorPicker;
    private Color selectedColor;

    public void setInitialColor(Color color) {
        colorPicker.setValue(color);
    }

    public Optional<Color> getSelectedColor() {
        return Optional.ofNullable(selectedColor);
    }

    @FXML
    private void onApply() {
        selectedColor = colorPicker.getValue();
        close();
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        ((Stage) colorPicker.getScene().getWindow()).close();
    }
}
