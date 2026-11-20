package app.ui.views;

import app.domain.User;
import app.ui.controllers.AuthController;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class ProfileView extends VBox {

    public ProfileView(AuthController authController, Runnable onProfileUpdated) {
        setSpacing(12);
        setPadding(new Insets(25));

        User user = authController.getCurrentUser();

        Label header = new Label("My Personal Profile");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Requirement 5b: ID cannot be updated
        TextField idField = new TextField(user.getId());
        idField.setDisable(true);
        idField.setMaxWidth(300);

        TextField nameField = new TextField(user.getName());
        nameField.setMaxWidth(300);

        TextField phoneField = new TextField(user.getPhone());
        phoneField.setMaxWidth(300);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Leave blank to keep current password");
        passField.setMaxWidth(300);

        Button saveBtn = new Button("Save Changes");
        Label statusLabel = new Label();

        saveBtn.setOnAction(e -> {
            boolean ok = authController.updateCurrentUserProfile(
                    nameField.getText(),
                    phoneField.getText(),
                    passField.getText()
            );
            statusLabel.setText(authController.getStatusMessage());
            statusLabel.setStyle(ok ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
            if (ok) {
                passField.clear();
                onProfileUpdated.run();
            }
        });

        getChildren().addAll(
                header,
                new Label("User ID (Read-Only):"), idField,
                new Label("Full Name:"), nameField,
                new Label("Phone Number:"), phoneField,
                new Label("New Password:"), passField,
                saveBtn,
                statusLabel
        );
    }
}

