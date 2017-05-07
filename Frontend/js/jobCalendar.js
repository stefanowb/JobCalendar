// Initialisierung
jobCalendar.view.GlobalNotification.initialize();
var WS_URL = 'ws://127.0.0.1:8080/jobCalendar/wsEndpoint';
jobCalendar.core.WebSocketConnector.connectToServer(WS_URL, webSocketConnectionEstablished);

function webSocketConnectionEstablished(obj) {
    if (jobCalendar.core.WebSocketConnector.isConnected()) {
        //Teständerrung für Push
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
    var timeoutInput = document.getElementById("timeoutInput");
    var timeRange = timeRangeInput.value;
    var timeout = timeoutInput.value;

    if (!isInt(timeRange)){

        jobCalendar.controller.GlobalNotification.showNotification(
            jobCalendar.model.GlobalNotificationType.WARNING,
            "Wrong TimeRange",
            "Time Range must be an Integer Value",
            5000);

    } else if (!isInt(timeout)){

        jobCalendar.controller.GlobalNotification.showNotification(
            jobCalendar.model.GlobalNotificationType.WARNING,
            "Wrong Timeout",
            "Timeout must be an Integer Value",
            5000);

    } else if (!isInt(serverID)) {

        jobCalendar.controller.GlobalNotification.showNotification(
            jobCalendar.model.GlobalNotificationType.WARNING,
            "No Server selected",
            "Please Select a Server",
            5000);

    } else {

        // entferne alle Kalendereinträge, die sich noch im Kalender befinden
        var calendarEvents = JSON.parse(localStorage.getItem("calendarEvents"));
        if (calendarEvents != null){
            $.each(calendarEvents, function(i, val){
                $('#calendar').fullCalendar('removeEvents',calendarEvents[i].id);
            });
        }

        var initData = JSON.parse(localStorage.getItem("initData"));
        var result = localStorage.getItem("result");

        // Zentrale INIT Variablen
        var serverName = initData[serverID].name;

        var exchangeObject = Object.create(jobCalendar.model.Request);
        exchangeObject.destination = 'SCalendar/calendarEventsRequest';
        var exchangeData = {
            serverName: serverName,
            nextDaysRange: timeRange,
            timeOut: timeout
        };
        exchangeObject.data = exchangeData;
        jobCalendar.core.WebSocketConnector.sendRequest(exchangeObject);
    }

}

function isInt(value) {
    return !isNaN(value) &&
        parseInt(Number(value)) == value &&
        !isNaN(parseInt(value, 10));
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

                var outputHeader = 'Server: Error while reading the ini file.';
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
                        outputMessage = 'The jdbc SQL-Server driver is missing on the application server.';
                        outputMessage += ' Please copy sqljdbc42.jar into the lib directory of your application server.'
                        break;
                    case 'SocketTimeout':
                        outputHeader = 'SocketTimeout';
                        outputMessage = 'The server ' + messageData.serverName + ' could not be reached.';
                        break;
                    case 'UnknownHost':
                        outputHeader = 'Unknown Host';
                        outputMessage = 'The Server ' + messageData.serverName + ' seems not to exist.';
                        break;
                    case 'SqlException':
                        outputHeader = 'SQL-Exception';
                        outputMessage = messageData.errorMessage;
                        break;
                    default:
                        outputHeader = 'Unknown Error';
                        outputMessage = 'An unknown exception occured while requesting the server  ' + messageData.serverName +
                            ': ' + messageData.errorMessage;
                        break;
                }

                jobCalendar.controller.GlobalNotification.showNotification(
                    jobCalendar.model.GlobalNotificationType.ERROR,
                    outputHeader, outputMessage, 10000);
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




