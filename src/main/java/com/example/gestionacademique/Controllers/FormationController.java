package com.example.gestionacademique.Controllers;

import com.example.gestionacademique.dao.FormationImp;
import com.example.gestionacademique.modele.Formation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class FormationController implements Initializable {
    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;

    @FXML private TableView<Formation> tableFormation;
    @FXML private TableColumn<Formation, Integer> colId;
    @FXML private TableColumn<Formation, String> colName;

    // Optimize: Create DAO once
    private FormationImp formationImp ;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            formationImp = new FormationImp();
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colName.setCellValueFactory(new PropertyValueFactory<>("name"));

            // Initial Load
            refreshTable();

            tableFormation.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((observableValue, oldValue, newValue) -> {
                        if (newValue != null) {
                            txtId.setText(String.valueOf(newValue.getId()));
                            txtName.setText(newValue.getName());

                            btnUpdate.setDisable(false);
                            btnDelete.setDisable(false);
                            btnAdd.setDisable(true);
                        } else {
                            onClear();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            showError("Initialisation", "Impossible de charger les données : " + e.getMessage());
        }
    }

    @FXML
    void onClear() {
        txtId.clear();
        txtName.clear();
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnAdd.setDisable(false);
        tableFormation.getSelectionModel().clearSelection();
    }

    // Must be public so MainController can call it
    public void refreshTable() {
        try {
            tableFormation.setItems(FXCollections.observableArrayList(formationImp.findAll()));
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Rafraîchissement", "Erreur lors du chargement de la liste : " + e.getMessage());
        }
    }

    @FXML
    void onCreate() {
        try {
            // 1. Validation
            if (txtName.getText().trim().isEmpty()) {
                showError("Validation", "Le nom de la formation ne peut pas être vide.");
                return;
            }

            // 2. Logic
            Formation formation = Formation.builder().name(txtName.getText()).build();
            formationImp.create(formation);
            System.out.println("Formation created: " + formation);

            refreshTable();
            onClear();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Ajout", "Erreur lors de la création : " + e.getMessage());
        }
    }

    @FXML
    void onUpdate() {
        try {
            // 1. Validation
            if (txtId.getText().isEmpty() || txtName.getText().trim().isEmpty()) {
                showError("Validation", "Veuillez sélectionner une formation et entrer un nom.");
                return;
            }

            // 2. Logic
            Formation formation = Formation.builder()
                    .id(Integer.parseInt(txtId.getText()))
                    .name(txtName.getText())
                    .build();

            formationImp.update(formation);
            System.out.println("Formation updated: " + formation);

            refreshTable();
            onClear();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Modification", "Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @FXML
    void onDelete() {
        try {
            // 1. Validation
            if (txtId.getText().isEmpty()) {
                showError("Validation", "Aucune formation sélectionnée.");
                return;
            }

            // Optional: Confirmation Dialog could go here

            // 2. Logic
            formationImp.delete(Integer.parseInt(txtId.getText()));
            System.out.println("Formation deleted ID: " + txtId.getText());

            refreshTable();
            onClear();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Suppression", "Erreur lors de la suppression (Vérifiez si des étudiants sont inscrits) : " + e.getMessage());
        }
    }

    // --- HELPER METHOD ---
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur " + title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}