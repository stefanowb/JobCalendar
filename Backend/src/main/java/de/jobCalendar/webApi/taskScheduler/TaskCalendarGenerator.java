package main.java.de.jobCalendar.webApi.taskScheduler;

import main.java.de.jobCalendar.webApi.scheduleConverter.ScheduleCalendar;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;


/**
 * Created by Stefan on 19.04.2017.
 */
public class TaskCalendarGenerator {

    public static ArrayList<ScheduleCalendar> getScheduleCalendar(String taskSchedulerResult, LocalDateTime fromDate, LocalDateTime toDate){
        JSONArray taskArray = new JSONArray(taskSchedulerResult);
        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        DateTimeFormatter intervalFormatter = DateTimeFormatter.ISO_LOCAL_TIME;

        ArrayList<ScheduleCalendar> scheduleCalendarList = new ArrayList<ScheduleCalendar>();

        long daysInRange = fromDate.until(toDate, ChronoUnit.DAYS);

        for (Object taskObject : taskArray){
            JSONObject task = (JSONObject)taskObject;

            LocalDateTime start = LocalDateTime.parse(task.getString("start"), isoFormatter);
            JSONArray triggers = task.getJSONArray("triggers");

            for (Object triggerObject : triggers){
                JSONObject trigger = (JSONObject)triggerObject;
                ScheduleCalendar calenderEntry = new ScheduleCalendar();

                LocalDateTime startBoundary = LocalDateTime.parse(trigger.getString("startBoundary"), isoFormatter);
                LocalDateTime endBoundary = LocalDateTime.parse(trigger.getString("endBoundary"), isoFormatter);

                //Prüfe, ob sich der Trigger im Suchzeitraum befindet
                if (fromDate.isBefore(endBoundary) && toDate.isAfter(startBoundary)){

                    // Bereite das Repitition-Intervall auf
                    JSONObject repitition = null;
                    int intervalDays = 0;
                    LocalTime intervalTime = null;

                    if (trigger.isNull("repitition") == false){
                        trigger.getJSONObject("repitition");
                    }

                    if (repitition != null){
                        String intervalString = repitition.getString("interval");
                        if (intervalString.contains(".")){
                            String[] intervalSplit = intervalString.split(".");
                            intervalDays = Integer.parseInt(intervalSplit[0]);
                            intervalTime = LocalTime.parse(intervalSplit[1], intervalFormatter);
                        } else {
                            intervalTime = LocalTime.parse(intervalString, intervalFormatter);
                        }
                    }

                    switch (trigger.getString("triggerType")){
                        case "Time":

                            break;
                        case "Daily":

                            for (int i = 1; i <= daysInRange; i++){

                                if (repitition != null){
                                    LocalDateTime repititionDate = start;
                                    while (repititionDate.isBefore(endBoundary)){

                                        // <== hier muss jetzt ein Eintrag geadded werden

                                        repititionDate = repititionDate.plusHours(intervalTime.getHour());
                                        repititionDate = repititionDate.plusMinutes(intervalTime.getMinute());
                                        repititionDate = repititionDate.plusSeconds(intervalTime.getSecond());
                                    }
                                }

                            }


                            break;
                        case "Weekly":
                            break;
                        case "Monthly":
                            break;
                        case "MonthlyDOW":
                            break;
                        default:
                            break;
                    }

                    scheduleCalendarList.add(calenderEntry);

                }
            }
        }

        return scheduleCalendarList;
    }
}
