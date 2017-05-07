package main.java.de.jobCalendar.webApi.taskScheduler;

import com.sun.javafx.binding.StringFormatter;
import main.java.de.jobCalendar.webApi.scheduleConverter.ScheduleCalendar;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.*;
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
    private MonthOfYearHelper monthOfYearHelper;
    private WeeksOfMonthHelper weeksOfMonthHelper;

    public TaskCalendarGenerator(String taskSchedulerResult){

        taskArray = new JSONArray(taskSchedulerResult);
        isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        intervalFormatter = DateTimeFormatter.ISO_LOCAL_TIME;
        daysOfWeekHelper = new DaysOfWeekHelper();
        monthOfYearHelper = new MonthOfYearHelper();
        weeksOfMonthHelper = new WeeksOfMonthHelper();
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
            LocalTime startTime = start.toLocalTime();

            JSONArray triggers = task.getJSONArray("triggers");

            LocalDateTime currentEventStartTime;
            LocalDateTime currentRepititionWeekStart;
            int monthsOfYearCode;
            TreeMap<Integer, Month> months;
            int daysOfWeekCode;
            TreeMap<Integer, DayOfWeek> daysOfWeek;
            int weeksOfMonthCode;
            TreeMap<Integer, Integer> weeksOfMonth;
            int currentYear;

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
                                addCalendarEventAndRepititions(scheduleCalendarList, title, start, eventDurationSeconds, repitition);
                            }
                            break;
                        case "Daily":
                            int repititionIntervalDays = trigger.getInt("daysInterval");

                            currentEventStartTime = start;

                            while (currentEventStartTime.isBefore(triggerEnd)){

                                // für jede Wiederholungstag ein neues Event erzeugen
                                addCalendarEventAndRepititions(scheduleCalendarList, title, currentEventStartTime, eventDurationSeconds, repitition);

                                currentEventStartTime = currentEventStartTime.plusDays(repititionIntervalDays);
                            }

                            break;
                        case "Weekly":

                            int repititionIntervalWeeks = trigger.getInt("weeksInterval");
                            daysOfWeekCode = trigger.getInt("daysOfWeek");
                            daysOfWeek = daysOfWeekHelper.decodeDaysOfWeek(daysOfWeekCode);

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

                                    if (currentEventStartTime.isBefore(triggerEnd) && !(currentEventStartTime.isBefore(start))){
                                        // für jede Wiederholungstag ein neues Event erzeugen
                                        addCalendarEventAndRepititions(scheduleCalendarList, title, currentEventStartTime, eventDurationSeconds, repitition);
                                    }
                                }

                                currentRepititionWeekStart = currentRepititionWeekStart.plusDays(repititionIntervalWeeks * 7);
                                currentEventStartTime = currentRepititionWeekStart;
                            }

                            break;
                        case "Monthly":

                            monthsOfYearCode = trigger.getInt("monthsOfYear");
                            JSONArray daysOfMonth = trigger.getJSONArray("daysOfMonth");
                            boolean runOnLastDayOfMonth = trigger.getBoolean("runOnLastDayOfMonth");

                            months = monthOfYearHelper.decodeMonths(monthsOfYearCode);

                            currentEventStartTime = start;
                            currentYear = start.getYear();

                            // Jahre durch iterieren
                            while (currentYear <= triggerEnd.getYear()) {

                                // Monate durch iterieren
                                for (Month currentMonth : months.values()){

                                    int currentMonthValue = currentMonth.getValue();
                                    int currentDay = 0;
                                    LocalDate firstOfThisMonth = LocalDate.of(currentYear, currentMonthValue, 1);
                                    boolean lastDayAdded = false;

                                    if (firstOfThisMonth.isBefore(triggerEnd.toLocalDate())){

                                        // alle Tage durchiterieren
                                        for (int i = 0; i < daysOfMonth.length(); i++){

                                            currentDay = daysOfMonth.getInt(i);

                                            // damit man nicht den 31. Februar produziert
                                            if (currentDay <= firstOfThisMonth.lengthOfMonth()){

                                                LocalDate currentDate = LocalDate.of(currentYear, currentMonthValue, currentDay);
                                                currentEventStartTime = LocalDateTime.of(currentDate, startTime);

                                                if (currentEventStartTime.isBefore(triggerEnd) && !currentEventStartTime.isBefore(start)){
                                                    // für jede Wiederholungstag ein neues Event erzeugen
                                                    addCalendarEventAndRepititions(scheduleCalendarList, title, currentEventStartTime, eventDurationSeconds, repitition);

                                                    if (currentDay == firstOfThisMonth.lengthOfMonth()){
                                                        lastDayAdded = true;
                                                    }
                                                }
                                            }
                                        }

                                        // falls das Event auch am letzten Tag des Monats ausgeführt werden soll
                                        // und noch nicht durch die anderen Tage abgedeckt wurde
                                        if (runOnLastDayOfMonth && !lastDayAdded){

                                            LocalDate currentDate = LocalDate.of(currentYear, currentMonthValue, firstOfThisMonth.lengthOfMonth());
                                            currentEventStartTime = LocalDateTime.of(currentDate, startTime);

                                            if (currentEventStartTime.isBefore(triggerEnd) && !currentEventStartTime.isBefore(start)){
                                                // für jede Wiederholungstag ein neues Event erzeugen
                                                addCalendarEventAndRepititions(scheduleCalendarList, title, currentEventStartTime, eventDurationSeconds, repitition);
                                            }
                                        }
                                    }
                                }
                                currentYear ++;
                            }

                            break;
                        case "MonthlyDOW":

                            monthsOfYearCode = trigger.getInt("monthsOfYear");
                            weeksOfMonthCode = trigger.getInt("weeksOfMonth");
                            daysOfWeekCode = trigger.getInt("daysOfWeek");
                            boolean runOnLastWeekOfMonth = trigger.getBoolean("runOnLastWeekOfMonth");

                            months = monthOfYearHelper.decodeMonths(monthsOfYearCode);
                            weeksOfMonth = weeksOfMonthHelper.decodeWeeks(weeksOfMonthCode);
                            daysOfWeek = daysOfWeekHelper.decodeDaysOfWeek(daysOfWeekCode);

                            currentYear = start.getYear();

                            // Jahre durch iterieren
                            while (currentYear <= triggerEnd.getYear()) {

                                // Monate durch iterieren
                                for (Month currentMonth : months.values()){

                                    int currentMonthValue = currentMonth.getValue();
                                    LocalDate firstOfThisMonth = LocalDate.of(currentYear, currentMonthValue, 1);
                                    LocalDate lastOfThisMonth = LocalDate.of(currentYear, currentMonthValue, firstOfThisMonth.lengthOfMonth());
                                    DayOfWeek lastWeekDayOfMonth = lastOfThisMonth.getDayOfWeek();

                                    if (firstOfThisMonth.isBefore(triggerEnd.toLocalDate())){

                                        // die ersten 7 Tage des Monats durchgehen
                                        // damit hat man auch alle Wochentage einmal abgearbeitet
                                        for (int i = 0; i < 7; i++){
                                            LocalDate currentFirstWeekDay = firstOfThisMonth.plusDays(i);
                                            DayOfWeek currentDayOfWeek = currentFirstWeekDay.getDayOfWeek();
                                            boolean lastWeekAdded = false;

                                            // soll der aktuelle Tag berücksichtigt werden?
                                            if (daysOfWeek.values().contains(currentDayOfWeek)){

                                                // ermittle den letzten des Monats für den Wochentag
                                                LocalDate lastOfThisDay = null;
                                                if (runOnLastWeekOfMonth){
                                                    for (int j = 0; j < 7; j++){
                                                        LocalDate currentLastWeekDay = lastOfThisMonth.plusDays(j * -1);
                                                        if (currentLastWeekDay.getDayOfWeek().equals(currentDayOfWeek)){
                                                            lastOfThisDay = currentLastWeekDay;
                                                        }
                                                    }
                                                }

                                                // alle berücksichtigten Wochen abarbeiten
                                                for (int week : weeksOfMonth.values()){

                                                    LocalDate currentStart = currentFirstWeekDay.plusDays((week - 1) * 7);
                                                    // aufpassen, dass wir noch im richtigen Monat sind
                                                    if (currentStart.getMonth().equals(currentMonth)){

                                                        currentEventStartTime = LocalDateTime.of(currentStart, startTime);

                                                        if (currentEventStartTime.isBefore(triggerEnd) && !currentEventStartTime.isBefore(start)){
                                                            // für jede Wiederholungstag ein neues Event erzeugen
                                                            addCalendarEventAndRepititions(scheduleCalendarList, title, currentEventStartTime, eventDurationSeconds, repitition);
                                                            if (runOnLastWeekOfMonth && currentStart.equals(lastOfThisDay)){
                                                                lastWeekAdded = true;
                                                            }
                                                        }
                                                    }
                                                }
                                                // füge den letzten des Monats für den Tag hinzu
                                                if (runOnLastWeekOfMonth && !lastWeekAdded){
                                                    currentEventStartTime = LocalDateTime.of(lastOfThisDay, startTime);

                                                    if (currentEventStartTime.isBefore(triggerEnd) && !currentEventStartTime.isBefore(start)){
                                                        // für jede Wiederholungstag ein neues Event erzeugen
                                                        addCalendarEventAndRepititions(scheduleCalendarList, title, currentEventStartTime, eventDurationSeconds, repitition);
                                                    }
                                                }
                                            }
                                        }


                                    }
                                }
                                currentYear ++;
                            }

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
     * fügt der übergebenen ScheduleCalendarList ein neues Event inklisive Wiederholungen hinzu
     * @param scheduleCalendarList
     * @param title
     * @param currentEventStartTime
     * @param eventDurationSeconds
     * @param repitition
     */
    private void addCalendarEventAndRepititions(ArrayList<ScheduleCalendar> scheduleCalendarList,
                                  String title,
                                  LocalDateTime currentEventStartTime,
                                  long eventDurationSeconds,
                                  JSONObject repitition){

        addCalendarEvent(scheduleCalendarList, title, currentEventStartTime, eventDurationSeconds);

        if (repitition != null){
            addRepititionEvents(scheduleCalendarList, repitition, currentEventStartTime, title, eventDurationSeconds);
        }
    }

    /**
     * fügt der übergebenen ScheduleCalendarList ein neues Event hinzu
     * @param scheduleCalendarList
     * @param title
     * @param currentEventStartTime
     * @param eventDurationSeconds
     */
    private void addCalendarEvent(ArrayList<ScheduleCalendar> scheduleCalendarList,
                                  String title,
                                  LocalDateTime currentEventStartTime,
                                  long eventDurationSeconds){

        ScheduleCalendar newCalenderEntry = new ScheduleCalendar();
        newCalenderEntry.setTitle(title);
        newCalenderEntry.setId(UUID.randomUUID().toString());
        newCalenderEntry.setStart(currentEventStartTime);
        newCalenderEntry.setEnd(currentEventStartTime.plus(eventDurationSeconds, SECONDS));
        scheduleCalendarList.add(newCalenderEntry);
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
            addCalendarEvent(scheduleCalendarList, title, currentEventStartTime, eventDurationSeconds);

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
