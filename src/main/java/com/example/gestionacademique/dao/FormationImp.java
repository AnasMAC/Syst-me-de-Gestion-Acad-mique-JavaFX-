package com.example.gestionacademique.dao;

import com.example.gestionacademique.modele.Formation;
import java.sql.*;
import java.util.ArrayList; // Import needed
import java.util.List;
import java.util.Optional;

public class FormationImp implements CRUD<Formation, Integer> {

    private Connection connection;

    public FormationImp() throws SQLException {
        this.connection = DBconnection.getInstance().getConnection();
    }

    @Override
    public void create(Formation formation) throws SQLException {
        String sql = "INSERT INTO formation (name) VALUES (?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, formation.getName());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Formation formation) throws SQLException {
        String sql = "UPDATE formation SET name = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, formation.getName());
            ps.setInt(2, formation.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public Boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM formation WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    @Override
    public Optional<Formation> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM formation WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Formation f = new Formation(
                            rs.getInt("id"),
                            rs.getString("name")
                    );
                    return Optional.of(f);
                }
            }
        }
        return Optional.empty();
    }

    // --- CORRECTION IS HERE ---
    @Override
    public List<Formation> findAll() throws SQLException {
        List<Formation> allFormations = new ArrayList<>();
        String sql = "SELECT * FROM formation";

        // Try-with-resources closes the Statement and ResultSet automatically
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Formation f = new Formation(
                        rs.getInt("id"),
                        rs.getString("name")
                );
                allFormations.add(f);
            }
        }
        return allFormations;
    }
}