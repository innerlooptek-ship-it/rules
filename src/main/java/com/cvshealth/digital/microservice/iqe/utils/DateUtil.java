package com.cvshealth.digital.microservice.iqe.utils;

import com.cvshealth.digital.microservice.common.constants.SchedulingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.*;

import static com.cvshealth.digital.microservice.locator.constants.DHSLocatorServiceConstants.AVAILABLE_DATE_FORMAT;
import static com.cvshealth.digital.microservice.locator.constants.DHSLocatorServiceConstants.MC_API_DATE_FORMAT;

/**
 * The Date Utils class to include date conversion functions.
 *
 * @author c246301
 */
public class DateUtil {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger("DateUtils");

    private static final List<String> mcDateFormats = Arrays.asList("M/d/yyyy h:mm:ss a", "MM/dd/yyyy HH:mm:ss", "yyyy-MM-dd HH:mm:ss");

    private static LocalDateTime parseWithMultiFormat(String dateStr) {
       for (String format : mcDateFormats) {
          try {
             return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(format));
          } catch (DateTimeParseException e) {
             // continue to the next loop iteration
          }
       }
       return null;
    }
    /**
     * Gets the current date.
     *
     * @return the current date
     */
    public static ZonedDateTime getCurrentJavaUtilDate() {
       LocalDateTime currentLocalDate = LocalDateTime.of(LocalDate.now(), LocalTime.now());
       ZonedDateTime currentDate = ZonedDateTime.from(currentLocalDate.atZone(ZoneId.systemDefault()).toInstant());
       logger.debug("Current java date {} ", currentDate);

       return currentDate;
    }
    
    public static String formatYear(String dateString) {
       
       LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(MC_API_DATE_FORMAT));

       DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(AVAILABLE_DATE_FORMAT);

       return dateFormat.format(date);
    }

    public static ZonedDateTime parseDate(String date) {
       DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AVAILABLE_DATE_FORMAT);
       LocalDate localDateTime = LocalDate.parse(date, formatter);

       ZoneId zoneId = ZoneId.systemDefault();

       return ZonedDateTime.of(localDateTime.atStartOfDay(), zoneId);
    }

    /**
     * Formats a date string into MinuteClinic API's expected date format.
     *
     * @param dateString The input date string to be formatted
     *                   Can be in various formats as supported by {@link #parseDate}
     *
     * @return A formatted date string following MinuteClinic API format (MC_API_DATE_FORMAT)
     *         Example: if MC_API_DATE_FORMAT is "MM/dd/yyyy", returns dates like "01/15/2024"
     *
     * @throws DateTimeParseException if the input date string cannot be parsed
     * @see #parseDate(String)
     */
    public static String formatDateToMcFormat(String dateString) {
       ZonedDateTime date = parseDate(dateString);
       DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(MC_API_DATE_FORMAT);

       return dateFormat.format(date);
    }

    public static String standardDateFormat(String mcDate) {
       LocalDateTime dateTime = parseWithMultiFormat(mcDate);

       if (dateTime != null) {
          return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
       } else {
          throw new IllegalArgumentException("Invalid date format: " + mcDate);
       }
    }

    public static String reformatWaitlistMCDate(String date) {
       DateTimeFormatter formatterInput = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH);
       DateTimeFormatter formatterOutput = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);

       LocalDate localDate = LocalDate.parse(date, formatterInput);
       ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());

       return formatterOutput.format(zonedDateTime);
    }

    public static String convertMcAppointmentDTFormat(String dateInString) {
       DateTimeFormatter formatterInput = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
       LocalDateTime dateTime = LocalDateTime.parse(dateInString, formatterInput);

       DateTimeFormatter formatterOutput = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
       return dateTime.format(formatterOutput);
    }

    public static String formatDateForPattern(String dateString, String format) {
       ZonedDateTime date = parseDate(dateString);
       DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(format);

       return dateFormat.format(date);
    }

    public static int calculateAge(String birthDate) {
       int age = 0;
       try {
          age = Period.between(LocalDate.parse(birthDate), LocalDate.now()).getYears();
       }catch (Exception e){
          logger.info("Error parsing date in calculateAge: {}", e.getMessage());
       }
       return age;
    }

    public static String timeConvert(String appointmentTime) {
       try {
          LocalTime localTime = LocalTime.parse(appointmentTime, DateTimeFormatter.ofPattern("hh:mm a"));
          DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");
          return localTime.format(outputFormatter);
       }catch (Exception e){
          logger.info("Error parsing date in timeConvert : {}", e.getMessage());
       }
       return null;
    }

    public static String dateConversionIMZ(String date, String time, String zone, Map<String,Object> tags){

       try {
          String fmtDateTime = String.join(" ", date, time);
          LocalDateTime ldt = LocalDateTime.parse(fmtDateTime, DateTimeFormatter.ofPattern(SchedulingConstants.RESERVE_DATE_REQ_FROM_PATTERN));
          assert zone != null;
          ZonedDateTime zonedDateTime = ldt.atZone(ZoneId.of(zone));
          Instant instant = zonedDateTime.toInstant();
          DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendInstant(3).toFormatter();
          String formattedDate = formatter.format(instant);
          tags.put("AppointmentDate",fmtDateTime + "zone");
          tags.put("FormattedAppointmentDate",formattedDate);
          logger.info("IMZ appointment time : " + formattedDate);
          return formattedDate;
       }catch (Exception e){
          logger.info("Error parsing date in dateConversionIMZ : {}", e.getMessage());
       }
          return null;

    }

    public static String dateFormatter(String dateTime, String fromPattern, String toPattern) {
       DateTimeFormatter formatter = DateTimeFormatter.ofPattern(fromPattern);
       DateTimeFormatter newFormat = DateTimeFormatter.ofPattern(toPattern);
       LocalDateTime dt = LocalDateTime.parse(dateTime, formatter);

       return dt.format(newFormat);
    }

    public static String convertToUTC(String dateInString, String timezone,String fromPattern, String toPattern) {
       DateTimeFormatter fromFormatter = DateTimeFormatter.ofPattern(fromPattern);

       LocalDateTime localdateTime = LocalDateTime.parse(dateInString, fromFormatter);

       ZonedDateTime dateTimeWithZone = localdateTime.atZone(ZoneId.of(timezone));

       ZonedDateTime dateTimeInUTC = dateTimeWithZone.withZoneSameInstant(ZoneId.of("UTC"));

       DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(toPattern);

       return dateTimeInUTC.format(outputFormatter);
    }
}