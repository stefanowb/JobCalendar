package main.java.de.jobCalendar.webApi.sqlServerQuery;


import main.java.de.jobCalendar.webApi.common.Response;
import main.java.de.jobCalendar.webApi.scheduleConverter.CalendarGenerator;
import main.java.de.jobCalendar.webApi.scheduleConverter.SQLschedule;
import main.java.de.jobCalendar.webApi.scheduleConverter.ScheduleCalendar;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class SqlSchedulerRequest {

    private String serverName;
    private String userName;
    private String password;
    ArrayList<SQLschedule> scheduleList;    // Zwischenergebnis

    public SqlSchedulerRequest(String serverName, String userName, String password){
        this.serverName = serverName;
        this.userName = userName;
        this.password = password;
    }

    /**
     * Führt die SQL-Request für den Server dieser Instanz aus.
     * Dies ist ein Zwischenschritt, der bei Erfolg die SQL-Response zwischenspeichert.
     * @return 'success' bei Erfolg, andernfalls die Fehlermeldung.
     */
    public String executeRequest(){

        String result = "";

        try
        {
            ArrayList<ScheduleCalendar> schedulCalList = new ArrayList<>();

            SQLserverConnector sqlServerConnector = new SQLserverConnector(serverName, userName, password);
            scheduleList = sqlServerConnector.getResultSet();

            result = "success";
        }
        catch ( ClassNotFoundException e )
        {
            result = "MissingDriver";
            return result;
        }
        catch ( SQLException e )
        {
            System.out.println("Fehler bei der SQL Abfrage:");
            System.out.println(e.getMessage());
            e.printStackTrace();
            result = e.getMessage();
        }

        return result;
    }

    /**
     * Gibt eine Liste von Calendar Events zurück.
     * (Diese werden mit Hilfe des CalendarGenerator aus der scheduleList erzeugt.)
     * @param fromDate
     * @param toDate
     * @return
     */
    public ArrayList<ScheduleCalendar> getCalendarEventList(LocalDateTime fromDate, LocalDateTime toDate){

        String fromString = String.format("%d%02d%d", fromDate.getYear(), fromDate.getMonthValue(), fromDate.getDayOfMonth());
        String toString = String.format("%d%02d%d", toDate.getYear(), toDate.getMonthValue(), toDate.getDayOfMonth());
        int fromInt = Integer.parseInt(fromString);
        int toInt = Integer.parseInt(toString);

        CalendarGenerator cg = new CalendarGenerator();
        cg.calculateSQLscheduleFrequencies(scheduleList);
        ArrayList<ScheduleCalendar>  scheduleCalendarList = cg.getScheduleCalendar(scheduleList, fromInt, toInt);

        return scheduleCalendarList;
    }

}
