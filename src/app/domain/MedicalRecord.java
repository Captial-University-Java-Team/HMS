package app.domain;

import java.time.LocalDate;
import java.util.UUID;

public class MedicalRecord {
    private final String id;
    private final String appointmentId;
    private final String doctorId;
    private final String patientId;
    private LocalDate visitDate;
    private String symptoms;
    private String diagnosis;

    public MedicalRecord(String appointmentId, String doctorId, String patientId,
                         LocalDate visitDate, String symptoms, String diagnosis) {
        this(UUID.randomUUID().toString().substring(0, 8),
             appointmentId, doctorId, patientId, visitDate, symptoms, diagnosis);
    }

    public MedicalRecord(String id, String appointmentId, String doctorId, String patientId,
                         LocalDate visitDate, String symptoms, String diagnosis) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.visitDate = visitDate;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
    }

    public void updateConsultationDetails(String newSymptoms, String newDiagnosis, LocalDate newVisitDate) {
        this.symptoms = newSymptoms.trim();
        this.diagnosis = newDiagnosis.trim();
        this.visitDate = newVisitDate;
    }

    public String getId() { return id; }
    public String getAppointmentId() { return appointmentId; }
    public String getDoctorId() { return doctorId; }
    public String getPatientId() { return patientId; }
    public LocalDate getVisitDate() { return visitDate; }
    public String getSymptoms() { return symptoms; }
    public String getDiagnosis() { return diagnosis; }

    @Override
    public String toString() {
        return "[" + id + "] Date: " + visitDate + " | Patient: " + patientId + " | Doctor: " + doctorId
                + " | Symptoms: " + symptoms + " | Diagnosis: " + diagnosis;
    }
}

