// Initialisierung
jobCalendar.view.GlobalNotification.initialize();
var WS_URL = 'ws://127.0.0.1:8080/jobCalendar/wsEndpoint';
jobCalendar.core.WebSocketConnector.connectToServer(WS_URL, webSocketConnectionEstablished);


function webSocketConnectionEstablished(obj)
{
    console.log('Hier könnte man jetzt nach dem Aufbau der Verbindung etwas auslösen');

    if (jobCalendar.core.WebSocketConnector.isConnected()) {

        var exchangeObject = Object.create(jobCalendar.model.Request);
        exchangeObject.destination = 'SCalendar/testRequest';
        var exchangeData = {
            beliebesFeld: "beliebiger Wert"
        };
        exchangeObject.data = exchangeData;

        jobCalendar.core.WebSocketConnector.sendRequest(exchangeObject);
    } else {
        //TODO: Was soll bei einer nicht bestehenden Verbindung passieren??
    }

}

jobCalendar.controller.MessageController = jobCalendar.controller.MessageController  || (function () {

        function pubHandleMessage(serverMessage) {

            var destination = serverMessage.destination;
            var result = serverMessage.result;
            var data = serverMessage.data;

            console.log('WebSocket Nachricht eingegangen!');
            console.log('destination: ' + destination);
            console.log('result: ' + result);
            console.log('data: ' + data);

            // Hier kommt die Logik zum Auswerten der Nachrichten rein
            switch(destination) {
                case "SCalendar/testResponse":
                    // hier sollte man dann eine Methode aufrufen, am besten aus einer anderen Klasse
                    window.alert(data.eineLustigeAntwort);
                    break;
                default:
                    console.log("Unbekannte WebSocket Nachricht eingegangen ...")
            }
        }

        return {
            handleMessage: pubHandleMessage,
        };

    })();



