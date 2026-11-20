package app.ui.controllers;

import app.domain.*;
import app.storage.*;

import java.time.LocalDate;
import java.util.*;

public class ReportController {
    private final User currentUser;
    private final AppointmentRepository appointmentRepo;
    private final UserRepository userRepo;
    private final MedicalRecordRepository recordRepo;

    public ReportController(User currentUser, AppointmentRepository appointmentRepo,
                            UserRepository userRepo, MedicalRecordRepository recordRepo) {
        this.currentUser = currentUser;
        this.appointmentRepo = appointmentRepo;
        this.userRepo = userRepo;
        this.recordRepo = recordRepo;
    }

    // Scenario 3: Filter by doctor + status + timestamp range, OR by doctor alone
    public List<Appointment> generateAppointmentReport(String doctorId, AppointmentStatus status,
                                                       LocalDate fromDate, LocalDate toDate) {
        if (currentUser.getRole() != Role.ADMIN) return List.of();

        return appointmentRepo.findAll().stream()
                .filter(a -> doctorId == null || doctorId.isBlank() || a.getDoctorId().equals(doctorId.trim()))
                .filter(a -> status == null || a.getStatus() == status)
                .filter(a -> fromDate == null || !a.getDate().toLocalDate().isBefore(fromDate))
                .filter(a -> toDate == null || !a.getDate().toLocalDate().isAfter(toDate))
                .toList();
    }

    // Requirement 4c: Number of appointments assigned to each doctor
    public List<String> generateDoctorWorkloadReport() {
        if (currentUser.getRole() != Role.ADMIN) return List.of();

        List<Appointment> allAppts = appointmentRepo.findAll();
        return userRepo.findAll().stream()
                .filter(u -> u instanceof Doctor)
                .map(u -> {
                    long total = allAppts.stream().filter(a -> a.getDoctorId().equals(u.getId())).count();
                    long completed = allAppts.stream()
                            .filter(a -> a.getDoctorId().equals(u.getId()) && a.getStatus() == AppointmentStatus.COMPLETED)
                            .count();
                    return u.getName() + " (" + u.getId() + ") -> Total Appointments: " + total + " | Completed: " + completed;
                })
                .toList();
    }

    // Requirement 4b: Admin views & searches patient records
    public List<MedicalRecord> searchPatientRecordsReport(String patientQuery) {
        if (currentUser.getRole() != Role.ADMIN) return List.of();
        if (patientQuery == null || patientQuery.isBlank()) return recordRepo.findAll();

        String q = patientQuery.toLowerCase().trim();
        return recordRepo.findAll().stream()
                .filter(r -> r.getPatientId().toLowerCase().contains(q) || r.getDiagnosis().toLowerCase().contains(q))
                .toList();
    }

    public List<Doctor> getAllDoctors() {
        return userRepo.findAll().stream()
                .filter(u -> u instanceof Doctor)
                .map(u -> (Doctor) u)
                .toList();
    }
}

