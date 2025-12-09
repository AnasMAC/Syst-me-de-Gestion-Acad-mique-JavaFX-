package com.example.gestionacademique.dao;

import com.example.gestionacademique.dao.FormationImp;
import com.example.gestionacademique.dao.StudentImp;
import com.example.gestionacademique.modele.Formation;
import com.example.gestionacademique.modele.Student;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("--- Starting Application Test ---");

        try {
            // 1. Initialize DAOs
            FormationImp formationDao = new FormationImp();
            StudentImp studentDao = new StudentImp();

            // ==========================================
            // TEST 1: CREATION (Formation)
            // ==========================================
            System.out.println("\n[1] Creating Formations...");

            // Note: ID is null because Postgres generates it (SERIAL)
            formationDao.create(new Formation(null, "Informatique"));
            formationDao.create(new Formation(null, "Gestion"));

            // Reload from DB to get the generated IDs
            List<Formation> allFormations = formationDao.findAll();
            System.out.println("-> Formations in DB: " + allFormations.size());

            // Get the ID of the first formation (e.g., "Informatique")
            Formation infoFormation = allFormations.get(0);
            Integer infoId = infoFormation.getId();
            System.out.println("-> Selected Formation: " + infoFormation.getName() + " (ID: " + infoId + ")");


            // ==========================================
            // TEST 2: CREATION (Student - Valid)
            // ==========================================
            System.out.println("\n[2] Creating a Valid Student...");

            Student s1 = new Student(null, "Alice", 15.5, infoId);
            studentDao.create(s1);
            System.out.println("-> Alice added successfully.");


            // ==========================================
            // TEST 3: VALIDATION (Student - Invalid)
            // ==========================================
            System.out.println("\n[3] Testing Validation (Bad Grade)...");

            try {
                // This should trigger your IllegalArgumentException immediately
                System.out.println("-> Attempting to create Bob with grade 25.0...");
                Student s2 = new Student(null, "Bob", 25.0, infoId);

                // If the line above works, we have a problem.
                studentDao.create(s2);
                System.err.println("❌ ERROR: Bob was added but shouldn't be!");

            } catch (IllegalArgumentException e) {
                // We expect to land here
                System.out.println("✅ SUCCESS: Validation caught the error!");
                System.out.println("   Message: " + e.getMessage());
            }


            // ==========================================
            // TEST 4: UPDATE
            // ==========================================
            System.out.println("\n[4] Testing Update...");

            // Retrieve Alice (we need her ID)
            List<Student> students = studentDao.findAll();
            if (!students.isEmpty()) {
                Student alice = students.get(0); // Assuming Alice is first
                System.out.println("-> Before Update: " + alice);

                // Change grade using the secure setter
                alice.setMoyenne(19.0);

                // Update in DB
                studentDao.update(alice);

                // Check if DB is updated
                Student updatedAlice = studentDao.findById(alice.getId()).get();
                System.out.println("-> After Update:  " + updatedAlice);
            }

            // ==========================================
            // TEST 5: READ ALL
            // ==========================================
            System.out.println("\n[5] Final List of Students:");
            for (Student s : studentDao.findAll()) {
                System.out.println("   - ID: " + s.getId() + " | Nom: " + s.getName() + " | Moy: " + s.getMoyenne());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}