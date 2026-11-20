package app.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class Appointment {
    private final String id;
    private final String doctorId;
    private final String patientId;
    private LocalDateTime dateOfConsultation;
    private AppointmentStatus status;

    public Appointment(String doctorId, String patientId, LocalDateTime dateOfConsultation) {
        this(UUID.randomUUID().toString().substring(0, 8), doctorId, patientId, dateOfConsultation, AppointmentStatus.SCHEDULED);
    }

    public Appointment(String id, String doctorId, String patientId, LocalDateTime dateOfConsultation, AppointmentStatus status) {
        this.id = id;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.dateOfConsultation = dateOfConsultation;
        this.status = status;
    }

    public boolean reschedule(LocalDateTime newDate) {
        if (this.status != AppointmentStatus.SCHEDULED) {
            return false; // Cannot reschedule completed or canceled appointments
        }
        this.dateOfConsultation = newDate;
        return true;
    }

    public boolean complete() {
        if (this.status != AppointmentStatus.SCHEDULED) {
            return false;
        }
        this.status = AppointmentStatus.COMPLETED;
        return true;
    }

    public boolean cancel() {
        if (this.status != AppointmentStatus.SCHEDULED) {
            return false;
        }
        this.status = AppointmentStatus.CANCELED;
        return true;
    }

    public String getId() { return id; }
    public String getDoctorId() { return doctorId; }
    public String getPatientId() { return patientId; }
    public LocalDateTime getDate() { return dateOfConsultation; }
    public AppointmentStatus getStatus() { return status; }

    @Override
    public String toString() {
        return "[" + id + "] Doctor: " + doctorId + " | Patient: " + patientId
                + " | " + dateOfConsultation + " | Status: " + status;
    }
}

