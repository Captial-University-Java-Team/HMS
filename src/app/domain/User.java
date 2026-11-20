package app.domain;

public abstract class User {
    private final String id; 
    private String name;
    private String phone;
    private String password;
    private final Role role;

    protected User(String id, String name, String phone, String password, Role role) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.role = role;
    }

    public boolean checkPassword(String inputPassword) {
        return this.password != null && this.password.equals(inputPassword);
    }

    public void updateProfile(String newName, String newPhone, String newPassword) {
        this.name = newName.trim();
        this.phone = newPhone.trim();
        this.password = newPassword;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
}

