package com.logistics.packinglist.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateFormatterUtil {
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ISO_DATE_TIME; // or ISO_LOCAL_DATE_TIME based on format
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("dd,MM,yyyy. HH:mm:ss");

    public static String format(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) {
            return "-";
        }
        try {
            // Some dates might come with 'Z', some without, some with spaces
            String cleanDate = isoDate.replace(" ", "T");
            // Also if it doesn't have seconds, it might fail, so better to parse defensively.
            LocalDateTime dateTime = LocalDateTime.parse(cleanDate, DateTimeFormatter.ISO_DATE_TIME);
            return dateTime.format(OUTPUT_FORMAT);
        } catch (DateTimeParseException e) {
            // Fallback for custom patterns if needed, or simply return the raw string
            try {
                // If it's already just a date without time?
                if (isoDate.length() == 10) {
                     LocalDateTime dt = LocalDateTime.parse(isoDate + "T00:00:00");
                     return dt.format(OUTPUT_FORMAT);
                }
            } catch (Exception ex) {
                // Ignore
            }
            return isoDate.replace("T", " "); // default fallback
        }
    }
}
