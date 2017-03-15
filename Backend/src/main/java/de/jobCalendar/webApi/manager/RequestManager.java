package main.java.de.jobCalendar.webApi.manager;

import main.java.de.jobCalendar.webApi.common.Response;
import org.json.JSONObject;

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
            default:
                response = new Response();
                response.setResult("error");
                response.setErrorMessage("unknown_request");
        }

        JSONObject responseJson = response.toJSONObject();
        String responseString = responseJson.toString();
        return responseString;
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


