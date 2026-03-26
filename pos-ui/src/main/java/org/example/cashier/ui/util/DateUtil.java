package org.example.cashier.ui.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String format(LocalDate date) {
        return date != null ? date.format(DATE_FMT) : "";
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FMT) : "";
    }

    public static LocalDate parseDate(String s) {
        return LocalDate.parse(s, DATE_FMT);
    }
}
