package app.ui.views;

import app.domain.*;
import app.ui.controllers.AppointmentController;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;

public class AppointmentView extends VBox {

    public AppointmentView(AppointmentController controller) {
        setSpacing(10);
        setPadding(new Insets(20));

        Label title = new Label("Appointment Management");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Doctor & Patient selectors
        ComboBox<Doctor> doctorBox = new ComboBox<>();
        doctorBox.getItems().setAll(controller.getAvailableDoctors());
        if (!doctorBox.getItems().isEmpty()) doctorBox.getSelectionModel().selectFirst();
        doctorBox.setConverter(new StringConverter<>() {
            @Override public String toString(Doctor d) { return d == null ? "" : d.getName() + " (" + d.getId() + ")"; }
            @Override public Doctor fromString(String s) { return null; }
        });

        ComboBox<Patient> patientBox = new ComboBox<>();
        patientBox.getItems().setAll(controller.getAvailablePatients());
        if (!patientBox.getItems().isEmpty()) patientBox.getSelectionModel().selectFirst();
        patientBox.setConverter(new StringConverter<>() {
            @Override public String toString(Patient p) { return p == null ? "" : p.getName() + " (" + p.getId() + ")"; }
            @Override public Patient fromString(String s) { return null; }
        });

        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        Spinner<Integer> hourSpinner = new Spinner<>(8, 20, 10);

        Button bookBtn = new Button("Book New");
        Button rescheduleBtn = new Button("Reschedule Selected (to Date/Hour above)");
        Button cancelBtn = new Button("Cancel Selected");
        Button completeBtn = new Button("Mark Completed");

        // Only Doctors see the "Mark Completed" button (Scenario 5)
        completeBtn.setVisible(controller.canCompleteAppointments());
        completeBtn.setManaged(controller.canCompleteAppointments());

        HBox actionButtons = new HBox(10, bookBtn, rescheduleBtn, cancelBtn, completeBtn);

        Label statusLabel = new Label();
        ListView<Appointment> appointmentList = new ListView<>();
        appointmentList.getItems().setAll(controller.getVisibleAppointments());

        Runnable refresh = () -> {
            statusLabel.setText(controller.getStatusMessage());
            statusLabel.setStyle(controller.isError() ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
            appointmentList.getItems().setAll(controller.getVisibleAppointments());
        };

        bookBtn.setOnAction(e -> {
            controller.scheduleNewAppointment(
                    doctorBox.getValue(),
                    patientBox.getValue(),
                    datePicker.getValue(),
                    hourSpinner.getValue()
            );
            refresh.run();
        });

        rescheduleBtn.setOnAction(e -> {
            Appointment selected = appointmentList.getSelectionModel().getSelectedItem();
            controller.rescheduleAppointment(selected, datePicker.getValue(), hourSpinner.getValue());
            refresh.run();
        });

        cancelBtn.setOnAction(e -> {
            Appointment selected = appointmentList.getSelectionModel().getSelectedItem();
            controller.cancelAppointment(selected);
            refresh.run();
        });

        completeBtn.setOnAction(e -> {
            Appointment selected = appointmentList.getSelectionModel().getSelectedItem();
            controller.completeAppointment(selected);
            refresh.run();
        });

        getChildren().addAll(
                title,
                new Label("Doctor:"), doctorBox,
                new Label("Patient:"), patientBox,
                new Label("Date & Hour (08 - 20):"), new HBox(10, datePicker, hourSpinner),
                actionButtons,
                statusLabel,
                new Label("Appointments (Select one to Reschedule / Cancel / Complete):"),
                appointmentList
        );
    }
}

