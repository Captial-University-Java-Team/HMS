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

    public boolean scheduleNewAppointment(Appointment newAppt) {
        if (hasActiveAppointmentAt(newAppt.getDate())) return false;
        appointments.add(newAppt);
        return true;
    }

    private boolean hasActiveAppointmentAt(LocalDateTime date) {
        for (Appointment appt : appointments) {
            if (appt.getStatus() != AppointmentStatus.CANCELED && appt.getDate().equals(date)) {
                return true; }
        }
        return false;
    }
  
    
    public boolean assignToDepartment(Department department) {
        if (department == null || this.speciality == null) return false;
        if (!this.speciality.equalsIgnoreCase(department.getRequiredSpeciality())) {
            return false; 
        }

        this.departmentId = department.getId();
        return true;
    }

    public void loadAppointments(List<Appointment> existing) {
      appointments.clear();
      appointments.addAll(existing);
    }

    public String getSpeciality() { return speciality; }
    public void setSpeciality(String speciality) { this.speciality = speciality; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public List<Appointment> getAppointments() { return List.copyOf(appointments); }
}

