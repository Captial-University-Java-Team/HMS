package app.storage;

import app.domain.Appointment;
import app.domain.AppointmentStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class SqliteAppointmentRepository implements AppointmentRepository {
    private final String jdbcUrl;

    public SqliteAppointmentRepository(String dbPath) {
        this.jdbcUrl = "jdbc:sqlite:" + dbPath;
        initTable();
    }

    private void initTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS appointments (
                id TEXT PRIMARY KEY,
                doctor_id TEXT NOT NULL,
                patient_id TEXT NOT NULL,
                date_time TEXT NOT NULL,
                status TEXT NOT NULL
            );
            """;
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize appointments table", e);
        }
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        return new Appointment(
                rs.getString("id"),
                rs.getString("doctor_id"),
                rs.getString("patient_id"),
                LocalDateTime.parse(rs.getString("date_time")),
                AppointmentStatus.valueOf(rs.getString("status"))
        );
    }

    @Override
    public void save(Appointment appt) {
        String sql = """
            INSERT INTO appointments (id, doctor_id, patient_id, date_time, status)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                date_time = excluded.date_time,
                status = excluded.status;
            """;
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, appt.getId());
            stmt.setString(2, appt.getDoctorId());
            stmt.setString(3, appt.getPatientId());
            stmt.setString(4, appt.getDate().toString());
            stmt.setString(5, appt.getStatus().name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save appointment", e);
        }
    }

    @Override
    public Optional<Appointment> findById(String id) {
        String sql = "SELECT * FROM appointments WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find appointment " + id, e);
        }
    }

    @Override
    public List<Appointment> findByDoctorId(String doctorId) {
        String sql = "SELECT * FROM appointments WHERE doctor_id = ? ORDER BY date_time";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load doctor appointments", e);
        }
        return list;
    }

    @Override
    public List<Appointment> findAll() {
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM appointments ORDER BY date_time")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load all appointments", e);
        }
        return list;
    }
}

