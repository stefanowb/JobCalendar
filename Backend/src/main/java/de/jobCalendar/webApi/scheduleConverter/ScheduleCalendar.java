package main.java.de.jobCalendar.webApi.scheduleConverter;

import org.json.JSONObject;

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
    public String getBackgroundColor() {
        return backgroundColor;
    }
    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    public String getBorderColor() {
        return borderColor;
    }
    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }
    public String getTextColor() {
        return textColor;
    }
    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    private String id;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private String backgroundColor;
    private String borderColor;
    private String textColor;

    public JSONObject toJSON(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", this.getId());
        jsonObject.put("title", this.getTitle());
        jsonObject.put("start", this.getStart());
        jsonObject.put("end", this.getEnd());
        jsonObject.put("backgroundColor", this.getBackgroundColor());
        jsonObject.put("borderColor", this.getBorderColor());
        jsonObject.put("textColor", this.getTextColor());

        return jsonObject;
    }
}
