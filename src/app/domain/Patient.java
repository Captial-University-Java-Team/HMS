package app.domain;

public class Patient extends User {
    public Patient(String id, String name, String phone, String password) {
        super(id, name, phone, password, Role.PATIENT);
    }
}

