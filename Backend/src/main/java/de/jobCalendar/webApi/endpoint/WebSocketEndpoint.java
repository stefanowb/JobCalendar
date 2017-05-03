package main.java.de.jobCalendar.webApi.endpoint;

import main.java.de.jobCalendar.webApi.manager.InstanceManager;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.logging.Level;
import java.util.logging.Logger;

@ServerEndpoint("/wsEndpoint")
public class WebSocketEndpoint {

    private static final Logger log = Logger.getLogger( WebSocketEndpoint.class.getName() );

    // Request-String in Java-Objekt umwandeln und weitersenden
    @OnMessage
    public void handleRequest(Session session, String request) {

        try {
            if (session.isOpen()) {
                // Validierung des Tokens mit UserID
                // außer bei UserSession -> dort nur UserID und Token erzeugen
                String response = InstanceManager.getRequestManager().getResponse(request);
                session.getBasicRemote().sendText(response);
            }
        } catch (Exception ex) {
            try {
                log.log(Level.SEVERE, "An error occurred while processing the request! The " +
                        "WebSocket-Connection will be terminated!\n" +
                        "Error: " + ex.getMessage());
                session.close();
            } catch (Exception e1) {
                log.log(Level.SEVERE, "An error occurred while closing the " +
                        "WebSocket-Connection!\n" +
                        "Error: " + ex.getMessage());
            }
        }
    }

    @OnOpen
    public void open(Session session) {
        log.log(Level.INFO, "A new Session was started.");
    }

    @OnClose
    public void close(Session session) {
        log.log(Level.INFO, "The session was terminated.");
    }

    @OnError
    public void onError(Throwable error) {
        log.log(Level.SEVERE, "An Error occurred in this session.\n" +
                "Error: " + error.toString());
    }
}
