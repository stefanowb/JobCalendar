// Initialisierung
jobCalendar.view.GlobalNotification.initialize();
var WS_URL = 'ws://127.0.0.1:8080/jobCalendar/wsEndpoint';
jobCalendar.core.WebSocketConnector.connectToServer(WS_URL, webSocketConnectionEstablished);

function webSocketConnectionEstablished(obj)
{
    console.log('Hier könnte man jetzt nach dem Aufbau der Verbindung etwas auslösen');


    if (jobCalendar.core.WebSocketConnector.isConnected()) {

        var exchangeObject = Object.create(jobCalendar.model.Request);
        // exchangeObject.destination = 'SCalendar/scheduledTasksRequest';
        exchangeObject.destination = 'SCalendar/SQLRequest';

        var exchangeData = {
            serverName: "localhost",
            userName: "sa",
            password: "SQLPasswort",
            fromDate: 20170418,
            toDate: 20170425
        };

        exchangeObject.data = exchangeData;



        jobCalendar.core.WebSocketConnector.sendRequest(exchangeObject);
    } else {
        //TODO: Was soll bei einer nicht bestehenden Verbindung passieren??
    }
}

function changeServer() {

    var e = document.getElementById("mySelect");
    var strUser = e.options[e.selectedIndex].text;

    window.alert(strUser);
}

function changeTRange() {

    var e = document.getElementById("myInput");
    var strUser = e.value;

    window.alert(strUser);
}

function ShowCalenderEventsArrayInCalender(calenderEventsArray, doParse) {
    $(document).ready(function() {

        var parsedArray;

        if (doParse){
            parsedArray = JSON.parse(calenderEventsArray);
        } else {
            parsedArray = calenderEventsArray;
        }

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
                case 'SCalendar/SQLResponse':
                    // hier sollte man dann eine Methode aufrufen, die dann etwas mit den Daten tut
                    SQLFunktion(data, result);
                    break;
                case 'SCalendar/scheduledTasksResponse':
                    // hier sollte man dann eine Methode aufrufen, die dann etwas mit den Daten tut
                    ScheduledTasksFunktion(data, result);
                    break;
                default:
                    console.log('Unbekannte WebSocket Nachricht eingegangen ...')
            }
        }

        function SQLFunktion(messageData, result) {
            if (result == 'success'){
                ShowCalenderEventsArrayInCalender(messageData.sqlResult, false);
                //window.alert(messageData.sqlResult);
                //console.log(messageData.sqlResult);
            } else {

                var outputHeader = '';
                var outputMessage = '';

                switch (messageData.errorMessage){
                    case 'MissingDriver':
                        outputHeader = 'ScheduledTasks - MissingDriver';
                        outputMessage = 'Der SQL-Server Treiber ist auf dem Web-Server nicht vorhanden.';
                        break;
                    default:
                        outputHeader = 'SQLQuery - Abfrage Fehler';
                        outputMessage = 'Bei der Abfrage der SQL-Tasks des Servers ' + messageData.serverName +
                            ' ist ein Fehler aufgetreten. Fehlermeldung: ' + messageData.errorMessage;
                        break;
                }

                jobCalendar.controller.GlobalNotification.showNotification(
                    jobCalendar.model.GlobalNotificationType.ERROR,
                    outputHeader, outputMessage, 5000);
            }
        }

        function ScheduledTasksFunktion(messageData, result) {

            if (result == 'success'){
                ShowCalenderEventsArrayInCalender(messageData.httpResponse, true);

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



