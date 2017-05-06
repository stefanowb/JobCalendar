package main.java.de.jobCalendar.webApi.taskScheduler;

import java.time.Month;
import java.util.TreeMap;

/**
 * Created by Stefan on 23.04.2017.
 */
public class MonthOfYearHelper {

    private TreeMap<Integer, Month> monthsMap;

    public MonthOfYearHelper(){
        monthsMap = new TreeMap<>();
        monthsMap.put(1, Month.JANUARY);
        monthsMap.put(2, Month.FEBRUARY);
        monthsMap.put(4, Month.MARCH);
        monthsMap.put(8, Month.APRIL);
        monthsMap.put(16, Month.MAY);
        monthsMap.put(32, Month.JUNE);
        monthsMap.put(64, Month.JULY);
        monthsMap.put(128, Month.AUGUST);
        monthsMap.put(256, Month.SEPTEMBER);
        monthsMap.put(512, Month.OCTOBER);
        monthsMap.put(1024, Month.NOVEMBER);
        monthsMap.put(2048, Month.DECEMBER);
    }

    public TreeMap<Integer, Month> getMonthsMap() {
        return monthsMap;
    }

    /**
     * Analysiert den übergebenen MonthsCode und gibt eine Liste mit den darin verschlüsselten Monate zurück.
     * @param monthsCode
     * @return
     */
    public TreeMap<Integer, Month> decodeMonths(int monthsCode){
        // day of weeks im Intervall ermitteln
        TreeMap<Integer, Month> resultValues = new TreeMap<>();
        int highestOneBit = -1;
        while (highestOneBit != 0) {
            highestOneBit = Integer.highestOneBit(monthsCode);
            if (highestOneBit != 0) {
                for ( int key : monthsMap.keySet() ){
                    if (key == highestOneBit ) {
                        resultValues.put(key, monthsMap.get(key));
                    }
                }
            }
            monthsCode = monthsCode -highestOneBit;
        }

        return resultValues;
    }

    public int getMonthsCountBetweenMonths(Month fromMonth, Month toMonth){

        int dayCount = 0;
        boolean fromMonthMatched = false;
        boolean toMonthMatched = false;
        int currentDayKey = 1;

        while ( !(fromMonthMatched && toMonthMatched)){
            if (currentDayKey > 2048){
                currentDayKey = 1;
            }

            if (fromMonthMatched){
                dayCount ++;
            }

            if (fromMonth == this.getMonthsMap().get(currentDayKey)){
                fromMonthMatched = true;
            }
            if (fromMonthMatched){
                if (toMonth == this.getMonthsMap().get(currentDayKey)){
                    toMonthMatched = true;
                }
            }


            currentDayKey *= 2;
        }

        return dayCount;
    }

}
