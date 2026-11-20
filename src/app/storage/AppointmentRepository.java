package app.storage;

import app.domain.Appointment;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {
    void save(Appointment appointment);
    Optional<Appointment> findById(String id);
    List<Appointment> findByDoctorId(String doctorId);
    List<Appointment> findAll();
}

