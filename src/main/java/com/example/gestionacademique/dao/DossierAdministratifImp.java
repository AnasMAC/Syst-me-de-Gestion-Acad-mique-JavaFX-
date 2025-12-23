package com.example.gestionacademique.dao;

import com.example.gestionacademique.modele.DossierAdministratif;

import java.sql.*;
import java.time.LocalDate; // Important pour les dates modernes
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DossierAdministratifImp implements CRUD<DossierAdministratif, Integer> {

    private Connection connection;

    public DossierAdministratifImp() throws SQLException {
        this.connection = DBconnection.getInstance().getConnection();
    }

    @Override
    public void create(DossierAdministratif dossier) throws SQLException {
        // student_id est UNIQUE dans la BDD, donc si on essaie de créer un 2ème dossier
        // pour le même étudiant, PostgreSQL lancera une exception ici.
        String sql = "INSERT INTO DossierAdministratif (numero_inscription, date_creation, student_id) VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, dossier.getNumeroInscription());

            // Conversion LocalDate (Java) -> Date (SQL)
            ps.setDate(2, Date.valueOf(dossier.getDateCreation().toLocalDate()));

            ps.setInt(3, dossier.getStudentId());

            ps.executeUpdate();
        }
    }

    @Override
    public void update(DossierAdministratif dossier) throws SQLException {
        // On ne modifie généralement pas le student_id (le dossier n'appartient pas à un autre étudiant)
        String sql = "UPDATE DossierAdministratif SET numero_inscription = ?, date_creation = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, dossier.getNumeroInscription());
            ps.setDate(2, Date.valueOf(dossier.getDateCreation().toLocalDate()));
            ps.setInt(3, dossier.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public Boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM DossierAdministratif WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    @Override
    public Optional<DossierAdministratif> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM DossierAdministratif WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDossier(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<DossierAdministratif> findAll() throws SQLException {
        List<DossierAdministratif> list = new ArrayList<>();
        String sql = "SELECT * FROM DossierAdministratif";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToDossier(rs));
            }
        }
        return list;
    }

    // --- Helper pour éviter la répétition du mapping ---
    private DossierAdministratif mapResultSetToDossier(ResultSet rs) throws SQLException {
        return new DossierAdministratif(
                rs.getInt("id"),
                rs.getString("numero_inscription"),
                rs.getDate("date_creation"),
                rs.getInt("student_id")
        );
    }
}