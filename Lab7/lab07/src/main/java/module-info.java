module kz.atu.lab07 {
    requires javafx.controls;
    requires javafx.fxml;

    opens kz.atu.lab07 to javafx.fxml;
    exports kz.atu.lab07;
}