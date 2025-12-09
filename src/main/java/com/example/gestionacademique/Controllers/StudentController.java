package com.example.gestionacademique.Controllers;

import com.example.gestionacademique.dao.FormationImp;
import com.example.gestionacademique.dao.StudentImp;
import com.example.gestionacademique.modele.Formation;
import com.example.gestionacademique.modele.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class StudentController implements Initializable {

    // --- FXML INJECTIONS ---
    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private TextField txtMoyenne;
    @FXML private TextField txtFormationId;

    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;

    @FXML private ComboBox<Formation> cbFilterFormation;

    @FXML private TableView<Student> tableStudent;
    @FXML private TableColumn<Student, Integer> colId;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, Double> colMoyenne;

    // Data Lists
    private ObservableList<Student> studentsList = FXCollections.observableArrayList();
    private ObservableList<Formation> formationsList = FXCollections.observableArrayList();

    // Optimisation: DAOs instantiated once
    private StudentImp studentImp ;
    private FormationImp formationImp ;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            studentImp = new StudentImp();
            formationImp = new FormationImp();
            // 1. LINK COLUMNS TO MODEL
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colName.setCellValueFactory(new PropertyValueFactory<>("name"));
            colMoyenne.setCellValueFactory(new PropertyValueFactory<>("moyenne"));

            // Bind Lists
            tableStudent.setItems(studentsList);
            cbFilterFormation.setItems(formationsList);

            // 2. SELECTION LISTENER
            tableStudent.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    txtId.setText(String.valueOf(newSelection.getId()));
                    txtName.setText(newSelection.getName());
                    txtMoyenne.setText(String.valueOf(newSelection.getMoyenne()));
                    txtFormationId.setText(String.valueOf(newSelection.getFormationId()));

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
            showError("Initialisation", "Erreur critique au démarrage : " + e.getMessage());
        }
    }

    // --- LOGIC METHODS ---

    @FXML
    void onCreate() {
        try {
            // 1. Validation: Empty Fields
            if (txtName.getText().trim().isEmpty() || txtMoyenne.getText().isEmpty() || txtFormationId.getText().isEmpty()) {
                showError("Validation", "Veuillez remplir tous les champs (Nom, Moyenne, ID Formation).");
                return;
            }

            // 2. Validation: Logic Checks
            double moyenne = Double.parseDouble(txtMoyenne.getText());
            if (moyenne < 0 || moyenne > 20) {
                showError("Validation", "La moyenne doit être comprise entre 0 et 20.");
                return;
            }

            // 3. Create
            Student student = new Student(
                    null,
                    txtName.getText(),
                    moyenne,
                    Integer.parseInt(txtFormationId.getText())
            );

            studentImp.create(student);
            System.out.println("Student created: " + student.getName());

            refreshTable();
            onClear();

        } catch (NumberFormatException e) {
            showError("Format Invalide", "La moyenne et l'ID Formation doivent être des nombres valides.");
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Base de données", "Erreur lors de l'ajout (Vérifiez l'ID formation) : " + e.getMessage());
        }
    }

    @FXML
    void onUpdate() {
        try {
            // 1. Validation
            if (txtId.getText().isEmpty()) {
                showError("Selection", "Aucun étudiant sélectionné.");
                return;
            }
            if (txtName.getText().trim().isEmpty() || txtMoyenne.getText().isEmpty()) {
                showError("Validation", "Le nom et la moyenne ne peuvent pas être vides.");
                return;
            }

            double moyenne = Double.parseDouble(txtMoyenne.getText());
            if (moyenne < 0 || moyenne > 20) {
                showError("Validation", "La moyenne doit être comprise entre 0 et 20.");
                return;
            }

            // 2. Update
            Student student = new Student(
                    Integer.parseInt(txtId.getText()),
                    txtName.getText(),
                    moyenne,
                    Integer.parseInt(txtFormationId.getText())
            );

            studentImp.update(student);
            System.out.println("Student updated ID: " + student.getId());

            refreshTable();
            onClear();

        } catch (NumberFormatException e) {
            showError("Format Invalide", "Vérifiez que la moyenne et l'ID sont des nombres.");
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Modification", "Erreur SQL : " + e.getMessage());
        }
    }

    @FXML
    void onDelete() {
        try {
            if (txtId.getText().isEmpty()) {
                showError("Validation", "Aucun étudiant sélectionné pour la suppression.");
                return;
            }

            studentImp.delete(Integer.parseInt(txtId.getText()));
            System.out.println("Student deleted ID: " + txtId.getText());

            refreshTable();
            onClear();

        } catch (NumberFormatException e) {
            showError("Erreur", "ID invalide.");
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Suppression", "Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @FXML
    void onClear() {
        txtId.clear();
        txtName.clear();
        txtMoyenne.clear();
        txtFormationId.clear();

        tableStudent.getSelectionModel().clearSelection();

        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnAdd.setDisable(false);
    }

    @FXML
    void onFilter() {
        if (cbFilterFormation.getValue() != null) {
            try {
                int targetId = cbFilterFormation.getValue().getId();

                // Filter logic
                List<Student> filtered = studentsList.stream()
                        .filter(s -> s.getFormationId() == targetId)
                        .collect(Collectors.toList());

                // Update UI without clearing the main list
                tableStudent.setItems(FXCollections.observableArrayList(filtered));

            } catch (Exception e) {
                e.printStackTrace();
                showError("Filtre", "Erreur lors du filtrage.");
            }
        }
    }

    @FXML
    void onResetFilter() {
        cbFilterFormation.setValue(null);
        // Restore full list
        refreshTable();
    }

    // Changed to PUBLIC so MainController can access it
    public void refreshTable() {
        try {
            System.out.println("Refreshing Student Table...");

            // Reload Students
            studentsList.setAll(studentImp.findAll());
            tableStudent.setItems(studentsList); // Reset items in case filter was active

            // Reload Formations (in case a new one was added in the other tab)
            formationsList.setAll(formationImp.findAll());
            cbFilterFormation.setItems(formationsList);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Connexion", "Impossible de charger les données : " + e.getMessage());
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