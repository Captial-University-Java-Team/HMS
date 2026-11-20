package app.storage;

import app.domain.MedicalRecord;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class SqliteMedicalRecordRepository implements MedicalRecordRepository {
    private final String jdbcUrl;

    public SqliteMedicalRecordRepository(String dbPath) {
        this.jdbcUrl = "jdbc:sqlite:" + dbPath;
        initTable();
    }

    private void initTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS medical_records (
                id TEXT PRIMARY KEY,
                appointment_id TEXT UNIQUE NOT NULL,
                doctor_id TEXT NOT NULL,
                patient_id TEXT NOT NULL,
                visit_date TEXT NOT NULL,
                symptoms TEXT NOT NULL,
                diagnosis TEXT NOT NULL
            );
            """;
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to init medical_records table", e);
        }
    }

    private MedicalRecord mapRow(ResultSet rs) throws SQLException {
        return new MedicalRecord(
                rs.getString("id"),
                rs.getString("appointment_id"),
                rs.getString("doctor_id"),
                rs.getString("patient_id"),
                LocalDate.parse(rs.getString("visit_date")),
                rs.getString("symptoms"),
                rs.getString("diagnosis")
        );
    }

    @Override
    public void save(MedicalRecord r) {
        String sql = """
            INSERT INTO medical_records (id, appointment_id, doctor_id, patient_id, visit_date, symptoms, diagnosis)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                visit_date = excluded.visit_date,
                symptoms = excluded.symptoms,
                diagnosis = excluded.diagnosis;
            """;
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, r.getId());
            stmt.setString(2, r.getAppointmentId());
            stmt.setString(3, r.getDoctorId());
            stmt.setString(4, r.getPatientId());
            stmt.setString(5, r.getVisitDate().toString());
            stmt.setString(6, r.getSymptoms());
            stmt.setString(7, r.getDiagnosis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save medical record", e);
        }
    }

    @Override
    public Optional<MedicalRecord> findByAppointmentId(String appointmentId) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM medical_records WHERE appointment_id = ?")) {
            stmt.setString(1, appointmentId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding record by appointment", e);
        }
    }

    @Override
    public List<MedicalRecord> findAll() {
        return queryList("SELECT * FROM medical_records ORDER BY visit_date DESC", null);
    }

    @Override
    public List<MedicalRecord> findByDoctorId(String doctorId) {
        return queryList("SELECT * FROM medical_records WHERE doctor_id = ? ORDER BY visit_date DESC", doctorId);
    }

    @Override
    public List<MedicalRecord> findByPatientId(String patientId) {
        return queryList("SELECT * FROM medical_records WHERE patient_id = ? ORDER BY visit_date DESC", patientId);
    }

    private List<MedicalRecord> queryList(String sql, String param) {
        List<MedicalRecord> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (param != null) stmt.setString(1, param);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error querying medical records", e);
        }
        return list;
    }
}

