package app.tests;

import app.domain.*;
import app.storage.*;
import app.ui.controllers.*;

import java.nio.file.*;
import java.time.*;

public class FullHmsModulesTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            LocalDateTime.of(2026, 11, 20, 9, 0).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC
    );

    public static void main(String[] args) throws Exception {
        Path tempDb = Files.createTempFile("hms_full_test", ".db");
        try {
            UserRepository userRepo = new SqliteUserRepository(tempDb.toString());
            DepartmentRepository deptRepo = new SqliteDepartmentRepository(tempDb.toString());
            AppointmentRepository apptRepo = new SqliteAppointmentRepository(tempDb.toString());
            MedicalRecordRepository recordRepo = new SqliteMedicalRecordRepository(tempDb.toString());

            Admin admin = new Admin("A01", "Mona", "01001", "1234");
            Employee emp = new Employee("E01", "Tarek", "01002", "1234");
            Doctor doc1 = new Doctor("D01", "Dr. Omar", "01003", "1234", "Cardiology", null);
            Doctor doc2 = new Doctor("D02", "Dr. Ahmed", "01004", "1234", "Dentistry", null);
            Patient pat1 = new Patient("P01", "Nour", "01005", "1234");
            Patient pat2 = new Patient("P02", "Ali", "01006", "1234");

            userRepo.save(admin); userRepo.save(emp);
            userRepo.save(doc1);  userRepo.save(doc2);
            userRepo.save(pat1);  userRepo.save(pat2);

            // --- SCENARIO 1 & 2: Admin Department CRUD & Speciality Matching ---
            AdminController adminCtrl = new AdminController(admin, userRepo, deptRepo);
            adminCtrl.saveDepartment("DEPT-1", "Heart Center", "Cardiology");
            assert !adminCtrl.isError();

            // Mismatched speciality (Dentistry -> Cardiology) must fail
            adminCtrl.assignDoctorToDepartment("D02", "DEPT-1");
            assert adminCtrl.isError();

            // Matching speciality (Cardiology -> Cardiology) succeeds
            adminCtrl.assignDoctorToDepartment("D01", "DEPT-1");
            assert !adminCtrl.isError();
            assert "DEPT-1".equals(((Doctor) userRepo.findById("D01").orElseThrow()).getDepartmentId());

            // --- SCENARIO 6 & 7: Medical Records & Permission Matrix ---
            AppointmentController apptCtrl = new AppointmentController(emp, apptRepo, userRepo, FIXED_CLOCK);
            LocalDate day = LocalDate.of(2026, 11, 21);
            apptCtrl.scheduleNewAppointment(doc1, pat1, day, 10);
            apptCtrl.scheduleNewAppointment(doc2, pat2, day, 11);

            Appointment appt1 = apptRepo.findByDoctorId("D01").get(0);
            Appointment appt2 = apptRepo.findByDoctorId("D02").get(0);

            MedicalRecordController doc1RecCtrl = new MedicalRecordController(doc1, recordRepo, apptRepo);
            MedicalRecordController doc2RecCtrl = new MedicalRecordController(doc2, recordRepo, apptRepo);
            MedicalRecordController empRecCtrl = new MedicalRecordController(emp, recordRepo, apptRepo);
            MedicalRecordController pat1RecCtrl = new MedicalRecordController(pat1, recordRepo, apptRepo);

            // Non-doctor cannot create records
            empRecCtrl.saveConsultationRecord(appt1, day, "Chest pain", "Angina");
            assert empRecCtrl.isError();

            // Doctors create records for their own consultations
            doc1RecCtrl.saveConsultationRecord(appt1, day, "Chest pain", "Mild Angina");
            assert !doc1RecCtrl.isError();
            doc2RecCtrl.saveConsultationRecord(appt2, day, "Toothache", "Cavity");
            assert !doc2RecCtrl.isError();

            // Scenario 7.1: Admin/Employee sees ALL records (2)
            assert empRecCtrl.searchRecords("").size() == 2;
            // Scenario 7.2: Doctor sees ONLY their patients' records (1)
            assert doc1RecCtrl.searchRecords("").size() == 1;
            // Scenario 7.3: Patient sees ONLY their own records (1)
            assert pat1RecCtrl.searchRecords("").size() == 1;
            assert pat1RecCtrl.searchRecords("").get(0).getPatientId().equals("P01");

            // --- SCENARIO 3: Admin Reports ---
            ReportController reportCtrl = new ReportController(admin, apptRepo, userRepo, recordRepo);
            assert reportCtrl.generateAppointmentReport("D01", null, null, null).size() == 1;
            assert reportCtrl.generateAppointmentReport(null, AppointmentStatus.SCHEDULED, day, day).size() == 2;
            assert reportCtrl.generateDoctorWorkloadReport().size() == 2;

            System.out.println("All Scenarios (1, 2, 3, 6, 7) passed!");
        } finally {
            Files.deleteIfExists(tempDb);
        }
    }
}

