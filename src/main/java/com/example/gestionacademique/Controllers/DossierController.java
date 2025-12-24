package com.example.gestionacademique.Controllers;

import com.example.gestionacademique.dao.DossierAdministratifImp;
import com.example.gestionacademique.modele.DossierAdministratif;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Date; // Attention: java.sql.Date pour la compatibilité avec votre Modèle
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class DossierController implements Initializable {

    // --- FXML INJECTIONS ---
    @FXML private TextField txtId;
    @FXML private TextField txtNumero;
    @FXML private DatePicker dpDate; // On utilise un DatePicker pour la date
    @FXML private TextField txtStudentId; // On entre l'ID étudiant manuellement pour l'instant

    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;

    @FXML private TableView<DossierAdministratif> tableDossier;
    @FXML private TableColumn<DossierAdministratif, Integer> colId;
    @FXML private TableColumn<DossierAdministratif, String> colNumero;
    @FXML private TableColumn<DossierAdministratif, Date> colDate;
    @FXML private TableColumn<DossierAdministratif, Integer> colStudentId;

    // Data List
    private ObservableList<DossierAdministratif> dossierList = FXCollections.observableArrayList();

    // DAO
    private DossierAdministratifImp dossierImp;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            dossierImp = new DossierAdministratifImp();

            // 1. LINK COLUMNS TO MODEL
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroInscription"));
            colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
            colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentId"));

            // Bind List
            tableDossier.setItems(dossierList);

            // 2. SELECTION LISTENER
            tableDossier.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    txtId.setText(String.valueOf(newSelection.getId()));
                    txtNumero.setText(newSelection.getNumeroInscription());

                    // Conversion java.sql.Date -> java.time.LocalDate pour le DatePicker
                    if (newSelection.getDateCreation() != null) {
                        dpDate.setValue(newSelection.getDateCreation().toLocalDate());
                    }

                    txtStudentId.setText(String.valueOf(newSelection.getStudentId()));

                    btnUpdate.setDisable(false);
                    btnDelete.setDisable(false);
                    btnAdd.setDisable(true);
                } else {
                    onClear();
                }
            });

            // Initial Load
            refreshTable();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Initialisation", "Erreur critique : " + e.getMessage());
        }
    }

    // --- LOGIC METHODS ---

    @FXML
    void onCreate() {
        try {
            // Validation
            if (txtNumero.getText().isEmpty() || dpDate.getValue() == null || txtStudentId.getText().isEmpty()) {
                showError("Validation", "Tous les champs sont obligatoires.");
                return;
            }

            // Create Object
            // Note: Date.valueOf(LocalDate) convertit la date du DatePicker vers SQL Date
            DossierAdministratif dossier = new DossierAdministratif(
                    0,
                    txtNumero.getText(),
                    Date.valueOf(dpDate.getValue()),
                    Integer.parseInt(txtStudentId.getText())
            );

            dossierImp.create(dossier);
            System.out.println("Dossier created: " + dossier.getNumeroInscription());

            refreshTable();
            onClear();

        } catch (SQLException e) {
            e.printStackTrace();
            // C'est ici qu'on attrape l'erreur si l'étudiant a déjà un dossier (clé UNIQUE)
            showError("Base de données", "Erreur (Cet étudiant a peut-être déjà un dossier) : " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Format", "L'ID Étudiant doit être un nombre.");
        }
    }

    @FXML
    void onUpdate() {
        try {
            if (txtId.getText().isEmpty()) return;

            DossierAdministratif dossier = new DossierAdministratif(
                    Integer.parseInt(txtId.getText()),
                    txtNumero.getText(),
                    Date.valueOf(dpDate.getValue()),
                    Integer.parseInt(txtStudentId.getText())
            );

            dossierImp.update(dossier);
            refreshTable();
            onClear();

        } catch (SQLException e) {
            showError("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    void onDelete() {
        try {
            if (txtId.getText().isEmpty()) return;
            dossierImp.delete(Integer.parseInt(txtId.getText()));
            refreshTable();
            onClear();
        } catch (SQLException e) {
            showError("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    void onClear() {
        txtId.clear();
        txtNumero.clear();
        dpDate.setValue(null);
        txtStudentId.clear();

        tableDossier.getSelectionModel().clearSelection();

        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnAdd.setDisable(false);
    }

    public void refreshTable() {
        try {
            System.out.println("Refreshing Dossier Table...");
            dossierList.setAll(dossierImp.findAll());
            tableDossier.setItems(dossierList);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Connexion", "Impossible de charger les dossiers : " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}