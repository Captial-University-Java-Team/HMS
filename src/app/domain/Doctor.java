package app.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Doctor extends User {
    private String speciality;
    private String departmentId;
    private final List<Appointment> appointments = new ArrayList<>();

    public Doctor(String id, String name, String phone, String password, String speciality, String departmentId) {
        super(id, name, phone, password, Role.DOCTOR);
        this.speciality = speciality;
        this.departmentId = departmentId;
    }

    public boolean scheduleNewAppointment(LocalDateTime date) {
        if (hasAppointmentAt(date)) return false;
        appointments.add(new Appointment(date));
        return true;
    }

    private boolean hasAppointmentAt(LocalDateTime date) {
        return appointments.stream().anyMatch(a -> a.getDate().equals(date));
    }

    public String getSpeciality() { return speciality; }
    public void setSpeciality(String speciality) { this.speciality = speciality; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public List<Appointment> getAppointments() { return List.copyOf(appointments); }
}

