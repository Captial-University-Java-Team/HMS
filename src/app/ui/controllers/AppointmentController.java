package app.ui.controllers;

import app.domain.*;
import app.storage.*;

import java.time.*;
import java.util.List;

public class AppointmentController {
    private final User currentUser;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    private String statusMessage = "";
    private boolean isError = false;

    public AppointmentController(User currentUser, AppointmentRepository appointmentRepository,
                                 UserRepository userRepository, Clock clock) {
        this.currentUser = currentUser;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    public void scheduleNewAppointment(Doctor doctor, Patient patient, LocalDate date, Integer hour) {
        if (currentUser.getRole() == Role.PATIENT) {
            fail("Access Denied: Patients cannot book appointments.");
            return;
        }
        if (doctor == null || patient == null || date == null || hour == null) {
            fail("Please select a doctor, patient, date, and hour.");
            return;
        }

        LocalDateTime requestedTime = validateDateTime(date, hour);
        if (requestedTime == null) return;

        doctor.loadAppointments(appointmentRepository.findByDoctorId(doctor.getId()));
        Appointment newAppointment = new Appointment(doctor.getId(), patient.getId(), requestedTime);

        if (!doctor.scheduleNewAppointment(newAppointment)) {
            fail("Doctor " + doctor.getName() + " already has an appointment at " + requestedTime);
            return;
        }

        appointmentRepository.save(newAppointment);
        succeed("Appointment scheduled successfully!");
    }

    // Scenario 4: Authorized user (Admin, Employee, Doctor) reschedules an existing appointment
    public void rescheduleAppointment(Appointment selected, LocalDate newDate, Integer newHour) {
        if (currentUser.getRole() == Role.PATIENT) {
            fail("Access Denied: Patients cannot modify appointments.");
            return;
        }
        if (selected == null) {
            fail("Please select an appointment from the list first.");
            return;
        }
        if (selected.getStatus() != AppointmentStatus.SCHEDULED) {
            fail("Only SCHEDULED appointments can be rescheduled.");
            return;
        }

        LocalDateTime newDateTime = validateDateTime(newDate, newHour);
        if (newDateTime == null) return;

        // Check if the doctor already has another SCHEDULED appointment at the new slot
        boolean conflict = appointmentRepository.findByDoctorId(selected.getDoctorId()).stream()
                .filter(a -> !a.getId().equals(selected.getId())) // Ignore itself
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .anyMatch(a -> a.getDate().equals(newDateTime));

        if (conflict) {
            fail("Doctor is not available at " + newDateTime);
            return;
        }

        selected.reschedule(newDateTime);
        appointmentRepository.save(selected);
        succeed("Appointment rescheduled to " + newDateTime);
    }

    // Scenario 4 & 5: Authorized user cancels an appointment
    public void cancelAppointment(Appointment selected) {
        if (currentUser.getRole() == Role.PATIENT) {
            fail("Access Denied: Patients cannot cancel appointments.");
            return;
        }
        if (selected == null) {
            fail("Please select an appointment to cancel.");
            return;
        }
        if (!selected.cancel()) {
            fail("Cannot cancel an appointment that is already " + selected.getStatus());
            return;
        }

        appointmentRepository.save(selected);
        succeed("Appointment [" + selected.getId() + "] canceled.");
    }

    // Scenario 5: Doctor marks appointment COMPLETED after consultation
    public void completeAppointment(Appointment selected) {
        if (currentUser.getRole() != Role.DOCTOR) {
            fail("Access Denied: Only doctors can mark consultations as completed.");
            return;
        }
        if (selected == null) {
            fail("Please select an appointment to complete.");
            return;
        }
        if (!selected.getDoctorId().equals(currentUser.getId())) {
            fail("Access Denied: You can only complete your own appointments.");
            return;
        }
        if (!selected.complete()) {
            fail("Cannot complete an appointment that is already " + selected.getStatus());
            return;
        }

        appointmentRepository.save(selected);
        succeed("Consultation completed for appointment [" + selected.getId() + "].");
    }

    private LocalDateTime validateDateTime(LocalDate date, Integer hour) {
        if (date == null || hour == null) {
            fail("Please select both a valid date and hour.");
            return null;
        }
        if (hour < 8 || hour > 20) {
            fail("Hospital working hours are 08:00 to 20:00.");
            return null;
        }
        LocalDateTime dt = LocalDateTime.of(date, LocalTime.of(hour, 0));
        if (dt.isBefore(LocalDateTime.now(clock))) {
            fail("Cannot schedule or reschedule in the past.");
            return null;
        }
        return dt;
    }

    private void fail(String msg) { this.statusMessage = msg; this.isError = true; }
    private void succeed(String msg) { this.statusMessage = msg; this.isError = false; }

    public boolean canCompleteAppointments() {
        return currentUser.getRole() == Role.DOCTOR;
    }

    public List<Doctor> getAvailableDoctors() {
        if (currentUser instanceof Doctor doc) return List.of(doc);
        return userRepository.findAll().stream()
                .filter(u -> u instanceof Doctor)
                .map(u -> (Doctor) u)
                .toList();
    }

    public List<Patient> getAvailablePatients() {
        return userRepository.findAll().stream()
                .filter(u -> u instanceof Patient)
                .map(u -> (Patient) u)
                .toList();
    }

    public List<Appointment> getVisibleAppointments() {
        if (currentUser instanceof Doctor doc) {
            return appointmentRepository.findByDoctorId(doc.getId());
        }
        return appointmentRepository.findAll();
    }

    public String getStatusMessage() { return statusMessage; }
    public boolean isError() { return isError; }
}

