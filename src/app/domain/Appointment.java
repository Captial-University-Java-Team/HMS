package app.domain;

import java.time.LocalDateTime;

public class Appointment {
  private LocalDateTime date;

  public Appointment(LocalDateTime date) {
    this.date = date;
  }
  
  public LocalDateTime getDate() { return date; }
  public void updateDate(LocalDateTime newDate) { this.date = newDate; }

  public String toString() {
    return "Appointment{date=" + date + "}";
  }
}
