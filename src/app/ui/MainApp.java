package app.ui;

import app.domain.*;
import app.storage.*;
import app.ui.controllers.*;
import app.ui.views.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainApp extends Application {

    private UserRepository userRepository;
    private AuthController authController;
    private Stage primaryStage;

    @Override
    public void init() {
        // 1. Initialize SQLite persistence
        Path dbPath = Path.of(
            System.getProperty("user.home"), ".hms", "hms.db"
        );

        try {
            Files.createDirectories(dbPath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create database directory", e);
        }

        this.userRepository = new SqliteUserRepository(dbPath.toString());

        // 2. Seed initial test accounts on first launch so you can test every role
        if (userRepository.findAll().isEmpty()) {
            userRepository.save(new Admin("A01", "Mona (Admin)", "0100001", "1234"));
            userRepository.save(new Employee("E01", "Tarek (Staff)", "0100002", "1234"));
            userRepository.save(new Doctor("D01", "Dr. Omar", "0100003", "1234", "Cardiology", "DEPT-1"));
            userRepository.save(new Patient("P01", "Nour (Patient)", "0100004", "1234"));
        }

        this.authController = new AuthController(userRepository);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Hospital Management System");
        stage.setScene(new Scene(createLoginScreen(), 860, 560));
        stage.show();
    }

    // Screen 1: Enforced Login Gate
    private LoginView createLoginScreen() {
        return new LoginView(authController, () -> {
            User currentUser = authController.getCurrentUser();
            primaryStage.getScene().setRoot(createDashboardShell(currentUser));
        });
    }

    // Screen 2: Role-Based Main Shell (Sidebar + Swappable Center Content)
    private BorderPane createDashboardShell(User currentUser) {
        BorderPane shell = new BorderPane();

        // Top Header Bar
        Label userBanner = new Label("Logged in as: " + currentUser.getName() + " [" + currentUser.getRole() + "]");
        userBanner.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> {
            authController.logout();
            primaryStage.getScene().setRoot(createLoginScreen());
        });

        HBox topBar = new HBox(15, userBanner, spacer, logoutBtn);
        topBar.setPadding(new Insets(12, 20, 12, 20));
        topBar.setStyle("-fx-background-color: #dce4ec; -fx-border-color: #b8c4d0; -fx-border-width: 0 0 1 0;");
        shell.setTop(topBar);

        // Left Sidebar Navigation (Built dynamically per Role!)
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(15));
        sidebar.setPrefWidth(190);
        sidebar.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #d0d7de; -fx-border-width: 0 1 0 0;");

        // Shared View available to all 4 roles
        ProfileView profileView = new ProfileView(authController, () -> {
            // Refresh top banner if user updates their name
            userBanner.setText("Logged in as: " + authController.getCurrentUser().getName()
                    + " [" + authController.getCurrentUser().getRole() + "]");
        });

        // Build role-specific navigation buttons
        switch (currentUser.getRole()) {
            case ADMIN -> {
                Button btnAdmin = navButton("Users & Departments");
                Button btnAppts = navButton("Appointments");
                Button btnRecords = navButton("Medical Records");
                Button btnReports = navButton("Reports");
                Button btnProfile = navButton("My Profile");

                btnAdmin.setOnAction(e -> shell.setCenter(placeholderView("Admin: Users & Departments")));
                btnAppts.setOnAction(e -> shell.setCenter(placeholderView("Appointments Management")));
                btnRecords.setOnAction(e -> shell.setCenter(placeholderView("All Medical Records")));
                btnReports.setOnAction(e -> shell.setCenter(placeholderView("Admin Reports")));
                btnProfile.setOnAction(e -> shell.setCenter(profileView));

                sidebar.getChildren().addAll(btnAdmin, btnAppts, btnRecords, btnReports, btnProfile);
            }
            case EMPLOYEE -> {
                Button btnAppts = navButton("Appointments");
                Button btnRecords = navButton("Patient Records");
                Button btnProfile = navButton("My Profile");

                btnAppts.setOnAction(e -> shell.setCenter(placeholderView("Appointments Management")));
                btnRecords.setOnAction(e -> shell.setCenter(placeholderView("All Medical Records")));
                btnProfile.setOnAction(e -> shell.setCenter(profileView));

                sidebar.getChildren().addAll(btnAppts, btnRecords, btnProfile);
            }
            case DOCTOR -> {
                Button btnAppts = navButton("My Appointments");
                Button btnRecords = navButton("My Patients' Records");
                Button btnProfile = navButton("My Profile");

                btnAppts.setOnAction(e -> shell.setCenter(placeholderView("Doctor: My Appointments")));
                btnRecords.setOnAction(e -> shell.setCenter(placeholderView("Doctor: Consultation Records")));
                btnProfile.setOnAction(e -> shell.setCenter(profileView));

                sidebar.getChildren().addAll(btnAppts, btnRecords, btnProfile);
            }
            case PATIENT -> {
                Button btnRecords = navButton("My Medical History");
                Button btnProfile = navButton("My Profile");

                btnRecords.setOnAction(e -> shell.setCenter(placeholderView("Patient: My Medical Records")));
                btnProfile.setOnAction(e -> shell.setCenter(profileView));

                sidebar.getChildren().addAll(btnRecords, btnProfile);
            }
        }

        shell.setLeft(sidebar);
        shell.setCenter(profileView); // Opens on My Profile right now; swap placeholders as you build each view

        return shell;
    }

    private Button navButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 8 12;");
        return btn;
    }

    // Temporary placeholder until you plug in AdminView, AppointmentView, RecordView, ReportView
    private VBox placeholderView(String title) {
        VBox box = new VBox(10, new Label(title));
        box.setPadding(new Insets(25));
        box.setStyle("-fx-font-size: 16px;");
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

