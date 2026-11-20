package app.ui.views;

import app.domain.*;
import app.ui.controllers.MedicalRecordController;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;

public class MedicalRecordView extends VBox {

    public MedicalRecordView(MedicalRecordController controller) {
        setSpacing(10);
        setPadding(new Insets(20));

        Label title = new Label("Medical Records");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label statusLabel = new Label();
        ListView<MedicalRecord> recordsList = new ListView<>();
        recordsList.getItems().setAll(controller.searchRecords(""));

        // Doctor-only Consultation Editor (Hidden for Admin, Employee, Patient)
        VBox doctorEditor = new VBox(8);
        if (controller.canEditRecords()) {
            ComboBox<Appointment> apptBox = new ComboBox<>();
            apptBox.getItems().setAll(controller.getDoctorConsultations());
            if (!apptBox.getItems().isEmpty()) apptBox.getSelectionModel().selectFirst();

            DatePicker visitDate = new DatePicker(LocalDate.now());
            TextField symptomsField = new TextField(); symptomsField.setPromptText("Patient Symptoms");
            TextField diagnosisField = new TextField(); diagnosisField.setPromptText("Diagnosis");
            Button saveRecordBtn = new Button("Save / Update Consultation Record");

            saveRecordBtn.setOnAction(e -> {
                controller.saveConsultationRecord(
                        apptBox.getValue(), visitDate.getValue(),
                        symptomsField.getText(), diagnosisField.getText()
                );
                statusLabel.setText(controller.getStatusMessage());
                statusLabel.setStyle(controller.isError() ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
                recordsList.getItems().setAll(controller.searchRecords(""));
            });

            doctorEditor.getChildren().addAll(
                    new Label("Record Consultation (Doctors Only):"),
                    apptBox,
                    new HBox(8, visitDate, symptomsField, diagnosisField),
                    saveRecordBtn,
                    statusLabel,
                    new Separator()
            );
        }

        TextField searchField = new TextField();
        searchField.setPromptText("Search medical records by Patient ID, Doctor, Symptoms, or Diagnosis...");
        searchField.textProperty().addListener((obs, oldV, newV) ->
                recordsList.getItems().setAll(controller.searchRecords(newV)));

        getChildren().addAll(title, doctorEditor, searchField, recordsList);
    }
}

