package app.storage;

import app.domain.*;
import java.sql.*;
import java.util.*;

public class SqliteUserRepository implements UserRepository {
    private final String jdbcUrl;

    public SqliteUserRepository(String dbPath) {
        this.jdbcUrl = "jdbc:sqlite:" + dbPath;
        initTable();
    }

    private void initTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id TEXT PRIMARY KEY,
                role TEXT NOT NULL,
                name TEXT NOT NULL,
                phone TEXT,
                password TEXT NOT NULL,
                speciality TEXT,
                department_id TEXT
            );
            """;

        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize users table", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        Role role = Role.valueOf(rs.getString("role"));
        String id = rs.getString("id");
        String name = rs.getString("name");
        String phone = rs.getString("phone");
        String password = rs.getString("password");

        return switch (role) {
            case ADMIN    -> new Admin(id, name, phone, password);
            case EMPLOYEE -> new Employee(id, name, phone, password);
            case DOCTOR   -> new Doctor(
                    id, name, phone, password,
                    rs.getString("speciality"),
                    rs.getString("department_id")
            );
            case PATIENT  -> new Patient(id, name, phone, password);
        };
    }

    @Override
    public Optional<User> findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user " + id, e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users ORDER BY id")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error loading all users", e);
        }
        return list;
    }

    @Override
    public List<User> searchByNameOrId(String query) {
        String sql = "SELECT * FROM users WHERE LOWER(id) LIKE ? OR LOWER(name) LIKE ? ORDER BY id";
        String pattern = "%" + query.toLowerCase().trim() + "%";
        List<User> list = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error searching users", e);
        }
        return list;
    }

    @Override
    public void save(User user) {
        String sql = """
            INSERT INTO users (id, role, name, phone, password, speciality, department_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                name = excluded.name,
                phone = excluded.phone,
                password = excluded.password,
                speciality = excluded.speciality,
                department_id = excluded.department_id;
            """;

        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getId());
            stmt.setString(2, user.getRole().name());
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getPhone());
            stmt.setString(5, user.getPassword());

            if (user instanceof Doctor doc) {
                stmt.setString(6, doc.getSpeciality());
                stmt.setString(7, doc.getDepartmentId());
            } else {
                stmt.setNull(6, Types.VARCHAR);
                stmt.setNull(7, Types.VARCHAR);
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user " + user.getId(), e);
        }
    }

    @Override
    public void deleteById(String id) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user " + id, e);
        }
    }
}

