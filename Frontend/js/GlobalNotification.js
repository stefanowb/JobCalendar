jobCalendar.view.GlobalNotification = jobCalendar.view.GlobalNotification || (function () {

        //region local vars
        var htmlView = null;
        var htmlHeadline = null;
        var htmlDisplayText = null;
        var colorInfo = '#86a8ff';
        var colorWarning = '#ffa775';
        var colorError = '#ff3245';
        var colorSuccess = '#77ff60';

        //endregion

        //region ini
        function pubIni() {
            console.log('initialisere GLOBAL NOTIFICATION');
            htmlView = document.querySelector('.globalNotification_view');
            htmlHeadline = document.querySelector('#globalNotification_headline_h3');
            htmlDisplayText = document.querySelector('#globalNotification_displayText_span');
            jobCalendar.controller.GlobalNotification.registerShowNotification(cbShowNotification);
        }
        //endregion

        //region
        function cbShowNotification(notificationType, headline, text, duration) {

            switch(jobCalendar.model.GlobalNotificationType[notificationType]) {
                case jobCalendar.model.GlobalNotificationType.INFO:
                    htmlView.style.backgroundColor = colorInfo;
                    break;
                case jobCalendar.model.GlobalNotificationType.WARNING:
                    htmlView.style.backgroundColor = colorWarning;
                    break;
                case jobCalendar.model.GlobalNotificationType.ERROR:
                    htmlView.style.backgroundColor = colorError;
                    break;
                case jobCalendar.model.GlobalNotificationType.SUCCESS:
                    htmlView.style.backgroundColor = colorSuccess;
                    break;
                default:
                    htmlView.style.backgroundColor = colorInfo;
                    break;
            }

            htmlView.style.right = '50px';

            htmlHeadline.textContent = headline;
            htmlDisplayText.textContent = text;
            htmlView.style.opacity = '1.0';

            setTimeout(function () {

                htmlView.style.right = '-350px';
                htmlView.style.opacity = '0.0';

            },duration);
            console.log('show Notification');
        }
        //endregion

        return {
            initialize: pubIni
        };

    })();

jobCalendar.controller.GlobalNotification = jobCalendar.controller.GlobalNotification || (function () {

        // quick and dirty Hack, nicht zum nachmachen;
        onkeydown = function(e){
            if(e.ctrlKey && e.keyCode == 'I'.charCodeAt(0)){
                pubShowNotification(ideaWatcher.model.GlobalNotificationType.ERROR,'UserSession','UserSession leider' +
                    ' fehlgeschlagen', 4000);
            }
        };


        //region local vars
        var cbShowNotification = null;
        //endregion

        function pubShowNotification(notificationType,headline,text,duration)
        {
            cbShowNotification(notificationType,headline,text,duration);
        }

        function pubRegisterShowNotification(cbFunction) {
            cbShowNotification = cbFunction;
        }

        return {
            registerShowNotification: pubRegisterShowNotification,
            showNotification: pubShowNotification
        };

    })();

jobCalendar.model.GlobalNotificationType = jobCalendar.model.GlobalNotificationType || {

        SUCCESS: 'SUCCESS',
        INFO: 'INFO',
        WARNING: 'WARNING',
        ERROR: 'ERROR'
    };