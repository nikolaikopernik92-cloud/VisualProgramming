module kz.atu.lab06 {
    requires javafx.controls;
    requires javafx.fxml;


    opens kz.atu.lab06 to javafx.fxml;
    exports kz.atu.lab06;
}