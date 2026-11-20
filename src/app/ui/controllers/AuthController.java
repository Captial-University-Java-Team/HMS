package app.ui.controllers;

import app.domain.User;
import app.storage.UserRepository;
import java.util.Optional;

public class AuthController {
    private final UserRepository userRepository;
    private User currentUser;
    private String statusMessage = "";
    private boolean isError = false;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean login(String id, String password) {
        if (id == null || id.isBlank() || password == null || password.isBlank()) {
            statusMessage = "Please enter both ID and password.";
            isError = true;
            return false;
        }

        Optional<User> found = userRepository.findById(id.trim());
        if (found.isEmpty() || !found.get().checkPassword(password)) {
            statusMessage = "Invalid ID or password.";
            isError = true;
            return false;
        }

        this.currentUser = found.get();
        this.statusMessage = "Welcome, " + currentUser.getName() + " (" + currentUser.getRole() + ")";
        this.isError = false;
        return true;
    }

    public void logout() {
        this.currentUser = null;
        this.statusMessage = "Logged out successfully.";
        this.isError = false;
    }

    public boolean updateCurrentUserProfile(String newName, String newPhone, String newPassword) {
        if (currentUser == null) {
            statusMessage = "No user is currently logged in.";
            isError = true;
            return false;
        }

        if (newName == null || newName.isBlank()) {
            statusMessage = "Name cannot be empty.";
            isError = true;
            return false;
        }

        currentUser.updateProfile(newName, newPhone, newPassword);
        userRepository.save(currentUser); // Persists updated profile immediately

        statusMessage = "Profile updated successfully (ID " + currentUser.getId() + " unchanged).";
        isError = false;
        return true;
    }

    public boolean isAuthenticated() { return currentUser != null; }
    public User getCurrentUser() { return currentUser; }
    public String getStatusMessage() { return statusMessage; }
    public boolean isError() { return isError; }
}

