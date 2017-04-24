package main.java.de.jobCalendar.webApi.taskScheduler;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by Stefan on 21.04.2017.
 */
public class IntervalSplitter {

    private int intervalDays;
    private LocalTime intervalTime;

    public IntervalSplitter(String intervalString, DateTimeFormatter intervalFormatter){

        if (intervalString.contains(".")){
            String[] intervalSplit = intervalString.split("[.]");

            intervalDays = Integer.parseInt(intervalSplit[0]);
            intervalTime = LocalTime.parse(intervalSplit[1], intervalFormatter);
        } else {
            intervalDays = 0;
            intervalTime = LocalTime.parse(intervalString, intervalFormatter);
        }
    }

    public int getIntervalDays() {
        return intervalDays;
    }

    public LocalTime getIntervalTime() {
        return intervalTime;
    }

}
