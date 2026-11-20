package app.ui.controllers;

import app.domain.*;
import app.storage.*;

import java.util.List;
import java.util.Optional;

public class AdminController {
    private final User currentUser;
    private final UserRepository userRepo;
    private final DepartmentRepository deptRepo;

    private String statusMessage = "";
    private boolean isError = false;

    public AdminController(User currentUser, UserRepository userRepo, DepartmentRepository deptRepo) {
        this.currentUser = currentUser;
        this.userRepo = userRepo;
        this.deptRepo = deptRepo;
    }

    private boolean ensureAdmin() {
        if (currentUser.getRole() != Role.ADMIN) {
            fail("Access Denied: Admin privileges required.");
            return false;
        }
        return true;
    }

    // Scenario 1a: User CRUD (Employees, Doctors, Patients)
    public void saveUser(Role role, String id, String name, String phone, String password, String speciality) {
        if (!ensureAdmin()) return;
        if (role == null || isBlank(id) || isBlank(name) || isBlank(password)) {
            fail("Role, ID, Name, and Password are required.");
            return;
        }
        if (role == Role.DOCTOR && isBlank(speciality)) {
            fail("Doctors must have a speciality.");
            return;
        }

        // Preserve existing departmentId if updating an existing Doctor
        String existingDeptId = userRepo.findById(id.trim())
                .filter(u -> u instanceof Doctor)
                .map(u -> ((Doctor) u).getDepartmentId())
                .orElse(null);

        User user = switch (role) {
            case ADMIN    -> new Admin(id.trim(), name.trim(), phone, password);
            case EMPLOYEE -> new Employee(id.trim(), name.trim(), phone, password);
            case DOCTOR   -> new Doctor(id.trim(), name.trim(), phone, password, speciality.trim(), existingDeptId);
            case PATIENT  -> new Patient(id.trim(), name.trim(), phone, password);
        };

        userRepo.save(user);
        succeed("Saved " + role + ": " + user.getName() + " (" + user.getId() + ")");
    }

    public void deleteUser(String userId) {
        if (!ensureAdmin()) return;
        if (isBlank(userId)) { fail("Select a user to delete."); return; }
        if (userId.equals(currentUser.getId())) { fail("Cannot delete your own logged-in Admin account."); return; }

        userRepo.deleteById(userId.trim());
        succeed("Deleted user " + userId);
    }

    public List<User> searchUsers(String query) {
        if (isBlank(query)) return userRepo.findAll();
        return userRepo.searchByNameOrId(query);
    }

    // Scenario 1b: Department CRUD
    public void saveDepartment(String id, String name, String requiredSpeciality) {
        if (!ensureAdmin()) return;
        if (isBlank(id) || isBlank(name) || isBlank(requiredSpeciality)) {
            fail("Department ID, Name, and Required Speciality are required.");
            return;
        }
        deptRepo.save(new Department(id.trim(), name.trim(), requiredSpeciality.trim()));
        succeed("Saved Department: " + name.trim());
    }

    public void deleteDepartment(String deptId) {
        if (!ensureAdmin()) return;
        if (isBlank(deptId)) { fail("Select a department to delete."); return; }
        deptRepo.deleteById(deptId.trim());
        succeed("Deleted department " + deptId);
    }

    public List<Department> searchDepartments(String query) {
        if (isBlank(query)) return deptRepo.findAll();
        return deptRepo.searchByNameOrId(query);
    }

    // Scenario 2: Assign Doctor to ONE Department matching their speciality
    public void assignDoctorToDepartment(String doctorId, String deptId) {
        if (!ensureAdmin()) return;
        if (isBlank(doctorId) || isBlank(deptId)) {
            fail("Please select both a Doctor and a Department.");
            return;
        }

        Optional<User> userOpt = userRepo.findById(doctorId.trim());
        if (userOpt.isEmpty() || !(userOpt.get() instanceof Doctor doctor)) {
            fail("Doctor not found: " + doctorId);
            return;
        }

        Optional<Department> deptOpt = deptRepo.findById(deptId.trim());
        if (deptOpt.isEmpty()) {
            fail("Department not found: " + deptId);
            return;
        }

        Department dept = deptOpt.get();
        if (!doctor.assignToDepartment(dept)) {
            fail("Speciality mismatch: Dr. " + doctor.getName() + " (" + doctor.getSpeciality()
                    + ") cannot be assigned to " + dept.getName() + " (" + dept.getRequiredSpeciality() + ").");
            return;
        }

        userRepo.save(doctor);
        succeed("Assigned " + doctor.getName() + " to " + dept.getName() + ".");
    }

    private boolean isBlank(String s) { return s == null || s.isBlank(); }
    private void fail(String msg) { this.statusMessage = msg; this.isError = true; }
    private void succeed(String msg) { this.statusMessage = msg; this.isError = false; }

    public String getStatusMessage() { return statusMessage; }
    public boolean isError() { return isError; }
}

