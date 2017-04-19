// Initialisierung
jobCalendar.view.GlobalNotification.initialize();
var WS_URL = 'ws://127.0.0.1:8080/jobCalendar/wsEndpoint';
jobCalendar.core.WebSocketConnector.connectToServer(WS_URL, webSocketConnectionEstablished);

function webSocketConnectionEstablished(obj) {
    if (jobCalendar.core.WebSocketConnector.isConnected()) {

        var exchangeObject = Object.create(jobCalendar.model.Request);
        //exchangeObject.destination = 'SCalendar/server';
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
                case 'SCalendar/serverResponse':
                    // hier sollte man dann eine Methode aufrufen, die dann etwas mit den Daten tut
                    setServerFunktion(data, result, errorMessage);
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

        //setzt server mit DAten aus der INIT Datei
        function setServerFunktion(messageData, result, errorMessage) {

            var parsedArray = JSON.parse(messageData.httpResponse);

            localStorage.setItem("initData",messageData.httpResponse);
            localStorage.setItem("result",result);

            var $select = $('#mySelect');
            $.each(parsedArray, function(i, val){
                $select.append($('<option />', { id: parsedArray[i].id, text: parsedArray[i].name }));
            });

            var calendarEvents = null;
            showCalendar(calendarEvents, result);
        }

        //zeigt Calender leer oder mit Serverdaten
        function showCalendar(calendarEvents, result) {
            var parsedArray = null;

            // if (result == 'success') {
            //     ShowCalenderEventsArrayInCalender(messageData.httpResponse, true);
            // }

            if(calendarEvents != null) {
                //werden gesetzt um sie wieder löschen zu können
                localStorage.setItem("calendarEvents", calendarEvents);

                parsedArray = JSON.parse(calendarEvents);

                $.each(parsedArray, function(i, val){
                    $('#calendar').fullCalendar( 'renderEvent', parsedArray[i] );
                });
            }
            else {
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
                        eventLimit: true // allow "more" link when too many events
                    });
                });
            }
        }

        function SQLFunktion(messageData, result, errorMessage) {
            if (result == 'success') {
                ShowCalenderEventsArrayInCalender(messageData.sqlResult, false);
                //window.alert(messageData.sqlResult);
                //console.log(messageData.sqlResult);
            } else {

                var outputHeader = '';
                var outputMessage = '';

                switch (errorMessage) {
                    case 'MissingDriver':
                        outputHeader = 'SQLQuery - MissingDriver';
                        outputMessage = 'Der SQL-Server Treiber ist auf dem Web-Server nicht vorhanden.';
                        outputMessage += ' Kopieren Sie die sqljdbc42.jar in das Lib-Verzeichnis des Tomcat-Servers.'
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

        function ScheduledTasksFunktion(messageData, result, errorMessage) {

            if (result == 'success'){
                // window.alert(messageData.httpResponse);
                showCalendar(messageData.httpResponse);
            } else {

                var outputHeader = '';
                var outputMessage = '';

                switch (errorMessage){
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


function changeServer() {

    var e = document.getElementById("mySelect");
    var serverID = e.options[e.selectedIndex].id;

    var calendarEvents = JSON.parse(localStorage.getItem("calendarEvents"));

    if (calendarEvents != null){
        $.each(calendarEvents, function(i, val){
            $('#calendar').fullCalendar('removeEvents',calendarEvents[i].id);
        });
    }

    // console.log(calendarEvents);

    if(serverID >= 0){

        var initData = JSON.parse(localStorage.getItem("initData"));
        var result = localStorage.getItem("result");

        // Zentrale INIT Variablen

        var serverName = initData[serverID].name;
        var serverID = initData[serverID].id;
        var serverTask = initData[serverID].task;
        var serverSQL = initData[serverID].sql;

        // HIER kommt dann die Auswertung was aufgerufen wird (Server Anmeldedaten)

        var exchangeObject = Object.create(jobCalendar.model.Request);
        var exchangeData = {
            serverName: "localhost"
        };

        if(serverTask == "true"){
            exchangeObject.destination = 'SCalendar/scheduledTasksRequest';
            exchangeObject.result = result;
            exchangeObject.data = exchangeData;

            jobCalendar.core.WebSocketConnector.sendRequest(exchangeObject);
        }
    }
}

function changeTRange() {

    var e = document.getElementById("myInput");
    var timeRange = e.value;

    console.log(timeRange);
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
