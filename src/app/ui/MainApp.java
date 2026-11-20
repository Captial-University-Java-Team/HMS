package app.ui;

import app.domain.*;
import app.storage.*;
import app.ui.controllers.*;
import app.ui.views.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainApp extends Application {

    private UserRepository userRepository;
    private AppointmentRepository appointmentRepository;
    private DepartmentRepository departmentRepository;
    private MedicalRecordRepository medicalRecordRepository;
    private AuthController authController;
    private Stage primaryStage;

    @Override
    public void init() {
        // 1. Initialize SQLite persistence inside ~/.hms/hms.db
        Path dbPath = Path.of(System.getProperty("user.home"), ".hms", "hms.db");

        try {
            Files.createDirectories(dbPath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create database directory", e);
        }

        String dbUrl = dbPath.toString();
        this.userRepository = new SqliteUserRepository(dbUrl);
        this.appointmentRepository = new SqliteAppointmentRepository(dbUrl);
        this.departmentRepository = new SqliteDepartmentRepository(dbUrl);
        this.medicalRecordRepository = new SqliteMedicalRecordRepository(dbUrl);

        // 2. Seed initial departments and users if missing
        seedDepartmentIfMissing(new Department("DEPT-1", "Cardiology Department", "Cardiology"));
        seedDepartmentIfMissing(new Department("DEPT-2", "Dental Clinic", "Dentist"));

        seedUserIfMissing(new Admin("A01", "Mona (Admin)", "0100001", "1234"));
        seedUserIfMissing(new Employee("E01", "Tarek (Staff)", "0100002", "1234"));

        seedUserIfMissing(new Doctor("D01", "Dr. Omar", "0100003", "1234", "Cardiology", "DEPT-1"));
        seedUserIfMissing(new Doctor("D02", "Dr. Ahmed", "0100023", "1234", "Dentist", "DEPT-2"));

        seedUserIfMissing(new Patient("P01", "Nour (Patient)", "0100004", "1234"));
        seedUserIfMissing(new Patient("P02", "Ali (Patient)", "0100082", "1234"));

        this.authController = new AuthController(userRepository);
    }

    private void seedUserIfMissing(User user) {
        if (userRepository.findById(user.getId()).isEmpty()) {
            userRepository.save(user);
        }
    }

    private void seedDepartmentIfMissing(Department dept) {
        if (departmentRepository.findById(dept.getId()).isEmpty()) {
            departmentRepository.save(dept);
        }
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Hospital Management System");
        stage.setScene(new Scene(createLoginScreen(), 920, 600));
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

        // Left Sidebar Navigation
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(15));
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #d0d7de; -fx-border-width: 0 1 0 0;");

        // View Factories (Creates fresh views on tab click so dropdowns/lists always reflect latest SQLite data)
        Runnable showProfile = () -> shell.setCenter(new ProfileView(authController, () ->
                userBanner.setText("Logged in as: " + authController.getCurrentUser().getName()
                        + " [" + authController.getCurrentUser().getRole() + "]")
        ));

        Runnable showAdmin = () -> shell.setCenter(new AdminView(
                new AdminController(currentUser, userRepository, departmentRepository)
        ));

        Runnable showAppointments = () -> shell.setCenter(new AppointmentView(
                new AppointmentController(currentUser, appointmentRepository, userRepository, Clock.systemDefaultZone())
        ));

        Runnable showRecords = () -> shell.setCenter(new MedicalRecordView(
                new MedicalRecordController(currentUser, medicalRecordRepository, appointmentRepository)
        ));

        Runnable showReports = () -> shell.setCenter(new ReportView(
                new ReportController(currentUser, appointmentRepository, userRepository, medicalRecordRepository)
        ));

        // Build role-specific navigation buttons & default landing screen
        switch (currentUser.getRole()) {
            case ADMIN -> {
                Button btnAdmin = navButton("Users & Departments");
                Button btnAppts = navButton("Appointments");
                Button btnRecords = navButton("Medical Records");
                Button btnReports = navButton("Reports");
                Button btnProfile = navButton("My Profile");

                btnAdmin.setOnAction(e -> showAdmin.run());
                btnAppts.setOnAction(e -> showAppointments.run());
                btnRecords.setOnAction(e -> showRecords.run());
                btnReports.setOnAction(e -> showReports.run());
                btnProfile.setOnAction(e -> showProfile.run());

                sidebar.getChildren().addAll(btnAdmin, btnAppts, btnRecords, btnReports, btnProfile);
                showAdmin.run();
            }

            case EMPLOYEE -> {
                Button btnAppts = navButton("Appointments");
                Button btnRecords = navButton("Patient Records");
                Button btnProfile = navButton("My Profile");

                btnAppts.setOnAction(e -> showAppointments.run());
                btnRecords.setOnAction(e -> showRecords.run());
                btnProfile.setOnAction(e -> showProfile.run());

                sidebar.getChildren().addAll(btnAppts, btnRecords, btnProfile);
                showAppointments.run();
            }

            case DOCTOR -> {
                Button btnAppts = navButton("My Appointments");
                Button btnRecords = navButton("Consultation Records");
                Button btnProfile = navButton("My Profile");

                btnAppts.setOnAction(e -> showAppointments.run());
                btnRecords.setOnAction(e -> showRecords.run());
                btnProfile.setOnAction(e -> showProfile.run());

                sidebar.getChildren().addAll(btnAppts, btnRecords, btnProfile);
                showAppointments.run();
            }

            case PATIENT -> {
                Button btnRecords = navButton("My Medical History");
                Button btnProfile = navButton("My Profile");

                btnRecords.setOnAction(e -> showRecords.run());
                btnProfile.setOnAction(e -> showProfile.run());

                sidebar.getChildren().addAll(btnRecords, btnProfile);
                showRecords.run();
            }
        }

        shell.setLeft(sidebar);
        return shell;
    }

    private Button navButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 8 12;");
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

