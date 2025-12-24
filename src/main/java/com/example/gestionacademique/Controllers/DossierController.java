package com.example.gestionacademique.Controllers;

import com.example.gestionacademique.dao.DossierAdministratifImp;
import com.example.gestionacademique.dao.StudentImp;
import com.example.gestionacademique.modele.DossierAdministratif;
import com.example.gestionacademique.modele.Student;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.sql.Date; // Attention: java.sql.Date pour la compatibilité avec votre Modèle
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DossierController implements Initializable {


    // --- FXML INJECTIONS ---
    @FXML private TextField txtId;
    @FXML private TextField txtNumero;
    @FXML private DatePicker dpDate; // On utilise un DatePicker pour la date
    @FXML private TextField txtStudentId;


    @FXML private TextField txtSearch;
// On entre l'ID étudiant manuellement pour l'instant

    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;

    @FXML private TableView<DossierAdministratif> tableDossier;
    @FXML private TableColumn<DossierAdministratif, Integer> colId;
    @FXML private TableColumn<DossierAdministratif, String> colNumero;
    @FXML private TableColumn<DossierAdministratif, Date> colDate;
    @FXML private TableColumn<DossierAdministratif,String> colStudentName;


    // Data List
    private ObservableList<DossierAdministratif> dossierList = FXCollections.observableArrayList();
    private ObservableList<Student> studentList = FXCollections.observableArrayList();
    // DAO
    private DossierAdministratifImp dossierImp;
    private StudentImp studentImp;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            dossierImp = new DossierAdministratifImp();
            studentImp = new StudentImp();

            // 1. LINK COLUMNS TO MODEL
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroInscription"));
            colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
            colStudentName.setCellValueFactory(cellData -> {
                int studentId = cellData.getValue().getStudentId();
                String studentName = studentList.stream()
                        .filter(s -> s.getId() == studentId)
                        .findFirst()
                        .map(Student::getName)
                        .orElse("ID: " + studentId);
                return new SimpleStringProperty(studentName);
            });

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
        txtSearch.clear();

        tableDossier.setItems(dossierList);
        tableDossier.getSelectionModel().clearSelection();

        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnAdd.setDisable(false);
    }
    public void refreshTable() {
        try {
            studentList.setAll(studentImp.findAll()); // Reload students for names
            dossierList.setAll(dossierImp.findAll()); // Reload dossiers
            tableDossier.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void onSearch() {
        String text = txtSearch.getText().toLowerCase().trim();

        // 1. Si la recherche est vide, on remet tout
        if (text.isEmpty()) {
            tableDossier.setItems(dossierList);
            return;
        }

        // 2. Analyse de la requête (Parsing)
        String type = "";
        String value = text;

        if (text.contains(":")) {
            // On sépare seulement si ":" existe
            String[] parts = text.split(":", 2); // Le '2' limite la coupe en 2 morceaux max
            type = parts[0].trim();
            if (parts.length > 1) {
                value = parts[1].trim();
            }
        }

        final String searchType = type; // Variable effective final pour le stream
        final String searchValue = value;

        List<DossierAdministratif> filtered = dossierList.stream()
                .filter(dossier -> {
                    // Récupérer le nom de l'étudiant associé
                    String studentName = studentList.stream()
                            .filter(s -> s.getId() == dossier.getStudentId())
                            .findFirst()
                            .map(Student::getName)
                            .orElse("")
                            .toLowerCase();

                    String matricule = dossier.getNumeroInscription().toLowerCase();

                    // 3. Logique de filtrage
                    if (searchType.equals("name") || searchType.equals("nom")) {
                        return studentName.contains(searchValue);
                    } else if (searchType.equals("mat") || searchType.equals("matricule")) {
                        return matricule.contains(searchValue);
                    } else {
                        // RECHERCHE GLOBALE (Si pas de type précisé ou type inconnu)
                        // On cherche dans le nom OU le matricule
                        return studentName.contains(searchValue) || matricule.contains(searchValue);
                    }
                })
                .collect(Collectors.toList());

        tableDossier.setItems(FXCollections.observableArrayList(filtered));
    }
}