package app.ui.views;

import app.domain.*;
import app.ui.controllers.ReportController;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ReportView extends VBox {

    public ReportView(ReportController controller) {
        setSpacing(10);
        setPadding(new Insets(20));

        Label title = new Label("Admin Reports");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Filter controls (Doctor, Status, From/To Date)
        TextField doctorIdField = new TextField();
        doctorIdField.setPromptText("Doctor ID (e.g. D01, or blank for all)");

        ComboBox<AppointmentStatus> statusBox = new ComboBox<>();
        statusBox.getItems().add(null); // null = All statuses
        statusBox.getItems().addAll(AppointmentStatus.values());
        statusBox.setPromptText("All Statuses");

        DatePicker fromDate = new DatePicker();
        fromDate.setPromptText("From Date (optional)");
        DatePicker toDate = new DatePicker();
        toDate.setPromptText("To Date (optional)");

        Button apptReportBtn = new Button("Generate Appointment Report");
        Button workloadReportBtn = new Button("Doctor Workload Report");

        ListView<String> reportOutput = new ListView<>();

        apptReportBtn.setOnAction(e -> {
            var results = controller.generateAppointmentReport(
                    doctorIdField.getText(), statusBox.getValue(), fromDate.getValue(), toDate.getValue()
            );
            reportOutput.getItems().setAll(results.stream().map(Appointment::toString).toList());
            if (results.isEmpty()) reportOutput.getItems().add("No appointments match the selected criteria.");
        });

        workloadReportBtn.setOnAction(e ->
                reportOutput.getItems().setAll(controller.generateDoctorWorkloadReport()));

        getChildren().addAll(
                title,
                new HBox(8, doctorIdField, statusBox, fromDate, toDate),
                new HBox(10, apptReportBtn, workloadReportBtn),
                reportOutput
        );
    }
}

