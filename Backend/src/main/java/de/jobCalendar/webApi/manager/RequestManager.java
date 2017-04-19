package main.java.de.jobCalendar.webApi.manager;

import com.sun.javafx.tk.Toolkit;
import main.java.de.jobCalendar.webApi.common.Response;
import main.java.de.jobCalendar.webApi.scheduleConverter.*;
import main.java.de.jobCalendar.webApi.sqlServerQuery.*;
import main.java.de.jobCalendar.webApi.taskScheduler.TaskCalendarGenerator;
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
            case "SCalendar/testRequest":
                response = getTestResponse(requestData);
                break;
            case "SCalendar/SQLRequest":
                response = getSQLResponse(requestData);
                break;
            case "SCalendar/scheduledTasksRequest":
                response = getScheduledTasksResponse(requestData);
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

    public Response getTestResponse(JSONObject requestData){

        Response response = new Response();
        response.setDestination("SCalendar/testResponse");
        response.setResult("success");

        JSONObject dataObject = new JSONObject();
        dataObject.put("eineLustigeAntwort", "Hey, dies ist eine Antwort auf eine Websocket Nachricht. Es funzt. Wuhuuuu!");
        response.setData(dataObject);

        return response;
    }

    public  Response getScheduledTasksResponse(JSONObject requestData) throws Exception{
        Response response = new Response();
        JSONObject responseDataObject = new JSONObject();
        response.setDestination("SCalendar/scheduledTasksResponse");

        String serverName = requestData.getString("serverName");
        responseDataObject.put("serverName", serverName);
        String targetURL = String.format("http://%s:9876/taskschedulermonitor/scheduled_tasks", serverName);

        HttpURLConnection connection = null;

        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);     // Connection Timeout (5 Sekunden)
            connection.setReadTimeout(5000);        // Socket Timeout (5 Sekunden)
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

                response.setResult("success");

                LocalDateTime fromDate = LocalDateTime.now();
                LocalDateTime toDate = fromDate.plusDays(7);
                //TaskCalendarGenerator.getScheduleCalendar(httpResponse.toString(), fromDate, toDate );

                responseDataObject.put("httpResponse", httpResponse.toString());
            } else {
                response.setResult("error");
                responseDataObject.put("errorMessage", "HTTP-Code: " + responseCode);
            }

        } catch (java.net.SocketTimeoutException e) {
            e.printStackTrace();
            response.setResult("error");
            responseDataObject.put("errorMessage", "SocketTimeout");

        } catch (java.net.UnknownHostException e) {
            e.printStackTrace();
            response.setResult("error");
            responseDataObject.put("errorMessage", "UnknownHost");


        } catch (Exception e) {
            e.printStackTrace();
            response.setResult("error");
            responseDataObject.put("errorMessage", e.getMessage());

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        response.setData(responseDataObject);
        return response;
    }

    public Response getSQLResponse(JSONObject requestData) throws Exception{
        Response response = new Response();
        response.setDestination("SCalendar/SQLResponse");

        String serverName = requestData.getString("serverName");
        String userName = requestData.getString("userName");
        String password = requestData.getString("password");
        int fromDate = requestData.getInt("fromDate");
        int toDate = requestData.getInt("toDate");

        try
        {
            Class.forName( "com.microsoft.sqlserver.jdbc.SQLServerDriver" );
        }
        catch ( ClassNotFoundException e )
        {
            response.setResult("error");
            response.setErrorMessage("MissingDriver");
            return response;
        }

        try
        {
            CalendarGenerator cg = new CalendarGenerator();
            ArrayList<ScheduleCalendar> schedulCalList = new ArrayList<>();

            SQLserverConnector sqlServerConnector = new SQLserverConnector(serverName, userName, password);
            ArrayList<SQLschedule> scheduleList = sqlServerConnector.getResultSet();

            cg.calculateSQLscheduleFrequencies(scheduleList);
            schedulCalList = cg.getScheduleCalendar(scheduleList, fromDate, toDate);

            JSONArray calenderEntriesArray = new JSONArray();
            for(ScheduleCalendar sca : schedulCalList){
                JSONObject calenderEntryObject = new JSONObject();

                calenderEntryObject.put("id", sca.getId());
                calenderEntryObject.put("title", sca.getTitle());
                calenderEntryObject.put("start", sca.getStart());
                calenderEntryObject.put("end", sca.getEnd());

                calenderEntriesArray.put(calenderEntryObject);
            }

            response.setResult("success");
            JSONObject dataObject = new JSONObject();
            dataObject.put("sqlResult", calenderEntriesArray);
            response.setData(dataObject);

        }
        catch ( SQLException e )
        {
            System.out.println("Fehler bei der SQL Abfrage:");
            System.out.println(e.getMessage());
            e.printStackTrace();
            response.setResult("error");
            response.setErrorMessage(e.getMessage());
        }

        return response;
    }

}


