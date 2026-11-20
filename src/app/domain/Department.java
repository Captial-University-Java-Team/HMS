package app.domain;

public class Department {
    private final String id;
    private String name;
    private String requiredSpeciality;

    public Department(String id, String name, String requiredSpeciality) {
        this.id = id;
        this.name = name;
        this.requiredSpeciality = requiredSpeciality;
    }

    public void update(String newName, String newSpeciality) {
        this.name = newName.trim();
        this.requiredSpeciality = newSpeciality.trim();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getRequiredSpeciality() { return requiredSpeciality; }

    @Override
    public String toString() {
        return id + " - " + name + " (Speciality: " + requiredSpeciality + ")";
    }
}

