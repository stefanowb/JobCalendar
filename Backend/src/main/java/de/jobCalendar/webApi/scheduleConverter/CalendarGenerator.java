package main.java.de.jobCalendar.webApi.scheduleConverter;

import main.java.de.jobCalendar.webApi.sqlServerQuery.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CalendarGenerator {

    public Integer getDatePosition (int start_active_date, int startDate, int endDate) {

        if( start_active_date >= startDate && start_active_date <= endDate ){
            return 0;
        }
        if( start_active_date < startDate ){
            return -1;
        }
        else{
            return 1;
        }
    }

    public ScheduleCalendar generateCal ( int startDate, SQLschedule sqls, int count){
        ScheduleCalendar scheduleCal = new ScheduleCalendar();
        // Start der ersten AusfÃ¼hrung
        scheduleCal.setStart(LocalDateTimeBuilder.getLDT(startDate, sqls.getActive_start_time()));
        // Ende des gefragten Zeitraums als letzter Termin erzeugen
        scheduleCal.setEnd(LocalDateTimeBuilder.getLDT(startDate, ( sqls.getActive_start_time() + sqls.getRun_duration())));
        scheduleCal.setId(sqls.getJob_id() + "_" + String.valueOf(count));
        scheduleCal.setTitle(sqls.getNameJobs());
        return scheduleCal;
    }

    public static ScheduleCalendar generateCalWeekly (int startDate, SQLschedule sqls, int count){
        ScheduleCalendar scheduleCal = new ScheduleCalendar();

        return scheduleCal;
    }

    public ArrayList<ScheduleCalendar> getScheduleCalendar(ArrayList<SQLschedule> scheduleList, int startDate, int endDate){
        int datePosition = 1;

        ArrayList<ScheduleCalendar> schedulCalList = new ArrayList<>();
        // ScheduleCalendar schedulCal = new ScheduleCalendar();
        LocalDate ldEnd = LocalDateTimeBuilder.getLD(endDate);
        // active_start_date
        LocalDate ldaD = null;
        Boolean start = false;

        for(SQLschedule sqls : scheduleList){


            // AuftrÃ¤ge die tÃ¤glich eimal starten
            if(sqls.getFreq_type() == 4){
                datePosition = getDatePosition(sqls.getActive_start_date(),startDate,endDate);
                if( datePosition == 0){
                    ldaD = LocalDateTimeBuilder.getLD(sqls.getActive_start_date());
                    start = true;
                } else if (datePosition == -1){
                    ldaD = LocalDateTimeBuilder.getLD(startDate);
                    start = true;
                }
                int count = 0;
                int startDateInt = 0;
                if(start){
                    while ( ldaD.isBefore(ldEnd) || ldaD.isEqual(ldEnd)){
                        count +=1;
                        startDateInt = Integer.valueOf(String.valueOf(ldaD.format(DateTimeFormatter.BASIC_ISO_DATE)));
                        if(sqls.getFreq_subday_type() == 1){
                            schedulCalList.add(generateCal( startDateInt, sqls, count));
                        }
                        // aller "bestimmter" Stunden, kann in wÃ¶chentlichen und tÃ¤glichen AuftrÃ¤gen vorkommen
                        else if(sqls.getFreq_subday_type() == 8){
                            for(int hour : sqls.getSubClockList()){
                                count+=1;
                                sqls.setActive_start_time(hour);
                                schedulCalList.add(generateCal( startDateInt, sqls, count));
                            }
                        }
                        ldaD = ldaD.plusDays(1);
                    }
                }
            }
            // wÃ¶chentlich
            if(sqls.getFreq_type() == 8){

                for( int termin : sqls.getSubDayList()){
                    start = false;
                    datePosition = getDatePosition(termin,startDate,endDate);
                    if( datePosition == 0){
                        ldaD = LocalDateTimeBuilder.getLD(termin);
                        start = true;
                    } else if (datePosition == -1){
                        LocalDate ldTermin = LocalDateTimeBuilder.getLD(termin);
                        LocalDate ldStartDate = LocalDateTimeBuilder.getLD(startDate);
                        while( ldTermin.isBefore(ldStartDate) ){
                            ldTermin = ldTermin.plusDays(7);
                        }
                        ldaD = ldTermin;
                        start = true;
                    }

                    int count = 0;
                    int startDateInt = 0;
                    if(start){
                        while ( ldaD.isBefore(ldEnd) || ldaD.isEqual(ldEnd)){
                            count +=1;
                            startDateInt = Integer.valueOf(String.valueOf(ldaD.format(DateTimeFormatter.BASIC_ISO_DATE)));
                            if(sqls.getFreq_subday_type() == 1){
                                schedulCalList.add(generateCal( startDateInt, sqls, count));
                            }
                            // aller "bestimmter" Stunden, kann in wÃ¶chentlichen und tÃ¤glichen AuftrÃ¤gen vorkommen
                            else if(sqls.getFreq_subday_type() == 8){
                                for(int hour : sqls.getSubClockList()){
                                    count+=1;
                                    sqls.setActive_start_time(hour);
                                    schedulCalList.add(generateCal( startDateInt, sqls, count));
                                }
                            }
                            ldaD = ldaD.plusDays(7);
                        }
                    }
                }


            }

            if(sqls.getFreq_subday_type() == 8){

            }
            // einmalige AusfÃ¼hrung des gwÃ¤hlten Tages
            if(sqls.getFreq_subday_type() == 1){
                // ToDo
            }

        }
        return schedulCalList;
    }

    public ArrayList<SQLschedule> getSQLscheduleValues(SQLserverConnector sqlServerConnector) throws ClassNotFoundException, SQLException{
        ArrayList<SQLschedule> scheduleList = sqlServerConnector.getResultSet();

        // Listen erstellen fÃ¼r die AuftrÃ¤ge mit subdays und subClocks
        for(SQLschedule ss : scheduleList){
            ScheduleCalculator sc = new ScheduleCalculator(ss);
            if (ss.getFreq_type() > 4) {
                ss.setSubDayList(sc.findFirstStartsWeeklyJobs());
            }
            if (ss.getFreq_subday_type() == 8){
                ss.setSubClockList(sc.findSubDaysStarts());
            }
        }
        return scheduleList;
    }

}

