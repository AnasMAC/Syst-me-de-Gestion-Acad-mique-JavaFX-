package com.example.gestionacademique.dao;


import com.example.gestionacademique.modele.Student;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentImp implements CRUD<Student, Integer> {

    private Connection connection;

    public StudentImp() throws SQLException {
        this.connection = DBconnection.getInstance().getConnection();
    }

    @Override
    public void create(Student student) throws SQLException {
        // We exclude 'id' because it is SERIAL (Auto-increment) in PostgreSQL
        String sql = "INSERT INTO student (name, moyenne, formation_id) VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, student.getName());
            ps.setDouble(2, student.getMoyenne());
            ps.setInt(3, student.getFormationId());

            ps.executeUpdate();
        }
    }

    @Override
    public void update(Student student) throws SQLException {
        String sql = "UPDATE student SET name = ?, moyenne = ?, formation_id = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, student.getName());
            ps.setDouble(2, student.getMoyenne());
            ps.setInt(3, student.getFormationId());
            ps.setInt(4, student.getId()); // The ID is used for the WHERE clause

            ps.executeUpdate();
        }
    }

    @Override
    public Boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM student WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    @Override
    public Optional<Student> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM student WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStudent(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Student> findAll() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM student";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToStudent(rs));
            }
        }
        return list;
    }

    // Helper method to keep code clean and avoid repetition
    // This maps the Database Snake_Case to Java CamelCase
    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        // We use the constructor directly: new Student(...)
        return new Student(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("moyenne"),
                rs.getInt("formation_id") // Maps SQL "formation_id" to Java "formationId"
        );
    }
    public void inscrireEtudiantAuCours(int studentId, int coursId) throws SQLException {
        String sqlCheck = "SELECT formation_id FROM Student WHERE id = ?";
        String sqlVerify = "SELECT 1 FROM Formation_Cours WHERE formation_id = ? AND cours_id = ?";
        String sqlInsert = "INSERT INTO Student_Cours (student_id, cours_id) VALUES (?, ?)";

        try {
            connection.setAutoCommit(false); // DÉBUT TRANSACTION

            // 1. Récupérer la formation de l'étudiant
            int formationId = -1;
            try (PreparedStatement ps = connection.prepareStatement(sqlCheck)) {
                ps.setInt(1, studentId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) formationId = rs.getInt("formation_id");
            }

            // 2. Vérifier que le cours appartient bien à cette formation (Règle Métier Java)
            boolean isAllowed = false;
            try (PreparedStatement ps = connection.prepareStatement(sqlVerify)) {
                ps.setInt(1, formationId);
                ps.setInt(2, coursId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) isAllowed = true;
            }

            if (!isAllowed) {
                throw new SQLException("Règle violée : Ce cours ne fait pas partie de la filière de l'étudiant.");
            }

            // 3. Inscription
            try (PreparedStatement ps = connection.prepareStatement(sqlInsert)) {
                ps.setInt(1, studentId);
                ps.setInt(2, coursId);
                ps.executeUpdate();
            }

            connection.commit(); // VALIDER TRANSACTION
        } catch (SQLException e) {
            connection.rollback(); // ANNULER EN CAS D'ERREUR
            throw e;
        } finally {
            connection.setAutoCommit(true); // Remettre en mode normal
        }
    }
}