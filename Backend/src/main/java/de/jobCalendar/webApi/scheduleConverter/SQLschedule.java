package main.java.de.jobCalendar.webApi.scheduleConverter;

import java.util.ArrayList;
import java.util.TreeMap;

public class SQLschedule {

    public String getJob_id() {
        return job_id;
    }
    public void setJob_id(String job_id) {
        this.job_id = job_id;
    }
    public String getNameJobs() {
        return nameJobs;
    }
    public void setNameJobs(String nameJobs) {
        this.nameJobs = nameJobs;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getNext_run_date() {
        return next_run_date;
    }
    public void setNext_run_date(int next_run_date) {
        this.next_run_date = next_run_date;
    }
    public int getNext_run_time() {
        return next_run_time;
    }
    public void setNext_run_time(int next_run_time) {
        this.next_run_time = next_run_time;
    }
    public int getEnabled() {
        return enabled;
    }
    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }
    public int getFreq_type() {
        return freq_type;
    }
    public void setFreq_type(int freq_type) {
        this.freq_type = freq_type;
    }
    public int getFreq_interval() {
        return freq_interval;
    }
    public void setFreq_interval(int freq_interval) {
        this.freq_interval = freq_interval;
    }
    public int getFreq_subday_type() {
        return freq_subday_type;
    }
    public void setFreq_subday_type(int freq_subday_type) {
        this.freq_subday_type = freq_subday_type;
    }
    public int getFreq_subday_interval() {
        return freq_subday_interval;
    }
    public void setFreq_subday_interval(int freq_subday_interval) {
        this.freq_subday_interval = freq_subday_interval;
    }
    public int getFreq_recurrence_factor() {
        return freq_recurrence_factor;
    }
    public void setFreq_recurrence_factor(int freq_recurrence_factor) {
        this.freq_recurrence_factor = freq_recurrence_factor;
    }
    public int getActive_start_date() {
        return active_start_date;
    }
    public void setActive_start_date(int active_start_date) {
        this.active_start_date = active_start_date;
    }
    public int getActive_end_date() {
        return active_end_date;
    }
    public void setActive_end_date(int active_end_date) {
        this.active_end_date = active_end_date;
    }
    public int getActive_start_time() {
        return active_start_time;
    }
    public void setActive_start_time(int active_start_time) {
        this.active_start_time = active_start_time;
    }
    public int getRun_date() {
        return run_date;
    }
    public void setRun_date(int run_date) {
        this.run_date = run_date;
    }
    public int getRun_time() {
        return run_time;
    }
    public void setRun_time(int run_time) {
        this.run_time = run_time;
    }
    public int getRun_duration() {
        return run_duration;
    }
    public void setRun_duration(int run_duration) {
        this.run_duration = run_duration;
    }

    public TreeMap<Integer, String> getFreqTypeWeekly() {
        return freqTypeWeekly;
    }

    public TreeMap<Integer, String> getFreqTypeWeeklyValues() {
        return freqTypeWeeklyValues;
    }
    public ArrayList<Integer> getSubDayList() {
        return subDayList;
    }
    public void setSubDayList(ArrayList<Integer> subDayList) {
        this.subDayList = subDayList;
    }
    public ArrayList<Integer> getSubClockList() {
        return subClockList;
    }
    public void setSubClockList(ArrayList<Integer> subClockList) {
        this.subClockList = subClockList;
    }
    public void setFreqTypeWeeklylvalues(int freq_interval) {

        this.freqTypeWeeklyValues = new TreeMap<>();
        int highestOneBit = -1;
        while (highestOneBit != 0) {
            highestOneBit = Integer.highestOneBit(freq_interval);
            if (highestOneBit != 0) {
                for ( int key : freqTypeWeekly.keySet() ){
                    if (key == highestOneBit ) {
                        this.freqTypeWeeklyValues.put(key, freqTypeWeekly.get(key));
                    }
                }
            }
            freq_interval = freq_interval -highestOneBit;
        }
    }

    private String job_id;
    private String nameJobs;
    private String description;
    private int next_run_date;
    private int next_run_time;          // nÃ¤chste geplante AusfÃ¼hrung
    private int enabled;
    private int freq_type;              // 4: tÃ¤glich, 8: wÃ¶chentlich
    private int freq_interval;          // Wert entspricht Summe der Tage, bei tÃ¤glich immer 1, wÃ¶chentlich Siehe Bitmaske
    private int freq_subday_type;       // 8: alle Stunden
    private int freq_subday_interval;   // Wert aller wieviel Stunden am Tag starten
    private int freq_recurrence_factor; // Anzahl der Wochen zwischen den geplanten AusfÃ¼hrungen, gilt nur erst ab freq_type = 8
    private int active_start_date;      // erste AusfÃ¼hrung, bei wÃ¶chentlich muss das aber nicht der erste Start sein
    private int active_end_date;
    private int active_start_time;
    private int run_date;               // letzter bekannter Start, kann auch null sein
    private int run_time;
    private int run_duration;

    private ArrayList<Integer> subDayList;
    private ArrayList<Integer> subClockList;

    private TreeMap<Integer, String> freqTypeWeekly;
    private TreeMap<Integer, String> freqTypeWeeklyValues;


    public SQLschedule (){
        freqTypeWeekly = new TreeMap<>();
        freqTypeWeekly.put(1, "SUNDAY");
        freqTypeWeekly.put(2, "MONDAY");
        freqTypeWeekly.put(4, "TUESDAY");
        freqTypeWeekly.put(8, "WEDNESDAY");
        freqTypeWeekly.put(16, "THURSDAY");
        freqTypeWeekly.put(32, "FRIDAY");
        freqTypeWeekly.put(64, "SATURDAY");
    }

}



