// Initialisierung
jobCalendar.view.GlobalNotification.initialize();
var WS_URL = 'ws://127.0.0.1:8080/jobCalendar/wsEndpoint';
jobCalendar.core.WebSocketConnector.connectToServer(WS_URL, webSocketConnectionEstablished);


function webSocketConnectionEstablished(obj)
{
    console.log('Hier könnte man jetzt nach dem Aufbau der Verbindung etwas auslösen');

    if (jobCalendar.core.WebSocketConnector.isConnected()) {

        var exchangeObject = Object.create(jobCalendar.model.Request);
        exchangeObject.destination = 'SCalendar/testSQLRequest';
        var exchangeData = {
            serverName: "STEFAN-PC"
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
                    // hier sollte man dann eine Methode aufrufen, die dann etwas mit den Daten tut
                    testFunktion(data);
                    break;
                case "SCalendar/testSQLResponse":
                    // hier sollte man dann eine Methode aufrufen, die dann etwas mit den Daten tut
                    testSQLFunktion(data);
                    break;
                case "SCalendar/testScheduledTasksResponse":
                    // hier sollte man dann eine Methode aufrufen, die dann etwas mit den Daten tut
                    testScheduledTasksFunktion(data);
                    break;
                default:
                    console.log("Unbekannte WebSocket Nachricht eingegangen ...")
            }
        }

        function testFunktion(messageData) {
            window.alert(messageData.eineLustigeAntwort);
        }

        function testSQLFunktion(messageData) {
            window.alert(messageData.sqlResult);
        }

        function testScheduledTasksFunktion(messageData) {
            window.alert(messageData.httpResponse);
        }

        return {
            handleMessage: pubHandleMessage,
        };

    })();



