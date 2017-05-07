package main.java.de.jobCalendar.webApi.taskScheduler;

import java.time.DayOfWeek;
import java.util.TreeMap;

/**
 * Created by Stefan on 23.04.2017.
 */
public class DaysOfWeekHelper {

    private TreeMap<Integer, DayOfWeek> weekDaysMap;
    private TreeMap<DayOfWeek, Integer> weekDayOrderMap;

    public DaysOfWeekHelper(){
        weekDaysMap = new TreeMap<>();
        weekDaysMap.put(1, DayOfWeek.SUNDAY);
        weekDaysMap.put(2, DayOfWeek.MONDAY);
        weekDaysMap.put(4, DayOfWeek.TUESDAY);
        weekDaysMap.put(8, DayOfWeek.WEDNESDAY);
        weekDaysMap.put(16, DayOfWeek.THURSDAY);
        weekDaysMap.put(32, DayOfWeek.FRIDAY);
        weekDaysMap.put(64, DayOfWeek.SATURDAY);

        weekDayOrderMap = new TreeMap<>();
        weekDayOrderMap.put(DayOfWeek.SUNDAY, 1);
        weekDayOrderMap.put(DayOfWeek.MONDAY, 2);
        weekDayOrderMap.put(DayOfWeek.TUESDAY, 3);
        weekDayOrderMap.put(DayOfWeek.WEDNESDAY, 4);
        weekDayOrderMap.put(DayOfWeek.THURSDAY, 5);
        weekDayOrderMap.put(DayOfWeek.FRIDAY, 6);
        weekDayOrderMap.put(DayOfWeek.SATURDAY, 7);
    }

    public TreeMap<Integer, DayOfWeek> getWeekDaysMap() {
        return weekDaysMap;
    }

    public TreeMap<DayOfWeek, Integer> getWeekDayOrderMap() {
        return weekDayOrderMap;
    }

    /**
     * Analysiert den übergebenen daysOfWeekCode und gibt eine Liste mit den darin verschlüsselten Wochentagen zurück.
     * @param daysOfWeekCode
     * @return
     */
    public TreeMap<Integer, DayOfWeek> decodeDaysOfWeek(int daysOfWeekCode){
        // day of weeks im Intervall ermitteln
        TreeMap<Integer, DayOfWeek> resultValues = new TreeMap<>();
        int highestOneBit = -1;
        while (highestOneBit != 0) {
            highestOneBit = Integer.highestOneBit(daysOfWeekCode);
            if (highestOneBit != 0) {
                for ( int key : weekDaysMap.keySet() ){
                    if (key == highestOneBit ) {
                        resultValues.put(key, weekDaysMap.get(key));
                    }
                }
            }
            daysOfWeekCode = daysOfWeekCode -highestOneBit;
        }

        return resultValues;
    }

    public int getDaysBetweenWeekdays(DayOfWeek fromDay, DayOfWeek toDay){

        return weekDayOrderMap.get(toDay) - weekDayOrderMap.get(fromDay);

        /*
        int dayCount = 0;
        boolean fromDayMatched = false;
        boolean toDayMatched = false;
        int currentDayKey = 1;

        while ( !(fromDayMatched && toDayMatched)){
            if (currentDayKey > 64){
                currentDayKey = 1;
            }

            if (fromDayMatched){
                dayCount ++;
            }

            if (fromDay == this.getWeekDaysMap().get(currentDayKey)){
                fromDayMatched = true;
            }
            if (fromDayMatched){
                if (toDay == this.getWeekDaysMap().get(currentDayKey)){
                    toDayMatched = true;
                }
            }


            currentDayKey *= 2;
        }

        return dayCount;
        */
    }

}
