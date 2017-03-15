package main.java.de.jobCalendar.webApi.common;

import org.json.JSONObject;

/**
 * Klasse zur Erzeugung einer WebSocket Antwort Nachricht
 */
public class Response {

    private String destination;
    private String result;
    private JSONObject data;
    private String errorMessage;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Erzeugt eine leere Response-Instanz
     */
    public Response() {
        this.destination = "";
        this.result = "";
        this.data = new JSONObject();
        this.errorMessage = "";
    }

    /**
     * Erzeugt eine gefuellte Response-Instanz
     * @param destination {String} Zieladresse der Antwort im Frontend
     * @param result {String} zu uebermittelndes Ergebnis
     * @param data {JsonObject} zu uebermittelnde Daten
     * @param errorMessage {String} Fehlernachricht
     */
    public Response(String destination, String result, JSONObject data, String errorMessage) {
        this.destination = destination;
        this.result = result;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject =  new JSONObject()
                .put("destination", this.destination)
                .put("result", this.result)
                .put("data", this.data)
                .put("error", this.errorMessage);
        return jsonObject;
    }

}
