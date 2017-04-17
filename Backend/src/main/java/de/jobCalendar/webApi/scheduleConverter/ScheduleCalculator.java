package main.java.de.jobCalendar.webApi.scheduleConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map.Entry;

public class ScheduleCalculator {

    private SQLschedule sqls;
    private ArrayList<ScheduleCalendar> scList = new ArrayList<>();

    public ScheduleCalculator(SQLschedule sqls) {
        this.sqls = sqls;
    }

    public ScheduleCalendar generateSchedule(){
        ScheduleCalendar sc = new ScheduleCalendar();
        // ToDo
        return sc;
    }

    public void listSchedule(ScheduleCalendar sc){

        scList.add(sc);
    }
    /*
      public LocalDateTime dateTimeBuilder(int startDate, int startTime ){
          // Uhrzeit mit Nullen auffÃ¼llen um Format zu erhalten

          String date = String.valueOf(startDate);
          String time = String.valueOf(startTime);

          String datePattern = "yyyyMMddHHmmss";
          if (time.length() == 5) {
              time = "0"+time;
          } else if (time.length() == 4) {
              time = "00"+time;
          }
          else if (time.length() == 3) {
              time = "000"+time;
          }
          else if (time.length() == 2) {
              time = "0000"+time;
          }
          else if (time.length() == 1) {
              time = "00000"+time;
          }

          String datetime = date+time;
          DateTimeFormatter dtf = DateTimeFormatter.ofPattern(datePattern);
          LocalDateTime ldt = LocalDateTime.parse(datetime, dtf);
          return ldt;
      }
    */
    public ArrayList<Integer> findSubDaysStarts(){
        ArrayList<Integer> subTimeList = new ArrayList<>();
        int start = sqls.getActive_start_time();
        int anz = 24/sqls.getFreq_subday_interval();
        for(int i = 0; i < anz; i++){
            subTimeList.add(start);
            start = start + (10000 * sqls.getFreq_subday_interval() );
        }
        return subTimeList;
    }

    public ArrayList<Integer> findFirstStartsWeeklyJobs (){
        DateTimeFormatter df;
        df = DateTimeFormatter.BASIC_ISO_DATE;    // 20160131 Formatieren des Datums in int
        ArrayList<Integer> scheduleList = new ArrayList<>();
        LocalDateTimeBuilder.getLDT(sqls.getActive_start_date(), sqls.getActive_start_time());

        // System.out.println("geplante Wochentage");
        /*
        for ( int value : sqls.getFreqTypeWeeklyValues().keySet() ){
            System.out.println(value + ": " + sqls.getFreqTypeWeeklyValues().get(value));
        }
        */
        // System.out.println("Start des Auftrages: " + sqls.getActive_start_date());

        // Int-Wert zu einem Datum
        LocalDate ld = LocalDate.parse( String.valueOf(sqls.getActive_start_date()), DateTimeFormatter.BASIC_ISO_DATE);

        // PrÃ¼fung ob active_start_date bei wÃ¶chentlichen AuftrÃ¤gen auch die erste AusfÃ¼hrung stattfindet
        Boolean activeStartIsFirstStart = false;
        if( sqls.getFreqTypeWeeklyValues().containsValue(ld.getDayOfWeek().toString())){
            activeStartIsFirstStart = true;
        }
        int valueOfDay = 0;
        int raiseValueOfDay = 0;
        int countDays = 0;
        // Den Wert des Tages des active_start_date (ld) ermitteln
        if(!activeStartIsFirstStart){
            for(Entry<Integer, String> e : sqls.getFreqTypeWeekly().entrySet()){
                if( e.getValue().toString().equals(ld.getDayOfWeek().toString())){
                    valueOfDay = (int) e.getKey();
                    raiseValueOfDay = valueOfDay;
                }
            }
            for( int key : sqls.getFreqTypeWeeklyValues().keySet()){
                countDays++;
                while ( raiseValueOfDay < 64){
                    if( Integer.rotateLeft(raiseValueOfDay, 1) == key ){
                        scheduleList.add( Integer.valueOf(ld.plusDays(countDays).format(df)) );
                        raiseValueOfDay = 64;
                    }else {
                        raiseValueOfDay = Integer.rotateLeft(raiseValueOfDay, 1);
                    }
                }
                raiseValueOfDay = valueOfDay;
                while ( raiseValueOfDay > 0){
                    if( Integer.rotateRight(raiseValueOfDay, 1) == key ){
                        scheduleList.add( Integer.valueOf(ld.plusDays(7-countDays).format(df)) );
                        raiseValueOfDay = 0;
                    }else {
                        raiseValueOfDay = Integer.rotateRight(raiseValueOfDay, 1);
                    }
                }
                raiseValueOfDay = valueOfDay;
            }
        }
        /*
        for(int i : scheduleList){
            System.out.println("nÃ¤chster Termin: " + i);
        }
        */
        return scheduleList;
    }

}
