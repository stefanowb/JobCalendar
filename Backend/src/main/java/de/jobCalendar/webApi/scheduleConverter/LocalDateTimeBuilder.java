package main.java.de.jobCalendar.webApi.scheduleConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeBuilder {

    public static LocalDateTime getLDT(int startDate, int startTime ){
        // Uhrzeit mit Nullen auffÃ¼llen um Format zu erhalten

        String date = String.valueOf(startDate);
        String time = String.valueOf(startTime);

        String datePattern = "yyyyMMddHHmmss";
        if (time.length() == 5) {
            time = "0"+time;
        } else if (time.length() == 4) {
            time = "00"+time;
        }
        else if (time.length() == 3) {
            time = "000"+time;
        }
        else if (time.length() == 2) {
            time = "0000"+time;
        }
        else if (time.length() == 1) {
            time = "00000"+time;
        }

        String datetime = date+time;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(datePattern);
        LocalDateTime ldt = LocalDateTime.parse(datetime, dtf);
        return ldt;
    }
    public static LocalDate getLD(int startDate ){

        String dateTime = String.valueOf(startDate);
        String datePattern = "yyyyMMdd";

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(datePattern);
        LocalDate ld = LocalDate.parse(dateTime, dtf);
        return ld;
    }

}
