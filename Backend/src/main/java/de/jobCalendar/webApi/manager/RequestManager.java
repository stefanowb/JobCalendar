package main.java.de.jobCalendar.webApi.manager;

import main.java.de.jobCalendar.webApi.common.Response;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;

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
            case "SCalendar/testSQLRequest":
                response = getTestSQLResponse(requestData);
                break;
            case "SCalendar/scheduledTasksRequest":
                response = getScheduledTasksResponse(requestData);
                break;
            case "SCalendar/btnClick":
                response = getBtnResponse(requestData);
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

    public Response getBtnResponse(JSONObject requestData){
        Response response = new Response();
        System.out.println("TEEEEst");
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

    public Response getTestSQLResponse(JSONObject requestData) throws Exception{
        Response response = new Response();
        response.setDestination("SCalendar/testSQLResponse");

        String userName = "sa";
        String password = "SQLPasswort";
        String url = "jdbc:sqlserver://Stefan-PC\\MSSQLSERVER;databaseName=Northwind";

        try
        {
            Class.forName( "com.microsoft.sqlserver.jdbc.SQLServerDriver" );
        }
        catch ( ClassNotFoundException e )
        {
            response.setResult("error");
            response.setErrorMessage("SQL Treiber nicht vorhanden!");
        }

        Connection con = null;

        try
        {
            con = DriverManager.getConnection(url, userName, password);
            Statement stmt = con.createStatement();

            ResultSet rs = stmt.executeQuery( "SELECT * FROM Customers" );

            String resultString = "";

            while ( rs.next() ){
                resultString += rs.getString("CompanyName") + "\n";
            }

            response.setResult("success");
            JSONObject dataObject = new JSONObject();
            dataObject.put("sqlResult", resultString);
            response.setData(dataObject);

            rs.close();
            stmt.close();
        }
        catch ( SQLException e )
        {
            e.printStackTrace();
            response.setResult("error");
            response.setErrorMessage("Fehler bei der SQL Abfrage");
        }
        finally
        {
            if ( con != null )
                try { con.close(); } catch ( SQLException e ) { e.printStackTrace(); }
        }

        return response;
    }

}


