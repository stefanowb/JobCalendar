package main.java.de.jobCalendar.webApi.taskScheduler;

import java.util.TreeMap;

/**
 * Created by Stefan on 07.05.2017.
 */
public class WeeksOfMonthHelper {
    private TreeMap<Integer, Integer> weeksMap;

    public WeeksOfMonthHelper(){
        weeksMap = new TreeMap<>();
        weeksMap.put(1, 1);
        weeksMap.put(2, 2);
        weeksMap.put(4, 3);
        weeksMap.put(8, 4);
    }

    public TreeMap<Integer, Integer> getWeeksMap() {
        return weeksMap;
    }

    /**
     * Analysiert den übergebenen MonthsCode und gibt eine Liste mit den darin verschlüsselten Monate zurück.
     * @param weeksCode
     * @return
     */
    public TreeMap<Integer, Integer> decodeWeeks(int weeksCode){
        // day of weeks im Intervall ermitteln
        TreeMap<Integer, Integer> resultValues = new TreeMap<>();
        int highestOneBit = -1;
        while (highestOneBit != 0) {
            highestOneBit = Integer.highestOneBit(weeksCode);
            if (highestOneBit != 0) {
                for ( int key : weeksMap.keySet() ){
                    if (key == highestOneBit ) {
                        resultValues.put(key, weeksMap.get(key));
                    }
                }
            }
            weeksCode = weeksCode -highestOneBit;
        }

        return resultValues;
    }

}
