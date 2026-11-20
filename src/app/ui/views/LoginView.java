package app.ui.views;

import app.ui.controllers.AuthController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginView extends VBox {

    public LoginView(AuthController authController, Runnable onLoginSuccess) {
        setSpacing(12);
        setPadding(new Insets(30));
        setAlignment(Pos.CENTER);

        Label title = new Label("Hospital Management System");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label hint = new Label("Test IDs: A01 (Admin), E01 (Employee), D01 (Doctor), P01 (Patient) | Pass: 1234");
        hint.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");

        TextField idField = new TextField();
        idField.setPromptText("User ID (e.g., A01, D01)");
        idField.setMaxWidth(280);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setMaxWidth(280);

        Button loginBtn = new Button("Login");
        loginBtn.setDefaultButton(true);
        loginBtn.setPrefWidth(280);

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");

        loginBtn.setOnAction(e -> {
            if (authController.login(idField.getText(), passField.getText())) {
                statusLabel.setText("");
                onLoginSuccess.run();
            } else {
                statusLabel.setText(authController.getStatusMessage());
            }
        });

        getChildren().addAll(title, hint, idField, passField, loginBtn, statusLabel);
    }
}

