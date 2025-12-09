package com.example.gestionacademique.Controllers;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private TabPane tabPane;
    @FXML private StudentController studentViewController;
    @FXML private FormationController formationViewController;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // DEBUG CHECK:
        if (studentViewController == null) {
            System.err.println("CRITICAL ERROR: studentViewController is NULL! Check MainLayout.fxml fx:id");
        }

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                String tabName = newTab.getText().trim();

                if (tabName.equals("Gestion Étudiants")) {
                    System.out.println("Refreshing Student Table...");
                    // If this is null, the code stops here and throws an error in your console
                    if (studentViewController != null) {
                        studentViewController.refreshTable();
                    }
                }
                else if (tabName.equals("Gestion Formations")) {
                    if (formationViewController != null) {
                        formationViewController.refreshTable();
                    }
                }
            }
        });
    }
}