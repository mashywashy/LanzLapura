module org.yohi.lapura_evaluation_system {
    // JavaFX requirements
    requires javafx.controls;
    requires javafx.fxml;

    // HTTP client and SQL
    requires java.net.http;
    requires java.sql;
    requires org.json;




    // Open package for FXML controller access
    opens org.yohi.lapura_evaluation_system to javafx.fxml;

    // Export main package
    exports org.yohi.lapura_evaluation_system;
}