package app.storage;

import app.domain.Department;
import java.sql.*;
import java.util.*;

public class SqliteDepartmentRepository implements DepartmentRepository {
    private final String jdbcUrl;

    public SqliteDepartmentRepository(String dbPath) {
        this.jdbcUrl = "jdbc:sqlite:" + dbPath;
        initTable();
    }

    private void initTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS departments (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                required_speciality TEXT NOT NULL
            );
            """;
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to init departments table", e);
        }
    }

    @Override
    public void save(Department d) {
        String sql = """
            INSERT INTO departments (id, name, required_speciality) VALUES (?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET name = excluded.name, required_speciality = excluded.required_speciality;
            """;
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, d.getId());
            stmt.setString(2, d.getName());
            stmt.setString(3, d.getRequiredSpeciality());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save department", e);
        }
    }

    @Override
    public Optional<Department> findById(String id) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM departments WHERE id = ?")) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return Optional.empty();
            return Optional.of(new Department(rs.getString("id"), rs.getString("name"), rs.getString("required_speciality")));
        } catch (SQLException e) {
            throw new RuntimeException("Error finding department", e);
        }
    }

    @Override
    public List<Department> findAll() {
        List<Department> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM departments ORDER BY id")) {
            while (rs.next()) {
                list.add(new Department(rs.getString("id"), rs.getString("name"), rs.getString("required_speciality")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing departments", e);
        }
        return list;
    }

    @Override
    public List<Department> searchByNameOrId(String query) {
        String pattern = "%" + query.toLowerCase().trim() + "%";
        List<Department> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM departments WHERE LOWER(id) LIKE ? OR LOWER(name) LIKE ? ORDER BY id")) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Department(rs.getString("id"), rs.getString("name"), rs.getString("required_speciality")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching departments", e);
        }
        return list;
    }

    @Override
    public void deleteById(String id) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM departments WHERE id = ?")) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting department", e);
        }
    }
}

