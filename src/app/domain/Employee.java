package app.domain;

public class Employee extends User {
    public Employee(String id, String name, String phone, String password) {
        super(id, name, phone, password, Role.EMPLOYEE);
    }
}

