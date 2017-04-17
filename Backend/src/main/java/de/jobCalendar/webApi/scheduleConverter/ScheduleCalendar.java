package main.java.de.jobCalendar.webApi.scheduleConverter;

import java.time.LocalDateTime;

public class ScheduleCalendar {
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public LocalDateTime getStart() {
        return start;
    }
    public void setStart(LocalDateTime start) {
        this.start = start;
    }
    public LocalDateTime getEnd() {
        return end;
    }
    public void setEnd(LocalDateTime end) {
        this.end = end;
    }
    private String id;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;

}
