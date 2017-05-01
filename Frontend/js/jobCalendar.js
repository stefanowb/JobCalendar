// Initialisierung
jobCalendar.view.GlobalNotification.initialize();
var WS_URL = 'ws://127.0.0.1:8080/jobCalendar/wsEndpoint';
jobCalendar.core.WebSocketConnector.connectToServer(WS_URL, webSocketConnectionEstablished);

function webSocketConnectionEstablished(obj) {
    if (jobCalendar.core.WebSocketConnector.isConnected()) {

        // nachdem die Websocket-Verbindung aufgebaut wurde holt sich das Frontend
        // die Liste der verfügbaren Server vom Backend
        var exchangeObject = Object.create(jobCalendar.model.Request);
        exchangeObject.destination = 'SCalendar/serverListRequest';
        exchangeObject.data = null;
        jobCalendar.core.WebSocketConnector.sendRequest(exchangeObject);

    } else {
        //TODO: Was soll bei einer nicht bestehenden Verbindung passieren??
    }
}

function changeServer() {

    var selectServerInput = document.getElementById("selectServerInput");
    var serverID = selectServerInput.options[selectServerInput.selectedIndex].id;

    var timeRangeInput = document.getElementById("timeRangeInput");
    var timeRange = timeRangeInput.value;
    if (timeRange == ""){
        timeRange = 0;
    }

    // entferne alle Kalendereinträge, die sich noch im Kalender befinden
    var calendarEvents = JSON.parse(localStorage.getItem("calendarEvents"));
    if (calendarEvents != null){
        $.each(calendarEvents, function(i, val){
            $('#calendar').fullCalendar('removeEvents',calendarEvents[i].id);
        });
    }

    if(serverID >= 0){

        var initData = JSON.parse(localStorage.getItem("initData"));
        var result = localStorage.getItem("result");

        // Zentrale INIT Variablen
        var serverName = initData[serverID].name;

        var exchangeObject = Object.create(jobCalendar.model.Request);
        exchangeObject.destination = 'SCalendar/calendarEventsRequest';
        var exchangeData = {
            serverName: serverName,
            nextDaysRange: timeRange
        };
        exchangeObject.data = exchangeData;
        jobCalendar.core.WebSocketConnector.sendRequest(exchangeObject);
    }
}

jobCalendar.controller.MessageController = jobCalendar.controller.MessageController  || (function () {

        function pubHandleMessage(serverMessage) {

            var destination = serverMessage.destination;
            var result = serverMessage.result;
            var data = serverMessage.data;
            var errorMessage = serverMessage.errorMessage;

            console.log('WebSocket Nachricht eingegangen!');
            console.log('destination: ' + destination);
            console.log('result: ' + result);
            console.log('data: ' + data);

            // Hier kommt die Logik zum Auswerten der Nachrichten rein
            switch(destination) {
                case 'SCalendar/serverListResponse':
                    // hier sollte man dann eine Methode aufrufen, die dann etwas mit den Daten tut
                    setServerFunktion(data, result, errorMessage);
                    break;
                case 'SCalendar/calendarEventsResponse':
                    setCalendarEventsFunktion(data, result, errorMessage);
                    break;
                case 'SCalendar/SQLResponse':
                    // hier sollte man dann eine Methode aufrufen, die dann etwas mit den Daten tut
                    SQLFunktion(data, result, errorMessage);
                    break;
                case 'SCalendar/scheduledTasksResponse':
                    // hier sollte man dann eine Methode aufrufen, die dann etwas mit den Daten tut
                    ScheduledTasksFunktion(data, result, errorMessage);
                    break;
                default:
                    console.log('Unbekannte WebSocket Nachricht eingegangen ...')
            }
        }

        //setzt server mit Daten aus der INIT Datei
        function setServerFunktion(messageData, result, errorMessage) {

            if (result == 'success'){

                var parsedArray = JSON.parse(messageData.initData);

                localStorage.setItem("initData",messageData.initData);
                localStorage.setItem("result",result);

                var $select = $('#selectServerInput');
                $.each(parsedArray, function(i, val){
                    $select.append($('<option />', { id: parsedArray[i].id, text: parsedArray[i].name }));
                });

                clearCalendar();

            } else {

                var outputHeader = 'Server: Fehler beim lesen der Ini-Datei';
                var outputMessage = messageData.errorMessage;

                jobCalendar.controller.GlobalNotification.showNotification(
                    jobCalendar.model.GlobalNotificationType.ERROR,
                    outputHeader, outputMessage, 5000);
            }
        }

        function setCalendarEventsFunktion(messageData, result, errorMessage) {
            if (result == 'success') {
                ShowCalenderEventsArrayInCalender(messageData.calendarEvents);
            } else {

                var outputHeader = '';
                var outputMessage = '';

                switch (errorMessage) {
                    case 'MissingDriver':
                        outputHeader = 'MissingDriver';
                        outputMessage = 'Der SQL-Server Treiber ist auf dem Web-Server nicht vorhanden.';
                        outputMessage += ' Kopieren Sie die sqljdbc42.jar in das Lib-Verzeichnis des Tomcat-Servers.'
                        break;
                    case 'SocketTimeout':
                        outputHeader = 'SocketTimeout';
                        outputMessage = 'Der Server ' + messageData.serverName + ' konnte nicht erreicht werden.';
                        break;
                    case 'UnknownHost':
                        outputHeader = 'Unbekannter Host';
                        outputMessage = 'Der Server mit dem Namen ' + messageData.serverName + ' existiert nicht.';
                        break;
                    default:
                        outputHeader = 'Unbekannter Fehler';
                        outputMessage = 'Bei der Abfrage der Daten des Servers ' + messageData.serverName +
                            ' ist ein unbekannter Fehler aufgetreten. Fehlermeldung: ' + messageData.errorMessage;
                        break;
                }

                jobCalendar.controller.GlobalNotification.showNotification(
                    jobCalendar.model.GlobalNotificationType.ERROR,
                    outputHeader, outputMessage, 8000);
            }
        }

        // Löscht alle Einträge im Kalender
        function clearCalendar(calendarEvents) {
            var parsedArray = null;

            if(calendarEvents != null) {
                //werden gesetzt um sie wieder löschen zu können
                localStorage.setItem("calendarEvents", calendarEvents);

                parsedArray = JSON.parse(calendarEvents);

                $.each(parsedArray, function(i, val){
                    $('#calendar').fullCalendar( 'renderEvent', parsedArray[i] );
                });
            }
        }

        function ShowCalenderEventsArrayInCalender(calenderEventsArray) {
            $(document).ready(function() {

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
                    events: calenderEventsArray
                });

            });
        }

        return {
            handleMessage: pubHandleMessage,
        };

    })();




