package main.java.de.jobCalendar.webApi.sqlServerQuery;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import main.java.de.jobCalendar.webApi.scheduleConverter.*;

public class SQLserverConnector {

    private String serverName;
    private String userName;
    private String password;

    public SQLserverConnector(String serverName, String userName, String password){
        this.serverName = serverName;
        this.userName = userName;
        this.password = password;
    }

    public ArrayList<SQLschedule> getResultSet () throws ClassNotFoundException, SQLException {
        Class.forName( "com.microsoft.sqlserver.jdbc.SQLServerDriver" );
        String connectionURL = String.format("jdbc:sqlserver://%s:1433;databaseName=msdb;user=%s;password=%s;", serverName, userName, password);
        Connection con = DriverManager.getConnection(connectionURL);
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<SQLschedule> scheduleList = new ArrayList<>();

        try{
            String sql = "select * from jobactivity";
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()){
                SQLschedule schedule = new SQLschedule();

                schedule.setJob_id(rs.getString(rs.findColumn("job_id")));
                schedule.setNameJobs(rs.getString(rs.findColumn("nameJobs")));
                schedule.setRun_duration(rs.getInt(rs.findColumn("run_duration")));
                if(schedule.getRun_duration() == 0 || schedule.getRun_duration() == Integer.MIN_VALUE ){
                    schedule.setDescription(rs.getString(rs.findColumn("description")) +
                            "\n\n\n Auftrag wurde bisher nicht ausgefÃ¼hrt!");
                } else {
                    schedule.setDescription(rs.getString(rs.findColumn("description")));
                }
                schedule.setActive_start_date(rs.getInt(rs.findColumn("active_start_date")));
                schedule.setActive_start_time(rs.getInt(rs.findColumn("active_start_time")));
                schedule.setEnabled(rs.getInt(rs.findColumn("enabled")));
                schedule.setFreq_interval(rs.getInt(rs.findColumn("freq_interval")));
                schedule.setFreq_recurrence_factor(rs.getInt(rs.findColumn("freq_recurrence_factor")));
                schedule.setFreq_subday_interval(rs.getInt(rs.findColumn("freq_subday_interval")));
                schedule.setFreq_subday_type(rs.getInt(rs.findColumn("freq_subday_type")));
                schedule.setFreq_type(rs.getInt(rs.findColumn("freq_type")));
                if (rs.getInt(rs.findColumn("freq_type")) > 4 ) {
                    schedule.setFreqTypeWeeklylvalues(schedule.getFreq_interval());
                }
                schedule.setNext_run_date(rs.getInt(rs.findColumn("next_run_date")));
                schedule.setNext_run_time(rs.getInt(rs.findColumn("next_run_time")));
                schedule.setRun_date(rs.getInt(rs.findColumn("run_date")));
                schedule.setRun_time(rs.getInt(rs.findColumn("run_time")));

                scheduleList.add(schedule);


            }
        }
        finally {
            if (rs != null) try { rs.close(); } catch (Exception e) {}
            if (stmt != null) try { rs.close(); } catch (Exception e) {}
            if (con != null) try { rs.close(); } catch (Exception e) {}
        }
        return scheduleList;
    }

}
