// Initialisierung
jobCalendar.view.GlobalNotification.initialize();
var WS_URL = 'ws://127.0.0.1:8080/jobCalendar/wsEndpoint';
jobCalendar.core.WebSocketConnector.connectToServer(WS_URL, webSocketConnectionEstablished);


function webSocketConnectionEstablished(obj)
{
    console.log('Hier könnte man jetzt nach dem Aufbau der Verbindung etwas auslösen');

    if (ideaWatcher.core.WebSocketConnector.isConnected()) {

        var exchangeObject = Object.create(jobCalendar.model.Request);
        exchangeObject.destination = 'SCalendar/testRequest';
        exchangeObject.data = null;

        jobCalendar.core.WebSocketConnector.sendRequest(exchangeObject);
    } else {
        //TODO: Was soll bei einer nicht bestehenden Verbindung passieren??
    }

}

jobCalendar.controller.MessageController = jobCalendar.controller.MessageController  || (function () {

        function pubHandleMessage(topic, exObject) {
            console.log('WebSocket Nachricht eingegangen!');
            console.log('Topic: ' + topic);
            console.log(exObject);

            // Hier kommt die Logik zum Auswerten der Nachrichten rein
        }

        return {
            handleMessage: pubHandleMessage,
        };

    })();



