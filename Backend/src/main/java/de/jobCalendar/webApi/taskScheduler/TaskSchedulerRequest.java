package main.java.de.jobCalendar.webApi.taskScheduler;

import main.java.de.jobCalendar.webApi.scheduleConverter.ScheduleCalendar;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;


public class TaskSchedulerRequest {

    private String targetURL;
    private String serverName;
    private String httpResponseString;

    public TaskSchedulerRequest(String serverName){
        this.serverName = serverName;
        this.targetURL = String.format("http://%s:9876/taskschedulermonitor/scheduled_tasks", serverName);
    }

    /**
     * Führt die HTTP-Request für den Server dieser Instanz aus.
     * Dies ist ein Zwischenschritt, der bei Erfolg die HTTP-Response zwischenspeichert.
     * @return 'success' bei Erfolg, andernfalls die Fehlermeldung.
     */
    public String executeRequest(){

        HttpURLConnection connection = null;
        String result = "";

        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);     // Connection Timeout (10 Sekunden)
            connection.setReadTimeout(5000);        // Socket Timeout (10 Sekunden)
            connection.setUseCaches(false);
            //Get Response
            int responseCode = connection.getResponseCode();

            // HTTP-Code 200 = OK
            if (responseCode == 200){
                InputStream is = connection.getInputStream();

                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder httpResponse = new StringBuilder(); // or StringBuffer if Java version 5+
                String line;
                while ((line = rd.readLine()) != null) {
                    httpResponse.append(line);
                    httpResponse.append('\r');
                }
                rd.close();

                result = "success";
                this.httpResponseString = httpResponse.toString();

            } else {
                result = "HTTP-Code: " + responseCode;
            }

        } catch (java.net.SocketTimeoutException e) {
            e.printStackTrace();
            result = "SocketTimeout";

        } catch (java.net.UnknownHostException e) {
            e.printStackTrace();
            result = "UnknownHost";


        } catch (Exception e) {
            e.printStackTrace();
            result = e.getMessage();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return result;
    }

    /**
     * Gibt eine Liste von Calendar Events zurück.
     * (Diese werden mit Hilfe des TaskCalendarGenerators aus dem httpResponseString erzeugt.)
     * @param fromDate
     * @param toDate
     * @return
     */
    public ArrayList<ScheduleCalendar> getCalendarEventList(LocalDateTime fromDate, LocalDateTime toDate){
        TaskCalendarGenerator taskCalendarGenerator = new TaskCalendarGenerator(httpResponseString);
        ArrayList<ScheduleCalendar> scheduleCalendarList = taskCalendarGenerator.getScheduleCalendar(fromDate, toDate);

        return scheduleCalendarList;
    }

}
