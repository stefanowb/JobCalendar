package main.java.de.jobCalendar.webApi.taskScheduler;

import java.util.Dictionary;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Stefan on 23.04.2017.
 */
public class DaysOfWeekHelper {

    private TreeMap<Integer, String> freqTypeWeekly;
    private TreeMap<Integer, Integer> resultTypeWeekly;

    public DaysOfWeekHelper(){
        freqTypeWeekly = new TreeMap<>();
        freqTypeWeekly.put(1, "SUNDAY");
        freqTypeWeekly.put(2, "MONDAY");
        freqTypeWeekly.put(4, "TUESDAY");
        freqTypeWeekly.put(8, "WEDNESDAY");
        freqTypeWeekly.put(16, "THURSDAY");
        freqTypeWeekly.put(32, "FRIDAY");
        freqTypeWeekly.put(64, "SATURDAY");

        resultTypeWeekly = new TreeMap<>();
        resultTypeWeekly.put(1, 0);
        resultTypeWeekly.put(2, 1);
        resultTypeWeekly.put(4, 2);
        resultTypeWeekly.put(8, 3);
        resultTypeWeekly.put(16, 4);
        resultTypeWeekly.put(32, 5);
        resultTypeWeekly.put(64, 6);
    }

    /**
     * Analysiert den übergebenen daysOfWeekCode und gibt eine Liste mit den darin verschlüsselten Wochentagen zurück.
     * @param daysOfWeekCode
     * @return
     */
    public TreeMap<Integer, String> getDaysOfWeek(int daysOfWeekCode){
        // day of weeks im Intervall ermitteln
        TreeMap<Integer, String> resultValues = new TreeMap<>();
        int highestOneBit = -1;
        while (highestOneBit != 0) {
            highestOneBit = Integer.highestOneBit(daysOfWeekCode);
            if (highestOneBit != 0) {
                for ( int key : freqTypeWeekly.keySet() ){
                    if (key == highestOneBit ) {
                        int resultKey = resultTypeWeekly.get(key);
                        resultValues.put(resultKey, freqTypeWeekly.get(key));
                    }
                }
            }
            daysOfWeekCode = daysOfWeekCode -highestOneBit;
        }

        return resultValues;
    }

}
