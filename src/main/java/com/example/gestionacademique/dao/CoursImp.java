package com.example.gestionacademique.dao;

import com.example.gestionacademique.modele.Cours;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CoursImp implements CRUD<Cours, Integer> {
    private Connection connection;

    public CoursImp() throws SQLException {
        this.connection = DBconnection.getInstance().getConnection();
    }

    @Override
    public void create(Cours cours) throws SQLException {
        String sql = "INSERT INTO Cours (code, intitule) VALUES (?, ?)";

        // Le PreparedStatement est déclaré DANS le try -> fermeture auto
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cours.getCode());
            ps.setString(2, cours.getIntitule());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Cours cours) throws SQLException {
        String sql = "UPDATE Cours SET code = ?, intitule = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cours.getCode());
            ps.setString(2, cours.getIntitule());
            ps.setInt(3, cours.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public Boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM Cours WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    @Override
    public Optional<Cours> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM Cours WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            // On imbrique un try pour le ResultSet pour garantir sa fermeture
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cours c = new Cours(
                            rs.getInt("id"),
                            rs.getString("code"),
                            rs.getString("intitule")
                    );
                    return Optional.of(c);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Cours> findAll() throws SQLException {
        List<Cours> list = new ArrayList<>();
        String sql = "SELECT * FROM Cours";

        // Ici, on peut déclarer Statement ET ResultSet dans le même try
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Cours(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("intitule")
                ));
            }
        }
        return list;
    }

    // --- Méthodes d'association ---

    public void addCoursToFormation(int formationId, int coursId) throws SQLException {
        String sql = "INSERT INTO Formation_Cours (formation_id, cours_id) VALUES (?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, formationId);
            ps.setInt(2, coursId);
            ps.executeUpdate();
        }
    }

    public List<Cours> getCoursByFormation(int formationId) throws SQLException {
        List<Cours> liste = new ArrayList<>();
        String sql = "SELECT c.* FROM Cours c " +
                "JOIN Formation_Cours fc ON c.id = fc.cours_id " +
                "WHERE fc.formation_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, formationId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(new Cours(
                            rs.getInt("id"),
                            rs.getString("code"),
                            rs.getString("intitule")
                    ));
                }
            }
        }
        return liste;
    }
}