package main.java.de.jobCalendar.webApi.manager;

import com.sun.javafx.tk.Toolkit;
import main.java.de.jobCalendar.webApi.common.Response;
import main.java.de.jobCalendar.webApi.scheduleConverter.*;
import main.java.de.jobCalendar.webApi.sqlServerQuery.*;
import main.java.de.jobCalendar.webApi.taskScheduler.TaskCalendarGenerator;
import main.java.de.jobCalendar.webApi.taskScheduler.TaskSchedulerRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

public class RequestManager {

    /**
     * Behandelt einen JSON-String-Request vom Frontend ueber einen zugehoerigen
     * Workflow und gibt das Ergebnis zurueck.
     *
     * @param request {String} Request als JSON-String
     * @return {String} Antwort zum Request als JSON-String
     */
    public String getResponse(String request) throws Exception {

        // JSON-String-Request in Request-Javaobjekt umwandeln
        JSONObject requestJson;
        String requestDestination;
        JSONObject requestData;
        Response response;

        try {
            requestJson = new JSONObject(request);
        } catch (Exception ex) {
            throw new Exception("Fehler beim Konvertieren eines " +
                    "Request-Strings in ein JsonObject!\nFehlermeldung: " + ex);
        }

        try {
            requestDestination = requestJson.getString("destination");
            requestData = requestJson.getJSONObject("data");

        } catch (NullPointerException ex) {
            throw new Exception("Fehler beim Extrahieren der Eigenschaften " +
                    "des Requests! Es sind nicht alle notwendigen " +
                    "Eigenschaften vorhanden (destination, data, token)");
        } catch (ClassCastException ex) {
            throw new Exception("Fehler beim Extrahieren der Eigenschaften " +
                    "des Requests! Der Request-String hat kein korrektes " +
                    "JSON-Format!");
        }

        // Hier wird je nach gesendeter WebSocket-Request eine bestimmte Methode für eine WebSocket-Response aufgerufen
        switch(requestDestination) {
            case "SCalendar/calendarEventsRequest":
                response = getCalendarEventsResponse(requestData);
                break;
            case "SCalendar/server":
                response = getServer(requestData);
                break;
            default:
                response = new Response();
                response.setResult("error");
                response.setErrorMessage("unknown_request");
        }

        JSONObject responseJson = response.toJSONObject();
        String responseString = responseJson.toString();
        return responseString;
    }

    public Response getServer(JSONObject requestData) throws Exception{
        Response response = new Response();

        response.setDestination("SCalendar/serverResponse");
        response.setResult("success");

        JSONObject dataObject = new JSONObject();

        String datName = "C:/jobCalendar/init.txt";

        File file = new File(datName);

        if (!file.canRead() || !file.isFile())
            System.exit(0);

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(datName));
            String zeile = null;

            StringBuilder httpResponse = new StringBuilder(); // or StringBuffer if Java version 5+

            while ((zeile = in.readLine()) != null) {
                    httpResponse.append(zeile);
                    httpResponse.append('\r');
            }

            dataObject.put("httpResponse", httpResponse.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                }
        }

        response.setData(dataObject);

        return response;
    }

    public  Response getCalendarEventsResponse(JSONObject requestData) throws Exception{
        Response response = new Response();
        JSONObject responseDataObject = new JSONObject();
        JSONArray calenderEntriesArray = new JSONArray();
        ArrayList<ScheduleCalendar> taskSchedulerEvents;
        ArrayList<ScheduleCalendar> sqlEvents;

        response.setDestination("SCalendar/calendarEventsResponse");
        String serverName = requestData.getString("serverName");
        int nextDaysRange = requestData.getInt("nextDaysRange");
        responseDataObject.put("serverName", serverName);

        LocalDateTime fromDate = LocalDateTime.now();
        LocalDateTime toDate = fromDate.plusDays(nextDaysRange);

        //ToDo: hier müsste irgendwie auf die INI zugegriffen werden und die Server-Paramter ermittelt werden
        // hier kommen jetzt dafür erstmal Platzhalter:
        boolean doSqlRequest = true;
        boolean doTaskSchedulerRequest = true;
        String sqlUser = "sa";
        String sqlPassword = "SQLPasswort";

        if (doTaskSchedulerRequest){
            TaskSchedulerRequest taskSchedulerRequest = new TaskSchedulerRequest(serverName);
            String requestResult = taskSchedulerRequest.executeRequest();
            if (requestResult == "success"){
                taskSchedulerEvents = taskSchedulerRequest.getCalendarEventList(fromDate, toDate);
                for(ScheduleCalendar calendarEvent : taskSchedulerEvents){
                    calendarEvent.setBackgroundColor("#2E64FE");
                    calendarEvent.setBorderColor("#2E2EFE");
                    calendarEvent.setTextColor("#FFFFFF");
                    calenderEntriesArray.put(calendarEvent.toJSON());
                }
            } else {
                response.setResult("error");
                responseDataObject.put("errorMessage", requestResult);
                response.setData(responseDataObject);
                return response;
            }

        }

        if (doSqlRequest){
            SqlSchedulerRequest sqlSchedulerRequest = new SqlSchedulerRequest(serverName, sqlUser, sqlPassword);
            String requestResult = sqlSchedulerRequest.executeRequest();
            if (requestResult == "success"){
                sqlEvents = sqlSchedulerRequest.getCalendarEventList(fromDate, toDate);
                for(ScheduleCalendar calendarEvent : sqlEvents){
                    calendarEvent.setBackgroundColor("#FE9A2E");
                    calendarEvent.setBorderColor("#B45F04");
                    calendarEvent.setTextColor("#000000");
                    calenderEntriesArray.put(calendarEvent.toJSON());
                }
            } else {
                response.setResult("error");
                responseDataObject.put("errorMessage", requestResult);
                response.setData(responseDataObject);
                return response;
            }
        }

        response.setResult("success");
        responseDataObject.put("calendarEvents", calenderEntriesArray);
        response.setData(responseDataObject);

        return response;
    }

}


