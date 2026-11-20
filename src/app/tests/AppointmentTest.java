package app.tests;

import app.domain.*;
import app.storage.*;
import app.ui.controllers.AppointmentController;

import java.nio.file.*;
import java.time.*;

public class AppointmentTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            LocalDateTime.of(2026, 11, 20, 9, 0).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC
    );

    public static void main(String[] args) throws Exception {
        Path tempDb = Files.createTempFile("hms_appt_test", ".db");
        try {
            UserRepository userRepo = new SqliteUserRepository(tempDb.toString());
            AppointmentRepository apptRepo = new SqliteAppointmentRepository(tempDb.toString());

            Employee staff = new Employee("E01", "Tarek", "01001", "1234");
            Doctor doc1 = new Doctor("D01", "Dr. Omar", "01002", "1234", "Cardiology", "DEPT-1");
            Doctor doc2 = new Doctor("D02", "Dr. Salma", "01003", "1234", "Neurology", "DEPT-2");
            Patient pat1 = new Patient("P01", "Nour", "01004", "1234");

            userRepo.save(staff);
            userRepo.save(doc1);
            userRepo.save(doc2);
            userRepo.save(pat1);

            AppointmentController staffCtrl = new AppointmentController(staff, apptRepo, userRepo, FIXED_CLOCK);
            LocalDate day = LocalDate.of(2026, 11, 21);

            // 1. Staff books appointment -> defaults to SCHEDULED
            staffCtrl.scheduleNewAppointment(doc1, pat1, day, 10);
            assert !staffCtrl.isError();
            Appointment booked = apptRepo.findByDoctorId("D01").get(0);
            assert booked.getStatus() == AppointmentStatus.SCHEDULED;

            // 2. Overlap at 10:00 blocked
            staffCtrl.scheduleNewAppointment(doc1, pat1, day, 10);
            assert staffCtrl.isError();

            // 3. Reschedule to 14:00 succeeds & frees up 10:00
            staffCtrl.rescheduleAppointment(booked, day, 14);
            assert !staffCtrl.isError();
            assert apptRepo.findById(booked.getId()).orElseThrow().getDate().getHour() == 14;

            // 4. Staff cannot mark appointment COMPLETED (only Doctor can)
            staffCtrl.completeAppointment(booked);
            assert staffCtrl.isError();

            // 5. Wrong doctor (D02) cannot complete D01's appointment
            AppointmentController doc2Ctrl = new AppointmentController(doc2, apptRepo, userRepo, FIXED_CLOCK);
            doc2Ctrl.completeAppointment(booked);
            assert doc2Ctrl.isError();

            // 6. Assigned doctor (D01) marks consultation COMPLETED
            AppointmentController doc1Ctrl = new AppointmentController(doc1, apptRepo, userRepo, FIXED_CLOCK);
            doc1Ctrl.completeAppointment(booked);
            assert !doc1Ctrl.isError();
            assert apptRepo.findById(booked.getId()).orElseThrow().getStatus() == AppointmentStatus.COMPLETED;

            // 7. Cannot cancel an already COMPLETED appointment
            staffCtrl.cancelAppointment(apptRepo.findById(booked.getId()).orElseThrow());
            assert staffCtrl.isError();

            System.out.println("All Appointment Lifecycle tests passed!");
        } finally {
            Files.deleteIfExists(tempDb);
        }
    }
}

