package com.example.gestionacademique.Controllers;

import com.example.gestionacademique.dao.CoursImp;
import com.example.gestionacademique.dao.FormationImp;
import com.example.gestionacademique.dao.StudentImp;
import com.example.gestionacademique.modele.Cours;
import com.example.gestionacademique.modele.Formation;
import com.example.gestionacademique.modele.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class StudentController implements Initializable {

    // --- FXML INJECTIONS (LEFT & CENTER) ---
    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private TextField txtMoyenne;
    @FXML private ComboBox<Formation> cbChoseformation;

    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;

    @FXML private ComboBox<Formation> cbFilterFormation;

    @FXML private TableView<Student> tableStudent;
    @FXML private TableColumn<Student, Integer> colId;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, Double> colMoyenne;

    // --- FXML INJECTIONS (RIGHT PANEL - NEW) ---
    @FXML private VBox vboxAvailableCourses; // Container for CheckBoxes
    @FXML private Button btnInscrire;
    @FXML private Label lblSelectedStudent;

    // Data Lists
    private ObservableList<Student> studentsList = FXCollections.observableArrayList();
    private ObservableList<Formation> formationsList = FXCollections.observableArrayList();

    // DAOs
    private StudentImp studentImp;
    private FormationImp formationImp;
    private CoursImp coursImp; // Needed to fetch courses

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            studentImp = new StudentImp();
            formationImp = new FormationImp();
            coursImp = new CoursImp(); // Initialize Course DAO

            // 1. LINK COLUMNS TO MODEL
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colName.setCellValueFactory(new PropertyValueFactory<>("name"));
            colMoyenne.setCellValueFactory(new PropertyValueFactory<>("moyenne"));

            // Bind Lists
            tableStudent.setItems(studentsList);
            cbFilterFormation.setItems(formationsList);
            cbChoseformation.setItems(formationsList);

            // 2. SELECTION LISTENER
            tableStudent.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    // --- A. Fill Left Form ---
                    txtId.setText(String.valueOf(newSelection.getId()));
                    txtName.setText(newSelection.getName());
                    txtMoyenne.setText(String.valueOf(newSelection.getMoyenne()));

                    int targetFormationId = newSelection.getFormationId();
                    Formation matchingFormation = formationsList.stream()
                            .filter(f -> f.getId() == targetFormationId)
                            .findFirst()
                            .orElse(null);
                    cbChoseformation.setValue(matchingFormation);

                    // --- B. Setup Right Panel (Registration) ---
                    lblSelectedStudent.setText("Étudiant : " + newSelection.getName());
                    lblSelectedStudent.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;"); // Green color

                    // Load CheckBoxes for courses available to this student's formation
                    loadCheckboxesForStudent(targetFormationId,newSelection.getId());

                    // --- C. Enable Buttons ---
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

    // --- NEW: DYNAMIC CHECKBOX LOADER ---
    private void loadCheckboxesForStudent(int formationId, int studentId) {
        vboxAvailableCourses.getChildren().clear(); // Clear previous checkboxes

        try {
            // Fetch courses linked to this formation
            List<Cours> courses = coursImp.getCoursByFormation(formationId);
            List<Integer> coursesForStrudents = studentImp.getCoursforStrudent(studentId);

            if (courses.isEmpty()) {
                Label lblEmpty = new Label("Aucun cours pour cette filière");
                lblEmpty.setStyle("-fx-text-fill: grey; -fx-font-style: italic;");
                vboxAvailableCourses.getChildren().add(lblEmpty);
                btnInscrire.setDisable(true);
            } else {
                for (Cours c : courses) {
                    CheckBox cb = new CheckBox(c.getCode() + " - " + c.getIntitule());
                    cb.setUserData(c); // Store Course object in the checkbox
                    cb.setStyle("-fx-cursor: hand; -fx-padding: 5; -fx-text-fill: #37474F; -fx-font-size: 13px;");
                    if(coursesForStrudents.contains(c.getId())) {
                        cb.setSelected(true);
                    }
                    vboxAvailableCourses.getChildren().add(cb);
                }
                btnInscrire.setDisable(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur Chargement Cours", e.getMessage());
        }
    }

    // --- NEW: REGISTRATION ACTION ---
    @FXML
    void onInscrire() {
        Student selectedStudent = tableStudent.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) return;

        int successCount = 0;
        int duplicateCount = 0;

        // Loop through all checkboxes in the VBox
        for (Node node : vboxAvailableCourses.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox cb = (CheckBox) node;
                if (cb.isSelected()) {
                    Cours c = (Cours) cb.getUserData();
                    try {
                        studentImp.inscrireEtudiantAuCours(selectedStudent.getId(), c.getId());
                        successCount++;
                    } catch (SQLException e) {
                        // Usually means duplicate key (already registered)
                        duplicateCount++;
                    }
                }
            }
        }

        if (successCount > 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Inscription Réussie");
            alert.setHeaderText(null);
            alert.setContentText(selectedStudent.getName() + " a été inscrit à " + successCount + " cours.");
            alert.showAndWait();
        } else if (duplicateCount > 0) {
            showError("Info", "L'étudiant était déjà inscrit aux cours sélectionnés.");
        } else {
            showError("Attention", "Veuillez cocher au moins un cours.");
        }
    }

    // --- CRUD METHODS ---

    @FXML
    void onCreate() {
        try {
            if (txtName.getText().trim().isEmpty() || txtMoyenne.getText().isEmpty() || cbChoseformation.getValue() == null) {
                showError("Validation", "Veuillez remplir tous les champs.");
                return;
            }

            double moyenne = Double.parseDouble(txtMoyenne.getText());
            if (moyenne < 0 || moyenne > 20) {
                showError("Validation", "La moyenne doit être comprise entre 0 et 20.");
                return;
            }

            Student student = new Student(
                    null,
                    txtName.getText(),
                    moyenne,
                    cbChoseformation.getValue().getId()
            );

            studentImp.create(student);
            refreshTable();
            onClear();

        } catch (NumberFormatException e) {
            showError("Format Invalide", "Moyenne invalide.");
        } catch (SQLException e) {
            showError("Base de données", e.getMessage());
        }
    }

    @FXML
    void onUpdate() {
        try {
            if (txtId.getText().isEmpty()) return;

            Student student = new Student(
                    Integer.parseInt(txtId.getText()),
                    txtName.getText(),
                    Double.parseDouble(txtMoyenne.getText()),
                    cbChoseformation.getValue().getId()
            );

            studentImp.update(student);
            refreshTable();
            onClear();

        } catch (Exception e) {
            showError("Erreur Update", e.getMessage());
        }
    }

    @FXML
    void onDelete() {
        try {
            if (txtId.getText().isEmpty()) return;
            studentImp.delete(Integer.parseInt(txtId.getText()));
            refreshTable();
            onClear();
        } catch (Exception e) {
            showError("Erreur Delete", e.getMessage());
        }
    }

    @FXML
    void onClear() {
        // Clear Left
        txtId.clear();
        txtName.clear();
        txtMoyenne.clear();
        cbChoseformation.setValue(null);

        // Clear Right (Registration Panel)
        lblSelectedStudent.setText("Aucun étudiant sélectionné");
        lblSelectedStudent.setStyle("-fx-text-fill: #E91E63; -fx-font-style: italic;");
        vboxAvailableCourses.getChildren().clear();
        btnInscrire.setDisable(true);

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
                List<Student> filtered = studentsList.stream()
                        .filter(s -> s.getFormationId() == targetId)
                        .collect(Collectors.toList());
                tableStudent.setItems(FXCollections.observableArrayList(filtered));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void onResetFilter() {
        cbFilterFormation.setValue(null);
        refreshTable();
    }

    public void refreshTable() {
        try {
            System.out.println("Refreshing Student Table...");
            studentsList.setAll(studentImp.findAll());
            tableStudent.setItems(studentsList);
            formationsList.setAll(formationImp.findAll());
            cbFilterFormation.setItems(formationsList);
            cbChoseformation.setItems(formationsList);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Connexion", e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur " + title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}