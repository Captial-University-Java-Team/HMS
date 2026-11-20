package app.tests;

import app.domain.*;
import app.storage.*;
import app.ui.controllers.AuthController;

import java.nio.file.Files;
import java.nio.file.Path;

public class AuthTest {

    public static void main(String[] args) throws Exception {
        testLoginAndLogoutForAllRoles();
        testProfileUpdatePreservesIdAndPersists();
        testSqliteUserPersistenceAndSearch();

        System.out.println("All Auth & SQLite User tests passed!");
    }

    static void testLoginAndLogoutForAllRoles() {
        UserRepository repo = new InMemoryUserRepository();
        repo.save(new Admin("A01", "Mona", "0100001", "admin123"));
        repo.save(new Employee("E01", "Tarek", "0100002", "emp123"));
        repo.save(new Doctor("D01", "Dr. Omar", "0100003", "doc123", "Cardiology", "DEPT-1"));
        repo.save(new Patient("P01", "Nour", "0100004", "pat123"));

        AuthController auth = new AuthController(repo);

        // 1. Blank or wrong credentials fail
        assert !auth.login("", "doc123");
        assert !auth.login("D01", "wrong-password");
        assert !auth.isAuthenticated();

        // 2. Valid login loads correct subclass & role
        assert auth.login("D01", "doc123");
        assert auth.isAuthenticated();
        assert auth.getCurrentUser().getRole() == Role.DOCTOR;
        assert auth.getCurrentUser() instanceof Doctor;

        // 3. Logout clears active session
        auth.logout();
        assert !auth.isAuthenticated();
        assert auth.getCurrentUser() == null;
    }

    static void testProfileUpdatePreservesIdAndPersists() {
        UserRepository repo = new InMemoryUserRepository();
        repo.save(new Patient("P01", "Nour", "0100004", "oldpass"));

        AuthController auth = new AuthController(repo);
        auth.login("P01", "oldpass");

        // Update personal info (ID remains locked as P01)
        boolean updated = auth.updateCurrentUserProfile("Nour Hassan", "0119999", "newpass");
        assert updated;
        assert auth.getCurrentUser().getId().equals("P01");
        assert auth.getCurrentUser().getName().equals("Nour Hassan");
        assert auth.getCurrentUser().getPhone().equals("0119999");

        // Re-login verifies new password persisted
        auth.logout();
        assert !auth.login("P01", "oldpass");
        assert auth.login("P01", "newpass");
    }

    static void testSqliteUserPersistenceAndSearch() throws Exception {
        Path tempDb = Files.createTempFile("hms_auth_test", ".db");
        try {
            // 1. Save users into SQLite
            UserRepository sqliteRepo = new SqliteUserRepository(tempDb.toString());
            sqliteRepo.save(new Admin("A01", "Mona Ali", "0100001", "admin123"));
            sqliteRepo.save(new Doctor("D02", "Dr. Salma", "0123456", "pass123", "Neurology", "DEPT-2"));

            // 2. Open a fresh repository pointing to the same SQLite file (simulates app restart)
            UserRepository reloadedRepo = new SqliteUserRepository(tempDb.toString());

            // Verify Doctor fields reloaded accurately
            User loadedUser = reloadedRepo.findById("D02").orElseThrow();
            assert loadedUser instanceof Doctor doc
                    && doc.getSpeciality().equals("Neurology")
                    && "DEPT-2".equals(doc.getDepartmentId());

            // Verify SQL LIKE search by name or ID
            assert reloadedRepo.searchByNameOrId("salma").size() == 1;
            assert reloadedRepo.searchByNameOrId("A01").size() == 1;

            // 3. Authenticate directly against SQLite and update profile
            AuthController auth = new AuthController(reloadedRepo);
            assert auth.login("D02", "pass123");
            assert auth.updateCurrentUserProfile("Dr. Salma Mahmoud", "0199999", "updatedPass");

            // 4. Verify update persisted in SQLite
            User afterUpdate = reloadedRepo.findById("D02").orElseThrow();
            assert afterUpdate.getName().equals("Dr. Salma Mahmoud");
            assert afterUpdate.checkPassword("updatedPass");

        } finally {
            Files.deleteIfExists(tempDb);
        }
    }
}

