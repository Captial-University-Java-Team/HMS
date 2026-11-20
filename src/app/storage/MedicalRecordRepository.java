package app.storage;

import app.domain.MedicalRecord;
import java.util.List;
import java.util.Optional;

public interface MedicalRecordRepository {
    void save(MedicalRecord record);
    Optional<MedicalRecord> findByAppointmentId(String appointmentId);
    List<MedicalRecord> findAll();
    List<MedicalRecord> findByDoctorId(String doctorId);
    List<MedicalRecord> findByPatientId(String patientId);
}

