package app.domain;

public class Admin extends User {
    public Admin(String id, String name, String phone, String password) {
        super(id, name, phone, password, Role.ADMIN);
    }
}

