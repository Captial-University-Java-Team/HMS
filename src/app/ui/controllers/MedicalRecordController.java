package app.ui.controllers;

import app.domain.*;
import app.storage.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MedicalRecordController {
    private final User currentUser;
    private final MedicalRecordRepository recordRepo;
    private final AppointmentRepository appointmentRepo;

    private String statusMessage = "";
    private boolean isError = false;

    public MedicalRecordController(User currentUser, MedicalRecordRepository recordRepo,
                                   AppointmentRepository appointmentRepo) {
        this.currentUser = currentUser;
        this.recordRepo = recordRepo;
        this.appointmentRepo = appointmentRepo;
    }

    // Scenario 6: Only Doctors can create or update records for their own consultations
    public void saveConsultationRecord(Appointment appointment, LocalDate visitDate, String symptoms, String diagnosis) {
        if (currentUser.getRole() != Role.DOCTOR) {
            fail("Access Denied: Only doctors can create or edit medical records.");
            return;
        }
        if (appointment == null || visitDate == null || symptoms == null || symptoms.isBlank()
                || diagnosis == null || diagnosis.isBlank()) {
            fail("Appointment, visit date, symptoms, and diagnosis are required.");
            return;
        }
        if (!appointment.getDoctorId().equals(currentUser.getId())) {
            fail("Access Denied: You can only record consultations for your own appointments.");
            return;
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELED) {
            fail("Cannot record consultation notes for a canceled appointment.");
            return;
        }

        Optional<MedicalRecord> existing = recordRepo.findByAppointmentId(appointment.getId());
        if (existing.isPresent()) {
            MedicalRecord record = existing.get();
            record.updateConsultationDetails(symptoms, diagnosis, visitDate);
            recordRepo.save(record);
            succeed("Updated medical record [" + record.getId() + "].");
        } else {
            MedicalRecord newRecord = new MedicalRecord(
                    appointment.getId(), currentUser.getId(), appointment.getPatientId(),
                    visitDate, symptoms.trim(), diagnosis.trim()
            );
            recordRepo.save(newRecord);
            succeed("Created medical record [" + newRecord.getId() + "].");
        }
    }

    // Scenario 7 Permission Matrix (7.1 Admin/Employee -> All, 7.2 Doctor -> Own patients, 7.3 Patient -> Own records)
    public List<MedicalRecord> searchRecords(String query) {
        List<MedicalRecord> scoped = switch (currentUser.getRole()) {
            case ADMIN, EMPLOYEE -> recordRepo.findAll();
            case DOCTOR          -> recordRepo.findByDoctorId(currentUser.getId());
            case PATIENT         -> recordRepo.findByPatientId(currentUser.getId());
        };

        if (query == null || query.isBlank()) {
            return scoped;
        }

        String q = query.toLowerCase().trim();
        return scoped.stream()
                .filter(r -> r.getPatientId().toLowerCase().contains(q)
                        || r.getDoctorId().toLowerCase().contains(q)
                        || r.getDiagnosis().toLowerCase().contains(q)
                        || r.getSymptoms().toLowerCase().contains(q))
                .toList();
    }

    public List<Appointment> getDoctorConsultations() {
        if (currentUser.getRole() != Role.DOCTOR) return List.of();
        return appointmentRepo.findByDoctorId(currentUser.getId()).stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELED)
                .toList();
    }

    public boolean canEditRecords() {
        return currentUser.getRole() == Role.DOCTOR;
    }

    private void fail(String msg) { this.statusMessage = msg; this.isError = true; }
    private void succeed(String msg) { this.statusMessage = msg; this.isError = false; }

    public String getStatusMessage() { return statusMessage; }
    public boolean isError() { return isError; }
}

