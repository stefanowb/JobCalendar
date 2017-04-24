package main.java.de.jobCalendar.webApi.taskScheduler;

import main.java.de.jobCalendar.webApi.scheduleConverter.ScheduleCalendar;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;


/**
 * Created by Stefan on 19.04.2017.
 */
public class TaskCalendarGenerator {

    private JSONArray taskArray;
    private DateTimeFormatter isoFormatter;
    private DateTimeFormatter intervalFormatter;
    private DaysOfWeekHelper daysOfWeekHelper;

    public TaskCalendarGenerator(String taskSchedulerResult){

        taskArray = new JSONArray(taskSchedulerResult);
        isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        intervalFormatter = DateTimeFormatter.ISO_LOCAL_TIME;
        daysOfWeekHelper = new DaysOfWeekHelper();
    }

    public ArrayList<ScheduleCalendar> getScheduleCalendar(LocalDateTime fromDate, LocalDateTime toDate){

        ScheduleCalendar newCalenderEntry;
        ArrayList<ScheduleCalendar> scheduleCalendarList = new ArrayList<ScheduleCalendar>();

        long daysInRange = fromDate.until(toDate, ChronoUnit.DAYS);

        for (Object taskObject : taskArray){
            JSONObject task = (JSONObject)taskObject;

            String eventID = task.getString("id");
            String title = task.getString("title");
            LocalDateTime start = LocalDateTime.parse(task.getString("start"), isoFormatter);
            LocalDateTime end = LocalDateTime.parse(task.getString("end"), isoFormatter);
            long eventDurationSeconds = start.until(end, SECONDS);

            JSONArray triggers = task.getJSONArray("triggers");

            LocalDateTime currentEventStartTime;
            LocalDateTime currentRepititionWeekStart;
            LocalDateTime currentRepititionWeekEnd;

            for (Object triggerObject : triggers){
                JSONObject trigger = (JSONObject)triggerObject;

                LocalDateTime startBoundary = LocalDateTime.parse(trigger.getString("startBoundary"), isoFormatter);
                LocalDateTime endBoundary = LocalDateTime.parse(trigger.getString("endBoundary"), isoFormatter);
                LocalDateTime triggerEnd = toDate;
                if (endBoundary.isBefore(toDate)){
                    triggerEnd = endBoundary;
                }

                //Prüfe, ob sich der Trigger im Suchzeitraum befindet
                if (fromDate.isBefore(endBoundary) && toDate.isAfter(startBoundary)){

                    // Bereite das Repitition-Intervall auf
                    JSONObject repitition = null;
                    if (trigger.isNull("repitition") == false){
                        repitition = trigger.getJSONObject("repitition");
                    }


                    switch (trigger.getString("triggerType")){
                        case "Time":

                            if (start.isBefore(toDate) &&
                                    start.isBefore(endBoundary)){

                                // ein neues Event für den ersten Start erzeugen
                                newCalenderEntry = new ScheduleCalendar();
                                newCalenderEntry.setTitle(title);
                                newCalenderEntry.setId(UUID.randomUUID().toString());
                                newCalenderEntry.setStart(start);
                                newCalenderEntry.setEnd(start.plus(eventDurationSeconds, SECONDS));
                                scheduleCalendarList.add(newCalenderEntry);

                                if (repitition != null){
                                    addRepititionEvents(scheduleCalendarList, repitition, start, title, eventDurationSeconds);
                                }
                            }
                            break;
                        case "Daily":
                            int repititionIntervalDays = trigger.getInt("daysInterval");

                            currentEventStartTime = start;

                            while (currentEventStartTime.isBefore(triggerEnd)){

                                // für jede Wiederholungstag ein neues Event erzeugen
                                newCalenderEntry = new ScheduleCalendar();
                                newCalenderEntry.setTitle(title);
                                newCalenderEntry.setId(UUID.randomUUID().toString());
                                newCalenderEntry.setStart(currentEventStartTime);
                                newCalenderEntry.setEnd(currentEventStartTime.plus(eventDurationSeconds, SECONDS));
                                scheduleCalendarList.add(newCalenderEntry);

                                if (repitition != null){
                                    addRepititionEvents(scheduleCalendarList, repitition, currentEventStartTime, title, eventDurationSeconds);
                                }

                                currentEventStartTime = currentEventStartTime.plusDays(repititionIntervalDays);
                            }

                            break;
                        case "Weekly":

                            int repititionIntervalWeeks = trigger.getInt("weeksInterval");
                            int daysOfWeekCode = trigger.getInt("daysOfWeek");
                            TreeMap<Integer, DayOfWeek> daysOfWeek = daysOfWeekHelper.decodeDaysOfWeek(daysOfWeekCode);

                            currentEventStartTime = start;
                            currentRepititionWeekStart = start;

                            // Wochen durch iterieren
                            while (currentEventStartTime.isBefore(triggerEnd)) {

                                // Wochentag des StartTermins ermitteln
                                DayOfWeek startWeekDay = currentRepititionWeekStart.getDayOfWeek();

                                // Wochentage durch iterieren
                                for (DayOfWeek weekDay : daysOfWeek.values()){

                                    int daysUntilStart = daysOfWeekHelper.getDaysBetweenWeekdays(startWeekDay, weekDay);
                                    currentEventStartTime = currentRepititionWeekStart.plusDays(daysUntilStart);

                                    if (currentEventStartTime.isBefore(triggerEnd)){
                                        // für jede Wiederholungstag ein neues Event erzeugen
                                        newCalenderEntry = new ScheduleCalendar();
                                        newCalenderEntry.setTitle(title);
                                        newCalenderEntry.setId(UUID.randomUUID().toString());
                                        newCalenderEntry.setStart(currentEventStartTime);
                                        newCalenderEntry.setEnd(currentEventStartTime.plus(eventDurationSeconds, SECONDS));
                                        scheduleCalendarList.add(newCalenderEntry);

                                        if (repitition != null){
                                            addRepititionEvents(scheduleCalendarList, repitition, currentEventStartTime, title, eventDurationSeconds);
                                        }
                                    }

                                }

                                currentRepititionWeekStart = currentRepititionWeekStart.plusDays(repititionIntervalWeeks * 7);
                                currentEventStartTime = currentRepititionWeekStart;
                            }

                            break;
                        case "Monthly":
                            break;
                        case "MonthlyDOW":
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        return scheduleCalendarList;
    }

    /**
     * fügt der übergebenen ScheduleCalendarList einzelne Kalender einträge für jede Repitition im
     * übergebenen Intervall hinzu.
     * @param scheduleCalendarList
     * @param repitition
     * @param repititionStart
     * @param title
     * @param eventDurationSeconds
     */
    private void addRepititionEvents(ArrayList<ScheduleCalendar> scheduleCalendarList, JSONObject repitition,
                                     LocalDateTime repititionStart,
                                     String title, long eventDurationSeconds){

        ScheduleCalendar newCalenderEntry;
        LocalDateTime currentEventStartTime = repititionStart;

        IntervalSplitter intervalSplitter = new IntervalSplitter(repitition.getString("interval"), intervalFormatter);
        IntervalSplitter durationSplitter = new IntervalSplitter(repitition.getString("duration"), intervalFormatter);
        int intervalDays = intervalSplitter.getIntervalDays();
        LocalTime intervalTime = intervalSplitter.getIntervalTime();
        int durationDays = durationSplitter.getIntervalDays();
        LocalTime durationTime = durationSplitter.getIntervalTime();

        LocalDateTime durationEnd = repititionStart.plusDays(durationDays);
        durationEnd = durationEnd.plusHours(durationTime.getHour());
        durationEnd = durationEnd.plusMinutes(durationTime.getMinute());
        durationEnd = durationEnd.plusSeconds(durationTime.getSecond());

        currentEventStartTime = currentEventStartTime.plusDays(intervalDays);
        currentEventStartTime = currentEventStartTime.plusHours(intervalTime.getHour());
        currentEventStartTime = currentEventStartTime.plusMinutes(intervalTime.getMinute());
        currentEventStartTime = currentEventStartTime.plusSeconds(intervalTime.getSecond());

        while (currentEventStartTime.isBefore(durationEnd)){

            // für jede Repitition ein neues Event erzeugen
            newCalenderEntry = new ScheduleCalendar();
            newCalenderEntry.setTitle(title);
            newCalenderEntry.setId(UUID.randomUUID().toString());
            newCalenderEntry.setStart(currentEventStartTime);
            newCalenderEntry.setEnd(currentEventStartTime.plus(eventDurationSeconds, SECONDS));
            scheduleCalendarList.add(newCalenderEntry);

            currentEventStartTime = currentEventStartTime.plusDays(intervalDays);
            currentEventStartTime = currentEventStartTime.plusHours(intervalTime.getHour());
            currentEventStartTime = currentEventStartTime.plusMinutes(intervalTime.getMinute());
            currentEventStartTime = currentEventStartTime.plusSeconds(intervalTime.getSecond());
        }
    }

    /*
    private void addTimeEvents(ArrayList<ScheduleCalendar> scheduleCalendarList){

    }

    private void addDaylyEvents(ArrayList<ScheduleCalendar> scheduleCalendarList){

    }

    private void addMonthlyEvents(ArrayList<ScheduleCalendar> scheduleCalendarList){

    }

    private void addMonthlyDowEvents(ArrayList<ScheduleCalendar> scheduleCalendarList){

    }
    */

}
