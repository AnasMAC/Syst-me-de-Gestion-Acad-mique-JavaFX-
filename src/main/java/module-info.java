module com.example.gestionacademique {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires static lombok;

    // 1. Open the root package (for MainController)
    opens com.example.gestionacademique to javafx.fxml;

    opens com.example.gestionacademique.modele to javafx.base;
    opens com.example.gestionacademique.Controllers to javafx.fxml;

    exports com.example.gestionacademique;
    exports com.example.gestionacademique.Controllers;
}