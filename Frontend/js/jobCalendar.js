// Initialisierung
jobCalendar.view.GlobalNotification.initialize();
var WS_URL = 'ws://127.0.0.1:8080/jobCalendar/wsEndpoint';
jobCalendar.core.WebSocketConnector.connectToServer(WS_URL, webSocketConnectionEstablished);

function webSocketConnectionEstablished(obj)
{
    console.log('Hier könnte man jetzt nach dem Aufbau der Verbindung etwas auslösen');

    var s = ('[{"server":"Stefans Server", "id":"1", "type":"task"}, {"server":"Rolands Server", "id":"2", "type":"sql"}]');

    var jsonData = $.parseJSON(s);

    var $select = $('#mySelectID');
    $(jsonData).each(function (index, value) {
        var $option = $("<option/>").attr("value", value.id).text(value.server);
        $select.append($option);
    });

    if (jobCalendar.core.WebSocketConnector.isConnected()) {

        var exchangeObject = Object.create(jobCalendar.model.Request);
        exchangeObject.destination = 'SCalendar/scheduledTasksRequest';
        var exchangeData = {
            serverName: "localhost"
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
                case 'SCalendar/testResponse':
                    // hier sollte man dann eine Methode aufrufen, die dann etwas mit den Daten tut
                    testFunktion(data);
                    break;
                case 'SCalendar/testSQLResponse':
                    // hier sollte man dann eine Methode aufrufen, die dann etwas mit den Daten tut
                    testSQLFunktion(data);
                    break;
                case 'SCalendar/scheduledTasksResponse':
                    // hier sollte man dann eine Methode aufrufen, die dann etwas mit den Daten tut
                    ScheduledTasksFunktion(data, result);
                    break;
                default:
                    console.log('Unbekannte WebSocket Nachricht eingegangen ...')
            }
        }

        function testFunktion(messageData) {
            window.alert(messageData.eineLustigeAntwort);
        }

        function testSQLFunktion(messageData) {
            window.alert(messageData.sqlResult);
        }

        function ScheduledTasksFunktion(messageData, result) {

            if (result == 'success'){
                // window.alert(messageData.httpResponse);

                $(document).ready(function() {
                    const parsedArray = JSON.parse(messageData.httpResponse);
                    $('#calendar').fullCalendar({
                        header: {
                            left: 'prev,next today',
                            center: 'title',
                            right: 'month,agendaWeek,agendaDay,listWeek'
                        },
                        defaultDate: new Date(),
                        editable: true,
                        navLinks: true, // can click day/week names to navigate views
                        eventLimit: true, // allow "more" link when too many events
                        events: parsedArray
                    });

                });

            } else {

                var outputHeader = '';
                var outputMessage = '';

                switch (messageData.errorMessage){
                    case 'SocketTimeout':
                        outputHeader = 'ScheduledTasks - SocketTimeout';
                        outputMessage = 'Der Server ' + messageData.serverName + ' konnte nicht erreicht werden.';
                        break;
                    case 'UnknownHost':
                        outputHeader = 'ScheduledTasks - Unbekannter Host';
                        outputMessage = 'Der Server mit dem Namen ' + messageData.serverName + ' existiert nicht.';
                        break;
                    default:
                        outputHeader = 'ScheduledTasks - Unbekannter Fehler';
                        outputMessage = 'Bei der Abfrage der geplanten Tasks des Servers ' + messageData.serverName +
                            ' ist ein unbekannter Fehler aufgetreten. Fehlermeldung: ' + messageData.errorMessage;
                        break;
                }

                jobCalendar.controller.GlobalNotification.showNotification(
                    jobCalendar.model.GlobalNotificationType.ERROR,
                    outputHeader, outputMessage, 5000);
            }
        }

        return {
            handleMessage: pubHandleMessage,
        };

    })();



