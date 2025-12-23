package com.example.gestionacademique.dao;

import com.example.gestionacademique.modele.Cours;
import com.example.gestionacademique.modele.DossierAdministratif;
import com.example.gestionacademique.modele.Formation;
import com.example.gestionacademique.modele.Student;

import java.sql.Date; // Important pour DossierAdministratif
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   DÉMARRAGE DES TESTS D'INTÉGRATION");
        System.out.println("==========================================");

        try {
            // 1. Initialisation des DAOs
            FormationImp formationDao = new FormationImp();
            StudentImp studentDao = new StudentImp();
            CoursImp coursDao = new CoursImp();
            DossierAdministratifImp dossierDao = new DossierAdministratifImp();

            // ==========================================
            // TEST 1: CRÉATION FORMATIONS & COURS
            // ==========================================
            System.out.println("\n--- [1] Initialisation des Données de base ---");

            // Création Formations
            formationDao.create(new Formation(0, "CS")); // ID 0 ignoré (Serial)
            formationDao.create(new Formation(0, "EC"));

            // Récupération des IDs générés (Indispensable pour la suite)
            List<Formation> formations = formationDao.findAll();
            int idInfo = formations.get(formations.size() - 2).getId(); // Avant-dernier ajout
            int idGestion = formations.get(formations.size() - 1).getId(); // Dernier ajout
            System.out.println("✅ Formations créées : Info(ID=" + idInfo + "), Gestion(ID=" + idGestion + ")");

            // Création Cours
            coursDao.create(new Cours(0, "JAVA-101", "Java Basics"));
            coursDao.create(new Cours(0, "COMPTA-200", "Comptabilité Générale"));

            List<Cours> courses = coursDao.findAll();
            int idJava = courses.get(courses.size() - 2).getId();
            int idCompta = courses.get(courses.size() - 1).getId();
            System.out.println("✅ Cours créés : Java(ID=" + idJava + "), Compta(ID=" + idCompta + ")");


            // ==========================================
            // TEST 2: ASSOCIATION FORMATION - COURS
            // ==========================================
            System.out.println("\n--- [2] Association Cours <-> Formation ---");

            // On dit que JAVA est pour INFORMATIQUE
            coursDao.addCoursToFormation(idInfo, idJava);

            // On dit que COMPTA est pour GESTION
            coursDao.addCoursToFormation(idGestion, idCompta);

            System.out.println("✅ Associations effectuées.");

            // Vérification
            List<Cours> coursInfo = coursDao.getCoursByFormation(idInfo);
            System.out.println("-> Cours en Informatique : " + coursInfo.size() + " trouvé(s).");


            // ==========================================
            // TEST 3: CRÉATION ÉTUDIANT
            // ==========================================
            System.out.println("\n--- [3] Création d'un Étudiant ---");

            Student alice = new Student(0, "Alice Dev", 16.5, idInfo); // Alice est en INFO
            studentDao.create(alice);

            // Récupérer ID Alice
            int idAlice = studentDao.findAll().stream()
                    .filter(s -> s.getName().equals("Alice Dev"))
                    .findFirst().get().getId();

            System.out.println("✅ Alice ajoutée en Informatique (ID=" + idAlice + ")");


            // ==========================================
            // TEST 4: INSCRIPTION COURS (Transaction)
            // ==========================================
            System.out.println("\n--- [4] Test des Transactions d'Inscription ---");

            // A. SCÉNARIO SUCCÈS : Alice (Info) s'inscrit en Java (Cours Info)
            System.out.print("👉 Tentative 1 (Valide) : Alice -> Java... ");
            try {
                studentDao.inscrireEtudiantAuCours(idAlice, idJava);
                System.out.println("✅ SUCCÈS (Attendu)");
            } catch (SQLException e) {
                System.out.println("❌ ERREUR : " + e.getMessage());
            }

            // B. SCÉNARIO ÉCHEC : Alice (Info) essaie de s'inscrire en Compta (Cours Gestion)
            System.out.print("👉 Tentative 2 (Interdite) : Alice -> Compta... ");
            try {
                studentDao.inscrireEtudiantAuCours(idAlice, idCompta);
                System.out.println("❌ ÉCHEC : Aurait du être bloqué !");
            } catch (SQLException e) {
                System.out.println("✅ BLOQUÉ (Attendu) : " + e.getMessage());
            }


            // ==========================================
            // TEST 5: DOSSIER ADMINISTRATIF
            // ==========================================
            System.out.println("\n--- [5] Gestion Dossier Administratif ---");

            DossierAdministratif dossier = new DossierAdministratif(
                    0,
                    "MAT-2025-ALICE",
                    Date.valueOf(LocalDate.now()), // Conversion LocalDate -> SQL Date
                    idAlice
            );

            dossierDao.create(dossier);
            System.out.println("✅ Dossier créé pour Alice.");



            // Test contrainte unique (Optionnel)
            System.out.print("👉 Tentative création doublon dossier... ");
            try {
                dossierDao.create(dossier); // Devrait planter car student_id unique
            } catch (SQLException e) {
                System.out.println("✅ BLOQUÉ (Attendu) : Un seul dossier par élève.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // ==========================================
            // TEST 6: NETTOYAGE
            // ==========================================
            System.out.println("\n--- [6] Fermeture ---");

            System.out.println("Fin du programme.");
        }
    }
}