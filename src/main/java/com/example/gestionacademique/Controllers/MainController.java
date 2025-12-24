package com.example.gestionacademique.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private TabPane tabPane;

    // INJECTION DES CONTROLEURS INCLUS
    // JavaFX injecte automatiquement les contrôleurs si on nomme la variable [id]Controller
    @FXML private StudentController studentViewController;
    @FXML private FormationController formationViewController;
    @FXML private CoursController coursViewController;
    @FXML private DossierController dossierViewController; // Assurez-vous d'avoir créé ce contrôleur

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Listener pour détecter le changement d'onglet
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                String tabTitle = newTab.getText().trim();
                System.out.println("Changement d'onglet vers : " + tabTitle);

                // Rafraîchir les données en fonction de l'onglet sélectionné
                switch (tabTitle) {
                    case "Gestion Étudiants":
                        if (studentViewController != null) studentViewController.refreshTable();
                        break;
                    case "Gestion Formations":
                        // formationViewController.refreshTable(); // Si cette méthode existe
                        break;
                    case "Gestion Cours":
                        if (coursViewController != null) coursViewController.refreshTable();
                        break;
                    case "Dossiers Administratifs":
                        if (dossierViewController != null);
                        break;
                }
            }
        });
    }
}