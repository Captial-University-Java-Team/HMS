package app.ui.views;

import app.domain.*;
import app.ui.controllers.AdminController;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AdminView extends VBox {

    public AdminView(AdminController controller) {
        setSpacing(10);
        setPadding(new Insets(15));

        Label statusLabel = new Label();
        Runnable showStatus = () -> {
            statusLabel.setText(controller.getStatusMessage());
            statusLabel.setStyle(controller.isError() ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        };

        // --- TAB 1: USERS CRUD & SEARCH ---
        ComboBox<Role> roleBox = new ComboBox<>();
        roleBox.getItems().setAll(Role.EMPLOYEE, Role.DOCTOR, Role.PATIENT, Role.ADMIN);
        roleBox.setValue(Role.DOCTOR);

        TextField uId = new TextField(); uId.setPromptText("ID (e.g. D03)");
        TextField uName = new TextField(); uName.setPromptText("Full Name");
        TextField uPhone = new TextField(); uPhone.setPromptText("Phone");
        TextField uPass = new TextField(); uPass.setPromptText("Password");
        TextField uSpec = new TextField(); uSpec.setPromptText("Speciality (Doctors only)");

        Button saveUserBtn = new Button("Save / Update User");
        Button delUserBtn = new Button("Delete Selected");
        TextField searchUserField = new TextField(); searchUserField.setPromptText("Search user by ID or Name...");

        ListView<User> userList = new ListView<>();
        userList.getItems().setAll(controller.searchUsers(""));

        saveUserBtn.setOnAction(e -> {
            controller.saveUser(roleBox.getValue(), uId.getText(), uName.getText(), uPhone.getText(), uPass.getText(), uSpec.getText());
            showStatus.run();
            userList.getItems().setAll(controller.searchUsers(searchUserField.getText()));
        });
        delUserBtn.setOnAction(e -> {
            User sel = userList.getSelectionModel().getSelectedItem();
            controller.deleteUser(sel == null ? null : sel.getId());
            showStatus.run();
            userList.getItems().setAll(controller.searchUsers(searchUserField.getText()));
        });
        searchUserField.textProperty().addListener((obs, oldV, newV) ->
                userList.getItems().setAll(controller.searchUsers(newV)));

        VBox usersTabContent = new VBox(8,
                new HBox(8, roleBox, uId, uName),
                new HBox(8, uPhone, uPass, uSpec),
                new HBox(8, saveUserBtn, delUserBtn),
                searchUserField, userList
        );
        usersTabContent.setPadding(new Insets(10));

        // --- TAB 2: DEPARTMENTS & DOCTOR ASSIGNMENT ---
        TextField dId = new TextField(); dId.setPromptText("Dept ID (e.g. DEPT-1)");
        TextField dName = new TextField(); dName.setPromptText("Department Name");
        TextField dSpec = new TextField(); dSpec.setPromptText("Required Speciality");
        Button saveDeptBtn = new Button("Save Department");
        Button delDeptBtn = new Button("Delete Selected");

        TextField searchDeptField = new TextField(); searchDeptField.setPromptText("Search department...");
        ListView<Department> deptList = new ListView<>();
        deptList.getItems().setAll(controller.searchDepartments(""));

        TextField assignDocId = new TextField(); assignDocId.setPromptText("Doctor ID (e.g. D01)");
        TextField assignDeptId = new TextField(); assignDeptId.setPromptText("Dept ID (e.g. DEPT-1)");
        Button assignBtn = new Button("Assign Doctor to Department");

        saveDeptBtn.setOnAction(e -> {
            controller.saveDepartment(dId.getText(), dName.getText(), dSpec.getText());
            showStatus.run();
            deptList.getItems().setAll(controller.searchDepartments(searchDeptField.getText()));
        });
        delDeptBtn.setOnAction(e -> {
            Department sel = deptList.getSelectionModel().getSelectedItem();
            controller.deleteDepartment(sel == null ? null : sel.getId());
            showStatus.run();
            deptList.getItems().setAll(controller.searchDepartments(searchDeptField.getText()));
        });
        searchDeptField.textProperty().addListener((obs, oldV, newV) ->
                deptList.getItems().setAll(controller.searchDepartments(newV)));

        assignBtn.setOnAction(e -> {
            controller.assignDoctorToDepartment(assignDocId.getText(), assignDeptId.getText());
            showStatus.run();
            userList.getItems().setAll(controller.searchUsers(""));
        });

        VBox deptTabContent = new VBox(8,
                new HBox(8, dId, dName, dSpec, saveDeptBtn, delDeptBtn),
                searchDeptField, deptList,
                new Separator(),
                new Label("Assign Doctor to Matching Department:"),
                new HBox(8, assignDocId, assignDeptId, assignBtn)
        );
        deptTabContent.setPadding(new Insets(10));

        TabPane tabs = new TabPane(
                new Tab("Users Management", usersTabContent),
                new Tab("Departments & Assignment", deptTabContent)
        );
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        getChildren().addAll(tabs, statusLabel);
    }
}

