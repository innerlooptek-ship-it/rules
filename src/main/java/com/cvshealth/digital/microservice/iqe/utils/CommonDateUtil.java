package com.cvshealth.digital.microservice.iqe.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonDateUtil {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(CommonDateUtil.class);
    private static final Logger logger = LoggerFactory.getLogger("CommonDateUtil");

    public CommonDateUtil() {
    }

    public static String formatDateToFormat(String dateString, String fromFormat, String toFormat) {
        ZonedDateTime date = parseDate(dateString, fromFormat);
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(toFormat);
        return dateFormat.format(date);
    }

    public static ZonedDateTime parseDate(String date, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDate localDateTime = LocalDate.parse(date, formatter);
        ZoneId zoneId = ZoneId.systemDefault();
        return ZonedDateTime.of(localDateTime.atStartOfDay(), zoneId);
    }

    public static String convertToUserTimeZone(String dateTime, String clinicTimeZone, String userTimeZone, String toFormat) {
        String[] possibleFormats = new String[]{"yyyy-MM-dd HH:mm:ss", "M/d/yyyy h:mm:ss a", "MM/dd/yyyy h:mm:ss a", "MM/dd/yyyy HH:mm:ss"};
        LocalDateTime localDateTime = null;

        for(String format : possibleFormats) {
            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(format);
                localDateTime = LocalDateTime.parse(dateTime, inputFormatter);
                break;
            } catch (Exception var12) {
            }
        }

        if (localDateTime == null) {
            throw new IllegalArgumentException("Invalid dateTime format");
        } else {
            ZonedDateTime clinicZonedDateTime = localDateTime.atZone(ZoneId.of(clinicTimeZone));
            ZoneId targetZoneId = userTimeZone != null && !userTimeZone.isEmpty() ? ZoneId.of(userTimeZone) : ZoneId.of(clinicTimeZone);
            ZonedDateTime userZonedDateTime = clinicZonedDateTime.withZoneSameInstant(targetZoneId);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(toFormat);
            return userZonedDateTime.format(outputFormatter);
        }
    }
}