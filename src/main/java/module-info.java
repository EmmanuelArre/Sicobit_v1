module utng.gtid234.jeae {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires java.xml;
    requires mysql.connector.j;
    opens utng.gtid234.jeae to javafx.fxml;
    opens utng.gtid234.jeae.modelo to javafx.base;
    exports utng.gtid234.jeae;
    exports utng.gtid234.jeae.modelo;


}