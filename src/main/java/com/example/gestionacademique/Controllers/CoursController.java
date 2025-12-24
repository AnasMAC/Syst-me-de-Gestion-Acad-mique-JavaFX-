package com.example.gestionacademique.Controllers;

import com.example.gestionacademique.dao.CoursImp;
import com.example.gestionacademique.dao.FormationImp;
import com.example.gestionacademique.modele.Cours;
import com.example.gestionacademique.modele.Formation;
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
public class CoursController implements Initializable {
    @FXML private TextField txtId;
    @FXML private TextField txtCode;
    @FXML private TextField txtIntitule;

    // Le conteneur FXML où on va mettre les RadioButtons
    @FXML private VBox vboxRadios;

    // Le ComboBox pour filtrer la table
    @FXML private ComboBox<Formation> cbFilter;

    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;

    @FXML private TableView<Cours> tableCours;
    @FXML private TableColumn<Cours, Integer> colId;
    @FXML private TableColumn<Cours, String> colCode;
    @FXML private TableColumn<Cours, String> colIntitule;

    private ObservableList<Cours> coursList = FXCollections.observableArrayList();
    private ObservableList<Formation> formationList = FXCollections.observableArrayList();

    private CoursImp coursImp;
    private FormationImp formationImp;




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)  {
        try{
            coursImp= new CoursImp();
            formationImp=new FormationImp();

            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
            colIntitule.setCellValueFactory(new PropertyValueFactory<>("intitule"));

            refreshTable();




            tableCours.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
                if(newValue!=null){
                    txtId.setText(String.valueOf(newValue.getId()));
                    txtCode.setText(newValue.getCode());
                    txtIntitule.setText(newValue.getIntitule());
                    setCheckboxStatus(newValue.getId());

                    btnAdd.setDisable(true);
                    btnUpdate.setDisable(false);
                    btnDelete.setDisable(false);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createCheckboxes() {
        vboxRadios.getChildren().clear();

        for (Formation f : formationList) {
            CheckBox cb = new CheckBox(f.getName()); // <--- CheckBox
            cb.setUserData(f);
            // No ToggleGroup here!
            vboxRadios.getChildren().add(cb);
        }
    }
    private void setCheckboxStatus(int coursId) {
        // First, uncheck everything
        onClearCheckboxes();

        try {
            // Get list of formations associated with this course from DB
            List<Formation> associatedFormations = formationImp.getFormationbyCourses(coursId);

            // Loop through UI Checkboxes
            for (Node node : vboxRadios.getChildren()) {
                if (node instanceof CheckBox) {
                    CheckBox cb = (CheckBox) node;
                    Formation fBtn = (Formation) cb.getUserData();

                    // If the checkbox's formation is in the DB list, check it
                    if (associatedFormations.contains(fBtn)) {
                        cb.setSelected(true);
                        // Do NOT break; keep going to find other matches
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Connexion", e.getMessage());
        }
    }
    void refreshTable()  {

        try {
            System.out.println("Refreshing Student Table...");

            coursList.setAll(coursImp.findAll());
            formationList.setAll(formationImp.findAll());

            tableCours.setItems(coursList);
            cbFilter.setItems(formationList);

            createCheckboxes();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Connexion", "Impossible de charger les données : " + e.getMessage());
        }
    }
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur " + title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void onClearCheckboxes() {
        for (Node node : vboxRadios.getChildren()) {
            if (node instanceof CheckBox) {
                ((CheckBox) node).setSelected(false);
            }
        }
    }

    @FXML
    void onCreate() {
        try {
            // 1. Validation
            if (txtCode.getText().trim().isEmpty() || txtIntitule.getText().trim().isEmpty()) {
                showError("Validation", "Veuillez remplir le code et l'intitulé.");
                return;
            }

            // 2. Create Course Object (ID is 0 because DB generates it)
            Cours cours = new Cours(0, txtCode.getText(), txtIntitule.getText());
            coursImp.create(cours);

            // 3. Handle Association (Radio Button)
            // If a formation is selected, we link the new course to it
            for (Node node : vboxRadios.getChildren()) {
                if (node instanceof CheckBox) {
                    CheckBox cb = (CheckBox) node;
                    if(cb.isSelected()){
                        Formation fBtn = (Formation) cb.getUserData();
                        int newCoursId = findIdByCode(cours.getCode());
                        if (newCoursId != -1) {
                            coursImp.addCoursToFormation(fBtn.getId(), newCoursId);
                        }
                    }

                }
            }

            refreshTable();
            onClear();
            System.out.println("Cours created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur SQL", "Erreur lors de la création : " + e.getMessage());
        }
    }

    @FXML
    void onUpdate() {
        try {
            // 1. Validation
            if (txtId.getText().isEmpty()) {
                showError("Selection", "Aucun cours sélectionné.");
                return;
            }

            // 2. Update Course Details
            Cours cours = new Cours(
                    Integer.parseInt(txtId.getText()),
                    txtCode.getText(),
                    txtIntitule.getText()
            );
            coursImp.update(cours);

            coursImp.removeCoursFromFormation(cours.getId());


            for (Node node : vboxRadios.getChildren()) {
                if (node instanceof CheckBox) {
                    CheckBox cb = (CheckBox) node;
                    if(cb.isSelected()){
                        Formation fBtn = (Formation) cb.getUserData();
                        coursImp.addCoursToFormation(fBtn.getId(), cours.getId());
                    }

                }
            }



            refreshTable();
            onClear();
            System.out.println("Cours updated successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur SQL", "Erreur lors de la modification : " + e.getMessage());
        }
    }

    @FXML
    void onDelete() {
        try {
            if (txtId.getText().isEmpty()) {
                showError("Selection", "Aucun cours sélectionné.");
                return;
            }

            int id = Integer.parseInt(txtId.getText());
            coursImp.delete(id);

            refreshTable();
            onClear();
            System.out.println("Cours deleted successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur SQL", "Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @FXML
    void onFilter() {
        Formation selectedFormation = cbFilter.getValue();
        if (selectedFormation != null) {
            try {
                // Get courses specific to this formation
                List<Cours> filteredList = coursImp.getCoursByFormation(selectedFormation.getId());
                coursList.setAll(filteredList);
                // We do not set items again because coursList is already bound to the table
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Filtre", "Erreur lors du filtrage : " + e.getMessage());
            }
        }
    }

    @FXML
    void onResetFilter() {
        cbFilter.setValue(null);
        refreshTable(); // Reloads all data
    }

    @FXML
    void onClear() {
        // Clear Text Fields
        txtId.clear();
        txtCode.clear();
        txtIntitule.clear();

        for(Node node : vboxRadios.getChildren()){
            if (node instanceof CheckBox) {
                CheckBox cb = (CheckBox) node;
                cb.setSelected(false);
            }
        }

        // Reset Table Selection
        tableCours.getSelectionModel().clearSelection();

        // Reset Buttons State
        btnAdd.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
    }
    // Helper to find ID after creation to handle association
    private int findIdByCode(String code) {
        try {
            // We reuse the existing list to avoid a DB call,
            // or we could query the DB if the list isn't up to date.
            // Here we stream through the updated list from DB.
            return coursImp.findAll().stream()
                    .filter(c -> c.getCode().equals(code))
                    .findFirst()
                    .map(Cours::getId)
                    .orElse(-1);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
