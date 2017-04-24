package main.java.de.jobCalendar.webApi.taskScheduler;

import java.util.TreeMap;

/**
 * Created by Stefan on 23.04.2017.
 */
public class MonthOfYearHelper {

    private TreeMap<Integer, String> freqTypeMonthly;
    private TreeMap<Integer, Integer> resultTypeMonthly;

    public MonthOfYearHelper(){
        freqTypeMonthly = new TreeMap<>();
        freqTypeMonthly.put(1, "JANUARY");
        freqTypeMonthly.put(2, "FEBRUARY");
        freqTypeMonthly.put(4, "MARCH");
        freqTypeMonthly.put(8, "APRIL");
        freqTypeMonthly.put(16, "MAI");
        freqTypeMonthly.put(32, "JUNE");
        freqTypeMonthly.put(64, "JULY");
        freqTypeMonthly.put(128, "AUGUST");
        freqTypeMonthly.put(256, "SEPTEMBER");
        freqTypeMonthly.put(512, "OCTOBER");
        freqTypeMonthly.put(1024, "NOVEMBER");
        freqTypeMonthly.put(2048, "DECEMBER");

        resultTypeMonthly = new TreeMap<>();
        resultTypeMonthly.put(1, 0);
        resultTypeMonthly.put(2, 1);
        resultTypeMonthly.put(4, 2);
        resultTypeMonthly.put(8, 3);
        resultTypeMonthly.put(16, 4);
        resultTypeMonthly.put(32, 5);
        resultTypeMonthly.put(64, 6);
    }
}
